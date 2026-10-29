package com.wpits.accountmanagement.service.impls;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.wpits.accountmanagement.config.KafkaTopicsProperties;
import com.wpits.accountmanagement.dtos.BillPresentationMediaDto;
import com.wpits.accountmanagement.entities.BillPresentationMedia;
import com.wpits.accountmanagement.exceptions.BadRequestException;
import com.wpits.accountmanagement.exceptions.ConflictException;
import com.wpits.accountmanagement.exceptions.ResourceNotFoundException;
import com.wpits.accountmanagement.kafka.AccountEventPublisher;
import com.wpits.accountmanagement.repositories.BillPresentationMediaRepository;
import com.wpits.accountmanagement.service.BillPresentationMediaService;
import com.wpits.accountmanagement.utils.BillPresentationMediaMapper;
import com.wpits.accountmanagement.utils.FieldFilterUtil;
import com.wpits.accountmanagement.utils.IdGenerator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BillPresentationMediaServiceImpl implements BillPresentationMediaService {

    private static final String TYPE = "BillPresentationMedia";
    private static final String RESOURCE_NAME = "billPresentationMedia";
    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 200;

    private static final Set<String> DEFAULT_GET_FIELDS = Set.of("id", "href", "name", "description", "@type");
    private static final Set<String> DEFAULT_LIST_FIELDS = Set.of("id", "href", "name", "@type");

    private final BillPresentationMediaRepository repository;
    private final BillPresentationMediaMapper mapper;
    private final FieldFilterUtil fieldFilterUtil;
    private final IdGenerator idGenerator;
    private final AccountEventPublisher eventPublisher;
    private final KafkaTopicsProperties topics;

    @Value("${app.base-url}")
    private String baseUrl;

    @Override
    public BillPresentationMediaDto create(BillPresentationMediaDto dto) {
        log.info("POST /billPresentationMedia - name=[{}]", dto.getName());

        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new BadRequestException("name is mandatory", TYPE, "NA");
        }
        if (dto.getId() != null && !dto.getId().isBlank() && repository.existsById(dto.getId())) {
            throw new ConflictException("BillPresentationMedia already exists with id: " + dto.getId(), TYPE, "NA");
        }

        BillPresentationMedia billPresentMedia = mapper.toEntity(dto);
        String id = nextId();
        billPresentMedia.setId(id);
        billPresentMedia.setHref(baseUrl + "/billPresentationMedia/" + id);
        billPresentMedia.setBaseType(TYPE);
        billPresentMedia.setType(TYPE);

        BillPresentationMedia saved = repository.save(billPresentMedia);
        BillPresentationMediaDto result = mapper.toDto(saved);

        //eventPublisher.publish(topics.getBillPresentationMedia(), "BillPresentationMediaCreateEvent", RESOURCE_NAME, saved.getId(), result);
        log.info("Created BillPresentationMedia id={}", saved.getId());
        return result;
    }
    private String nextId() {
        long next = repository.count() + 1;
        return String.format("BPM_%04d", next);
    }
    @Override
    public JsonNode getById(String id, String fields) {
        log.info("GET /billPresentationMedia/{} - fields=[{}]", id, fields);
        BillPresentationMedia billPresentMedia = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BillPresentationMedia not found with given Id: " + id, TYPE, "Null"));
        return fieldFilterUtil.applyFieldSelection(mapper.toDto(billPresentMedia), fields, DEFAULT_GET_FIELDS);
    }

    @Override
    public List<JsonNode> list(String fields, Integer offset, Integer limit) {
        int safeLimit = (limit == null || limit <= 0) ? DEFAULT_LIMIT : Math.min(limit, MAX_LIMIT);
        int safeOffset = (offset == null || offset < 0) ? 0 : offset;
        log.info("GET /billPresentationMedia - fields=[{}], offset=[{}], limit=[{}]", fields, safeOffset, safeLimit);

        return repository.findAllWithExactOffset(safeOffset, safeLimit).stream()
                .map(mapper::toDto)
                .map(dto -> fieldFilterUtil.applyFieldSelection(dto, fields, DEFAULT_LIST_FIELDS))
                .toList();
    }

    @Override
    public BillPresentationMediaDto patch(String id, BillPresentationMediaDto patch) {
        log.info("PATCH /billPresentationMedia/{}", id);
        BillPresentationMedia billPresentMedia = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BillPresentationMedia not found with given Id: " + id, TYPE, "Null"));

        if (patch != null) {
            if (patch.getName() != null) billPresentMedia.setName(patch.getName());
            if (patch.getDescription() != null) billPresentMedia.setDescription(patch.getDescription());
        }

        BillPresentationMedia saved = repository.save(billPresentMedia);
        BillPresentationMediaDto result = mapper.toDto(saved);

        //eventPublisher.publish(topics.getBillPresentationMedia(), "BillPresentationMediaAttributeValueChangeEvent", RESOURCE_NAME, saved.getId(), result);
        log.info("Patched BillPresentationMedia id={}", saved.getId());
        return result;
    }

    @Override
    public void delete(String id) {
        log.info("DELETE /billPresentationMedia/{}", id);
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("BillPresentationMedia not found with given Id: " + id, TYPE, "Null");
        }
        repository.deleteById(id);
        //eventPublisher.publish(topics.getBillPresentationMedia(), "BillPresentationMediaDeleteEvent", RESOURCE_NAME, id, BillPresentationMediaDto.builder().id(id).type(TYPE).build());
        log.info("Deleted BillPresentationMedia id={}", id);
    }
}
