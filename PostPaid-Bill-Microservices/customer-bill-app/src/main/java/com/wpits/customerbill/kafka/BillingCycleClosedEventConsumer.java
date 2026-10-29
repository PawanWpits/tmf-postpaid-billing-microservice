package com.wpits.customerbill.kafka;

import tools.jackson.databind.ObjectMapper;
import com.wpits.customerbill.dtos.BillingCycleClosedEvent;
import com.wpits.customerbill.service.CustomerBillGenerationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class BillingCycleClosedEventConsumer {
    private final ObjectMapper objectMapper;
    private final CustomerBillGenerationService generationService;

    @KafkaListener(topics = "${app.kafka.topics.billing-cycle-closed:customer-bill.billingCycleClosed.events}",
            groupId = "${spring.kafka.consumer.group-id}", containerFactory = "kafkaListenerContainerFactory")
    public void consume(Map<String, Object> envelope) {
        if (!"BILLING_CYCLE_CLOSED".equals(String.valueOf(envelope.get("eventType")))) {
            return;
        }
        Object payload = ((Map<?, ?>) envelope.get("event")).get("billingCycleClosed");
        generationService.generate(objectMapper.convertValue(payload, BillingCycleClosedEvent.class));
    }
}
