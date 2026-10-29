package com.wpits.accountmanagement.kafka;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Service
@RequiredArgsConstructor
@Slf4j
public class AccountEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String REPORTING_SYSTEM_ID = "account-management-app";

    /**
     * @param topic        destination Kafka topic
     * @param eventType    e.g. "PartyAccountCreateEvent"
     * @param resourceName camelCase JSON key for the payload, e.g. "partyAccount"
     * @param resourceId   id of the resource the event concerns (used as the Kafka message key,
     *                     so all events for the same resource land on the same partition, in order)
     * @param payload      the resource DTO (or a partial view of it)
     */
    public void publish(String topic, String eventType, String resourceName, String resourceId, Object payload) {
        try {
            ReportingResource system = ReportingResource.builder()
                    .id(REPORTING_SYSTEM_ID)
                    .name(REPORTING_SYSTEM_ID)
                    .build();

            EventEnvelope envelope = EventEnvelope.builder()
                    .correlationId(UUID.randomUUID().toString())
                    .description(eventType + " illustration")
                    .domain("Commercial")
                    .eventId(UUID.randomUUID().toString())
                    .eventTime(LocalDateTime.now())
                    .eventType(eventType)
                    .priority("1")
                    .timeOcurred(LocalDateTime.now())
                    .title(eventType)
                    .event(Map.of(resourceName, payload))
                    .reportingSystem(system)
                    .source(system)
                    .type(eventType)
                    .build();

            kafkaTemplate.send(topic, resourceId, envelope)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("Failed to publish {} for {}={} to topic {}: {}",
                                    eventType, resourceName, resourceId, topic, ex.getMessage());
                        } else {
                            log.info("Published {} for {}={} to topic {} (partition={}, offset={})",
                                    eventType, resourceName, resourceId, topic,
                                    result.getRecordMetadata().partition(),
                                    result.getRecordMetadata().offset());
                        }
                    });
        } catch (Exception ex) {
            // Defensive: never let event publishing break the calling request.
            log.error("Unexpected error while publishing {} for {}={}", eventType, resourceName, resourceId, ex);
        }
    }
}
