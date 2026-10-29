package com.wpits.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_method_ref_or_value")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentMethodRefOrValue {
    @Id
    private String id;
    private String description;
    private String href;
    private boolean isPreferred;
    private String name;
    private String status;
    private LocalDateTime statusDate;
    private String baseType;
    private String referredType;
    private String schemaLocation;
    private String type;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "startDateTime", column = @Column(name = "valid_for_start")),
            @AttributeOverride(name = "endDateTime", column = @Column(name = "valid_for_end"))
    })
    private TimePeriod validFor;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "related_party_id")
    private RelatedParty relatedParty;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "payment_method_account",
            joinColumns = @JoinColumn(name = "payment_method_id"),
            inverseJoinColumns = @JoinColumn(name = "account_id"))
    private List<AccountRef> account = new ArrayList<>();
}
