package com.wpits.accountmanagement.service;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.wpits.accountmanagement.dtos.PartyAccountDto;

public interface PartyAccountService {
	
    PartyAccountDto create(PartyAccountDto dto);
    
    JsonNode getById(String id, String fields);
    
    List<JsonNode> list(String fields, Integer offset, Integer limit);
    
    PartyAccountDto patch(String id, PartyAccountDto patch);
    
    void delete(String id);
}
