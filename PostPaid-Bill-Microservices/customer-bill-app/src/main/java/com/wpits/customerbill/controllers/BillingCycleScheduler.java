package com.wpits.customerbill.controllers;

import com.wpits.customerbill.service.BillingCycleOrchestrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class BillingCycleScheduler {

    private final BillingCycleOrchestrationService orchestrationService;

    /*@Scheduled(
            cron = "${billing.cycle.scheduler.cron:0 0 0 * * *}",
            zone = "${billing.cycle.scheduler.zone:Asia/Kolkata}")*/

    @Scheduled(cron = "0 0 0 * * *")
    public void processCompletedCycles() {
        LocalDateTime now = LocalDateTime.now();
        log.info("Scheduling billing cycle orchestration time: {}", now);
        orchestrationService.processScheduled();
    }
}
