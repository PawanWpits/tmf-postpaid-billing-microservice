package com.wpits.accountmanagement.service.impls;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.wpits.accountmanagement.config.KafkaTopicsProperties;
import com.wpits.accountmanagement.dtos.FinancialAccountDto;
import com.wpits.accountmanagement.entities.FinancialAccount;
import com.wpits.accountmanagement.enums.AccountStateType;
import com.wpits.accountmanagement.exceptions.BadRequestException;
import com.wpits.accountmanagement.exceptions.ConflictException;
import com.wpits.accountmanagement.exceptions.ResourceNotFoundException;
import com.wpits.accountmanagement.kafka.AccountEventPublisher;
import com.wpits.accountmanagement.repositories.FinancialAccountRepository;
import com.wpits.accountmanagement.service.FinancialAccountService;
import com.wpits.accountmanagement.utils.CommonMapper;
import com.wpits.accountmanagement.utils.FieldFilterUtil;
import com.wpits.accountmanagement.utils.FinancialAccountMapper;
import com.wpits.accountmanagement.utils.IdGenerator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implements TMF666 "Operations on FinancialAccount" (spec pages 142-160).
 * relatedParty (with role) is mandatory on create, per the spec's Mandatory
 * Attributes table (page 8051). Publishes FinancialAccountCreateEvent /
 * FinancialAccountStateChangeEvent / FinancialAccountAttributeValueChangeEvent
 * / FinancialAccountDeleteEvent to Kafka.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class FinancialAccountServiceImpl implements FinancialAccountService {

    private static final String TYPE = "FinancialAccount";
    private static final String RESOURCE_NAME = "financialAccount";
    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 200;

    private static final Set<String> DEFAULT_GET_FIELDS =
            Set.of("id", "href", "name", "description", "accountType", "state", "creditLimit", "relatedParty", "@type");
    private static final Set<String> DEFAULT_LIST_FIELDS = Set.of("id", "href", "name", "state", "@type");

    private final FinancialAccountRepository repository;
    private final FinancialAccountMapper mapper;
    private final CommonMapper commonMapper;
    private final FieldFilterUtil fieldFilterUtil;
    private final IdGenerator idGenerator;
    private final AccountEventPublisher eventPublisher;
    private final KafkaTopicsProperties topics;

    @Value("${app.base-url}")
    private String baseUrl;

    @Override
    public FinancialAccountDto create(FinancialAccountDto dto) {
    	
        log.info("POST /financialAccount - name=[{}]", dto.getName());
        validateMandatory(dto);

        if (dto.getId() != null && !dto.getId().isBlank() && repository.existsById(dto.getId())) {
            throw new ConflictException("FinancialAccount already exists with id: " + dto.getId(), TYPE, "NA");
        }

        FinancialAccount entity = mapper.toEntity(dto);
        String id = nextId();
        entity.setId(id);
        entity.setHref(baseUrl + "/financialAccount/" + id);
        entity.setBaseType(TYPE);
        entity.setType(TYPE);
        entity.setLastUpdate(LocalDateTime.now());
        if (entity.getState() == null) {
            entity.setState(AccountStateType.PENDING_ACTIVE);
        }

        FinancialAccount saved = repository.save(entity);
        FinancialAccountDto result = mapper.toDto(saved);

        //eventPublisher.publish(topics.getFinancialAccount(), "FinancialAccountCreateEvent", RESOURCE_NAME, saved.getId(), result);
        log.info("Created FinancialAccount id={}", saved.getId());
        return result;
    }
    private String nextId() {
        long next = repository.count() + 1;
        return String.format("FA_%04d", next);
    }
    private void validateMandatory(FinancialAccountDto dto) {
        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new BadRequestException("name is mandatory", TYPE, "NA");
        }
        if (dto.getRelatedParty() == null || dto.getRelatedParty().isEmpty()) {
            throw new BadRequestException("relatedParty is mandatory", TYPE, "NA");
        }
        dto.getRelatedParty().forEach(rp -> {
            if (rp.getRole() == null || rp.getRole().isBlank()) {
                throw new BadRequestException("relatedParty.role is mandatory", TYPE, "NA");
            }
        });
    }

    @Override
    public JsonNode getById(String id, String fields) {
        log.info("GET /financialAccount/{} - fields=[{}]", id, fields);
        FinancialAccount entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FinancialAccount not found with given Id: " + id, TYPE, "Null"));
        return fieldFilterUtil.applyFieldSelection(mapper.toDto(entity), fields, DEFAULT_GET_FIELDS);
    }

    @Override
    public List<JsonNode> list(String fields, Integer offset, Integer limit) {
        int safeLimit = (limit == null || limit <= 0) ? DEFAULT_LIMIT : Math.min(limit, MAX_LIMIT);
        int safeOffset = (offset == null || offset < 0) ? 0 : offset;
        log.info("GET /financialAccount - fields=[{}], offset=[{}], limit=[{}]", fields, safeOffset, safeLimit);

        return repository.findAllWithExactOffset(safeOffset, safeLimit).stream()
                .map(mapper::toDto)
                .map(dto -> fieldFilterUtil.applyFieldSelection(dto, fields, DEFAULT_LIST_FIELDS))
                .toList();
    }

    @Override
    public FinancialAccountDto patch(String id, FinancialAccountDto patch) {
        log.info("PATCH /financialAccount/{}", id);
        FinancialAccount entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FinancialAccount not found with given Id: " + id, TYPE, "Null"));

        boolean stateChanged = false;

        if (patch != null) {
            if (patch.getName() != null) entity.setName(patch.getName());
            if (patch.getDescription() != null) entity.setDescription(patch.getDescription());
            if (patch.getAccountType() != null) entity.setAccountType(patch.getAccountType());
            if (patch.getCreditLimit() != null) entity.setCreditLimit(commonMapper.toMoney(patch.getCreditLimit()));
            if (patch.getAccountBalance() != null) entity.setAccountBalance(commonMapper.toAccountBalanceList(patch.getAccountBalance()));
            if (patch.getAccountRelationship() != null) entity.setAccountRelationship(commonMapper.toAccountRelationshipList(patch.getAccountRelationship()));
            if (patch.getContact() != null) entity.setContact(commonMapper.toContactList(patch.getContact()));
            if (patch.getRelatedParty() != null) entity.setRelatedParty(commonMapper.toRelatedPartyList(patch.getRelatedParty()));
            if (patch.getTaxExemption() != null) entity.setTaxExemption(commonMapper.toTaxExemptionList(patch.getTaxExemption()));

            if (patch.getState() != null) {
                AccountStateType newState = AccountStateType.fromValue(patch.getState());
                stateChanged = entity.getState() != newState;
                entity.setState(newState);
            }
        }

        entity.setLastUpdate(LocalDateTime.now());
        FinancialAccount saved = repository.save(entity);
        FinancialAccountDto result = mapper.toDto(saved);

        String eventType = stateChanged ? "FinancialAccountStateChangeEvent" : "FinancialAccountAttributeValueChangeEvent";
        eventPublisher.publish(topics.getFinancialAccount(), eventType, RESOURCE_NAME, saved.getId(), result);
        log.info("Patched FinancialAccount id={} ({})", saved.getId(), eventType);
        return result;
    }

    @Override
    public void delete(String id) {
        log.info("DELETE /financialAccount/{}", id);
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("FinancialAccount not found with given Id: " + id, TYPE, "Null");
        }
        repository.deleteById(id);
        eventPublisher.publish(topics.getFinancialAccount(), "FinancialAccountDeleteEvent", RESOURCE_NAME, id,
                FinancialAccountDto.builder().id(id).type(TYPE).build());
        log.info("Deleted FinancialAccount id={}", id);
    }
}
