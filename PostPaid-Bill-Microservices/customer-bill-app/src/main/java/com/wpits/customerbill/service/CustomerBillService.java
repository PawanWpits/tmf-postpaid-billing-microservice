package com.wpits.customerbill.service;

import com.wpits.customerbill.dtos.CustomerBillDto;
import com.wpits.customerbill.dtos.CustomerBillPatchDto;
import com.wpits.customerbill.dtos.MoneyDto;
import tools.jackson.databind.JsonNode;

import java.time.LocalDate;
import java.util.List;

public interface CustomerBillService {

    CustomerBillDto create(CustomerBillDto dto);

    JsonNode getById(String id, String fields);

    List<JsonNode> list(String fields, Integer offset, Integer limit);

    List<CustomerBillDto> getAllByBillDate(LocalDate date);

    CustomerBillDto patch(String id, CustomerBillPatchDto patch);

    CustomerBillDto patchRemainingAmt(String id, MoneyDto remainingAmt);

    void delete(String id);


}
