package com.wpits.billingsimulator.service;

import com.wpits.billingsimulator.dto.ChargesResponse;

import java.time.LocalDate;

public interface ChargeDetailService {

    ChargesResponse chargeDetails(String billingAccountId, LocalDate periodStart, LocalDate periodEnd);
}
