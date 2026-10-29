package com.wpits.service.impls;

import com.wpits.dtos.MoneyDto;
import com.wpits.dtos.PartialPaymentRequest;
import com.wpits.entities.Payment;
import com.wpits.repositories.AccountRefRepository;
import com.wpits.repositories.EntityRefRepository;
import com.wpits.repositories.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class PartialPaymentServiceImplTest {

    @Test
    void savesPaymentRecordAfterUpdatingBill() {
        PaymentRepository paymentRepository = mock(PaymentRepository.class);
        AccountRefRepository accountRefRepository = mock(AccountRefRepository.class);
        EntityRefRepository entityRefRepository = mock(EntityRefRepository.class);
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();

        PartialPaymentServiceImpl service = new PartialPaymentServiceImpl(
                builder.build(), paymentRepository, accountRefRepository, entityRefRepository);
        ReflectionTestUtils.setField(service, "billingAccountUrl", "http://localhost/accounts/");
        ReflectionTestUtils.setField(service, "customerBillUrl", "http://localhost/bills/");

        when(accountRefRepository.findById("BA-001")).thenReturn(Optional.empty());
        when(entityRefRepository.findById("CB-001")).thenReturn(Optional.empty());
        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        server.expect(requestTo("http://localhost/accounts/BA-001"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));
        server.expect(requestTo("http://localhost/bills/CB-001"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {
                          "state":"NEW",
                          "remainingAmount":{"unit":"INR","value":500.00}
                        }
                        """, MediaType.APPLICATION_JSON));
        server.expect(requestTo("http://localhost/bills/CB-001"))
                .andExpect(method(HttpMethod.PATCH))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("\"value\":400.00")))
                .andRespond(withSuccess("{\"updated\":true}", MediaType.APPLICATION_JSON));

        PartialPaymentRequest request = new PartialPaymentRequest();
        request.setBillingAccountId("BA-001");
        request.setBillId("CB-001");
        request.setAmount(new MoneyDto("INR", new BigDecimal("100.00")));

        String result = service.processPayment(request);

        assertTrue(result.contains("paymentId: PAY-"));
        verify(paymentRepository).save(any(Payment.class));
        server.verify();
    }
}
