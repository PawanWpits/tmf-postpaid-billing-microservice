package com.wpits.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "entity_ref")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EntityRef {
    private String href;
    @Id
    private String id;
    private String name;
    private String baseType;
    private String referredType;
    private String schemaLocation;
    private String type;
}
