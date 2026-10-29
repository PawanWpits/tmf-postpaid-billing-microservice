package com.wpits.accountmanagement.utils;

import org.springframework.stereotype.Component;

import com.wpits.accountmanagement.dtos.BillFormatDto;
import com.wpits.accountmanagement.entities.BillFormat;

@Component
public class BillFormatMapper {

    public BillFormat toEntity(BillFormatDto d) {
        if (d == null) return null;
        return BillFormat.builder()
				.id(d.getId()).href(d.getHref()).name(d.getName())
                .description(d.getDescription() != null ? d.getDescription() : "This  bill format ...")
                .baseType(d.getBaseType()).schemaLocation(d.getSchemaLocation())
                .type(d.getType() == null ? "BillFormat" : d.getType())
                .build();
    }

    public BillFormatDto toDto(BillFormat e) {
        if (e == null) return null;
        return BillFormatDto.builder()
                .id(e.getId()).href(e.getHref()).name(e.getName()).description(e.getDescription())
                .baseType(e.getBaseType()).schemaLocation(e.getSchemaLocation()).type(e.getType())
                .build();
    }
}
