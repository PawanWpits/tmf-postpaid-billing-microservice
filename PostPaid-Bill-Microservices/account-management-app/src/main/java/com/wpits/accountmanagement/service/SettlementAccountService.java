package com.wpits.accountmanagement.service;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.wpits.accountmanagement.dtos.SettlementAccountDto;

public interface SettlementAccountService {
    SettlementAccountDto create(SettlementAccountDto dto);
    JsonNode getById(String id, String fields);
    List<JsonNode> list(String fields, Integer offset, Integer limit);
    SettlementAccountDto patch(String id, SettlementAccountDto patch);
    void delete(String id);
}
