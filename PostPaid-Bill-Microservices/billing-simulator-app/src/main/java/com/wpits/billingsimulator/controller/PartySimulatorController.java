package com.wpits.billingsimulator.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/partyManagement/v4/individual")
public class PartySimulatorController {

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getPartyManagement(@PathVariable String id) {

        Map<String, Object> response = Map.of(
                "id", id,
                "href", "http://localhost:8082/partyManagement/v4/individual/" + id,
                "name", "Pawan Sharma",
                "role", "payer",
                "@referenceType", "Individual"
        );

        return ResponseEntity.ok(response);
    }
}

