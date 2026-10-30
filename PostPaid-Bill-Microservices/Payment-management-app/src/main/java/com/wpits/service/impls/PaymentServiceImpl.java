package com.wpits.service.impls;

import com.wpits.dtos.AccountRefDto;
import com.wpits.dtos.MoneyDto;
import com.wpits.dtos.PaymentDto;
import com.wpits.dtos.PaymentItemDto;
import com.wpits.dtos.PaymentMethodRefOrValueDto;
import com.wpits.entities.Payment;
import com.wpits.entities.PaymentItem;
import com.wpits.helper.PaymentGatewayHelper;
import com.wpits.repositories.PaymentRepository;
import com.wpits.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

        private final RestClient restClient;
        private final PaymentRepository paymentRepository;
        private final ModelMapper modelMapper;
        private final PaymentGatewayHelper paymentGatewayHelper;

        @Override
        public PaymentDto create(PaymentDto paymentDto) {

            log.debug("Create PaymentDto: {}", paymentDto);

            if (paymentDto == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment request is required");
            }

            //validate dto
            validate(paymentDto);

            //payment amounts.
            MoneyDto amount = paymentDto.getAmount();
            MoneyDto taxAmount = paymentDto.getTaxAmount();
            MoneyDto totalAmount = paymentDto.getTotalAmount();

            validateMoney(amount, "Payment amount");
            validateMoney(taxAmount, "Payment taxAmount");
            validateMoney(totalAmount, "Payment totalAmount");

            BigDecimal paymentAmount = amount.getValue();
            BigDecimal paymentTax = taxAmount.getValue();
            BigDecimal paymentTotal = totalAmount.getValue();

            String paymentCurrency = amount.getUnit();

            if (paymentAmount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Payment amount must be greater than zero");
            }

            if (paymentTax.compareTo(BigDecimal.ZERO) < 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Payment taxAmount cannot be negative");
            }

            if (paymentTotal.compareTo(BigDecimal.ZERO) <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Payment totalAmount must be greater than zero");
            }

            validateCurrency(paymentCurrency, taxAmount.getUnit(),"Payment amount and taxAmount");

            validateCurrency(paymentCurrency, totalAmount.getUnit(),"Payment amount and totalAmount");

            //amount + tax =payment total
            if (paymentAmount.add(paymentTax).compareTo(paymentTotal) != 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Payment totalAmount must equal amount + taxAmount");
            }

            // sum all payment items
            List<PaymentItemDto> paymentItems = paymentDto.getPaymentItem();

            if (paymentItems == null || paymentItems.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"At least one paymentItem is required");
            }

            BigDecimal itemAmountSum = BigDecimal.ZERO;
            BigDecimal itemTaxSum = BigDecimal.ZERO;
            BigDecimal itemTotalSum = BigDecimal.ZERO;

            for (PaymentItemDto item : paymentItems) {

                if (item == null) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Payment item cannot be null");
                }

                validateMoney(item.getAmount(), "Payment item amount");
                validateMoney(item.getTaxAmount(), "Payment item taxAmount");
                validateMoney(item.getTotalAmount(), "Payment item totalAmount");

                BigDecimal itemAmount = item.getAmount().getValue();
                BigDecimal itemTax = item.getTaxAmount().getValue();
                BigDecimal itemTotal = item.getTotalAmount().getValue();

                if (itemAmount.compareTo(BigDecimal.ZERO) <= 0) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Payment item amount must be greater than zero");
                }

                if (itemTax.compareTo(BigDecimal.ZERO) < 0) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Payment item taxAmount cannot be negative");
                }

                if (itemTotal.compareTo(BigDecimal.ZERO) <= 0) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Payment item totalAmount must be greater than zero");
                }

                validateCurrency(paymentCurrency, item.getAmount().getUnit(),"Payment and payment item amount");

                validateCurrency(paymentCurrency, item.getTaxAmount().getUnit(),"Payment and payment item taxAmount");

                validateCurrency(paymentCurrency, item.getTotalAmount().getUnit(),"Payment and payment item totalAmount");

                if (itemAmount.add(itemTax).compareTo(itemTotal) != 0) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Payment item totalAmount must equal amount + taxAmount");
                }

                if (item.getItem() == null || item.getItem().getHref() == null || item.getItem().getHref().isBlank()
                        || item.getItem().getReferredType() == null || item.getItem().getReferredType().isBlank()) {

                    throw new ResponseStatusException( HttpStatus.BAD_REQUEST,"Payment item href and referredType are required");
                }

                itemAmountSum = itemAmountSum.add(itemAmount);
                itemTaxSum = itemTaxSum.add(itemTax);
                itemTotalSum = itemTotalSum.add(itemTotal);
            }

            // Compare sums with payment-level values.
            if (itemAmountSum.compareTo(paymentAmount) != 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Sum of paymentItem amounts (" + itemAmountSum + ") must equal payment amount (" + paymentAmount + ")");
            }

            if (itemTaxSum.compareTo(paymentTax) != 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Sum of paymentItem taxAmounts (" + itemTaxSum+ ") must equal payment taxAmount (" + paymentTax + ")");
            }

            if (itemTotalSum.compareTo(paymentTotal) != 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Sum of paymentItem totalAmounts (" + itemTotalSum+ ") must equal payment totalAmount (" + paymentTotal + ")");
            }

            log.info("Payment amount validation successful. amount={}, tax={}, total={}",itemAmountSum, itemTaxSum, itemTotalSum);

            // Verify account.
            verifyAccount(paymentDto.getAccount().getHref());

            //verify payer account
            verifyAccount(paymentDto.getPayer().getHref());


            //verify payment item - item hrefs
            paymentItems.forEach(item -> {
                if (item.getItem() == null || item.getItem().getHref() == null || item.getItem().getHref().isBlank()) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Payment item href is required");
                }
                verifyAccount(item.getItem().getHref());
            });

            //requested payment amount and match with customer bill db and product order db amount

            //Gateway
            processGatewayPayment(itemTotalSum, paymentCurrency, paymentDto.getPaymentMethod());
            // Verify and update referenced resources.
            // CustomerBill remainingAmount, ProductOrder is only retrieved
            for (PaymentItemDto item : paymentItems) {
                processPaymentItem(item);
            }

            // save payment
            Payment payment = modelMapper.map(paymentDto, Payment.class);

            payment.setId(nextId());
            payment.setPaymentDate(LocalDateTime.now());
            payment.setStatus("COMPLETED");
            payment.setStatusDate(LocalDateTime.now());

            if (payment.getPaymentMethod() != null && (payment.getPaymentMethod().getId() == null || payment.getPaymentMethod().getId().isBlank())) {
                payment.getPaymentMethod().setId(new Random().nextInt(1000) + 1 + "");
            }

            payment.setPaymentItem(mapPaymentItems(paymentDto.getPaymentItem()));
            setPaymentItemParents(payment);

            Payment savedPayment = paymentRepository.save(payment);

            log.info("Payment created: {}", savedPayment);

            return modelMapper.map(savedPayment, PaymentDto.class);
        }

    private void processGatewayPayment(BigDecimal amount, String currency, PaymentMethodRefOrValueDto paymentMethod) {

            String method = paymentMethod.getType();

        if (method == null || method.isBlank()) {
            method = paymentMethod.getName();
        }
        if (method == null || method.isBlank()) {
            method = paymentMethod.getId();
        }
        if (method == null || method.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Payment method type, name, or id is required for gateway processing");
        }

        String response = paymentGatewayHelper.processPayment(amount, currency, method);

        if (response == null || response.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,"Payment gateway returned an empty response");
        }

        try {

            JSONObject gatewayResponse = new JSONObject(response);

            if (!"success".equalsIgnoreCase(gatewayResponse.optString("status"))) {
                throw new ResponseStatusException(HttpStatus.PAYMENT_REQUIRED,"Payment gateway did not approve the payment");
            }
        } catch (org.json.JSONException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,"Payment gateway returned an invalid response", exception);
        }

        log.info("Payment gateway approved payment amount={} {}", amount, currency);
    }

    private void verifyAccount(String href) {

        if (href == null || href.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Account href is required");
        }

        try {
            String response = restClient.get()
                    .uri(href)
                    .retrieve()
                    .body(String.class);

            if (response == null || response.isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,"Account API returned an empty response");
            }

            log.info("Accounts verification successful: {}", href);

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Accounts verification failed: {}", href, e);
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,"Accounts verification failed: " + href,e);
        }
    }


    private void validateMoney(MoneyDto money, String fieldName) {

            if (money == null || money.getValue() == null || money.getUnit() == null || money.getUnit().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,fieldName + " value and unit are required");
            }
        }

        private void validateCurrency(String expectedUnit, String actualUnit, String fieldName) {

            if (!expectedUnit.equals(actualUnit)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,fieldName + " currency units must match");
            }
        }

        private void processPaymentItem(PaymentItemDto item) {

            String href = item.getItem().getHref();
            String referredType = item.getItem().getReferredType();

            log.info("Processing payment item. href={}, type={}, total={}", href, referredType, item.getTotalAmount());

            final String response;

            try {
                response = restClient.get()
                        .uri(href)
                        .retrieve()
                        .body(String.class);
            } catch (Exception e) {
                log.error("Unable to retrieve payment item resource: {}", href, e);
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,"Unable to retrieve referenced resource: " + href,e);
            }

            if (response == null || response.isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,"Referenced resource returned an empty response: " + href);
            }

            JSONObject json = new JSONObject(response);

            // CustomerBill balance validation and update.
            if ("CustomerBill".equalsIgnoreCase(referredType)) {

                if (!json.has("remainingAmount") || json.isNull("remainingAmount")) {
                    throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "CustomerBill response does not contain remainingAmount");
                }

                JSONObject remainingAmount = json.getJSONObject("remainingAmount");

                String remainingUnit = remainingAmount.optString("unit", null);

                if (remainingUnit == null || !remainingAmount.has("value") || remainingAmount.isNull("value")) {
                    throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "CustomerBill remainingAmount value and unit are required");
                }

                BigDecimal remainingValue = new BigDecimal(remainingAmount.get("value").toString());

                BigDecimal itemTotal = item.getTotalAmount().getValue();
                String itemUnit = item.getTotalAmount().getUnit();

                validateCurrency(itemUnit, remainingUnit, "Payment item and CustomerBill");

                // Preserve your existing rule: the payment item must settle
                // the entire outstanding balance of the referenced bill.
                if (itemTotal.compareTo(remainingValue) != 0) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Payment item totalAmount (" + itemTotal+ ")" +
                            " must equal CustomerBill remainingAmount ("+ remainingValue + ") for href: " + href);
                }

                BigDecimal updatedRemainingValue = remainingValue.subtract(itemTotal);

                String state = json.optString("state", null);

                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("@type", referredType);

                if (state != null) {
                    requestBody.put("state", state);
                }

                requestBody.put("remainingAmount",new MoneyDto(remainingUnit, updatedRemainingValue));

                try {
                    String patchResponse = restClient.patch()
                            .uri(href)
                            .body(requestBody)
                            .retrieve()
                            .body(String.class);

                    log.info("CustomerBill remainingAmount updated: {}",patchResponse);
                } catch (Exception e) {
                    log.error("Failed to update CustomerBill: {}", href, e);
                    throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,"Failed to update CustomerBill: " + href,e);
                }

            } else if ("ProductOrder".equalsIgnoreCase(referredType)) {

                // The GET above verifies that the referenced order can be
                // retrieved. Do not assume a ProductOrder has remainingAmount.
                log.info("ProductOrder retrieved successfully: {}", href);

            } else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Unsupported payment item referredType: " + referredType);
            }
        }


    private String nextId() {
        long next = paymentRepository.count() + 1;
        return String.format("PAY-%04d", next);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentDto getById(String id) {

        return modelMapper.map(findPayment(id), PaymentDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentDto> getAll() {
        return paymentRepository.findAll().stream()
                .map(payment -> modelMapper.map(payment, PaymentDto.class))
                .toList();
    }

    @Override
    public PaymentDto update(String id, PaymentDto paymentDto) {
        validate(paymentDto);
        Payment existingPayment = findPayment(id);
        modelMapper.map(paymentDto, existingPayment);
        existingPayment.setId(id);
        existingPayment.getPaymentItem().clear();
        existingPayment.getPaymentItem().addAll(mapPaymentItems(paymentDto.getPaymentItem()));
        setPaymentItemParents(existingPayment);
        return modelMapper.map(paymentRepository.save(existingPayment), PaymentDto.class);
    }

    @Override
    public void delete(String id) {
        Payment payment = findPayment(id);
        paymentRepository.delete(payment);
    }

    private Payment findPayment(String id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found with id: " + id));
    }

    private void validate(PaymentDto paymentDto) {

        log.info("Validating paymentDto: {}", paymentDto);

        if (paymentDto == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment request body is required");
        }
        if (paymentDto.getAccount() == null || paymentDto.getAccount().getId() == null
                || paymentDto.getAccount().getId().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment account id is required");
        }
        if (paymentDto.getPaymentMethod() == null /*|| paymentDto.getPaymentMethod().getId() == null
                || paymentDto.getPaymentMethod().getId().isBlank()*/) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment method id is required");
        }
        if (paymentDto.getPaymentItem() != null) {
            boolean missingItemId = paymentDto.getPaymentItem().stream()
                    .anyMatch(item -> item == null || item.getItem() == null || item.getItem().getId() == null
                            || item.getItem().getId().isBlank());
            if (missingItemId) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Each payment item requires an entity reference id");
            }
        }
    }

    private List<PaymentItem> mapPaymentItems(List<PaymentItemDto> paymentItemDtos) {
        log.info("Mapping payment items: {}", paymentItemDtos);
        if (paymentItemDtos == null) {
            return new ArrayList<>();
        }
        return paymentItemDtos.stream()
                .map(itemDto -> {
                    PaymentItem item = modelMapper.map(itemDto, PaymentItem.class);
                    if (item.getId() == null || item.getId().isBlank()) {
                        item.setId(UUID.randomUUID().toString());
                    }
                    log.info("Mapped payment item: {}", item);
                    return item;
                })
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private void setPaymentItemParents(Payment payment) {

        if (payment.getPaymentItem() != null) {
            for (PaymentItem item : payment.getPaymentItem()) {
                item.setPayment(payment);
            }
        }
    }
}
