package com.wpits.accountmanagement.utils;

import org.springframework.stereotype.Component;

import com.wpits.accountmanagement.dtos.BillPresentationMediaDto;
import com.wpits.accountmanagement.entities.BillPresentationMedia;

@Component
public class BillPresentationMediaMapper {

    public BillPresentationMedia toEntity(BillPresentationMediaDto d) {
        if (d == null) return null;
        return BillPresentationMedia.builder()
                .id(d.getId()).href(d.getHref()).name(d.getName())
                .description(d.getDescription() != null ? d.getDescription() : "This  bill format ...")
                .baseType(d.getBaseType()).schemaLocation(d.getSchemaLocation())
                .type(d.getType() == null ? "BillPresentationMedia" : d.getType())
                .build();
    }

    public BillPresentationMediaDto toDto(BillPresentationMedia e) {
        if (e == null) return null;
        return BillPresentationMediaDto.builder()
                .id(e.getId()).href(e.getHref()).name(e.getName()).description(e.getDescription())
                .baseType(e.getBaseType()).schemaLocation(e.getSchemaLocation()).type(e.getType())
                .build();
    }
}
