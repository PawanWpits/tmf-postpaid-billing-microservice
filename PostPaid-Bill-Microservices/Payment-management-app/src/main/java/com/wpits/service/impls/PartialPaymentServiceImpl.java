package com.wpits.service.impls;

import com.wpits.dtos.MoneyDto;
import com.wpits.dtos.PartialPaymentRequest;
import com.wpits.entities.AccountRef;
import com.wpits.entities.EntityRef;
import com.wpits.entities.Money;
import com.wpits.entities.Payment;
import com.wpits.entities.PaymentItem;
import com.wpits.entities.PaymentMethodRefOrValue;
import com.wpits.repositories.AccountRefRepository;
import com.wpits.repositories.EntityRefRepository;
import com.wpits.repositories.PaymentRepository;
import com.wpits.exceptions.BadRequestException;
import com.wpits.service.PartialPaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class PartialPaymentServiceImpl implements PartialPaymentService {
    private final RestClient restClient;
    private final PaymentRepository paymentRepository;
    private final AccountRefRepository accountRefRepository;
    private final EntityRefRepository entityRefRepository;

    @Value("${billing.account.url}")
    private String billingAccountUrl;

    @Value("${customer.bill.url}")
    private String customerBillUrl;

    @Override
    @Transactional
    public String processPayment(PartialPaymentRequest request) {

        String billingAccountId = request.getBillingAccountId();
        String billId = request.getBillId();
        MoneyDto amount = request.getAmount();

        if (billingAccountId == null || billingAccountId.isBlank()) {
            log.warn("billingAccountId is null or blank");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Billing account ID is required");
        }

        if (billId == null || billId.isBlank()) {
            log.warn("billId is null or blank");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bill ID is required");
        }

        if (amount == null || amount.getValue() == null || amount.getValue().compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("Amount is null or not greater than zero");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Amount must be greater than zero");
        }

        // Verify billing account
        String accountUrl = billingAccountUrl + billingAccountId;

        String accountResponse;

        try {
            accountResponse = restClient.get()
                    .uri(accountUrl)
                    .retrieve()
                    .body(String.class);
            log.info("url: {} ,accountResponse: {}" , accountUrl, accountResponse);

        } catch (Exception e) {
            log.error("url: {} ,Error getting account response", accountUrl, e);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Billing account not found: " + billingAccountId, e);
        }

        if (accountResponse == null || accountResponse.isBlank()) {
            log.warn("Account response is null or blank");
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Billing account not found: " + billingAccountId);
        }

        //Retrieve customer bill
        String billUrl = customerBillUrl+ billId;

        String billResponse;

        try {
            billResponse = restClient.get()
                    .uri(billUrl)
                    .retrieve()
                    .body(String.class);
            log.info("url: {} ,billResponse: {}" , billUrl, billResponse);

        } catch (Exception e) {
            log.error("url: {} ,Error getting bill response", billUrl, e);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer bill not found: " + billId, e);
        }

        if (billResponse == null || billResponse.isBlank()) {
            log.warn("url: {} ,Bill response is null or blank", billUrl);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer bill not found: " + billId);
        }

        JSONObject bill = new JSONObject(billResponse);

        // amountDue and remainingAmount
        //JSONObject amountDueJson = bill.getJSONObject("amountDue");
        JSONObject remainingJson = bill.getJSONObject("remainingAmount");

        //BigDecimal amountDue = new BigDecimal(amountDueJson.get("value").toString());

        BigDecimal remainingAmount = new BigDecimal(remainingJson.get("value").toString());

        String currency = remainingJson.getString("unit");
        //String amountDueCurrency = amountDueJson.getString("unit");

        if (!currency.equals(amount.getUnit())) {
            log.warn("paid amount and remainingAmount currencies do not match: {} vs {}", amount.getUnit(), currency);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "paid amount and remainingAmount currencies do not match");
        }

        //BigDecimal paidAmount = amount.getValue();
        // Reject overpayment
        if (amount.getValue().compareTo(remainingAmount) > 0) {
            log.warn("Payment rejected. Outstanding amount is {} {}, but you are trying to pay {} {}", currency, remainingAmount, currency, amount.getValue());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment rejected. Outstanding amount is "
                    + currency + " " + remainingAmount + ", but you are trying to pay " + currency + " " + amount.getValue());
        }

        //Calculate new outstanding balance
        BigDecimal updatedRemainingAmount = remainingAmount.subtract(amount.getValue());

        // Update only remainingAmount; amountDue stays unchanged
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("@type", "CustomerBill");
        requestBody.put("remainingAmount",
                Map.of(
                        "unit", currency,
                        "value", updatedRemainingAmount
                ));

        String state = bill.optString("state", null);

        if (state != null && !state.isBlank()) {
            requestBody.put("state", state);
        }

        try {
            String response = restClient.patch()
                    .uri(billUrl)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);
            if(response == null || response.isBlank()) {
                log.warn("url: {} ,Patch response is null or blank", billUrl);
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error updating customer bill: " + billId);
            }

        } catch (Exception e) {
            log.error("url: {} ,Error patching billing account response", billUrl, e);
            throw new BadRequestException("Error updating customer bill: " + billId + ". " + e.getMessage(), "", "");
        }

        Payment payment = createPaymentRecord(billingAccountId, accountUrl, billId, billUrl, amount, currency);
        Payment savedPayment = paymentRepository.save(payment);

        log.info("Partial payment saved. paymentId={}, billingAccountId={}, billId={}, amount={} {}",
                savedPayment.getId(), billingAccountId, billId, amount.getUnit(), amount.getValue());
        
        return "Payment processed successfully. paymentId: " + savedPayment.getId()
                + ", billingAccountId: " + billingAccountId
                + ", billId: " + billId
                + ", amount: " + amount.getUnit() + " " + amount.getValue();
    }

    private Payment createPaymentRecord(String billingAccountId, String accountUrl, String billId, String billUrl, MoneyDto paidAmount, String currency) {

        AccountRef account = accountRefRepository.findById(billingAccountId)
                .orElseGet(() -> new AccountRef(billingAccountId, null, null, accountUrl, null, "BillingAccount", null, null));
        System.out.println("account: " + account);

        EntityRef bill = entityRefRepository.findById(billId)
                .orElseGet(() -> new EntityRef(billUrl, billId, null, null, "CustomerBill", null, null));
        System.out.println("bill: " + bill);

        BigDecimal taxValue = BigDecimal.ZERO;
        Money paymentAmount = new Money(currency, paidAmount.getValue());

        PaymentItem paymentItem = new PaymentItem();
        paymentItem.setId(UUID.randomUUID().toString());
        paymentItem.setAmount(paymentAmount);
        paymentItem.setTaxAmount(new Money(currency, taxValue));
        paymentItem.setTotalAmount(paymentAmount);
        paymentItem.setItem(bill);

        PaymentMethodRefOrValue paymentMethod = new PaymentMethodRefOrValue();
        paymentMethod.setId(UUID.randomUUID().toString());
        paymentMethod.setType("PartialPayment");
        paymentMethod.setDescription("Partial payment");

        Payment payment = new Payment();
        payment.setId(nextId());
        payment.setName("Partial payment");
        payment.setDescription("Partial payment for customer bill " + billId);
        payment.setHref(billUrl);
        payment.setAmount(paymentAmount);
        payment.setTaxAmount(new Money(currency, taxValue));
        payment.setTotalAmount(paymentAmount);
        payment.setAccount(account);
        payment.setPaymentMethod(paymentMethod);
        payment.setPaymentDate(LocalDateTime.now());
        payment.setStatus("COMPLETED");
        payment.setStatusDate(payment.getPaymentDate());
        payment.setType("Payment");
        payment.setPaymentItem(new ArrayList<>(java.util.List.of(paymentItem)));
        paymentItem.setPayment(payment);
        return payment;
    }

    private String nextId() {
        long next = paymentRepository.count() + 1;
        return String.format("PAY-%04d", next);
    }
}
