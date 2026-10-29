package com.wpits.accountmanagement.entities;

import com.wpits.accountmanagement.entities.common.RelatedPartyRefOrPartyRoleRef;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * TMF666 BillingAccount managed entity (spec page 26) - "A party account used
 * for billing purposes. It includes a description of the bill structure
 * (frequency, presentation media, format and so on). It is a specialization
 * of entity PartyAccount."
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "billing_account")
public class BillingAccount extends AbstractPartyAccount {

    @Builder.Default
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "party_account_related_party",
            joinColumns = @JoinColumn(name = "billing_account_id"))
    private List<RelatedPartyRefOrPartyRoleRef> relatedParty = new ArrayList<>();

    /** Indicates whether the account follows prepaid or postpaid. */
    private String ratingType;
}
