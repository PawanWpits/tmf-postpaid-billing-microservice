package com.wpits.dtos;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class PartialPaymentRequest {
    private String billingAccountId;
    private String billId;
    private MoneyDto amount;
}
