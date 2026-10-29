package com.wpits.customerbill.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Maps to TMF678 AttachmentRefOrValue (CustomerBill.billDocument).
 * {@code type} is "Attachment" when the document content is embedded (by value),
 * or "AttachmentRef" when only a url reference is provided.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AttachmentRefOrValueDto {

    private String id;
    private String href;
    private String name;
    private String description;
    private String attachmentType;
    private String mimeType;
    private Long size;
    private String url;
    private String content;

    private TimePeriodDto validFor;

    @JsonProperty("@referredType")
    private String referredType;

    @JsonProperty("@baseType")
    private String baseType;

    @JsonProperty("@schemaLocation")
    private String schemaLocation;

    @JsonProperty("@type")
    private String type;
}
