package com.wpits.accountmanagement.service.impls;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.wpits.accountmanagement.config.KafkaTopicsProperties;
import com.wpits.accountmanagement.dtos.PartyAccountDto;
import com.wpits.accountmanagement.entities.PartyAccount;
import com.wpits.accountmanagement.enums.AccountStateType;
import com.wpits.accountmanagement.exceptions.BadRequestException;
import com.wpits.accountmanagement.exceptions.ConflictException;
import com.wpits.accountmanagement.exceptions.ResourceNotFoundException;
import com.wpits.accountmanagement.kafka.AccountEventPublisher;
import com.wpits.accountmanagement.repositories.PartyAccountRepository;
import com.wpits.accountmanagement.service.PartyAccountService;
import com.wpits.accountmanagement.utils.CommonMapper;
import com.wpits.accountmanagement.utils.FieldFilterUtil;
import com.wpits.accountmanagement.utils.IdGenerator;
import com.wpits.accountmanagement.utils.PartyAccountMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implements TMF666 "Operations on PartyAccount" (spec pages 121-132).
 * relatedParty (with role) is mandatory on create, per the spec's Mandatory
 * Attributes table (page 6653). Publishes PartyAccountCreateEvent /
 * PartyAccountStateChangeEvent / PartyAccountAttributeValueChangeEvent /
 * PartyAccountDeleteEvent to Kafka.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PartyAccountServiceImpl implements PartyAccountService {

    private static final String TYPE = "PartyAccount";
    private static final String RESOURCE_NAME = "partyAccount";
    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 200;

    private static final Set<String> DEFAULT_GET_FIELDS = Set.of(
            "id", "href", "name", "description", "accountType", "state", "paymentStatus",
            "creditLimit", "billStructure", "defaultPaymentMethod", "financialAccount", "relatedParty", "@type");
    private static final Set<String> DEFAULT_LIST_FIELDS = Set.of("id", "href", "name", "state", "@type");

    private final PartyAccountRepository repository;
    private final PartyAccountMapper mapper;
    private final CommonMapper commonMapper;
    private final FieldFilterUtil fieldFilterUtil;
    private final IdGenerator idGenerator;
    private final AccountEventPublisher eventPublisher;
    private final KafkaTopicsProperties topics;

    @Value("${app.base-url}")
    private String baseUrl;

    @Override
    public PartyAccountDto create(PartyAccountDto dto) {
    	
        log.info("POST /partyAccount - name=[{}]", dto.getName());
        
        validateMandatory(dto);

        if (dto.getId() != null && !dto.getId().isBlank() && repository.existsById(dto.getId())) {
            throw new ConflictException("PartyAccount already exists with id: " + dto.getId(), TYPE, "NA");
        }

        PartyAccount entity = mapper.toEntity(dto);
        String id = (dto.getId() == null || dto.getId().isBlank()) ? idGenerator.generate("PA") : dto.getId();
        entity.setId(id);
        entity.setHref(baseUrl + "/partyAccount/" + id);
        entity.setBaseType(TYPE);
        entity.setType(TYPE);
        entity.setLastUpdate(LocalDateTime.now());
        if (entity.getState() == null) {
            entity.setState(AccountStateType.PENDING_ACTIVE);
        }

        PartyAccount saved = repository.save(entity);
        PartyAccountDto result = mapper.toDto(saved);

        //eventPublisher.publish(topics.getPartyAccount(), "PartyAccountCreateEvent", RESOURCE_NAME, saved.getId(), result);
        log.info("Created PartyAccount id={}", saved.getId());
        return result;
    }

    private void validateMandatory(PartyAccountDto dto) {
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
        log.info("GET /partyAccount/{} - fields=[{}]", id, fields);
        PartyAccount entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PartyAccount not found with given Id: " + id, TYPE, "Null"));
        return fieldFilterUtil.applyFieldSelection(mapper.toDto(entity), fields, DEFAULT_GET_FIELDS);
    }

    @Override
    public List<JsonNode> list(String fields, Integer offset, Integer limit) {
        int safeLimit = (limit == null || limit <= 0) ? DEFAULT_LIMIT : Math.min(limit, MAX_LIMIT);
        int safeOffset = (offset == null || offset < 0) ? 0 : offset;
        log.info("GET /partyAccount - fields=[{}], offset=[{}], limit=[{}]", fields, safeOffset, safeLimit);

        return repository.findAllWithExactOffset(safeOffset, safeLimit).stream()
                .map(mapper::toDto)
                .map(dto -> fieldFilterUtil.applyFieldSelection(dto, fields, DEFAULT_LIST_FIELDS))
                .toList();
    }

    @Override
    public PartyAccountDto patch(String id, PartyAccountDto patch) {
        log.info("PATCH /partyAccount/{}", id);
        PartyAccount entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PartyAccount not found with given Id: " + id, TYPE, "Null"));

        boolean stateChanged = false;

        if (patch != null) {
            if (patch.getName() != null) entity.setName(patch.getName());
            if (patch.getDescription() != null) entity.setDescription(patch.getDescription());
            if (patch.getAccountType() != null) entity.setAccountType(patch.getAccountType());
            if (patch.getPaymentStatus() != null) entity.setPaymentStatus(patch.getPaymentStatus());
            if (patch.getCreditLimit() != null) entity.setCreditLimit(commonMapper.toMoney(patch.getCreditLimit()));
            if (patch.getBillStructure() != null) entity.setBillStructure(commonMapper.toBillStructure(patch.getBillStructure()));
            if (patch.getDefaultPaymentMethod() != null) entity.setDefaultPaymentMethod(commonMapper.toPaymentMethodRef(patch.getDefaultPaymentMethod()));
            if (patch.getFinancialAccount() != null) entity.setFinancialAccount(commonMapper.toFinancialAccountRef(patch.getFinancialAccount()));
            if (patch.getAccountBalance() != null) entity.setAccountBalance(commonMapper.toAccountBalanceList(patch.getAccountBalance()));
            if (patch.getAccountRelationship() != null) entity.setAccountRelationship(commonMapper.toAccountRelationshipList(patch.getAccountRelationship()));
            if (patch.getContact() != null) entity.setContact(commonMapper.toContactList(patch.getContact()));
            if (patch.getPaymentPlan() != null) entity.setPaymentPlan(commonMapper.toPaymentPlanList(patch.getPaymentPlan()));
            if (patch.getRelatedParty() != null) entity.setRelatedParty(commonMapper.toRelatedPartyList(patch.getRelatedParty()));
            if (patch.getTaxExemption() != null) entity.setTaxExemption(commonMapper.toTaxExemptionList(patch.getTaxExemption()));

            if (patch.getState() != null) {
                AccountStateType newState = AccountStateType.fromValue(patch.getState());
                stateChanged = entity.getState() != newState;
                entity.setState(newState);
            }
        }

        entity.setLastUpdate(LocalDateTime.now());
        PartyAccount saved = repository.save(entity);
        PartyAccountDto result = mapper.toDto(saved);

        String eventType = stateChanged ? "PartyAccountStateChangeEvent" : "PartyAccountAttributeValueChangeEvent";
        eventPublisher.publish(topics.getPartyAccount(), eventType, RESOURCE_NAME, saved.getId(), result);
        log.info("Patched PartyAccount id={} ({})", saved.getId(), eventType);
        return result;
    }

    @Override
    public void delete(String id) {
        log.info("DELETE /partyAccount/{}", id);
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("PartyAccount not found with given Id: " + id, TYPE, "Null");
        }
        repository.deleteById(id);
        eventPublisher.publish(topics.getPartyAccount(), "PartyAccountDeleteEvent", RESOURCE_NAME, id,
                PartyAccountDto.builder().id(id).type(TYPE).build());
        log.info("Deleted PartyAccount id={}", id);
    }
}
