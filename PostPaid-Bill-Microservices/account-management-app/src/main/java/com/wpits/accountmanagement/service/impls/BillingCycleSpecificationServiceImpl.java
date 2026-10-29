package com.wpits.accountmanagement.service.impls;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.wpits.accountmanagement.config.KafkaTopicsProperties;
import com.wpits.accountmanagement.dtos.BillingCycleSpecificationDto;
import com.wpits.accountmanagement.entities.BillingCycleSpecification;
import com.wpits.accountmanagement.exceptions.BadRequestException;
import com.wpits.accountmanagement.exceptions.ConflictException;
import com.wpits.accountmanagement.exceptions.ResourceNotFoundException;
import com.wpits.accountmanagement.kafka.AccountEventPublisher;
import com.wpits.accountmanagement.repositories.BillingCycleSpecificationRepository;
import com.wpits.accountmanagement.service.BillingCycleSpecificationService;
import com.wpits.accountmanagement.utils.BillingCycleSpecificationMapper;
import com.wpits.accountmanagement.utils.CommonMapper;
import com.wpits.accountmanagement.utils.FieldFilterUtil;
import com.wpits.accountmanagement.utils.IdGenerator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BillingCycleSpecificationServiceImpl implements BillingCycleSpecificationService {

    private static final String TYPE = "BillingCycleSpecification";
    private static final String RESOURCE_NAME = "billingCycleSpecification";
    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 200;

    private static final Set<String> DEFAULT_GET_FIELDS =
            Set.of("id", "href", "name", "description", "frequency", "billingPeriod", "@type");
    private static final Set<String> DEFAULT_LIST_FIELDS = Set.of("id", "href", "name", "@type");

    private final BillingCycleSpecificationRepository repository;
    private final BillingCycleSpecificationMapper mapper;
    private final CommonMapper commonMapper;
    private final FieldFilterUtil fieldFilterUtil;
    private final IdGenerator idGenerator;
    private final AccountEventPublisher eventPublisher;
    private final KafkaTopicsProperties topics;

    @Value("${app.base-url}")
    private String baseUrl;

    @Override
    public BillingCycleSpecificationDto create(BillingCycleSpecificationDto dto) {
    	
        log.info("POST /billingCycleSpecification - name=[{}]", dto.getName());

        if(repository.existsByBillingPeriod(dto.getBillingPeriod())) {
            throw new ConflictException("BillingCycleSpecification already exists with billingPeriod: " + dto.getBillingPeriod(), TYPE, "NA");

        }
        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new BadRequestException("name is mandatory", TYPE, "NA");
        }
        if (dto.getId() != null && !dto.getId().isBlank() && repository.existsById(dto.getId())) {
            throw new ConflictException("BillingCycleSpecification already exists with id: " + dto.getId(), TYPE, "NA");
        }

        BillingCycleSpecification bcs = mapper.toEntity(dto);
        String id = nextId();
        bcs.setId(id);
        bcs.setHref(baseUrl + "/billingCycleSpecification/" + id);
        bcs.setBaseType(TYPE);
        bcs.setType(TYPE);

        BillingCycleSpecification saved = repository.save(bcs);
        BillingCycleSpecificationDto result = mapper.toDto(saved);

        eventPublisher.publish(topics.getBillingCycleSpecification(), "BCS_CREATED",
                RESOURCE_NAME, saved.getId(), result);
        log.info("Created BillingCycleSpecification id={}", saved.getId());
        return result;
    }
    private String nextId() {
        long next = repository.count() + 1;
        return String.format("BCS_%04d", next);
    }
    @Override
    public JsonNode getById(String id, String fields) {
        log.info("GET /billingCycleSpecification/{} - fields=[{}]", id, fields);
        BillingCycleSpecification entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "BillingCycleSpecification not found with given Id: " + id, TYPE, "Null"));
        return fieldFilterUtil.applyFieldSelection(mapper.toDto(entity), fields, DEFAULT_GET_FIELDS);
    }

    @Override
    public List<JsonNode> list(String fields, Integer offset, Integer limit) {
        int safeLimit = (limit == null || limit <= 0) ? DEFAULT_LIMIT : Math.min(limit, MAX_LIMIT);
        int safeOffset = (offset == null || offset < 0) ? 0 : offset;
        log.info("GET /billingCycleSpecification - fields=[{}], offset=[{}], limit=[{}]", fields, safeOffset, safeLimit);

        return repository.findAllWithExactOffset(safeOffset, safeLimit).stream()
                .map(mapper::toDto)
                .map(dto -> fieldFilterUtil.applyFieldSelection(dto, fields, DEFAULT_LIST_FIELDS))
                .toList();
    }

    @Override
    public BillingCycleSpecificationDto patch(String id, BillingCycleSpecificationDto patch) {
        log.info("PATCH /billingCycleSpecification/{}", id);
        BillingCycleSpecification entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "BillingCycleSpecification not found with given Id: " + id, TYPE, "Null"));

        if (patch != null) {
            if (patch.getName() != null) entity.setName(patch.getName());
            if (patch.getDescription() != null) entity.setDescription(patch.getDescription());
            if (patch.getFrequency() != null) entity.setFrequency(patch.getFrequency());
            if (patch.getBillingPeriod() != null) entity.setBillingPeriod(patch.getBillingPeriod());
            if (patch.getBillingDateShift() != null) entity.setBillingDateShift(patch.getBillingDateShift());
            if (patch.getChargeDateOffset() != null) entity.setChargeDateOffset(patch.getChargeDateOffset());
            if (patch.getCreditDateOffset() != null) entity.setCreditDateOffset(patch.getCreditDateOffset());
            if (patch.getMailingDateOffset() != null) entity.setMailingDateOffset(patch.getMailingDateOffset());
            if (patch.getPaymentDueDateOffset() != null) entity.setPaymentDueDateOffset(patch.getPaymentDueDateOffset());
            if (patch.getValidFor() != null) entity.setValidFor(commonMapper.toTimePeriod(patch.getValidFor()));
        }

        BillingCycleSpecification saved = repository.save(entity);
        BillingCycleSpecificationDto result = mapper.toDto(saved);

        eventPublisher.publish(topics.getBillingCycleSpecification(), "BillingCycleSpecificationAttributeValueChangeEvent",
                RESOURCE_NAME, saved.getId(), result);
        log.info("Patched BillingCycleSpecification id={}", saved.getId());
        return result;
    }

    @Override
    public void delete(String id) {
        log.info("DELETE /billingCycleSpecification/{}", id);
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("BillingCycleSpecification not found with given Id: " + id, TYPE, "Null");
        }
        repository.deleteById(id);
        eventPublisher.publish(topics.getBillingCycleSpecification(), "BillingCycleSpecificationDeleteEvent",
                RESOURCE_NAME, id, BillingCycleSpecificationDto.builder().id(id).type(TYPE).build());
        log.info("Deleted BillingCycleSpecification id={}", id);
    }
}
