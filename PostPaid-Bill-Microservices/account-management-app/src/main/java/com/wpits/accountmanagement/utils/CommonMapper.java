package com.wpits.accountmanagement.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.wpits.accountmanagement.dtos.common.AccountBalanceDto;
import com.wpits.accountmanagement.dtos.common.AccountRefDto;
import com.wpits.accountmanagement.dtos.common.AccountRelationshipDto;
import com.wpits.accountmanagement.dtos.common.BillFormatRefDto;
import com.wpits.accountmanagement.dtos.common.BillPresentationMediaRefDto;
import com.wpits.accountmanagement.dtos.common.BillStructureDto;
import com.wpits.accountmanagement.dtos.common.BillingCycleSpecificationRefDto;
import com.wpits.accountmanagement.dtos.common.ContactDto;
import com.wpits.accountmanagement.dtos.common.FinancialAccountRefDto;
import com.wpits.accountmanagement.dtos.common.MoneyDto;
import com.wpits.accountmanagement.dtos.common.PartyRefOrPartyRoleRefDto;
import com.wpits.accountmanagement.dtos.common.PaymentMethodRefDto;
import com.wpits.accountmanagement.dtos.common.PaymentPlanDto;
import com.wpits.accountmanagement.dtos.common.RelatedPartyRefOrPartyRoleRefDto;
import com.wpits.accountmanagement.dtos.common.TaxDefinitionDto;
import com.wpits.accountmanagement.dtos.common.TaxExemptionCertificateDto;
import com.wpits.accountmanagement.dtos.common.TimePeriodDto;
import com.wpits.accountmanagement.entities.common.AccountBalance;
import com.wpits.accountmanagement.entities.common.AccountRef;
import com.wpits.accountmanagement.entities.common.AccountRelationship;
import com.wpits.accountmanagement.entities.common.BillFormatRef;
import com.wpits.accountmanagement.entities.common.BillPresentationMediaRef;
import com.wpits.accountmanagement.entities.common.BillStructure;
import com.wpits.accountmanagement.entities.common.BillingCycleSpecificationRef;
import com.wpits.accountmanagement.entities.common.Contact;
import com.wpits.accountmanagement.entities.common.FinancialAccountRef;
import com.wpits.accountmanagement.entities.common.Money;
import com.wpits.accountmanagement.entities.common.PartyRefOrPartyRoleRef;
import com.wpits.accountmanagement.entities.common.PaymentMethodRef;
import com.wpits.accountmanagement.entities.common.PaymentPlan;
import com.wpits.accountmanagement.entities.common.RelatedPartyRefOrPartyRoleRef;
import com.wpits.accountmanagement.entities.common.TaxDefinition;
import com.wpits.accountmanagement.entities.common.TaxExemptionCertificate;
import com.wpits.accountmanagement.entities.common.TimePeriod;

/**
 * Converts every TMF666 shared/common value type between its entity
 * ({@code @Embeddable}) and DTO representation. Reused by all seven
 * per-resource mappers so the conversion logic for e.g. Money, TimePeriod,
 * or RelatedPartyRefOrPartyRoleRef is written exactly once.
 */
@Component
public class CommonMapper {

    // ---------------------------------------------------------------- Money / TimePeriod

    public Money toMoney(MoneyDto d) {
        return d == null ? null : new Money(d.getUnit(), d.getValue());
    }

    public MoneyDto toMoneyDto(Money m) {
        return m == null ? null : MoneyDto.builder().unit(m.getUnit()).value(m.getValue()).build();
    }

    public TimePeriod toTimePeriod(TimePeriodDto d) {
        return d == null ? null : TimePeriod.builder()
                .startDateTime(d.getStartDateTime())
                .endDateTime(d.getEndDateTime())
                .build();
    }

    public TimePeriodDto toTimePeriodDto(TimePeriod t) {
        return t == null ? null : TimePeriodDto.builder()
                .startDateTime(t.getStartDateTime())
                .endDateTime(t.getEndDateTime())
                .build();
    }

    // ---------------------------------------------------------------- Plain refs

