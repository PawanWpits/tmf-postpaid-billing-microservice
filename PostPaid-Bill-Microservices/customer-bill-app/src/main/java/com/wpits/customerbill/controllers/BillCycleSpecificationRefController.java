package com.wpits.customerbill.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wpits.customerbill.dtos.BillCycleSpecificationRefDto;
import com.wpits.customerbill.service.BillCycleSpecificationRefService;

import lombok.RequiredArgsConstructor;

import java.util.List;

@RestController
@RequestMapping("/billCycleSpecification")
@RequiredArgsConstructor
public class BillCycleSpecificationRefController {

	private final BillCycleSpecificationRefService billCycleSpecificationRefService;

	@PostMapping
	public ResponseEntity<BillCycleSpecificationRefDto> create(
			@RequestBody BillCycleSpecificationRefDto dto) {
		return ResponseEntity.status(201).body(billCycleSpecificationRefService.create(dto));
	}

	@GetMapping
	public ResponseEntity<List<BillCycleSpecificationRefDto>> getBillCycleSpecificationRefs() {
		return ResponseEntity.ok(billCycleSpecificationRefService.getBillCycleSpecificationRefs());
	}
	
	
	@GetMapping("/{id}")
	public ResponseEntity<?> getByIdBillCycleSpec(@PathVariable String id){
		return ResponseEntity.ok(billCycleSpecificationRefService.getById(id));
	}
}
