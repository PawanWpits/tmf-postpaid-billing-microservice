package com.wpits.customerbill.service.impls;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;

import com.wpits.customerbill.dtos.BillCycleSpecificationRefDto;
import com.wpits.customerbill.entities.BillCycleSpecificationRef;
import com.wpits.customerbill.exceptions.BadRequestException;
import com.wpits.customerbill.exceptions.ResourceNotFoundException;
import com.wpits.customerbill.repositories.BillCycleSpecificationRefRepository;
import com.wpits.customerbill.service.BillCycleSpecificationRefService;
import com.wpits.customerbill.utils.IdGenerator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BillCycleSpecificationRefServiceImpl implements BillCycleSpecificationRefService {

	private final BillCycleSpecificationRefRepository billCycleSpecificationRefRepository;
	private final IdGenerator idGenerator;

	private static final String BCS_TYPE="BillCycleSpecificationRef";
	private static final String REFERRED_TYPE="BillingCycleSpecification";

	@Value("${app.base-url}")
	private String baseUrl;

	@Override
	public BillCycleSpecificationRefDto create(BillCycleSpecificationRefDto billCycleSpecificationRefDto) {
		if (billCycleSpecificationRefDto == null
				|| billCycleSpecificationRefDto.getName() == null
				|| billCycleSpecificationRefDto.getName().isBlank()) {
			throw new BadRequestException("name is mandatory", BCS_TYPE, "NA");
		}

		String id = billCycleSpecificationRefDto.getId();
		if (id == null || id.isBlank()) {
			id = idGenerator.generate("BCSREF");
		} else if (billCycleSpecificationRefRepository.existsById(id)) {
			throw new BadRequestException("BillCycleSpecificationRef already exists with id: " + id,
					BCS_TYPE, "NA");
		}

		BillCycleSpecificationRef entity = BillCycleSpecificationRef.builder()
				.id(id)
				.href(valueOrDefault(billCycleSpecificationRefDto.getHref(),
						baseUrl + "/billCycleSpecification/" + id))
				.name(billCycleSpecificationRefDto.getName())
				.description(billCycleSpecificationRefDto.getDescription())
				.baseType(valueOrDefault(billCycleSpecificationRefDto.getBaseType(), BCS_TYPE))
				.referredType(valueOrDefault(billCycleSpecificationRefDto.getReferredType(), REFERRED_TYPE))
				.schemaLocation(billCycleSpecificationRefDto.getSchemaLocation())
				.type(valueOrDefault(billCycleSpecificationRefDto.getType(), BCS_TYPE))
				.build();

		BillCycleSpecificationRef saved = billCycleSpecificationRefRepository.save(entity);
		log.info("Created BillCycleSpecificationRef id={}", saved.getId());
		return toDto(saved);
	}

	@Override
	public BillCycleSpecificationRefDto getById(String id) {
		
		 log.info("GET /billCycleSpecification/{} ", id);
	        
			BillCycleSpecificationRef bcsr = billCycleSpecificationRefRepository.findById(id).orElseThrow(() -> {
				log.warn("Bill cycle not found with given Id: {}",id);
				return new ResourceNotFoundException("Bill cycle not found with given Id: "+id, BCS_TYPE,"Null");
			});
			
			
		return toDto(bcsr);
	}

	@Override
	public List<BillCycleSpecificationRefDto> getBillCycleSpecificationRefs() {
		log.info("GET /billCycleSpecification - listing BillingCycleSpecification references");
		return billCycleSpecificationRefRepository.findAll().stream()
				.map(this::toDto)
				.toList();
	}

	private BillCycleSpecificationRefDto toDto(BillCycleSpecificationRef entity) {
		return BillCycleSpecificationRefDto.builder()
				.id(entity.getId())
				.href(entity.getHref())
				.name(entity.getName())
				.description(entity.getDescription())
				.referredType(entity.getReferredType())
				.baseType(entity.getBaseType())
				.schemaLocation(entity.getSchemaLocation())
				.type(entity.getType())
				.build();
	}

	private static String valueOrDefault(String value, String defaultValue) {
		return value == null || value.isBlank() ? defaultValue : value;
	}
}