    public AccountRef toAccountRef(AccountRefDto d) {
        if (d == null) return null;
        return AccountRef.builder().id(d.getId()).href(d.getHref()).name(d.getName())
                .referredType(d.getReferredType()).build();
    }

    public AccountRefDto toAccountRefDto(AccountRef r) {
        if (r == null) return null;
        return AccountRefDto.builder().id(r.getId()).href(r.getHref()).name(r.getName())
				.referredType(r.getReferredType())/* .baseType("AccountRef") */.type("AccountRef").build();
    }

    public FinancialAccountRef toFinancialAccountRef(FinancialAccountRefDto d) {
        if (d == null) return null;
        return FinancialAccountRef.builder().id(d.getId()).href(d.getHref()).name(d.getName())
                .referredType(d.getReferredType()).build();
    }

    public FinancialAccountRefDto toFinancialAccountRefDto(FinancialAccountRef r) {
        if (r == null) return null;
        return FinancialAccountRefDto.builder().id(r.getId()).href(r.getHref()).name(r.getName())
				.referredType(r.getReferredType())
				/* .baseType("FinancialAccountRef") */.type("FinancialAccountRef").build();
    }

    public PaymentMethodRef toPaymentMethodRef(PaymentMethodRefDto d) {
        if (d == null) return null;
        return PaymentMethodRef.builder().id(d.getId()).href(d.getHref()).name(d.getName())
                .referredType(d.getReferredType()).build();
    }

    public PaymentMethodRefDto toPaymentMethodRefDto(PaymentMethodRef r) {
        if (r == null) return null;
        return PaymentMethodRefDto.builder().id(r.getId()).href(r.getHref()).name(r.getName())
				.referredType(r.getReferredType())/* .baseType("PaymentMethodRef") */.type("PaymentMethodRef").build();
    }

    public BillFormatRef toBillFormatRef(BillFormatRefDto d) {
        if (d == null) return null;
        return BillFormatRef.builder().id(d.getId()).href(d.getHref()).name(d.getName())
                .referredType(d.getReferredType()).build();
    }

    public BillFormatRefDto toBillFormatRefDto(BillFormatRef r) {
        if (r == null) return null;
        return BillFormatRefDto.builder().id(r.getId()).href(r.getHref()).name(r.getName())
				.referredType(r.getReferredType())/* .baseType("BillFormatRef") */.type("BillFormatRef").build();
    }

    public BillPresentationMediaRef toBillPresentationMediaRef(BillPresentationMediaRefDto d) {
        if (d == null) return null;
        return BillPresentationMediaRef.builder().id(d.getId()).href(d.getHref()).name(d.getName())
                .referredType(d.getReferredType()).build();
    }

    public BillPresentationMediaRefDto toBillPresentationMediaRefDto(BillPresentationMediaRef r) {
        if (r == null) return null;
        return BillPresentationMediaRefDto.builder().id(r.getId()).href(r.getHref()).name(r.getName())
				.referredType(r.getReferredType())
				/* .baseType("BillPresentationMediaRef") */.type("BillPresentationMediaRef").build();
    }

    public BillingCycleSpecificationRef toBillingCycleSpecificationRef(BillingCycleSpecificationRefDto d) {
        if (d == null) return null;
        return BillingCycleSpecificationRef.builder().id(d.getId()).href(d.getHref()).name(d.getName())
                .referredType(d.getReferredType()).build();
    }

    public BillingCycleSpecificationRefDto toBillingCycleSpecificationRefDto(BillingCycleSpecificationRef r) {
        if (r == null) return null;
        return BillingCycleSpecificationRefDto.builder().id(r.getId()).href(r.getHref()).name(r.getName())
				.referredType(r.getReferredType())
				/* .baseType("BillingCycleSpecificationRef") */.type("BillingCycleSpecificationRef").build();
    }

    // ---------------------------------------------------------------- Party / RelatedParty

