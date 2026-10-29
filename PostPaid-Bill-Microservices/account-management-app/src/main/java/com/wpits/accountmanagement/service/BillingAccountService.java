package com.wpits.accountmanagement.service;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.wpits.accountmanagement.dtos.BillingAccountDto;

public interface BillingAccountService {
	
    BillingAccountDto create(BillingAccountDto dto);
    
    JsonNode getById(String id, String fields);
    
    List<JsonNode> list(String fields, Integer offset, Integer limit);

    List<BillingAccountDto> listBillingAccounts(String billCycleSpecificationId);
    BillingAccountDto patch(String id, BillingAccountDto patch);
    
    void delete(String id);
}
