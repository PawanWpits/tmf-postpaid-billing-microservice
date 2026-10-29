package com.wpits.accountmanagement.entities;

import com.wpits.accountmanagement.entities.common.RelatedPartyRefOrPartyRoleRef;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@Entity
@Table(name="party_account")
public class PartyAccount extends AbstractPartyAccount {

    @Builder.Default
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "party_account_entity_related_party",
            joinColumns = @JoinColumn(name = "party_account_id"))
    private List<RelatedPartyRefOrPartyRoleRef> relatedParty = new ArrayList<>();
}
