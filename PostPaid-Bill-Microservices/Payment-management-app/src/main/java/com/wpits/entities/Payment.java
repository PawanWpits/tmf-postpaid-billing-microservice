package com.wpits.entities;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    @Id
    private String id;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "unit", column = @Column(name = "amount_unit")),
            @AttributeOverride(name = "value", column = @Column(name = "amount_value"))
    })
    private Money amount;

    private String authorizationCode;
    private String correlatorId;
    private String description;
    private String href;
    private String name;
    private LocalDateTime paymentDate;
    private String status;
    private LocalDateTime statusDate;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "unit", column = @Column(name = "tax_amount_unit")),
            @AttributeOverride(name = "value", column = @Column(name = "tax_amount_value"))
    })
    private Money taxAmount;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "unit", column = @Column(name = "total_amount_unit")),
            @AttributeOverride(name = "value", column = @Column(name = "total_amount_value"))
    })
    private Money totalAmount;

    private String baseType;
    private String schemaLocation;
    private String type;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "channel_id")
    private ChannelRef channel;

    @ManyToOne(optional = false, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "account_id", nullable = false)
    private AccountRef account;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "payer_id")
    private RelatedParty payer;

    @OneToOne(optional = false, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "payment_method_id", nullable = false)
    private PaymentMethodRefOrValue paymentMethod;

    @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PaymentItem> paymentItem = new ArrayList<>();
}
