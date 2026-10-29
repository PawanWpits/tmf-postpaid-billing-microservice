package com.wpits.customerbill.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Data;

@Entity
@Table(name = "BillCycleSpecificationRef")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillCycleSpecificationRef {

	@Id
    private String id;

    private String href;

    private String name;

    private String description;
    
    private String baseType;

    private String referredType;
    
    private String schemaLocation;
    
    @Builder.Default
    private String type = "BillCycleSpecificationRef";
}
