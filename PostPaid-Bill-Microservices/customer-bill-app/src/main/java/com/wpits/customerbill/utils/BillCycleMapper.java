package com.wpits.customerbill.utils;

import org.springframework.stereotype.Component;

import com.wpits.customerbill.dtos.BillCycleDto;
import com.wpits.customerbill.dtos.BillCycleSpecificationRefDto;
import com.wpits.customerbill.dtos.TimePeriodDto;
import com.wpits.customerbill.entities.BillCycle;
import com.wpits.customerbill.entities.BillCycleSpecificationRef;
import com.wpits.customerbill.entities.TimePeriod;

@Component
public class BillCycleMapper {

	public BillCycle toEntity(BillCycleDto dto) {

	    return BillCycle.builder()
	            .id(dto.getId())          
	            .href(dto.getHref())
	            .name(dto.getName())
	            .description(dto.getDescription())
	            .billingDate(dto.getBillingDate())
	            .billingPeriod(dto.getBillingPeriod())
	            .chargeDate(dto.getChargeDate())
	            .creditDate(dto.getCreditDate())
	            .mailingDate(dto.getMailingDate())
	            .paymentDueDate(dto.getPaymentDueDate())

	            .billCycleSpecification(
	                    BillCycleSpecificationRef.builder()
	                            .id(dto.getBillCycleSpecification().getId())
	                            .href(dto.getBillCycleSpecification().getHref())
	                            .name(dto.getBillCycleSpecification().getName())
	                            .description(dto.getBillCycleSpecification().getDescription())
	                            .referredType(dto.getBillCycleSpecification().getReferredType())
	                            .build())

	            .validFor(
	                    TimePeriod.builder()
	                            .startDateTime(dto.getValidFor().getStartDateTime())
	                            .endDateTime(dto.getValidFor().getEndDateTime())
	                            .build())

	            .baseType(dto.getBaseType())
	            .schemaLocation(dto.getSchemaLocation())
	            .type(dto.getType())
	            .build();
	}
	
	public BillCycleDto toDto(BillCycle entity) {

	    if (entity == null) {
	        return null;
	    }

	    return BillCycleDto.builder()
	            .id(entity.getId()) 
	            .href(entity.getHref())
	            .name(entity.getName())
	            .description(entity.getDescription())
	            .billingDate(entity.getBillingDate())
	            .billingPeriod(entity.getBillingPeriod())
	            .chargeDate(entity.getChargeDate())
	            .creditDate(entity.getCreditDate())
	            .mailingDate(entity.getMailingDate())
	            .paymentDueDate(entity.getPaymentDueDate())

	            .billCycleSpecification(
	                    BillCycleSpecificationRefDto.builder()
	                            .id(entity.getBillCycleSpecification().getId())
	                            .href(entity.getBillCycleSpecification().getHref())
	                            .name(entity.getBillCycleSpecification().getName())
	                            .description(entity.getBillCycleSpecification().getDescription())
	                            .referredType(entity.getBillCycleSpecification().getReferredType())
	                            .build())

	            .validFor(
	                    TimePeriodDto.builder()
	                            .startDateTime(entity.getValidFor().getStartDateTime())
	                            .endDateTime(entity.getValidFor().getEndDateTime())
	                            .build())

	            .baseType(entity.getBaseType())
	            .schemaLocation(entity.getSchemaLocation())
	            .type(entity.getType())
	            .build();
	}
}
