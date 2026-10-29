package com.wpits.accountmanagement.entities;

import com.wpits.accountmanagement.entities.common.AccountBalance;
import com.wpits.accountmanagement.entities.common.AccountRelationship;
import com.wpits.accountmanagement.entities.common.Contact;
import com.wpits.accountmanagement.entities.common.Money;
import com.wpits.accountmanagement.entities.common.TaxExemptionCertificate;
import com.wpits.accountmanagement.enums.AccountStateType;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@MappedSuperclass
public abstract class AbstractAccount {

    @Id
    private String id;

    private String href;
    private String name;
    private String description;

    /** A categorization of an account, such as individual, joint, and so forth. */
    private String accountType;

    @Enumerated(EnumType.STRING)
    private AccountStateType state;

    private LocalDateTime lastUpdate;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "unit", column = @Column(name = "credit_limit_unit")),
            @AttributeOverride(name = "value", column = @Column(name = "credit_limit_value"))
    })
    private Money creditLimit;

    private String baseType;
    private String schemaLocation;

    private String type;

    // ---- 0..* relationships (each collection gets its own table, named after
    // the concrete entity's own @Table, so no collision across subclasses) ----

    @Builder.Default
    @ElementCollection(fetch = FetchType.LAZY)
    private List<AccountBalance> accountBalance = new ArrayList<>();

    @Builder.Default
    @ElementCollection(fetch = FetchType.LAZY)
    private List<AccountRelationship> accountRelationship = new ArrayList<>();

    @Builder.Default
    @ElementCollection(fetch = FetchType.LAZY)
    private List<Contact> contact = new ArrayList<>();

    @Builder.Default
    @ElementCollection(fetch = FetchType.LAZY)
    private List<TaxExemptionCertificate> taxExemption = new ArrayList<>();
}
