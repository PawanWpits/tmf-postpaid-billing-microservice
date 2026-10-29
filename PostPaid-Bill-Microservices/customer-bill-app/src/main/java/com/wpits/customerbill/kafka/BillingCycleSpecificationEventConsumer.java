package com.wpits.customerbill.kafka;

import java.util.Map;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.wpits.customerbill.entities.BillCycleSpecificationRef;
import com.wpits.customerbill.repositories.BillCycleSpecificationRefRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class BillingCycleSpecificationEventConsumer {

    private static final String RESOURCE_NAME = "billingCycleSpecification";
    private static final String CREATE_EVENT = "BillingCycleSpecificationCreateEvent";
    private static final String UPDATE_EVENT = "BillingCycleSpecificationAttributeValueChangeEvent";
    private static final String DELETE_EVENT = "BillingCycleSpecificationDeleteEvent";

    private final BillCycleSpecificationRefRepository repository;

    @Transactional
    @KafkaListener(
            topics = "${app.kafka.topics.billing-cycle-specification}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory")
    public void consume(Map<String, Object> envelope) {
        String eventType = requiredString(envelope, "eventType");
        Object eventObject = envelope.get("event");
        if (!(eventObject instanceof Map<?, ?>)) {
            throw new IllegalArgumentException("Kafka event must contain an event object");
        }
        Map<?, ?> event = (Map<?, ?>) eventObject;

        Object resourceObject = event.get(RESOURCE_NAME);
        if (!(resourceObject instanceof Map<?, ?>)) {
            throw new IllegalArgumentException("Kafka event must contain a billingCycleSpecification payload");
        }
        Map<?, ?> resource = (Map<?, ?>) resourceObject;

        String id = requiredString(resource, "id");
        if (DELETE_EVENT.equals(eventType)) {
            repository.deleteById(id);
            log.info("Deleted BillingCycleSpecificationRef id={} from Kafka event", id);
            return;
        }
        if (!CREATE_EVENT.equals(eventType) && !"BCS_CREATED".equals(eventType) && !UPDATE_EVENT.equals(eventType)) {
            log.debug("Ignoring unsupported BillingCycleSpecification event type={}", eventType);
            return;
        }

        repository.save(BillCycleSpecificationRef.builder()
                .id(id)
                .href(stringValue(resource, "href"))
                .name(stringValue(resource, "name"))
                .description(stringValue(resource, "description"))
                .baseType(stringValue(resource, "@baseType"))
                .referredType(stringValue(resource, "@type", "BillingCycleSpecification"))
                .schemaLocation(stringValue(resource, "@schemaLocation"))
                .type("BillCycleSpecificationRef")
                .build());
        log.info("Stored BillingCycleSpecificationRef id={} from Kafka event type={}", id, eventType);
    }

    private static String requiredString(Map<?, ?> values, String key) {
        String value = stringValue(values, key);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Kafka event is missing required field: " + key);
        }
        return value;
    }

    private static String stringValue(Map<?, ?> values, String key) {
        Object value = values.get(key);
        return value == null ? null : value.toString();
    }

    private static String stringValue(Map<?, ?> values, String key, String defaultValue) {
        String value = stringValue(values, key);
        return value == null || value.isBlank() ? defaultValue : value;
    }
}
