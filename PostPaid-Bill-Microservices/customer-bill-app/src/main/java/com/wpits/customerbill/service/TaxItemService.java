package com.wpits.customerbill.service;

import com.wpits.customerbill.dtos.TaxItemDto;

import java.util.List;

public interface TaxItemService {

    TaxItemDto create(TaxItemDto dto);

    TaxItemDto getById(String id);

    List<TaxItemDto> list();

    TaxItemDto update(String id, TaxItemDto dto);

    void delete(String id);

    TaxItemDto getBillingTax();
}
