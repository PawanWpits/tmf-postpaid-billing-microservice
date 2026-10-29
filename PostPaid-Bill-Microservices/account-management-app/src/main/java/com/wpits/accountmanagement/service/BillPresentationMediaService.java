package com.wpits.accountmanagement.service;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.wpits.accountmanagement.dtos.BillPresentationMediaDto;

public interface BillPresentationMediaService {

    BillPresentationMediaDto create(BillPresentationMediaDto dto);

    JsonNode getById(String id, String fields);

    List<JsonNode> list(String fields, Integer offset, Integer limit);
    BillPresentationMediaDto patch(String id, BillPresentationMediaDto patch);
    void delete(String id);
}
