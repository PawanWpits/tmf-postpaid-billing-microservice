package com.wpits.accountmanagement.service.impls;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.wpits.accountmanagement.config.KafkaTopicsProperties;
import com.wpits.accountmanagement.dtos.BillFormatDto;
import com.wpits.accountmanagement.entities.BillFormat;
import com.wpits.accountmanagement.exceptions.BadRequestException;
import com.wpits.accountmanagement.exceptions.ConflictException;
import com.wpits.accountmanagement.exceptions.ResourceNotFoundException;
import com.wpits.accountmanagement.kafka.AccountEventPublisher;
import com.wpits.accountmanagement.repositories.BillFormatRepository;
import com.wpits.accountmanagement.service.BillFormatService;
import com.wpits.accountmanagement.utils.BillFormatMapper;
import com.wpits.accountmanagement.utils.FieldFilterUtil;
import com.wpits.accountmanagement.utils.IdGenerator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BillFormatServiceImpl implements BillFormatService {

    private static final String TYPE = "BillFormat";
    private static final String RESOURCE_NAME = "billFormat";
    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 200;

    private static final Set<String> DEFAULT_GET_FIELDS = Set.of("id", "href", "name", "description", "@type");
    private static final Set<String> DEFAULT_LIST_FIELDS = Set.of("id", "href", "name", "@type");

    private final BillFormatRepository repository;
    private final BillFormatMapper mapper;
    private final FieldFilterUtil fieldFilterUtil;
    private final IdGenerator idGenerator;
    private final AccountEventPublisher eventPublisher;
    private final KafkaTopicsProperties topics;

    @Value("${app.base-url}")
    private String baseUrl;

    @Override
    public BillFormatDto create(BillFormatDto dto) {
    	
        log.info("POST /billFormat - name=[{}]", dto.getName());

        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new BadRequestException("name is mandatory", TYPE, "NA");
        }
        if (dto.getId() != null && !dto.getId().isBlank() && repository.existsById(dto.getId())) {
            throw new ConflictException("BillFormat already exists with id: " + dto.getId(), TYPE, "NA");
        }

        BillFormat billFormat = mapper.toEntity(dto);
        String id = nextId();
        billFormat.setId(id);
        billFormat.setHref(baseUrl + "/billFormat/" + id);
        billFormat.setBaseType(TYPE);
        billFormat.setType(TYPE);

        BillFormat saved = repository.save(billFormat);
        BillFormatDto result = mapper.toDto(saved);

        //eventPublisher.publish(topics.getBillFormat(), "BillFormatCreateEvent", RESOURCE_NAME, saved.getId(), result);
        log.info("Created BillFormat id={}", saved.getId());
        return result;
    }

    private String nextId() {
        long next = repository.count() + 1;
        return String.format("BF_%04d", next);
    }

    @Override
    public JsonNode getById(String id, String fields) {
        log.info("GET /billFormat/{} - fields=[{}]", id, fields);
        BillFormat entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BillFormat not found with given Id: " + id, TYPE, "Null"));
        return fieldFilterUtil.applyFieldSelection(mapper.toDto(entity), fields, DEFAULT_GET_FIELDS);
    }

    @Override
    public List<JsonNode> list(String fields, Integer offset, Integer limit) {
        int safeLimit = (limit == null || limit <= 0) ? DEFAULT_LIMIT : Math.min(limit, MAX_LIMIT);
        int safeOffset = (offset == null || offset < 0) ? 0 : offset;
        log.info("GET /billFormat - fields=[{}], offset=[{}], limit=[{}]", fields, safeOffset, safeLimit);

        return repository.findAllWithExactOffset(safeOffset, safeLimit).stream()
                .map(mapper::toDto)
                .map(dto -> fieldFilterUtil.applyFieldSelection(dto, fields, DEFAULT_LIST_FIELDS))
                .toList();
    }

    @Override
    public BillFormatDto patch(String id, BillFormatDto patch) {
        log.info("PATCH /billFormat/{}", id);
        BillFormat entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BillFormat not found with given Id: " + id, TYPE, "Null"));

        if (patch != null) {
            if (patch.getName() != null) entity.setName(patch.getName());
            if (patch.getDescription() != null) entity.setDescription(patch.getDescription());
        }

        BillFormat saved = repository.save(entity);
        BillFormatDto result = mapper.toDto(saved);

        eventPublisher.publish(topics.getBillFormat(), "BillFormatAttributeValueChangeEvent", RESOURCE_NAME, saved.getId(), result);
        log.info("Patched BillFormat id={}", saved.getId());
        return result;
    }

    @Override
    public void delete(String id) {
        log.info("DELETE /billFormat/{}", id);
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("BillFormat not found with given Id: " + id, TYPE, "Null");
        }
        repository.deleteById(id);
        eventPublisher.publish(topics.getBillFormat(), "BillFormatDeleteEvent", RESOURCE_NAME, id,
                BillFormatDto.builder().id(id).type(TYPE).build());
        log.info("Deleted BillFormat id={}", id);
    }
}
