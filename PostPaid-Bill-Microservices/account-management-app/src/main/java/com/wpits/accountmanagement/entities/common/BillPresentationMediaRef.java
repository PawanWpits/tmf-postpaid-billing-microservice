package com.wpits.accountmanagement.entities.common;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** TMF666 BillPresentationMediaRef - lightweight reference to a BillPresentationMedia (the media used to present a bill). */
@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillPresentationMediaRef {
    private String id;
    private String href;
    private String name;
    private String referredType;
}
