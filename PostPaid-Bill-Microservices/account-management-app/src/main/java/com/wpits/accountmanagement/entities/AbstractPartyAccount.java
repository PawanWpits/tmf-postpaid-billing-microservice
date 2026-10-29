package com.wpits.accountmanagement.entities;

import com.wpits.accountmanagement.entities.common.BillStructure;
import com.wpits.accountmanagement.entities.common.FinancialAccountRef;
import com.wpits.accountmanagement.entities.common.PaymentMethodRef;
import com.wpits.accountmanagement.entities.common.PaymentPlan;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.FetchType;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * TMF666 "PartyAccount" fields (spec page 9) - "Account used for billing or
 * for settlement purposes concerning a given party. It is a specialization
 * of entity Account." Shared by PartyAccount, BillingAccount and
 * SettlementAccount (BillingAccount additionally adds {@code ratingType}).
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@MappedSuperclass
public abstract class AbstractPartyAccount extends AbstractAccount {

    /** e.g. due, paid, in arrears */
    private String paymentStatus;

    @Embedded
    private BillStructure billStructure;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "id", column = @Column(name = "default_payment_method_id")),
            @AttributeOverride(name = "href", column = @Column(name = "default_payment_method_href")),
            @AttributeOverride(name = "name", column = @Column(name = "default_payment_method_name")),
            @AttributeOverride(name = "referredType", column = @Column(name = "default_payment_method_referred_type"))
    })
    private PaymentMethodRef defaultPaymentMethod;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "id", column = @Column(name = "financial_account_id")),
            @AttributeOverride(name = "href", column = @Column(name = "financial_account_href")),
            @AttributeOverride(name = "name", column = @Column(name = "financial_account_name")),
            @AttributeOverride(name = "referredType", column = @Column(name = "financial_account_referred_type"))
    })
    private FinancialAccountRef financialAccount;

    @Builder.Default
    @ElementCollection(fetch = FetchType.LAZY)
    private List<PaymentPlan> paymentPlan = new ArrayList<>();
}
