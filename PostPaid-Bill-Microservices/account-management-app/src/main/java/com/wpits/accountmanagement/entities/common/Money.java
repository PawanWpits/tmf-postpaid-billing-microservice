package com.wpits.accountmanagement.entities.common;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/** TMF666 Money - base / value business entity used to represent money. */
@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Money {
    private String unit;
    private BigDecimal value;
}
