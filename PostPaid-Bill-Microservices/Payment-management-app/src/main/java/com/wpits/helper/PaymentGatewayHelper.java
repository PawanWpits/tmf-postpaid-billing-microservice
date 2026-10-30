package com.wpits.helper;

import com.wpits.exceptions.BadRequestException;
import com.wpits.repositories.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentGatewayHelper {

    private final RestClient restClient;

    @Value("${payment.gateway.url}")
    private String paymentGatewayUrl;

    public String processPayment(BigDecimal amount, String currency, String paymentMethod) {

        //API call to payment gateway
        Map<String, String> paymentDetails = Map.of(
                "amount", amount.toString(),
                "currency", currency,
                "paymentMethod", paymentMethod
        );

        try {
            String response = restClient.post()
                    .uri(paymentGatewayUrl)
                    .body(paymentDetails)
                    .retrieve()
                    .body(String.class);

            log.info("Payment gateway api url: {}, request: {}, response: {}", paymentGatewayUrl, paymentDetails, response);

            return response;
        } catch (Exception e) {
            log.error("Error while api url: {}, request: {} processing payment: {}", paymentGatewayUrl, paymentDetails, e.getMessage());
            throw new BadRequestException("Payment processing failed"+ e.getMessage(),"","");
        }
    }
}
