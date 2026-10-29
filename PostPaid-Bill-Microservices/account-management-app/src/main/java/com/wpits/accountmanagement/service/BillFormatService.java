package com.wpits.accountmanagement.service;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.wpits.accountmanagement.dtos.BillFormatDto;

public interface BillFormatService {
	
    BillFormatDto create(BillFormatDto dto);
    
    JsonNode getById(String id, String fields);
    
    List<JsonNode> list(String fields, Integer offset, Integer limit);
    
    BillFormatDto patch(String id, BillFormatDto patch);
    
    void delete(String id);
}
