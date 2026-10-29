package com.wpits.customerbill.utils;

import com.wpits.customerbill.dtos.*;
import com.wpits.customerbill.entities.*;
import com.wpits.customerbill.repositories.PartyRefOrPartyRoleRefRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;


@Component
@RequiredArgsConstructor
public class CustomerBillMapper {

    private final PartyRefOrPartyRoleRefRepository partyRefRepository;

    public CustomerBill toEntity(CustomerBillDto dto) {
        if (dto == null) return null;

        CustomerBill entity = CustomerBill.builder()
                .id(dto.getId())
                .href(dto.getHref())
                .billNo(dto.getBillNo())
                .amountDue(toMoney(dto.getAmountDue()))
                .billDate(dto.getBillDate())
                .billingPeriod(toTimePeriod(dto.getBillingPeriod()))
                .category(dto.getCategory())
                .lastUpdate(dto.getLastUpdate())
                .nextBillDate(dto.getNextBillDate())
                .paymentDueDate(dto.getPaymentDueDate())
                .remainingAmount(toMoney(dto.getRemainingAmount()))
                .runType(dto.getRunType() == null ? null :
                        com.wpits.customerbill.enums.CustomerBillRunType.fromValue(dto.getRunType()))
                .state(dto.getState() == null ? null :
                        com.wpits.customerbill.enums.CustomerBillStateType.fromValue(dto.getState()))
                .taxExcludedAmount(toMoney(dto.getTaxExcludedAmount()))
                .taxIncludedAmount(toMoney(dto.getTaxIncludedAmount()))
                .baseType(dto.getBaseType())
                .schemaLocation(dto.getSchemaLocation())
                .type(dto.getType() == null ? "CustomerBill" : dto.getType())
                .billCycle(toBillCycleRef(dto.getBillCycle()))
                .billingAccount(toBillingAccountRef(dto.getBillingAccount()))
                .financialAccount(toFinancialAccountRef(dto.getFinancialAccount()))
                .paymentMethod(toPaymentMethodRef(dto.getPaymentMethod()))
                .build();

        if (dto.getAppliedPayment() != null) {
            List<AppliedPayment> list = dto.getAppliedPayment().stream()
                    .map(this::toAppliedPayment)
                    .collect(Collectors.toList());
            list.forEach(ap -> ap.setCustomerBill(entity));
            entity.setAppliedPayment(list);
        }

        if (dto.getBillDocument() != null) {
            List<AttachmentRefOrValue> list = dto.getBillDocument().stream()
                    .map(this::toAttachmentRefOrValue)
                    .collect(Collectors.toList());
            list.forEach(a -> a.setCustomerBill(entity));
            entity.setBillDocument(list);
        }

        if (dto.getTaxItem() != null) {
            List<TaxItem> list = dto.getTaxItem().stream()
                    .map(this::toTaxItem)
                    .collect(Collectors.toList());
            list.forEach(t -> t.setCustomerBill(entity));
            entity.setTaxItem(list);
        }

        if (dto.getRelatedParty() != null) {
            List<RelatedPartyRefOrPartyRoleRef> list = dto.getRelatedParty().stream()
                    .map(related -> toRelatedParty(related,
                            dto.getBillingAccount() == null ? null : dto.getBillingAccount().getId()))
                    .collect(Collectors.toList());
            list.forEach(r -> r.setCustomerBill(entity));
            entity.setRelatedParty(list);
        }
        if (dto.getBillItem() != null) {
            List<CustomerBillItem> list = dto.getBillItem().stream()
                    .map(item -> CustomerBillItem.builder().id(item.getId()).description(item.getDescription())
                            .planName(item.getPlanName()).amount(item.getAmount()).unit(item.getUnit())
                            .customerBill(entity).build())
                    .collect(Collectors.toList());
            entity.setBillItem(list);
        }

        return entity;
    }

    private Money toMoney(MoneyDto d) {
        return d == null ? null : new Money(d.getUnit(), d.getValue());
    }

    private TimePeriod toTimePeriod(TimePeriodDto d) {
        return d == null ? null : TimePeriod.builder()
                .startDateTime(d.getStartDateTime())
                .endDateTime(d.getEndDateTime())
                .build();
    }

