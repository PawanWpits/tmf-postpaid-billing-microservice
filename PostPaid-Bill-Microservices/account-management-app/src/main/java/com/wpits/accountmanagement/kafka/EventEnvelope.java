package com.wpits.accountmanagement.kafka;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Generic TMF notification envelope, matching the pattern shared by every
 * TM Forum Open API notification (see e.g. TMF666/TMF678 "Notification
 * Resource Models" / "Figure - Notification Pattern"): a generic Event
 * wrapper carrying an {@code event} payload keyed by the resource's
 * camelCase name (e.g. {@code "partyAccount": {...}}).
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EventEnvelope {

    private String correlationId;
    private String description;
    private String domain;
    private String eventId;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private LocalDateTime eventTime;

    /** e.g. "PartyAccountCreateEvent" */
    private String eventType;

    private String priority;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private LocalDateTime timeOcurred;

    private String title;

    /** single-entry map: { "<resourceName>": <payload dto> } */
    private Map<String, Object> event;

    private ReportingResource reportingSystem;
    private ReportingResource source;

    @JsonProperty("@baseType")
    @Builder.Default
    private String baseType = "Event";

    @JsonProperty("@type")
    private String type;
}
