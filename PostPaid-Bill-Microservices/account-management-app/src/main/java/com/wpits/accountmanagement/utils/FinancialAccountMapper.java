package com.wpits.accountmanagement.utils;

import org.springframework.stereotype.Component;

import com.wpits.accountmanagement.dtos.FinancialAccountDto;
import com.wpits.accountmanagement.entities.FinancialAccount;
import com.wpits.accountmanagement.enums.AccountStateType;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class FinancialAccountMapper {

    private final CommonMapper commonMapper;

    public FinancialAccount toEntity(FinancialAccountDto d) {
        if (d == null) return null;
        FinancialAccount entity = FinancialAccount.builder()
                .id(d.getId()).href(d.getHref()).name(d.getName()).description(d.getDescription())
                .accountType(d.getAccountType())
                .state(d.getState() == null ? null : AccountStateType.fromValue(d.getState()))
                .lastUpdate(d.getLastUpdate())
                .creditLimit(commonMapper.toMoney(d.getCreditLimit()))
                .baseType(d.getBaseType()).schemaLocation(d.getSchemaLocation())
                .type(d.getType() == null ? "FinancialAccount" : d.getType())
                .accountBalance(commonMapper.toAccountBalanceList(d.getAccountBalance()))
                .accountRelationship(commonMapper.toAccountRelationshipList(d.getAccountRelationship()))
                .contact(commonMapper.toContactList(d.getContact()))
                .relatedParty(commonMapper.toRelatedPartyList(d.getRelatedParty()))
                .taxExemption(commonMapper.toTaxExemptionList(d.getTaxExemption()))
                .build();
        return entity;
    }

    public FinancialAccountDto toDto(FinancialAccount e) {
        if (e == null) return null;
        return FinancialAccountDto.builder()
                .id(e.getId()).href(e.getHref()).name(e.getName()).description(e.getDescription())
                .accountType(e.getAccountType())
                .state(e.getState() == null ? null : e.getState().getValue())
                .lastUpdate(e.getLastUpdate())
                .creditLimit(commonMapper.toMoneyDto(e.getCreditLimit()))
                .baseType(e.getBaseType()).schemaLocation(e.getSchemaLocation()).type(e.getType())
                .accountBalance(commonMapper.toAccountBalanceDtoList(e.getAccountBalance()))
                .accountRelationship(commonMapper.toAccountRelationshipDtoList(e.getAccountRelationship()))
                .contact(commonMapper.toContactDtoList(e.getContact()))
                .relatedParty(commonMapper.toRelatedPartyDtoList(e.getRelatedParty()))
                .taxExemption(commonMapper.toTaxExemptionDtoList(e.getTaxExemption()))
                .build();
    }
}
