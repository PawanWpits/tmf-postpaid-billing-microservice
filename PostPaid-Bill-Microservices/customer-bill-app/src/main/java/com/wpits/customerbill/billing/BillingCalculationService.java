package com.wpits.customerbill.billing;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class BillingCalculationService {

    public BillCalculation calculate(BillingInput input) {

        if (input.taxRate() == null || input.taxRate().signum() < 0) {
            throw new IllegalArgumentException("Billing input must contain a non-negative tax rate");
        }
        BigDecimal subtotal = input.monthlyPlanCharge()
                .add(input.usageCharge()).add(input.vasCharge()).subtract(input.discount())
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal tax = subtotal.multiply(input.taxRate())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        BigDecimal currentBill = subtotal.add(tax).setScale(2, RoundingMode.HALF_UP);

        BigDecimal amountDue = currentBill.add(input.previousBalance()).setScale(2, RoundingMode.HALF_UP);

        return new BillCalculation(subtotal, tax, currentBill, amountDue);
    }
}
