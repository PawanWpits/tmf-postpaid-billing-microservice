package com.wpits.customerbill.controllers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.JsonNode;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.wpits.customerbill.dtos.BillCycleDto;
import com.wpits.customerbill.service.BillCycleService;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

@RestController
@RequestMapping("/billcycle")
@RequiredArgsConstructor
public class BillCycleController {

    private final BillCycleService billCycleService;
    
    @PostMapping
    public ResponseEntity<?> create(@RequestBody BillCycleDto dto){
    	return ResponseEntity.status(HttpStatus.CREATED).body(billCycleService.create(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<JsonNode> getById(@PathVariable @NotBlank String id,
            @RequestParam(required = false) String fields) {
    	
        return ResponseEntity.status(HttpStatus.OK).body(billCycleService.getById(id, fields));
    }

    @GetMapping
    public ResponseEntity<List<JsonNode>> list(
            @RequestParam(required = false) String fields,
            @RequestParam(required = false) Integer offset,
            @RequestParam(required = false) Integer limit) {
    	
        return ResponseEntity.status(HttpStatus.OK).body(billCycleService.list(fields, offset, limit));
    }
}
