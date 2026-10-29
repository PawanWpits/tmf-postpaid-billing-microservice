package com.wpits.billingsimulator.service.impls;

import com.wpits.billingsimulator.dto.AddressDto;
import com.wpits.billingsimulator.service.AddressDetailService;
import org.springframework.stereotype.Service;

@Service
public class AddressDetailServiceImpl implements AddressDetailService {

    @Override
    public AddressDto getAddressDetails(String partyId) {

        AddressDto addressDto = new AddressDto();
        addressDto.setPartyId(partyId);
        addressDto.setAddressLine1("123 Main St");
        addressDto.setAddressLine2("Apt 4B");
        addressDto.setAddressLine3("New Delhi");
        addressDto.setCity("Anytown");
        addressDto.setState("CA");
        addressDto.setPostalCode("12345");
        addressDto.setCountry("USA");

        return addressDto;
    }
}
