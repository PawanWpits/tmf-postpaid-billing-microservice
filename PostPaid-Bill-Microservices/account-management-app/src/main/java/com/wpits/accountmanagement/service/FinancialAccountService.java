package com.wpits.accountmanagement.service;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.wpits.accountmanagement.dtos.FinancialAccountDto;

public interface FinancialAccountService {

    FinancialAccountDto create(FinancialAccountDto dto);

    JsonNode getById(String id, String fields);

    List<JsonNode> list(String fields, Integer offset, Integer limit);

    FinancialAccountDto patch(String id, FinancialAccountDto patch);

    void delete(String id);
}
