package com.wpits.accountmanagement.utils;

import org.springframework.stereotype.Component;

import com.wpits.accountmanagement.dtos.BillingCycleSpecificationDto;
import com.wpits.accountmanagement.entities.BillingCycleSpecification;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class BillingCycleSpecificationMapper {

    private final CommonMapper commonMapper;

    public BillingCycleSpecification toEntity(BillingCycleSpecificationDto d) {
    	
        if (d == null) return null;
        
        return BillingCycleSpecification.builder()
                .id(d.getId()).href(d.getHref()).name(d.getName())
                .description(d.getDescription() != null ? d.getDescription() : "This  billing cycle specification ...")
                .billingDateShift(d.getBillingDateShift()).billingPeriod(d.getBillingPeriod())
                .chargeDateOffset(d.getChargeDateOffset()).creditDateOffset(d.getCreditDateOffset())
                .mailingDateOffset(d.getMailingDateOffset()).paymentDueDateOffset(d.getPaymentDueDateOffset())
                .frequency(d.getFrequency())
                .validFor(commonMapper.toTimePeriod(d.getValidFor()))
                .baseType(d.getBaseType()).schemaLocation(d.getSchemaLocation())
                .type(d.getType() == null ? "BillingCycleSpecification" : d.getType())
                .build();
    }

    public BillingCycleSpecificationDto toDto(BillingCycleSpecification e) {
    	
        if (e == null) return null;
        
        return BillingCycleSpecificationDto.builder()
                .id(e.getId()).href(e.getHref()).name(e.getName()).description(e.getDescription())
                .billingDateShift(e.getBillingDateShift()).billingPeriod(e.getBillingPeriod())
                .chargeDateOffset(e.getChargeDateOffset()).creditDateOffset(e.getCreditDateOffset())
                .mailingDateOffset(e.getMailingDateOffset()).paymentDueDateOffset(e.getPaymentDueDateOffset())
                .frequency(e.getFrequency())
                .validFor(commonMapper.toTimePeriodDto(e.getValidFor()))
                .baseType(e.getBaseType()).schemaLocation(e.getSchemaLocation()).type(e.getType())
                .build();
    }
}
