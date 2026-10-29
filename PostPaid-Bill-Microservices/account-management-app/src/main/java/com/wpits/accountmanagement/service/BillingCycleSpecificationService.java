package com.wpits.accountmanagement.service;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.wpits.accountmanagement.dtos.BillingCycleSpecificationDto;

public interface BillingCycleSpecificationService {
	
    BillingCycleSpecificationDto create(BillingCycleSpecificationDto dto);
    
    JsonNode getById(String id, String fields);
    
    List<JsonNode> list(String fields, Integer offset, Integer limit);
    
    BillingCycleSpecificationDto patch(String id, BillingCycleSpecificationDto patch);
    
    void delete(String id);
}
