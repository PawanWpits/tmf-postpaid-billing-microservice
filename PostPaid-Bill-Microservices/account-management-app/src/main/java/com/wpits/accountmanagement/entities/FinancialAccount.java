package com.wpits.accountmanagement.entities;

import com.wpits.accountmanagement.entities.common.RelatedPartyRefOrPartyRoleRef;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
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
@Table(name = "financial_account")
public class FinancialAccount extends AbstractAccount {

    @Builder.Default
    @ElementCollection(fetch = FetchType.LAZY)
    private List<RelatedPartyRefOrPartyRoleRef> relatedParty = new ArrayList<>();
}
