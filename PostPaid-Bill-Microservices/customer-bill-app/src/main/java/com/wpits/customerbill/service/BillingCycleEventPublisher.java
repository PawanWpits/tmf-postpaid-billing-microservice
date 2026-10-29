package com.wpits.customerbill.service;

import com.wpits.customerbill.dtos.BillingCycleClosedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BillingCycleEventPublisher {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    @Value("${app.kafka.topics.billing-cycle-closed:customer-bill.billingCycleClosed.events}")
    private String topic;

    public void publish(BillingCycleClosedEvent event) {
        Map<String, Object> envelope = Map.of(
                "correlationId", UUID.randomUUID().toString(),
                "eventId", UUID.randomUUID().toString(),
                "eventTime", LocalDateTime.now().toString(),
                "eventType", "BILLING_CYCLE_CLOSED",
                "type", "BILLING_CYCLE_CLOSED",
                "event", Map.of("billingCycleClosed", event)
        );
        kafkaTemplate.send(topic, event.getBillingAccountId(), envelope);
        log.info("[BILLING-CYCLE] BILLING_CYCLE_CLOSED event published for account={} period={} to {}", event.getBillingAccountId(), event.getPeriodStart() + " to " + event.getPeriodEnd(), topic);
    }
}
