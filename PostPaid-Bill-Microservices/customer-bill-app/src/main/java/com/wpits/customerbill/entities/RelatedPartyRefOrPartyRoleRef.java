package com.wpits.customerbill.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "related_party_ref_or_role_ref")
public class RelatedPartyRefOrPartyRoleRef {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //initiator, customer, salesAgent, user
    private String role;

    private String baseType;
    private String schemaLocation;

    @Builder.Default
    private String type = "RelatedPartyRefOrPartyRoleRef";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "party_ref_or_role_id", unique = false)
    private PartyRefOrPartyRoleRef partyOrPartyRole;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_bill_id")
    private CustomerBill customerBill;
}