    private BillCycle toBillCycleRef(BillCycleDto billCycleDto) {
        if (billCycleDto == null) return null;
        return BillCycle.builder()
                .id(billCycleDto.getId()).href(billCycleDto.getHref()).name(billCycleDto.getName())
                //.referredType(d.getReferredType()).baseType(d.getBaseType())
                .schemaLocation(billCycleDto.getSchemaLocation())
                .type(billCycleDto.getType() == null ? "BillCycleRef" : billCycleDto.getType())
                .build();
    }

    private BillingAccountRef toBillingAccountRef(BillingAccountRefDto d) {
        if (d == null) return null;
        return BillingAccountRef.builder()
                .id(d.getId()).href(d.getHref()).name(d.getName()).ratingType(d.getRatingType())
                .referredType(d.getReferredType()).baseType(d.getBaseType())
                .schemaLocation(d.getSchemaLocation())
                .type(d.getType() == null ? "BillingAccountRef" : d.getType())
                .build();
    }

    private FinancialAccountRef toFinancialAccountRef(FinancialAccountRefDto d) {
        if (d == null) return null;
        return FinancialAccountRef.builder()
                .id(d.getId()).href(d.getHref()).name(d.getName())
                .referredType(d.getReferredType()).baseType(d.getBaseType())
                .schemaLocation(d.getSchemaLocation())
                .type(d.getType() == null ? "FinancialAccountRef" : d.getType())
                .build();
    }

    private PaymentMethodRef toPaymentMethodRef(PaymentMethodRefDto d) {
        if (d == null) return null;
        return PaymentMethodRef.builder()
                .id(d.getId()).href(d.getHref()).name(d.getName())
                .referredType(d.getReferredType()).baseType(d.getBaseType())
                .schemaLocation(d.getSchemaLocation())
                .type(d.getType() == null ? "PaymentMethodRef" : d.getType())
                .build();
    }

    private PaymentRef toPaymentRef(PaymentRefDto d) {
        if (d == null) return null;
        return PaymentRef.builder()
                .id(d.getId()).href(d.getHref()).name(d.getName())
                .referredType(d.getReferredType()).baseType(d.getBaseType())
                .schemaLocation(d.getSchemaLocation())
                .type(d.getType() == null ? "PaymentRef" : d.getType())
                .build();
    }

    private AppliedPayment toAppliedPayment(AppliedPaymentDto d) {
        return AppliedPayment.builder()
                .appliedAmount(toMoney(d.getAppliedAmount()))
                .payment(toPaymentRef(d.getPayment()))
                .baseType(d.getBaseType())
                .schemaLocation(d.getSchemaLocation())
                .type(d.getType() == null ? "AppliedPayment" : d.getType())
                .build();
    }

    private AttachmentRefOrValue toAttachmentRefOrValue(AttachmentRefOrValueDto d) {
        return AttachmentRefOrValue.builder()
                .id(d.getId()).href(d.getHref()).name(d.getName()).description(d.getDescription())
                .attachmentType(d.getAttachmentType()).mimeType(d.getMimeType()).size(d.getSize())
                .url(d.getUrl()).content(d.getContent())
                .validFor(toTimePeriod(d.getValidFor()))
                .referredType(d.getReferredType()).baseType(d.getBaseType())
                .schemaLocation(d.getSchemaLocation())
                .type(d.getType() == null ? "AttachmentRef" : d.getType())
                .build();
    }

    private TaxItem toTaxItem(TaxItemDto d) {
        return TaxItem.builder()
                .id(d.getId()).taxCategory(d.getTaxCategory()).taxRate(d.getTaxRate())
                .taxAmount(toMoney(d.getTaxAmount()))
                .baseType(d.getBaseType())
                .schemaLocation(d.getSchemaLocation())
                .type(d.getType() == null ? "TaxItem" : d.getType())
                .build();
    }

    private PartyRefOrPartyRoleRef toPartyRefOrPartyRoleRef(PartyRefOrPartyRoleRefDto d,
                                                            String billingAccountId) {
        if (d == null) return null;
        String externalPartyId = d.getPartyId() == null || d.getPartyId().isBlank()
                ? d.getId()
                : d.getPartyId();
        if (externalPartyId == null || externalPartyId.isBlank()) {
            throw new IllegalArgumentException("CustomerBill related party must have a party id");
        }
        return partyRefRepository.findFirstByBillingAccountIdAndPartyId(billingAccountId, externalPartyId)
                .orElseGet(() -> partyRefRepository.save(PartyRefOrPartyRoleRef.builder()
                        .id(UUID.randomUUID().toString())
                        .billingAccountId(billingAccountId)
                        .href(d.getHref())
                        .name(d.getName())
                        .partyId(externalPartyId)
                        .partyName(d.getPartyName())
                        .referredType(d.getReferredType())
                        .baseType(d.getBaseType())
                        .schemaLocation(d.getSchemaLocation())
                        .type(d.getType() == null ? "PartyRef" : d.getType())
                        .build()));
    }

