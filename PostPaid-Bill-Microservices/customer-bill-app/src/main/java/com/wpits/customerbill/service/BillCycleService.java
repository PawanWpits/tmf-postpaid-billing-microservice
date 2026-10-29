package com.wpits.customerbill.service;

import java.util.List;

import com.wpits.customerbill.dtos.BillCycleDto;

import tools.jackson.databind.JsonNode;

public interface BillCycleService {

    BillCycleDto create(BillCycleDto dto);

    BillCycleDto update(Long id, BillCycleDto dto);

    JsonNode getById(String id, String fields);

    List<JsonNode> list(String fields, Integer offset, Integer limit);

    void delete(String id);

}
