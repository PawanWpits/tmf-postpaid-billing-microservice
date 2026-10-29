package com.wpits.customerbill.billing;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class SimulatorBillingInputProvider implements BillingInputProvider {

    private final RestClient.Builder restClientBuilder;

    @Value("${billing.simulator.base-url}")
    private String simulatorBaseUrl;

    @Override
    public BillingInput getBillingInput(String billingAccountId, LocalDate periodStart, LocalDate periodEnd) {

        ChargesResponse response = restClientBuilder.build().get()
                .uri(uriBuilder -> uriBuilder
                        .scheme(java.net.URI.create(simulatorBaseUrl).getScheme())
                        .host(java.net.URI.create(simulatorBaseUrl).getHost())
                        .port(java.net.URI.create(simulatorBaseUrl).getPort())
                        .path("/api/charges")
                        .queryParam("billingAccountId", billingAccountId)
                        .queryParam("periodStart", periodStart)
                        .queryParam("periodEnd", periodEnd)
                        .build())
                .retrieve()
                .body(ChargesResponse.class);

        if (response == null) {
            throw new IllegalStateException("Billing simulator returned no charges");
        }

        return new BillingInput(response.planName(), response.monthlyPlanCharge(), response.usageCharge(),
                response.vasCharge(), response.discount(), response.previousBalance(), response.taxRate(),
                response.currency());
    }

    private record ChargesResponse(String planName,
                                   BigDecimal monthlyPlanCharge,
                                   BigDecimal usageCharge,
                                   BigDecimal vasCharge,
                                   BigDecimal discount,
                                   BigDecimal previousBalance,
                                   BigDecimal taxRate,
                                   String currency) {
    }
}
