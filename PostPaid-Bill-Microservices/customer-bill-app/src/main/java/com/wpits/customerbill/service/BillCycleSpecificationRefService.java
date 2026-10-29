package com.wpits.customerbill.service;

import com.wpits.customerbill.dtos.BillCycleSpecificationRefDto;

import java.util.List;

public interface BillCycleSpecificationRefService {

	BillCycleSpecificationRefDto create(BillCycleSpecificationRefDto billCycleSpecificationRefDto);
	BillCycleSpecificationRefDto getById(String id);

    List<BillCycleSpecificationRefDto> getBillCycleSpecificationRefs();
}