    private RelatedPartyRefOrPartyRoleRef toRelatedParty(RelatedPartyRefOrPartyRoleRefDto d,
                                                         String billingAccountId) {
        return RelatedPartyRefOrPartyRoleRef.builder()
                .role(d.getRole())
                .partyOrPartyRole(toPartyRefOrPartyRoleRef(d.getPartyOrPartyRole(), billingAccountId))
                .baseType(d.getBaseType())
                .schemaLocation(d.getSchemaLocation())
                .type(d.getType() == null ? "RelatedPartyRefOrPartyRoleRef" : d.getType())
                .build();
    }

    // ------------------------------------------------------------------ toDto

    public CustomerBillDto toDto(CustomerBill e) {
        if (e == null) return null;

        return CustomerBillDto.builder()
                .id(e.getId())
                .href(e.getHref())
                .billNo(e.getBillNo())
                .amountDue(toMoneyDto(e.getAmountDue()))
                .billDate(e.getBillDate())
                .billingPeriod(toTimePeriodDto(e.getBillingPeriod()))
                .category(e.getCategory())
                .lastUpdate(e.getLastUpdate())
                .nextBillDate(e.getNextBillDate())
                .paymentDueDate(e.getPaymentDueDate())
                .remainingAmount(toMoneyDto(e.getRemainingAmount()))
                .runType(e.getRunType() == null ? null : e.getRunType().getValue())
                .state(e.getState() == null ? null : e.getState().getValue())
                .taxExcludedAmount(toMoneyDto(e.getTaxExcludedAmount()))
                .taxIncludedAmount(toMoneyDto(e.getTaxIncludedAmount()))
                .baseType(e.getBaseType())
                .schemaLocation(e.getSchemaLocation())
                .type(e.getType())
                .billCycle(toBillCycleRefDto(e.getBillCycle()))
                .billingAccount(toBillingAccountRefDto(e.getBillingAccount()))
                .financialAccount(toFinancialAccountRefDto(e.getFinancialAccount()))
                .paymentMethod(toPaymentMethodRefDto(e.getPaymentMethod()))
                .appliedPayment(e.getAppliedPayment() == null ? new ArrayList<>() :
                        e.getAppliedPayment().stream().map(this::toAppliedPaymentDto).collect(Collectors.toList()))
                .billDocument(e.getBillDocument() == null ? new ArrayList<>() :
                        e.getBillDocument().stream().map(this::toAttachmentRefOrValueDto).collect(Collectors.toList()))
                .taxItem(e.getTaxItem() == null ? new ArrayList<>() :
                        e.getTaxItem().stream().map(this::toTaxItemDto).collect(Collectors.toList()))
                .relatedParty(e.getRelatedParty() == null ? new ArrayList<>() :
                        e.getRelatedParty().stream().map(this::toRelatedPartyDto).collect(Collectors.toList()))
                .billItem(e.getBillItem() == null ? new ArrayList<>() :
                        e.getBillItem().stream().map(item -> CustomerBillItemDto.builder()
                                .id(item.getId()).description(item.getDescription()).planName(item.getPlanName())
                                .amount(item.getAmount())
                                .unit(item.getUnit()).build()).collect(Collectors.toList()))
                .build();
    }

    private MoneyDto toMoneyDto(Money m) {
        return m == null ? null : MoneyDto.builder().unit(m.getUnit()).value(m.getValue()).build();
    }

    private TimePeriodDto toTimePeriodDto(TimePeriod t) {
        return t == null ? null : TimePeriodDto.builder()
                .startDateTime(t.getStartDateTime())
                .endDateTime(t.getEndDateTime())
                .build();
    }

