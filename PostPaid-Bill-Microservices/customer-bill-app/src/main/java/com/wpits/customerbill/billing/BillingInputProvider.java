package com.wpits.customerbill.billing;

import java.time.LocalDate;

public interface BillingInputProvider {

    BillingInput getBillingInput(String billingAccountId, LocalDate periodStart, LocalDate periodEnd);
}
