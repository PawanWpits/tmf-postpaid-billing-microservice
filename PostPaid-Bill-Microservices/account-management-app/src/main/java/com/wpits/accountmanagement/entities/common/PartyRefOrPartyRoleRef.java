package com.wpits.accountmanagement.entities.common;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * TMF666 PartyRefOrPartyRoleRef - the polymorphic "partyOrPartyRole" attribute
 * of RelatedPartyRefOrPartyRoleRef. Discriminated by {@code type} ("PartyRef"
 * or "PartyRoleRef"); partyId/partyName only apply to PartyRoleRef.
 */
@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartyRefOrPartyRoleRef {
    private String id;
    private String href;
    private String name;

    /** only meaningful when type = "PartyRoleRef" */
    private String partyId;
    private String partyName;

    private String referredType;

    /** "PartyRef" or "PartyRoleRef" */
    private String type;
}
