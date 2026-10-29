package com.wpits.dtos;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class MoneyDto {
    private String unit;
    private BigDecimal value;
}
