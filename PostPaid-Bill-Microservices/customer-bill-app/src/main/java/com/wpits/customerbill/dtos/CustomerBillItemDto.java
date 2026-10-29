package com.wpits.customerbill.dtos;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerBillItemDto {
    private String id;
    private String description;
    private String planName;
    private BigDecimal amount;
    private String unit;
}