    public PartyRefOrPartyRoleRef toPartyRefOrPartyRoleRef(PartyRefOrPartyRoleRefDto d) {
        if (d == null) return null;
        return PartyRefOrPartyRoleRef.builder()
                .id(UUID.randomUUID().toString()).href(d.getHref()).name(d.getName())
                .partyId(d.getPartyId() == null || d.getPartyId().isBlank()
                        ? d.getId() : d.getPartyId())
                .partyName(d.getPartyName())
                .referredType(d.getReferredType())
                .type(d.getType() == null ? "PartyRef" : d.getType())
                .build();
    }

    public PartyRefOrPartyRoleRefDto toPartyRefOrPartyRoleRefDto(PartyRefOrPartyRoleRef p) {
        if (p == null) return null;
        return PartyRefOrPartyRoleRefDto.builder()
                .id(p.getPartyId() == null || p.getPartyId().isBlank()
                        ? p.getId() : p.getPartyId())
                .href(p.getHref()).name(p.getName())
                .partyId(p.getPartyId()).partyName(p.getPartyName())
                .referredType(p.getReferredType()).type(p.getType())
                .build();
    }

    public RelatedPartyRefOrPartyRoleRef toRelatedParty(RelatedPartyRefOrPartyRoleRefDto d) {
        if (d == null) return null;
        return RelatedPartyRefOrPartyRoleRef.builder()
                .role(d.getRole())
                .partyOrPartyRole(toPartyRefOrPartyRoleRef(d.getPartyOrPartyRole()))
                .build();
    }

    public RelatedPartyRefOrPartyRoleRefDto toRelatedPartyDto(RelatedPartyRefOrPartyRoleRef e) {
        if (e == null) return null;
        return RelatedPartyRefOrPartyRoleRefDto.builder()
                .role(e.getRole())
                .partyOrPartyRole(toPartyRefOrPartyRoleRefDto(e.getPartyOrPartyRole()))
                .baseType("RelatedParty")
                .type("RelatedPartyRefOrPartyRoleRef")
                .build();
    }

    public List<RelatedPartyRefOrPartyRoleRef> toRelatedPartyList(List<RelatedPartyRefOrPartyRoleRefDto> list) {
        if (list == null) return new ArrayList<>();
        return list.stream().map(this::toRelatedParty).collect(Collectors.toList());
    }

    public List<RelatedPartyRefOrPartyRoleRefDto> toRelatedPartyDtoList(List<RelatedPartyRefOrPartyRoleRef> list) {
        if (list == null) return new ArrayList<>();
        return list.stream().map(this::toRelatedPartyDto).collect(Collectors.toList());
    }

    // ---------------------------------------------------------------- AccountBalance

    public AccountBalance toAccountBalance(AccountBalanceDto d) {
        return AccountBalance.builder()
                .id(d.getId()).balanceType(d.getBalanceType())
                .amount(toMoney(d.getAmount())).validFor(toTimePeriod(d.getValidFor()))
                .build();
    }

    public AccountBalanceDto toAccountBalanceDto(AccountBalance e) {
        return AccountBalanceDto.builder()
                .id(e.getId()).balanceType(e.getBalanceType())
                .amount(toMoneyDto(e.getAmount())).validFor(toTimePeriodDto(e.getValidFor()))
                .baseType("AccountBalance").type("AccountBalance")
                .build();
    }

    public List<AccountBalance> toAccountBalanceList(List<AccountBalanceDto> list) {
        if (list == null) return new ArrayList<>();
        return list.stream().map(this::toAccountBalance).collect(Collectors.toList());
    }

    public List<AccountBalanceDto> toAccountBalanceDtoList(List<AccountBalance> list) {
        if (list == null) return new ArrayList<>();
        return list.stream().map(this::toAccountBalanceDto).collect(Collectors.toList());
    }

    // ---------------------------------------------------------------- AccountRelationship

    public AccountRelationship toAccountRelationship(AccountRelationshipDto d) {
        return AccountRelationship.builder()
                .id(d.getId()).relationshipType(d.getRelationshipType())
                .account(toAccountRef(d.getAccount())).validFor(toTimePeriod(d.getValidFor()))
                .build();
    }

