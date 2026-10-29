package com.wpits.customerbill.controllers;

import com.wpits.customerbill.dtos.TaxItemDto;
import com.wpits.customerbill.service.TaxItemService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/taxItem")
@RequiredArgsConstructor
public class TaxItemController {
    private final TaxItemService service;

    @PostMapping
    public ResponseEntity<TaxItemDto> create(@RequestBody TaxItemDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @GetMapping
    public ResponseEntity<List<TaxItemDto>> list() {
        return ResponseEntity.ok(service.list());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaxItemDto> getById(@PathVariable @NotBlank String id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaxItemDto> update(@PathVariable @NotBlank String id, @RequestBody TaxItemDto dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable @NotBlank String id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
