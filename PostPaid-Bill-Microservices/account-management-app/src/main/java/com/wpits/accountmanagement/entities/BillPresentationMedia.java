package com.wpits.accountmanagement.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** TMF666 BillPresentationMedia managed entity (spec page 79) - the media used to present a bill. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "bill_presentation_media")
public class BillPresentationMedia {

    @Id
    private String id;

    private String href;
    private String name;
    private String description;

    private String baseType;
    private String schemaLocation;
    private String type;
}
