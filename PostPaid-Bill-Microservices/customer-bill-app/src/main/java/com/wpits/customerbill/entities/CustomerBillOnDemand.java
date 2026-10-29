package com.wpits.customerbill.entities;

import com.wpits.customerbill.enums.CustomerBillOnDemandStateType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "customer_bill_on_demand")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CustomerBillOnDemand {

    @Id
    private String id;

    private String href;

    private String description;

    private LocalDateTime lastUpdate;

    private String name;

    @Enumerated(EnumType.STRING)
    private CustomerBillOnDemandStateType state;

    private String baseType;

    private String schemaLocation;

    @Builder.Default
    private String type = "CustomerBillOnDemand";

    @ManyToOne
    @JoinColumn(name = "billing_account_id")
    private BillingAccountRef billingAccount;

    @ManyToOne
    @JoinColumn(name = "customer_bill_id")
    private CustomerBill customerBill;

    @OneToOne(cascade = jakarta.persistence.CascadeType.ALL)
    @JoinColumn(name = "related_party_id")
    private RelatedPartyRefOrPartyRoleRef relatedParty;
}