    public AccountRelationshipDto toAccountRelationshipDto(AccountRelationship e) {
        return AccountRelationshipDto.builder()
                .id(e.getId()).relationshipType(e.getRelationshipType())
                .account(toAccountRefDto(e.getAccount())).validFor(toTimePeriodDto(e.getValidFor()))
                .baseType("AccountRelationship").type("AccountRelationship")
                .build();
    }

    public List<AccountRelationship> toAccountRelationshipList(List<AccountRelationshipDto> list) {
        if (list == null) return new ArrayList<>();
        return list.stream().map(this::toAccountRelationship).collect(Collectors.toList());
    }

    public List<AccountRelationshipDto> toAccountRelationshipDtoList(List<AccountRelationship> list) {
        if (list == null) return new ArrayList<>();
        return list.stream().map(this::toAccountRelationshipDto).collect(Collectors.toList());
    }

    // ---------------------------------------------------------------- Contact

    public Contact toContact(ContactDto d) {
        return Contact.builder()
                .id(d.getId()).contactName(d.getContactName()).contactType(d.getContactType())
                .partyRoleType(d.getPartyRoleType()).preferred(d.getPreferred())
                .mediumType(d.getMediumType()).emailAddress(d.getEmailAddress())
                .phoneNumber(d.getPhoneNumber()).faxNumber(d.getFaxNumber())
                .socialNetworkId(d.getSocialNetworkId())
                .street1(d.getStreet1()).street2(d.getStreet2()).city(d.getCity())
                .stateOrProvince(d.getStateOrProvince()).postCode(d.getPostCode()).country(d.getCountry())
                .validFor(toTimePeriod(d.getValidFor()))
                .build();
    }

    public ContactDto toContactDto(Contact e) {
        return ContactDto.builder()
                .id(e.getId()).contactName(e.getContactName()).contactType(e.getContactType())
                .partyRoleType(e.getPartyRoleType()).preferred(e.getPreferred())
                .mediumType(e.getMediumType()).emailAddress(e.getEmailAddress())
                .phoneNumber(e.getPhoneNumber()).faxNumber(e.getFaxNumber())
                .socialNetworkId(e.getSocialNetworkId())
                .street1(e.getStreet1()).street2(e.getStreet2()).city(e.getCity())
                .stateOrProvince(e.getStateOrProvince()).postCode(e.getPostCode()).country(e.getCountry())
                .validFor(toTimePeriodDto(e.getValidFor()))
                .baseType("Contact").type("Contact")
                .build();
    }

    public List<Contact> toContactList(List<ContactDto> list) {
        if (list == null) return new ArrayList<>();
        return list.stream().map(this::toContact).collect(Collectors.toList());
    }

    public List<ContactDto> toContactDtoList(List<Contact> list) {
        if (list == null) return new ArrayList<>();
        return list.stream().map(this::toContactDto).collect(Collectors.toList());
    }

    // ---------------------------------------------------------------- PaymentPlan

    public PaymentPlan toPaymentPlan(PaymentPlanDto d) {
        return PaymentPlan.builder()
                .id(d.getId()).numberOfPayments(d.getNumberOfPayments())
                .paymentFrequency(d.getPaymentFrequency()).planType(d.getPlanType())
                .priority(d.getPriority()).status(d.getStatus())
                .totalAmount(toMoney(d.getTotalAmount())).validFor(toTimePeriod(d.getValidFor()))
                .paymentMethod(toPaymentMethodRef(d.getPaymentMethod()))
                .build();
    }

    public PaymentPlanDto toPaymentPlanDto(PaymentPlan e) {
        return PaymentPlanDto.builder()
                .id(e.getId()).numberOfPayments(e.getNumberOfPayments())
                .paymentFrequency(e.getPaymentFrequency()).planType(e.getPlanType())
                .priority(e.getPriority()).status(e.getStatus())
                .totalAmount(toMoneyDto(e.getTotalAmount())).validFor(toTimePeriodDto(e.getValidFor()))
                .paymentMethod(toPaymentMethodRefDto(e.getPaymentMethod()))
                .baseType("PaymentPlan").type("PaymentPlan")
                .build();
    }

