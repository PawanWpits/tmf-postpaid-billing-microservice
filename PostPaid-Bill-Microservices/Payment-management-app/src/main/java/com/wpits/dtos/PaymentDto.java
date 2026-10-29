package com.wpits.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@ToString
public class PaymentDto {
    private String id;
    private MoneyDto amount;
    private String authorizationCode;
    private String correlatorId;
    private String description;
    private String href;
    private String name;
    private LocalDateTime paymentDate;
    private String status;
    private LocalDateTime statusDate;
    private MoneyDto taxAmount;
    private MoneyDto totalAmount;
    private ChannelRefDto channel;
    private RelatedPartyDto payer;
    private AccountRefDto account;
    private PaymentMethodRefOrValueDto paymentMethod;
    private List<PaymentItemDto> paymentItem;

    @JsonProperty("@baseType")
    private String baseType;

    @JsonProperty("@schemaLocation")
    private String schemaLocation;

    @JsonProperty("@type")
    private String type;
}
