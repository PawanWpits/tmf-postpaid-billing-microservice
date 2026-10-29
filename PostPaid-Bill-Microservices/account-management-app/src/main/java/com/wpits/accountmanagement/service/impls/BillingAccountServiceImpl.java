package com.wpits.accountmanagement.service.impls;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.wpits.accountmanagement.config.KafkaTopicsProperties;
import com.wpits.accountmanagement.dtos.BillingAccountDto;
import com.wpits.accountmanagement.dtos.common.PartyRefOrPartyRoleRefDto;
import com.wpits.accountmanagement.dtos.common.RelatedPartyRefOrPartyRoleRefDto;
import com.wpits.accountmanagement.entities.BillingAccount;
import com.wpits.accountmanagement.entities.common.RelatedPartyRefOrPartyRoleRef;
import com.wpits.accountmanagement.enums.AccountStateType;
import com.wpits.accountmanagement.exceptions.BadRequestException;
import com.wpits.accountmanagement.exceptions.ConflictException;
import com.wpits.accountmanagement.exceptions.ResourceNotFoundException;
import com.wpits.accountmanagement.kafka.AccountEventPublisher;
import com.wpits.accountmanagement.repositories.BillingAccountRepository;
import com.wpits.accountmanagement.service.BillingAccountService;
import com.wpits.accountmanagement.utils.CommonMapper;
import com.wpits.accountmanagement.utils.FieldFilterUtil;
import com.wpits.accountmanagement.utils.IdGenerator;
import com.wpits.accountmanagement.utils.BillingAccountMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implements TMF666 "Operations on BillingAccount" (spec pages 132-142).
 * relatedParty (with role) is mandatory on create, per the spec's Mandatory
 * Attributes table (page 7366). Publishes BillingAccountCreateEvent /
 * BillingAccountStateChangeEvent / BillingAccountAttributeValueChangeEvent /
 * BillingAccountDeleteEvent to Kafka.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BillingAccountServiceImpl implements BillingAccountService {

    private static final String TYPE = "BillingAccount";
    private static final String RESOURCE_NAME = "billingAccount";
    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 200;

    private static final Set<String> DEFAULT_GET_FIELDS = Set.of(
            "id", "href", "name", "description", "accountType", "state", "paymentStatus",
            "ratingType", "creditLimit", "billStructure", "defaultPaymentMethod",
            "financialAccount", "relatedParty", "@type");
    private static final Set<String> DEFAULT_LIST_FIELDS = Set.of("id", "href", "name", "state", "@type");

    private final BillingAccountRepository repository;
    private final BillingAccountMapper mapper;
    private final CommonMapper commonMapper;
    private final FieldFilterUtil fieldFilterUtil;
    private final IdGenerator idGenerator;
    private final AccountEventPublisher eventPublisher;
    private final KafkaTopicsProperties topics;

    @Value("${app.base-url}")
    private String baseUrl;

    @Override
    public BillingAccountDto create(BillingAccountDto dto) {
    	
        log.info("POST /billingAccount - name=[{}]", dto.getName());
        
        validateMandatory(dto);

        if (dto.getId() != null && !dto.getId().isBlank() && repository.existsById(dto.getId())) {
            throw new ConflictException("BillingAccount already exists with id: " + dto.getId(), TYPE, "NA");
        }

        BillingAccount billingAccount = mapper.toEntity(dto);
        String id = nextId();
        billingAccount.setId(id);
        billingAccount.setHref(baseUrl + "/billingAccount/" + id);
        billingAccount.setBaseType(TYPE);
        billingAccount.setType(TYPE);
        billingAccount.setLastUpdate(LocalDateTime.now());
        
        if (billingAccount.getState() == null) {
            billingAccount.setState(AccountStateType.PENDING_ACTIVE);
        }

        BillingAccount saved = repository.save(billingAccount);
        BillingAccountDto result = mapper.toDto(saved);

        eventPublisher.publish(topics.getBillingAccount(), "BillingAccountCreateEvent",
                RESOURCE_NAME, saved.getId(), toEventDto(saved));
        log.info("Created BillingAccount id={}", saved.getId());
        return result;
    }

    private BillingAccountDto toEventDto(BillingAccount account) {
        BillingAccountDto eventDto = mapper.toDto(account);
        List<RelatedPartyRefOrPartyRoleRef> relatedParties = account.getRelatedParty();
        List<RelatedPartyRefOrPartyRoleRefDto> relatedPartyDtos = eventDto.getRelatedParty();
        if (relatedParties == null || relatedPartyDtos == null) {
            return eventDto;
        }

        int count = Math.min(relatedParties.size(), relatedPartyDtos.size());
        for (int i = 0; i < count; i++) {
            RelatedPartyRefOrPartyRoleRef relatedParty = relatedParties.get(i);
            RelatedPartyRefOrPartyRoleRefDto relatedPartyDto = relatedPartyDtos.get(i);
            PartyRefOrPartyRoleRefDto partyDto = relatedPartyDto.getPartyOrPartyRole();
            if (partyDto != null && relatedParty.getPartyOrPartyRole() != null) {
                partyDto.setId(relatedParty.getPartyOrPartyRole().getId());
            }
        }
        return eventDto;
    }

    private String nextId() {
        long next = repository.count() + 1;
        return String.format("BA_%04d", next);
    }

    private void validateMandatory(BillingAccountDto dto) {
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
    	
        log.info("GET /billingAccount/{} - fields=[{}]", id, fields);
        
        BillingAccount entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BillingAccount not found with given Id: " + id, TYPE, "Null"));
        
        return fieldFilterUtil.applyFieldSelection(mapper.toDto(entity), fields, DEFAULT_GET_FIELDS);
    }

    @Override
    public List<JsonNode> list(String fields, Integer offset, Integer limit) {
        int safeLimit = (limit == null || limit <= 0) ? DEFAULT_LIMIT : Math.min(limit, MAX_LIMIT);
        int safeOffset = (offset == null || offset < 0) ? 0 : offset;
        log.info("GET /billingAccount - fields=[{}], offset=[{}], limit=[{}]", fields, safeOffset, safeLimit);

        return repository.findAllWithExactOffset(safeOffset, safeLimit).stream()
                .map(mapper::toDto)
                .map(dto -> fieldFilterUtil.applyFieldSelection(dto, fields, DEFAULT_LIST_FIELDS))
                .toList();
    }

    @Override
    public List<BillingAccountDto> listBillingAccounts(String billCycleSpecificationId) {
        if (billCycleSpecificationId == null || billCycleSpecificationId.isBlank()) {
            throw new BadRequestException("billCycleSpecificationId is mandatory", TYPE, "NA");
        }

        return repository.fetchAllBillingAccountByBillCycleSpecification(billCycleSpecificationId)
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    @Override
    public BillingAccountDto patch(String id, BillingAccountDto patch) {
    	
        log.info("PATCH /billingAccount/{}", id);
        
        BillingAccount entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BillingAccount not found with given Id: " + id, TYPE, "Null"));

        boolean stateChanged = false;

        if (patch != null) {
            if (patch.getName() != null) entity.setName(patch.getName());
            if (patch.getDescription() != null) entity.setDescription(patch.getDescription());
            if (patch.getAccountType() != null) entity.setAccountType(patch.getAccountType());
            if (patch.getPaymentStatus() != null) entity.setPaymentStatus(patch.getPaymentStatus());
            if (patch.getRatingType() != null) entity.setRatingType(patch.getRatingType());
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
        BillingAccount saved = repository.save(entity);
        BillingAccountDto result = mapper.toDto(saved);

        String eventType = stateChanged ? "BillingAccountStateChangeEvent" : "BillingAccountAttributeValueChangeEvent";
        eventPublisher.publish(topics.getBillingAccount(), eventType,
                RESOURCE_NAME, saved.getId(), toEventDto(saved));
        log.info("Patched BillingAccount id={} ({})", saved.getId(), eventType);
        
        return result;
    }

    @Override
    public void delete(String id) {
    	
        log.info("DELETE /billingAccount/{}", id);
        
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("BillingAccount not found with given Id: " + id, TYPE, "Null");
        }
        repository.deleteById(id);
        
        eventPublisher.publish(topics.getBillingAccount(), "BillingAccountDeleteEvent",
                RESOURCE_NAME, id, BillingAccountDto.builder().id(id).type(TYPE).build());
        log.info("Deleted BillingAccount id={}", id);
    }
}
