package com.wpits.customerbill.kafka;

import java.util.Map;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.wpits.customerbill.entities.BillingAccountRef;
import com.wpits.customerbill.entities.PartyRefOrPartyRoleRef;
import com.wpits.customerbill.repositories.BillingAccountRefRepository;
import com.wpits.customerbill.repositories.PartyRefOrPartyRoleRefRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class BillingAccountEventConsumer {

    private static final String RESOURCE_NAME = "billingAccount";
    private static final String CREATE_EVENT = "BillingAccountCreateEvent";
    private static final String UPDATE_EVENT = "BillingAccountAttributeValueChangeEvent";
    private static final String STATE_CHANGE_EVENT = "BillingAccountStateChangeEvent";
    private static final String DELETE_EVENT = "BillingAccountDeleteEvent";

    private final BillingAccountRefRepository repository;
    private final PartyRefOrPartyRoleRefRepository partyRepository;

    @Transactional
    @KafkaListener(
            topics = "${app.kafka.topics.billing-account}",
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
            throw new IllegalArgumentException("Kafka event must contain a billingAccount payload");
        }
        Map<?, ?> resource = (Map<?, ?>) resourceObject;
        String id = requiredString(resource, "id");

        if (DELETE_EVENT.equals(eventType)) {
            repository.deleteById(id);
            log.info("Deleted BillingAccountRef id={} from Kafka event", id);
            return;
        }
        if (!CREATE_EVENT.equals(eventType)
                && !UPDATE_EVENT.equals(eventType)
                && !STATE_CHANGE_EVENT.equals(eventType)) {
            log.debug("Ignoring unsupported BillingAccount event type={}", eventType);
            return;
        }

        repository.save(BillingAccountRef.builder()
                .id(id)
                .href(stringValue(resource, "href"))
                .name(stringValue(resource, "name"))
                .ratingType(stringValue(resource, "ratingType"))
                .baseType(stringValue(resource, "@baseType"))
                .referredType(stringValue(resource, "@type", "BillingAccount"))
                .schemaLocation(stringValue(resource, "@schemaLocation"))
                .type("BillingAccountRef")
                .build());
        synchronizeRelatedParties(id, resource.get("relatedParty"));
        log.info("Stored BillingAccountRef id={} from Kafka event type={}", id, eventType);
    }

    private void synchronizeRelatedParties(String billingAccountId, Object relatedPartyObject) {
        if (relatedPartyObject == null) {
            return;
        }
        if (!(relatedPartyObject instanceof List<?> relatedParties)) {
            throw new IllegalArgumentException("BillingAccount relatedParty must be an array");
        }

        for (Object relatedPartyObjectItem : relatedParties) {
            if (!(relatedPartyObjectItem instanceof Map<?, ?> relatedParty)) {
                throw new IllegalArgumentException("BillingAccount relatedParty entries must be objects");
            }
            Object partyObject = relatedParty.get("partyOrPartyRole");
            if (partyObject == null) {
                continue;
            }
            if (!(partyObject instanceof Map<?, ?> party)) {
                throw new IllegalArgumentException("relatedParty.partyOrPartyRole must be an object");
            }

            String internalPartyRefId = stringValue(party, "id");
            String externalPartyId = stringValue(party, "partyId");
            if (externalPartyId == null || externalPartyId.isBlank()) {
                externalPartyId = internalPartyRefId;
            }
            if (externalPartyId == null || externalPartyId.isBlank()) {
                log.warn("Skipping BillingAccount related party without an external party id");
                continue;
            }

            String referenceId = internalPartyRefId == null || internalPartyRefId.isBlank()
                    ? UUID.randomUUID().toString()
                    : internalPartyRefId;
            PartyRefOrPartyRoleRef reference = partyRepository.findById(referenceId)
                    .orElseGet(() -> PartyRefOrPartyRoleRef.builder()
                            .id(referenceId)
                            .build());
            reference.setBillingAccountId(billingAccountId);
            reference.setPartyId(externalPartyId);
            reference.setHref(stringValue(party, "href"));
            reference.setName(stringValue(party, "name"));
            reference.setPartyName(stringValue(party, "partyName"));
            reference.setReferredType(stringValue(party, "@referredType"));
            reference.setBaseType(stringValue(party, "@baseType"));
            reference.setSchemaLocation(stringValue(party, "@schemaLocation"));
            reference.setType(stringValue(party, "@type", "PartyRef"));
            partyRepository.save(reference);
            log.info("Stored PartyRef id={} partyId={} from BillingAccount event",
                    reference.getId(), externalPartyId);
        }
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
