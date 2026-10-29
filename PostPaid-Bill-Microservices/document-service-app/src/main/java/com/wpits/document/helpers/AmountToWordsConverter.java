package com.wpits.document.helpers;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class AmountToWordsConverter {

    private static final String[] units = {
            "", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine",
            "Ten", "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen",
            "Seventeen", "Eighteen", "Nineteen"
    };

    private static final String[] tens = {
            "", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety"
    };

    public  String convertAmountToWords(BigDecimal amount) {
        if (amount == null) {
            return "Zero Rupees Only";
        }

        // Ensure 2 decimal places for currency
        amount = amount.setScale(2, RoundingMode.HALF_UP);

        long rupees = amount.longValue();
        int paise = amount.remainder(BigDecimal.ONE).movePointRight(2).intValue();

        String rupeesWords = (rupees == 0) ? "Zero" : convertNumberToWords(rupees);
        String paiseWords = (paise == 0) ? "" : " and " + convertNumberToWords(paise) + " Paise";

        return rupeesWords + " Rupees" + paiseWords + " Only";
    }

    private  String convertNumberToWords(long n) {
        if (n == 0) {
            return "Zero";
        }
        if (n < 20) {
            return units[(int) n];
        }
        if (n < 100) {
            return tens[(int) (n / 10)] + ((n % 10 != 0) ? " " + units[(int) (n % 10)] : "");
        }
        if (n < 1000) {
            return units[(int) (n / 100)] + " Hundred" + ((n % 100 != 0) ? " " + convertNumberToWords(n % 100) : "");
        }
        if (n < 100000) {
            return convertNumberToWords(n / 1000) + " Thousand" + ((n % 1000 != 0) ? " " + convertNumberToWords(n % 1000) : "");
        }
        if (n < 10000000) {
            return convertNumberToWords(n / 100000) + " Lakh" + ((n % 100000 != 0) ? " " + convertNumberToWords(n % 100000) : "");
        }
        return convertNumberToWords(n / 10000000) + " Crore" + ((n % 10000000 != 0) ? " " + convertNumberToWords(n % 10000000) : "");
    }

    /*public static void main(String[] args) {
        BigDecimal amount = new BigDecimal("56721.50");
        System.out.println(convertAmountToWords(amount));

    }*/
}

