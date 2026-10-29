package com.wpits.customerbill.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * TMF678 PartyRefOrPartyRoleRef - the polymorphic "partyOrPartyRole" attribute of
 * RelatedPartyRefOrPartyRoleRef (0..1). Discriminated by {@code type}
 * ("PartyRef" or "PartyRoleRef"); partyId/partyName only apply to PartyRoleRef.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "party_ref_or_role_ref")
public class PartyRefOrPartyRoleRef {

    @Id
    private String id;

    private String href;
    private String name;

    /** only meaningful when type = "PartyRoleRef" */
    private String partyId;
    private String partyName;

    @Column(name = "billing_account_id")
    private String billingAccountId;

    private String referredType;
    private String baseType;
    private String schemaLocation;

    /** "PartyRef" or "PartyRoleRef" */
    private String type;
}
