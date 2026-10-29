package com.wpits.customerbill.controllers;

import com.wpits.customerbill.dtos.BillingCycleProcessRequest;
import com.wpits.customerbill.service.BillingCycleOrchestrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/billing-cycle")
@RequiredArgsConstructor
public class BillingCycleOrchestrationController {

    private final BillingCycleOrchestrationService orchestrationService;

    @PostMapping("/process")
    public ResponseEntity<String> process() {
        orchestrationService.processScheduled();
        return ResponseEntity.accepted().body("Billing cycle orchestration started");
    }

}
