package com.wpits.customerbill.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "attachment_ref_or_value")
public class AttachmentRefOrValue {

    @Id
    private String id;

    private String href;
    private String name;
    private String description;
    private String attachmentType;
    private String mimeType;
    private Long size;
    private String url;

    @Lob
    private String content;

    @Embedded
    private TimePeriod validFor;

    private String referredType;
    private String baseType;
    private String schemaLocation;

    // "Attachment" (by value) or "AttachmentRef" (by reference) 
    @Builder.Default
    private String type = "AttachmentRef";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_bill_id")
    private CustomerBill customerBill;
}