    private BillCycleDto toBillCycleRefDto(BillCycle r) {
        if (r == null) return null;
        return BillCycleDto.builder()
                .id(r.getId()).href(r.getHref()).name(r.getName())
                //.referredType(r.getReferredType()).baseType(r.getBaseType())
                .schemaLocation(r.getSchemaLocation()).type(r.getType())
                .build();
    }

    private BillingAccountRefDto toBillingAccountRefDto(BillingAccountRef r) {
        if (r == null) return null;
        return BillingAccountRefDto.builder()
                .id(r.getId()).href(r.getHref()).name(r.getName()).ratingType(r.getRatingType())
                .referredType(r.getReferredType()).baseType(r.getBaseType())
                .schemaLocation(r.getSchemaLocation()).type(r.getType())
                .build();
    }

    private FinancialAccountRefDto toFinancialAccountRefDto(FinancialAccountRef r) {
        if (r == null) return null;
        return FinancialAccountRefDto.builder()
                .id(r.getId()).href(r.getHref()).name(r.getName())
                .referredType(r.getReferredType()).baseType(r.getBaseType())
                .schemaLocation(r.getSchemaLocation()).type(r.getType())
                .build();
    }

    private PaymentMethodRefDto toPaymentMethodRefDto(PaymentMethodRef r) {
        if (r == null) return null;
        return PaymentMethodRefDto.builder()
                .id(r.getId()).href(r.getHref()).name(r.getName())
                .referredType(r.getReferredType()).baseType(r.getBaseType())
                .schemaLocation(r.getSchemaLocation()).type(r.getType())
                .build();
    }

    private PaymentRefDto toPaymentRefDto(PaymentRef r) {
        if (r == null) return null;
        return PaymentRefDto.builder()
                .id(r.getId()).href(r.getHref()).name(r.getName())
                .referredType(r.getReferredType()).baseType(r.getBaseType())
                .schemaLocation(r.getSchemaLocation()).type(r.getType())
                .build();
    }

    private AppliedPaymentDto toAppliedPaymentDto(AppliedPayment e) {
        return AppliedPaymentDto.builder()
                .appliedAmount(toMoneyDto(e.getAppliedAmount()))
                .payment(toPaymentRefDto(e.getPayment()))
                .baseType(e.getBaseType())
                .schemaLocation(e.getSchemaLocation())
                .type(e.getType())
                .build();
    }

    private AttachmentRefOrValueDto toAttachmentRefOrValueDto(AttachmentRefOrValue e) {
        return AttachmentRefOrValueDto.builder()
                .id(e.getId()).href(e.getHref()).name(e.getName()).description(e.getDescription())
                .attachmentType(e.getAttachmentType()).mimeType(e.getMimeType()).size(e.getSize())
                .url(e.getUrl()).content(e.getContent())
                .validFor(toTimePeriodDto(e.getValidFor()))
                .referredType(e.getReferredType()).baseType(e.getBaseType())
                .schemaLocation(e.getSchemaLocation()).type(e.getType())
                .build();
    }

    private TaxItemDto toTaxItemDto(TaxItem e) {
        return TaxItemDto.builder()
                .id(e.getId()).taxCategory(e.getTaxCategory()).taxRate(e.getTaxRate())
                .taxAmount(toMoneyDto(e.getTaxAmount()))
                .baseType(e.getBaseType())
                .schemaLocation(e.getSchemaLocation())
                .type(e.getType())
                .build();
    }

    private PartyRefOrPartyRoleRefDto toPartyRefOrPartyRoleRefDto(PartyRefOrPartyRoleRef p) {
        if (p == null) return null;
        return PartyRefOrPartyRoleRefDto.builder()
                .id(p.getId()).href(p.getHref()).name(p.getName())
                .partyId(p.getPartyId()).partyName(p.getPartyName())
                .referredType(p.getReferredType()).baseType(p.getBaseType())
                .schemaLocation(p.getSchemaLocation()).type(p.getType())
                .build();
    }

    private RelatedPartyRefOrPartyRoleRefDto toRelatedPartyDto(RelatedPartyRefOrPartyRoleRef e) {
        return RelatedPartyRefOrPartyRoleRefDto.builder()
                .role(e.getRole())
                .partyOrPartyRole(toPartyRefOrPartyRoleRefDto(e.getPartyOrPartyRole()))
                .baseType(e.getBaseType())
                .schemaLocation(e.getSchemaLocation())
                .type(e.getType())
                .build();
    }
}
