package com.wpits.accountmanagement.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.wpits.accountmanagement.dtos.common.AccountBalanceDto;
import com.wpits.accountmanagement.dtos.common.AccountRelationshipDto;
import com.wpits.accountmanagement.dtos.common.BillStructureDto;
import com.wpits.accountmanagement.dtos.common.ContactDto;
import com.wpits.accountmanagement.dtos.common.FinancialAccountRefDto;
import com.wpits.accountmanagement.dtos.common.MoneyDto;
import com.wpits.accountmanagement.dtos.common.PaymentMethodRefDto;
import com.wpits.accountmanagement.dtos.common.PaymentPlanDto;
import com.wpits.accountmanagement.dtos.common.RelatedPartyRefOrPartyRoleRefDto;
import com.wpits.accountmanagement.dtos.common.TaxExemptionCertificateDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * TMF666 PartyAccount resource DTO (spec page 7). Also used, unchanged, as
 * the PATCH request body (merge-patch semantics; lists are replaced wholesale
 * when supplied).
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PartyAccountDto {
    private String id;
    private String href;
    private String name;
    private String description;
    private String accountType;

    /** PendingActive | Active | Suspended | Closed */
    private String state;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private LocalDateTime lastUpdate;

    private MoneyDto creditLimit;
    private String paymentStatus;

    private BillStructureDto billStructure;
    private PaymentMethodRefDto defaultPaymentMethod;
    private FinancialAccountRefDto financialAccount;

    private List<AccountBalanceDto> accountBalance;
    private List<AccountRelationshipDto> accountRelationship;
    private List<ContactDto> contact;
    private List<PaymentPlanDto> paymentPlan;
    private List<RelatedPartyRefOrPartyRoleRefDto> relatedParty;
    private List<TaxExemptionCertificateDto> taxExemption;

    @JsonProperty("@baseType")
    private String baseType;

    @JsonProperty("@schemaLocation")
    private String schemaLocation;

    @JsonProperty("@type")
    private String type;
}
