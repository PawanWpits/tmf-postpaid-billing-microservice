package com.wpits.customerbill.controllers;

import com.wpits.customerbill.dtos.CustomerBillOnDemandDto;
import com.wpits.customerbill.enums.CustomerBillOnDemandStateType;
import com.wpits.customerbill.service.CustomerBillOnDemandService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/customerBillOnDemand")
@RequiredArgsConstructor
public class CustomerBillOnDemandController {

    private final CustomerBillOnDemandService service;

    @PostMapping
    public ResponseEntity<CustomerBillOnDemandDto> create(@RequestBody CustomerBillOnDemandDto request) {
        CustomerBillOnDemandDto response = service.create(request);
        if (CustomerBillOnDemandStateType.REJECTED.getValue().equals(response.getState())) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response);
        }
        if (CustomerBillOnDemandStateType.TERMINATED_WITH_ERROR.getValue().equals(response.getState())) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(response);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
