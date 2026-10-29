package com.wpits.accountmanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * TMF666 Account Management API.
 * Covers: BillFormat, PartyAccount, BillingAccount, FinancialAccount,
 * SettlementAccount, BillPresentationMedia, BillingCycleSpecification.
 */
@SpringBootApplication
public class AccountManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(AccountManagementApplication.class, args);
    }
}
