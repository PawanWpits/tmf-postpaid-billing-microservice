package com.wpits.test;

import java.math.BigDecimal;

public class PartialPayment {
    public static void main(String[] args) {

        BigDecimal amount = new BigDecimal("100");
        BigDecimal remainingAmount = new BigDecimal("50");

        if (remainingAmount.compareTo(BigDecimal.ZERO) < 0 || amount.compareTo(remainingAmount) > 0) {
            System.out.println("Your paid amount is greater than the remaining amount.");
            //throw new IllegalArgumentException("Your paid amount is greater than the remaining amount ");
        }

        if (amount.compareTo(remainingAmount) > 0) {
            System.out.println("Payment rejected. Outstanding amount is greater than the remaining amount.");
            //throw new IllegalArgumentException("Payment rejected. Outstanding amount is greater than the remaining amount.");
        }
    }
}
