package com.wpits.accountmanagement.entities.common;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** TMF666 BillFormatRef - lightweight reference to a BillFormat (the way in which a bill is presented). */
@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillFormatRef {
    private String id;
    private String href;
    private String name;
    private String referredType;
}
