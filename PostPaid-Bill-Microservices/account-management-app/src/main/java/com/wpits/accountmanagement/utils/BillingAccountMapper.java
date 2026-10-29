package com.wpits.accountmanagement.utils;

import org.springframework.stereotype.Component;

import com.wpits.accountmanagement.dtos.BillingAccountDto;
import com.wpits.accountmanagement.entities.BillingAccount;
import com.wpits.accountmanagement.enums.AccountStateType;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class BillingAccountMapper {

    private final CommonMapper commonMapper;

    public BillingAccount toEntity(BillingAccountDto d) {
        if (d == null) return null;
        return BillingAccount.builder()
                .id(d.getId()).href(d.getHref()).name(d.getName()).description(d.getDescription())
                .accountType(d.getAccountType())
                .state(d.getState() == null ? null : AccountStateType.fromValue(d.getState()))
                .lastUpdate(d.getLastUpdate())
                .creditLimit(commonMapper.toMoney(d.getCreditLimit()))
                .paymentStatus(d.getPaymentStatus())
                .ratingType(d.getRatingType())
                .billStructure(commonMapper.toBillStructure(d.getBillStructure()))
                .defaultPaymentMethod(commonMapper.toPaymentMethodRef(d.getDefaultPaymentMethod()))
                .financialAccount(commonMapper.toFinancialAccountRef(d.getFinancialAccount()))
                .baseType(d.getBaseType()).schemaLocation(d.getSchemaLocation())
                .type(d.getType() == null ? "BillingAccount" : d.getType())
                .accountBalance(commonMapper.toAccountBalanceList(d.getAccountBalance()))
                .accountRelationship(commonMapper.toAccountRelationshipList(d.getAccountRelationship()))
                .contact(commonMapper.toContactList(d.getContact()))
                .paymentPlan(commonMapper.toPaymentPlanList(d.getPaymentPlan()))
                .relatedParty(commonMapper.toRelatedPartyList(d.getRelatedParty()))
                .taxExemption(commonMapper.toTaxExemptionList(d.getTaxExemption()))
                .build();
    }

    public BillingAccountDto toDto(BillingAccount e) {
        if (e == null) return null;
        return BillingAccountDto.builder()
                .id(e.getId()).href(e.getHref()).name(e.getName()).description(e.getDescription())
                .accountType(e.getAccountType())
                .state(e.getState() == null ? null : e.getState().getValue())
                .lastUpdate(e.getLastUpdate())
                .creditLimit(commonMapper.toMoneyDto(e.getCreditLimit()))
                .paymentStatus(e.getPaymentStatus())
                .ratingType(e.getRatingType())
                .billStructure(commonMapper.toBillStructureDto(e.getBillStructure()))
                .defaultPaymentMethod(commonMapper.toPaymentMethodRefDto(e.getDefaultPaymentMethod()))
                .financialAccount(commonMapper.toFinancialAccountRefDto(e.getFinancialAccount()))
                .baseType(e.getBaseType()).schemaLocation(e.getSchemaLocation()).type(e.getType())
                .accountBalance(commonMapper.toAccountBalanceDtoList(e.getAccountBalance()))
                .accountRelationship(commonMapper.toAccountRelationshipDtoList(e.getAccountRelationship()))
                .contact(commonMapper.toContactDtoList(e.getContact()))
                .paymentPlan(commonMapper.toPaymentPlanDtoList(e.getPaymentPlan()))
                .relatedParty(commonMapper.toRelatedPartyDtoList(e.getRelatedParty()))
                .taxExemption(commonMapper.toTaxExemptionDtoList(e.getTaxExemption()))
                .build();
    }
}
