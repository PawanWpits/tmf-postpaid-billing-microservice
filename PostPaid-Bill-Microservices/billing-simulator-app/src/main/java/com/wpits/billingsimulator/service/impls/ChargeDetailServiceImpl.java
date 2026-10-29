package com.wpits.billingsimulator.service.impls;

import com.wpits.billingsimulator.dto.ChargesResponse;
import com.wpits.billingsimulator.service.ChargeDetailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@Slf4j
public class ChargeDetailServiceImpl implements ChargeDetailService {

    @Override
    public ChargesResponse chargeDetails(String billingAccountId, LocalDate periodStart, LocalDate periodEnd) {

        log.info("ChargeDetailServiceImpl.chargeDetails()");

        if (periodEnd.isBefore(periodStart)) {
            log.warn("periodEnd must not be before periodStart");
            throw new IllegalArgumentException("periodEnd must not be before periodStart");
        }

        return ChargesResponse.builder()
                .billingAccountId(billingAccountId)
                .periodStart(periodStart.toString())
                .periodEnd(periodEnd.toString())
                .planName("W! Max 499")
                .monthlyPlanCharge(new BigDecimal("499.00"))
                .usageCharge(new BigDecimal("100.00"))
                .vasCharge(new BigDecimal("50.00"))
                .discount(new BigDecimal("50.00"))
                .previousBalance(new BigDecimal("0"))
                .taxRate(new BigDecimal("10"))
                .currency("INR")
                .build();
    }
}
