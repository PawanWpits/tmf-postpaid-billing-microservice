package com.wpits.billingsimulator.controller;

import com.wpits.billingsimulator.dto.AddressDto;
import com.wpits.billingsimulator.service.AddressDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/address-details")
@RequiredArgsConstructor
public class AddressDetailController {

    private final AddressDetailService addressDetailService;

    @GetMapping("/{partyId}")
    public ResponseEntity<AddressDto> getAddressDetails(
            @PathVariable String partyId) {
        return ResponseEntity.ok(addressDetailService.getAddressDetails(partyId));
    }
}
