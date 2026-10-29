package com.wpits.customerbill.entities;

import com.wpits.customerbill.enums.CustomerBillRunType;
import com.wpits.customerbill.enums.CustomerBillStateType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "customer_bill",
        uniqueConstraints = @UniqueConstraint(name = "uk_customer_bill_number", columnNames = "billNo"))
public class CustomerBill {

    @Id
    private String id;

    private String href;

    private String billNo;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "unit", column = @Column(name = "amount_due_unit")),
            @AttributeOverride(name = "value", column = @Column(name = "amount_due_value"))
    })
    private Money amountDue;

    private LocalDateTime billDate;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "startDateTime", column = @Column(name = "billing_period_start")),
            @AttributeOverride(name = "endDateTime", column = @Column(name = "billing_period_end"))
    })
    private TimePeriod billingPeriod;

    private String category;

    private LocalDateTime lastUpdate;
    private LocalDateTime nextBillDate;
    private LocalDateTime paymentDueDate;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "unit", column = @Column(name = "remaining_unit")),
            @AttributeOverride(name = "value", column = @Column(name = "remaining_value"))
    })
    private Money remainingAmount;

    @Enumerated(EnumType.STRING)
    private CustomerBillRunType runType;

    @Enumerated(EnumType.STRING)
    private CustomerBillStateType state;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "unit", column = @Column(name = "tax_excluded_unit")),
            @AttributeOverride(name = "value", column = @Column(name = "tax_excluded_value"))
    })
    private Money taxExcludedAmount;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "unit", column = @Column(name = "tax_included_unit")),
            @AttributeOverride(name = "value", column = @Column(name = "tax_included_value"))
    })
    private Money taxIncludedAmount;

    private String baseType;
    private String schemaLocation;

    @Builder.Default
    private String type = "CustomerBill";

    // 0..1 relationships

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "bill_cycle_ref_id")
    private BillCycle billCycle;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "billing_account_id")
    private BillingAccountRef billingAccount;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "financial_account_id")
    private FinancialAccountRef financialAccount;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "payment_method_id")
    private PaymentMethodRef paymentMethod;

    //0..* relationships

    @Builder.Default
    @OneToMany(mappedBy = "customerBill", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AppliedPayment> appliedPayment = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "customerBill", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AttachmentRefOrValue> billDocument = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "customerBill", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TaxItem> taxItem = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "customerBill", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RelatedPartyRefOrPartyRoleRef> relatedParty = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "customerBill", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CustomerBillItem> billItem = new ArrayList<>();
}
