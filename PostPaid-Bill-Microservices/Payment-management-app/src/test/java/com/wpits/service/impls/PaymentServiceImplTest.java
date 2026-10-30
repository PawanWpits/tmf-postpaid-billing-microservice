package com.wpits.service.impls;

import com.wpits.config.ModelMapperConfig;
import com.wpits.dtos.AccountRefDto;
import com.wpits.dtos.EntityRefDto;
import com.wpits.dtos.MoneyDto;
import com.wpits.dtos.PaymentDto;
import com.wpits.dtos.PaymentItemDto;
import com.wpits.dtos.PaymentMethodRefOrValueDto;
import com.wpits.dtos.RelatedPartyDto;
import com.wpits.entities.Payment;
import com.wpits.helper.PaymentGatewayHelper;
import com.wpits.repositories.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class PaymentServiceImplTest {

    @Test
    void chargesGatewayBeforeUpdatingBillAndSavingPayment() {
        PaymentRepository repository = mock(PaymentRepository.class);
        PaymentGatewayHelper paymentGatewayHelper = mock(PaymentGatewayHelper.class);
        RestClient.Builder restClientBuilder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restClientBuilder).build();
        RestClient restClient = restClientBuilder.build();
        PaymentServiceImpl service = new PaymentServiceImpl(
                restClient, repository, new ModelMapperConfig().modelMapper(), paymentGatewayHelper);

        String accountHref = "http://localhost:8080/accounts/PA-001";
        String payerHref = "http://localhost:8080/individual/333";
        String orderHref = "http://localhost:8080/productOrdering/v4/productOrder/PO-ORDER";
        String billHref = "http://localhost:8081/customerBill/CB-001";

        server.expect(requestTo(accountHref))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));
        server.expect(requestTo(payerHref))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));
        server.expect(requestTo(orderHref))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));
        server.expect(requestTo(billHref))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));
        server.expect(requestTo(orderHref))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {"id":"PO-ORDER"}
                        """, MediaType.APPLICATION_JSON));
        server.expect(requestTo(billHref))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {
                          "state":"NEW",
                          "remainingAmount":{"unit":"INR","value":55.25}
                        }
                        """, MediaType.APPLICATION_JSON));
        server.expect(requestTo(billHref))
                .andExpect(method(HttpMethod.PATCH))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("\"value\":0.00")))
                .andRespond(withSuccess());
        when(repository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PaymentDto payment = validPayment(accountHref, orderHref, billHref);
        when(paymentGatewayHelper.processPayment(new BigDecimal("165.75"), "INR", "Check"))
                .thenReturn("{\"status\":\"success\",\"message\":\"Payment processed successfully\"}");

        PaymentDto created = service.create(payment);

        assertEquals("COMPLETED", created.getStatus());
        assertEquals(new BigDecimal("165.75"), created.getTotalAmount().getValue());
        server.verify();
        verify(repository).save(any(Payment.class));
        verify(paymentGatewayHelper).processPayment(new BigDecimal("165.75"), "INR", "Check");
    }

    @Test
    void rejectsPaymentWhenPaymentLevelTotalsDoNotMatchItems() {
        PaymentRepository repository = mock(PaymentRepository.class);
        PaymentGatewayHelper paymentGatewayHelper = mock(PaymentGatewayHelper.class);
        RestClient.Builder restClientBuilder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restClientBuilder).build();
        PaymentServiceImpl service = new PaymentServiceImpl(
                restClientBuilder.build(), repository, new ModelMapperConfig().modelMapper(), paymentGatewayHelper);
        PaymentDto payment = validPayment(
                "http://localhost:8080/accounts/PA-001",
                "http://localhost:8080/productOrdering/v4/productOrder/PO-ORDER",
                "http://localhost:8081/customerBill/CB-001");
        payment.getTotalAmount().setValue(new BigDecimal("165.74"));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> service.create(payment));

        assertEquals(400, exception.getStatusCode().value());
        server.verify();
    }

    @Test
    void doesNotContinueWhenGatewayDeclinesPayment() {
        PaymentRepository repository = mock(PaymentRepository.class);
        PaymentGatewayHelper paymentGatewayHelper = mock(PaymentGatewayHelper.class);
        RestClient.Builder restClientBuilder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restClientBuilder).build();
        PaymentServiceImpl service = new PaymentServiceImpl(
                restClientBuilder.build(), repository, new ModelMapperConfig().modelMapper(), paymentGatewayHelper);
        PaymentDto payment = validPayment(
                "http://localhost:8080/accounts/PA-001",
                "http://localhost:8082/product-offerings/PO-001",
                "http://localhost:8081/customerBill/CB-001");
        when(paymentGatewayHelper.processPayment(new BigDecimal("165.75"), "INR", "Check"))
                .thenReturn("{\"status\":\"declined\",\"message\":\"Insufficient funds\"}");

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> service.create(payment));

        assertEquals(HttpStatus.PAYMENT_REQUIRED, exception.getStatusCode());
        verify(paymentGatewayHelper).processPayment(new BigDecimal("165.75"), "INR", "Check");
        verify(repository, org.mockito.Mockito.never()).save(any(Payment.class));
        server.verify();
    }

    private PaymentDto validPayment(String accountHref, String orderHref, String billHref) {
        PaymentDto payment = new PaymentDto();
        payment.setName("Payment test");
        payment.setAmount(money("150.00"));
        payment.setTaxAmount(money("15.75"));
        payment.setTotalAmount(money("165.75"));

        AccountRefDto account = new AccountRefDto();
        account.setId("PA-001");
        account.setHref(accountHref);
        payment.setAccount(account);

        RelatedPartyDto payer = new RelatedPartyDto();
        payer.setHref("http://localhost:8080/individual/333");
        payer.setId("333");
        payment.setPayer(payer);
        payment.setPaymentMethod(new PaymentMethodRefOrValueDto());
        payment.getPaymentMethod().setType("Check");
        payment.setPaymentItem(new ArrayList<>(List.of(
                item("PO-ORDER", orderHref, "ProductOrder",
                        money("100.00"), money("10.50"), money("110.50")),
                item("CB-001", billHref, "CustomerBill",
                        money("50.00"), money("5.25"), money("55.25")))));
        return payment;
    }

    private PaymentItemDto item(String id, String href, String referredType,
                                MoneyDto amount, MoneyDto tax, MoneyDto total) {
        EntityRefDto reference = new EntityRefDto();
        reference.setId(id);
        reference.setHref(href);
        reference.setReferredType(referredType);
        PaymentItemDto item = new PaymentItemDto();
        item.setItem(reference);
        item.setAmount(amount);
        item.setTaxAmount(tax);
        item.setTotalAmount(total);
        return item;
    }

    private MoneyDto money(String value) {
        return new MoneyDto("INR", new BigDecimal(value));
    }
}
