package com.wpits.accountmanagement.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** TMF666 BillFormat managed entity (spec page 6) - the way a bill is presented. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "bill_format")
public class BillFormat {

    @Id
    private String id;

    private String href;
    private String name;
    private String description;

    private String baseType;
    private String schemaLocation;
    private String type;
}
