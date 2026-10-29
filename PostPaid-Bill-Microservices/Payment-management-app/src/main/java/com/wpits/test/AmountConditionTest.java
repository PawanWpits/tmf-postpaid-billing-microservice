package com.wpits.test;

import java.math.BigDecimal;

public class AmountConditionTest {

    public static void main(String[] args) {
        BigDecimal totalAmount = BigDecimal.valueOf(100);
        BigDecimal remainingValue = BigDecimal.valueOf(100);

        if (totalAmount.equals(remainingValue) )
        {

            System.out.println("Condition met: totalAmount equals remainingValue.");
        } else {
            System.out.println("Condition not met.");
            throw new IllegalArgumentException("totalAmount does not equal remainingValue.");
        }
    }
}