    public List<PaymentPlan> toPaymentPlanList(List<PaymentPlanDto> list) {
        if (list == null) return new ArrayList<>();
        return list.stream().map(this::toPaymentPlan).collect(Collectors.toList());
    }

    public List<PaymentPlanDto> toPaymentPlanDtoList(List<PaymentPlan> list) {
        if (list == null) return new ArrayList<>();
        return list.stream().map(this::toPaymentPlanDto).collect(Collectors.toList());
    }

    // ---------------------------------------------------------------- TaxExemptionCertificate

    public TaxDefinition toTaxDefinition(TaxDefinitionDto d) {
        if (d == null) return null;
        return TaxDefinition.builder()
                .id(d.getId()).jurisdictionLevel(d.getJurisdictionLevel()).jurisdictionName(d.getJurisdictionName())
                .name(d.getName()).taxType(d.getTaxType()).validFor(toTimePeriod(d.getValidFor()))
                .build();
    }

    public TaxDefinitionDto toTaxDefinitionDto(TaxDefinition e) {
        if (e == null) return null;
        return TaxDefinitionDto.builder()
                .id(e.getId()).jurisdictionLevel(e.getJurisdictionLevel()).jurisdictionName(e.getJurisdictionName())
                .name(e.getName()).taxType(e.getTaxType()).validFor(toTimePeriodDto(e.getValidFor()))
                .baseType("TaxDefinition").type("TaxDefinition")
                .build();
    }

    public TaxExemptionCertificate toTaxExemption(TaxExemptionCertificateDto d) {
        return TaxExemptionCertificate.builder()
                .id(d.getId()).certificateNumber(d.getCertificateNumber())
                .issuingJurisdiction(d.getIssuingJurisdiction()).reason(d.getReason())
                .attachmentName(d.getAttachmentName()).attachmentUrl(d.getAttachmentUrl())
                .taxDefinition(toTaxDefinition(d.getTaxDefinition()))
                .validFor(toTimePeriod(d.getValidFor()))
                .build();
    }

    public TaxExemptionCertificateDto toTaxExemptionDto(TaxExemptionCertificate e) {
        return TaxExemptionCertificateDto.builder()
                .id(e.getId()).certificateNumber(e.getCertificateNumber())
                .issuingJurisdiction(e.getIssuingJurisdiction()).reason(e.getReason())
                .attachmentName(e.getAttachmentName()).attachmentUrl(e.getAttachmentUrl())
                .taxDefinition(toTaxDefinitionDto(e.getTaxDefinition()))
                .validFor(toTimePeriodDto(e.getValidFor()))
                .baseType("TaxExemptionCertificate").type("TaxExemptionCertificate")
                .build();
    }

    public List<TaxExemptionCertificate> toTaxExemptionList(List<TaxExemptionCertificateDto> list) {
        if (list == null) return new ArrayList<>();
        return list.stream().map(this::toTaxExemption).collect(Collectors.toList());
    }

    public List<TaxExemptionCertificateDto> toTaxExemptionDtoList(List<TaxExemptionCertificate> list) {
        if (list == null) return new ArrayList<>();
        return list.stream().map(this::toTaxExemptionDto).collect(Collectors.toList());
    }

    // ---------------------------------------------------------------- BillStructure

    public BillStructure toBillStructure(BillStructureDto d) {
        if (d == null) return null;
        return BillStructure.builder()
                .cycleSpecification(toBillingCycleSpecificationRef(d.getCycleSpecification()))
                .format(toBillFormatRef(d.getFormat()))
                .presentationMedia(toBillPresentationMediaRef(d.getPresentationMedia()))
                .build();
    }

    public BillStructureDto toBillStructureDto(BillStructure e) {
        if (e == null) return null;
        return BillStructureDto.builder()
                .cycleSpecification(toBillingCycleSpecificationRefDto(e.getCycleSpecification()))
                .format(toBillFormatRefDto(e.getFormat()))
                .presentationMedia(toBillPresentationMediaRefDto(e.getPresentationMedia()))
                .baseType("BillStructure").type("BillStructure")
                .build();
    }
}
