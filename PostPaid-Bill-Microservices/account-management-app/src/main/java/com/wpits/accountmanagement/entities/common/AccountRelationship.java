package com.wpits.accountmanagement.entities.common;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * TMF666 AccountRelationship sub-resource - Account.accountRelationship (0..*).
 * NOTE: this class' own {@code id} collides with the nested {@link AccountRef}'s
 * {@code id} (and href/name/referredType), so every nested column is remapped
 * via @AttributeOverrides to avoid Hibernate's "Column is duplicated" error.
 */
@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountRelationship {

    /** Identifier of the relationship within the list. */
    private String id;

    /** Type of relationship. */
    private String relationshipType;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "id", column = @Column(name = "related_account_id")),
            @AttributeOverride(name = "href", column = @Column(name = "related_account_href")),
            @AttributeOverride(name = "name", column = @Column(name = "related_account_name")),
            @AttributeOverride(name = "referredType", column = @Column(name = "related_account_referred_type"))
    })
    private AccountRef account;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "startDateTime", column = @Column(name = "relationship_valid_from")),
            @AttributeOverride(name = "endDateTime", column = @Column(name = "relationship_valid_to"))
    })
    private TimePeriod validFor;
}
