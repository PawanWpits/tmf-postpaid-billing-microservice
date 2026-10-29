package com.wpits.accountmanagement.entities.common;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * TMF666 RelatedPartyRefOrPartyRoleRef - e.g. Account.relatedParty (0..*).
 * No field-name collisions with the nested {@link PartyRefOrPartyRoleRef}
 * (this class only declares {@code role}), so no @AttributeOverrides needed.
 */
@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RelatedPartyRefOrPartyRoleRef {

    /** e.g. 'initiator', 'customer', 'salesAgent', 'user' */
    private String role;

    @Embedded
    private PartyRefOrPartyRoleRef partyOrPartyRole;
}
