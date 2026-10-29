package com.wpits.customerbill.controllers;

import com.wpits.customerbill.service.MissingBillGenerationService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/missing-bills")
@RequiredArgsConstructor
public class MissingBillGenerationController {

    private final MissingBillGenerationService service;

    @PostMapping("/{billingCycleSpecificationId}")
    public ResponseEntity<MissingBillGenerationService.MissingBillRecoveryResult> reprocess(
            @PathVariable @NotBlank String billingCycleSpecificationId) {

        return ResponseEntity.ok(service.reprocess(billingCycleSpecificationId));
    }
}
