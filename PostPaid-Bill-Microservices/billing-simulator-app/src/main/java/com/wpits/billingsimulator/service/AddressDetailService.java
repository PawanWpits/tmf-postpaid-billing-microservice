package com.wpits.billingsimulator.service;

import com.wpits.billingsimulator.dto.AddressDto;

public interface AddressDetailService {

    AddressDto getAddressDetails(String partyId);
}
