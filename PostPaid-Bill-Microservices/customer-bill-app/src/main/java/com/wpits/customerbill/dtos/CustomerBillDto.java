package com.wpits.customerbill.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CustomerBillDto {

    private String id;
    private String href;
    private String billNo;
    private MoneyDto amountDue;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private LocalDateTime billDate;

    private TimePeriodDto billingPeriod;
    private String category;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private LocalDateTime lastUpdate;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private LocalDateTime nextBillDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private LocalDateTime paymentDueDate;

    private MoneyDto remainingAmount;

    //onCycle, offCycle 
    private String runType;

    // new, validated, sent, settled, partiallyPaid, onHold
    private String state;

    private MoneyDto taxExcludedAmount;
    private MoneyDto taxIncludedAmount;

    
    private BillCycleDto billCycle;
    private BillingAccountRefDto billingAccount;
    private FinancialAccountRefDto financialAccount;
    private PaymentMethodRefDto paymentMethod;

    
    private List<AppliedPaymentDto> appliedPayment;
    private List<AttachmentRefOrValueDto> billDocument;
    private List<TaxItemDto> taxItem;
    private List<RelatedPartyRefOrPartyRoleRefDto> relatedParty;
    private List<CustomerBillItemDto> billItem;

    @JsonProperty("@baseType")
    private String baseType;

    @JsonProperty("@schemaLocation")
    private String schemaLocation;

    @JsonProperty("@type")
    private String type;
}
