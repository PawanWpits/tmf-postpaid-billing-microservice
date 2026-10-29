package com.wpits.customerbill.service.impls;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wpits.customerbill.dtos.BillCycleDto;
import com.wpits.customerbill.dtos.CustomerBillDto;
import com.wpits.customerbill.dtos.CustomerBillItemDto;
import com.wpits.customerbill.dtos.CustomerBillPatchDto;
import com.wpits.customerbill.dtos.MoneyDto;
import com.wpits.customerbill.entities.BillCycle;
import com.wpits.customerbill.entities.CustomerBill;
import com.wpits.customerbill.entities.Money;
import com.wpits.customerbill.enums.CustomerBillStateType;
import com.wpits.customerbill.exceptions.BadRequestException;
import com.wpits.customerbill.exceptions.ResourceNotFoundException;
import com.wpits.customerbill.repositories.BillCycleRepository;
import com.wpits.customerbill.repositories.CustomerBillItemRepository;
import com.wpits.customerbill.repositories.CustomerBillRepository;
import com.wpits.customerbill.service.CustomerBillService;
import com.wpits.customerbill.utils.CustomerBillMapper;
import com.wpits.customerbill.utils.FieldFilterUtil;
import com.wpits.customerbill.utils.IdGenerator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.JsonNode;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CustomerBillServiceImpl implements CustomerBillService {

    private final CustomerBillRepository customerBillRepository;
    private final BillCycleRepository billCycleRepository;
    private final CustomerBillItemRepository customerBillItemRepository;
    private final CustomerBillMapper mapper;
    private final FieldFilterUtil fieldFilterUtil;
    private final IdGenerator idGenerator;

    private static final String CB_TYPE = "CustomerBill";

    private static final int DEFAULT_OFFSET = 0;
    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 200;

    private static final Set<String> DEFAULT_GET_BY_ID_FIELDS = Set.of(
            "id", "href", "billNo", "amountDue", "billDate", "billingPeriod", "category",
            "lastUpdate", "nextBillDate", "paymentDueDate", "remainingAmount", "runType",
            "state", "taxExcludedAmount", "taxIncludedAmount", "billCycle", "billingAccount",
            "financialAccount", "paymentMethod", "appliedPayment", "billDocument", "taxItem",
            "relatedParty", "billItem", "@type"
    );

    private static final Set<String> DEFAULT_LIST_FIELDS = Set.of(
            "id", "href", "billingAccount", "@type"
    );

    @Value("${app.base-url}")
    private String baseUrl;

    @Override
    public CustomerBillDto create(CustomerBillDto dto) {
    	
        log.info("POST /customerBill - billingAccount=[{}]",dto.getBillingAccount() == null ? null : dto.getBillingAccount().getId());

        if (dto.getBillingAccount() == null || dto.getBillingAccount().getId() == null
                || dto.getBillingAccount().getId().isBlank()) {
            throw new BadRequestException("billingAccount.id is mandatory", CB_TYPE, "NA");
        }

        if (dto.getId() != null && !dto.getId().isBlank() && customerBillRepository.existsById(dto.getId())) {
            throw new BadRequestException("CustomerBill already exists", CB_TYPE, "NA");
        }

        CustomerBill customerBill = mapper.toEntity(dto);

        String bill_Id = nextId();
        customerBill.setId(bill_Id);
        customerBill.setHref(baseUrl + "/customerBill/" + bill_Id);
        customerBill.setBaseType(CB_TYPE);
        customerBill.setType(CB_TYPE);
        
        if (customerBill.getState() == null) {
            customerBill.setState(CustomerBillStateType.NEW);
        }

        assignChildIds(customerBill);

        CustomerBill saved = customerBillRepository.save(customerBill);
        log.info("Created CustomerBill id={}", saved.getId());
        return mapper.toDto(saved);
    }
    private String nextId() {
        long next = customerBillRepository.count() + 1;
        return String.format("CB_%04d", next);
    }

    private void assignChildIds(CustomerBill customerBill) {
    	
        if (customerBill.getAppliedPayment() != null) {
        	
            customerBill.getAppliedPayment().forEach(ap -> {
                if (ap.getPayment() != null
                        && (ap.getPayment().getId() == null || ap.getPayment().getId().isBlank())) {
                    ap.getPayment().setId(idGenerator.generate("PAY"));
                }
            });
        }
        
        if (customerBill.getBillDocument() != null) {
            customerBill.getBillDocument().forEach(doc -> {
                if (doc.getId() == null || doc.getId().isBlank()) {
                    doc.setId(idGenerator.generate("DOC"));
                }
            });
        }
        
        if (customerBill.getTaxItem() != null) {
            customerBill.getTaxItem().forEach(t -> {
                if (t.getId() == null || t.getId().isBlank()) {
                    t.setId(idGenerator.generate("TI"));
                }
            });
        }
        if (customerBill.getRelatedParty() != null) {
            customerBill.getRelatedParty().forEach(rp -> {
                if (rp.getPartyOrPartyRole() != null
                        && (rp.getPartyOrPartyRole().getId() == null || rp.getPartyOrPartyRole().getId().isBlank())) {
                    rp.getPartyOrPartyRole().setId(idGenerator.generate("RP"));
                }
            });
        }
    }

    @Override
    public JsonNode getById(String id, String fields) {
        log.info("GET /customerBill/{} - fields=[{}]", id, fields);

        CustomerBill entity = customerBillRepository.findById(id).orElseThrow(() -> {
            log.warn("CustomerBill not found with given Id: {}", id);
            return new ResourceNotFoundException("CustomerBill not found with given Id: " + id, CB_TYPE, "Null");
        });

        CustomerBillDto dto = mapper.toDto(entity);
        return fieldFilterUtil.applyFieldSelection(dto, fields, DEFAULT_GET_BY_ID_FIELDS);
    }

    @Override
    public List<JsonNode> list(String fields, Integer offset, Integer limit) {
    	
        int safeLimit = (limit == null || limit <= 0) ? DEFAULT_LIMIT : Math.min(limit, MAX_LIMIT);
        int safeOffset = (offset == null || offset < 0) ? DEFAULT_OFFSET : offset;

        log.info("GET /customerBill - fields=[{}], offset=[{}], limit=[{}]", fields, safeOffset, safeLimit);

        List<CustomerBill> records = customerBillRepository.findAllWithExactOffset(safeOffset, safeLimit);

        return records.stream()
                .map(mapper::toDto)
                .map(dto -> fieldFilterUtil.applyFieldSelection(dto, fields, DEFAULT_LIST_FIELDS))
                .toList();
    }

    @Override
    public List<CustomerBillDto> getAllByBillDate(LocalDate date) {
        //log.info("Fetch customers bill by date: {}", date);

        LocalDateTime startOfDay = date.atTime(LocalTime.MIN);
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
        log.info("Fetch customers bill by date: {} - {}", startOfDay,endOfDay);
        List<CustomerBill> bills = customerBillRepository
                .findByBillDateBetween(startOfDay, endOfDay);
        if (bills.isEmpty()) {
            return List.of();
        }

        List<String> billIds = bills.stream()
                .map(CustomerBill::getId)
                .toList();
        Map<String, List<CustomerBillItemDto>> billItemsByBillId =
                customerBillItemRepository.findByCustomerBill_IdIn(billIds).stream()
                        .collect(Collectors.groupingBy(
                                item -> item.getCustomerBill().getId(),
                                Collectors.mapping(item -> CustomerBillItemDto.builder()
                                                .id(item.getId())
                                                .description(item.getDescription())
                                                .planName(item.getPlanName())
                                                .amount(item.getAmount())
                                                .unit(item.getUnit())
                                                .build(),
                                        Collectors.toList())));

        List<CustomerBillDto> billDtos = bills.stream()
                .map(bill -> {
                    CustomerBillDto dto = mapper.toDto(bill);
                    dto.setBillItem(billItemsByBillId.getOrDefault(dto.getId(), List.of()));
                    return dto;
                })
                .toList();
        //billDtos.forEach(System.out::println);
        log.info("billDtos: {}", billDtos.size());
        return billDtos;
    }

    @Override
    public CustomerBillDto patch(String id, CustomerBillPatchDto patch) {
        log.info("PATCH /customerBill/{} - state=[{}]", id, patch == null ? null : patch.getState());

        CustomerBill customerBill = customerBillRepository.findById(id).orElseThrow(() -> {
            log.warn("CustomerBill not found with given Id: {}", id);
            return new ResourceNotFoundException("CustomerBill not found with given Id: " + id, CB_TYPE, "Null");
        });

        if (patch == null) {
            return mapper.toDto(customerBill);
        }

        if (patch.getState() != null) {
            customerBill.setState(CustomerBillStateType.fromValue(patch.getState()));
        }

        if (patch.getRemainingAmount() != null) {
            if (patch.getRemainingAmount().getUnit() == null || patch.getRemainingAmount().getUnit().isBlank()
                    || patch.getRemainingAmount().getValue() == null
                    || patch.getRemainingAmount().getValue().compareTo(BigDecimal.ZERO) < 0) {
                throw new BadRequestException(
                        "remainingAmount must include a currency unit and a non-negative value", CB_TYPE, "NA");
            }
            customerBill.setRemainingAmount(new Money(
                    patch.getRemainingAmount().getUnit(), patch.getRemainingAmount().getValue()));
        }

        if (patch.getBillCycle() != null) {
        	
            BillCycleDto bc = patch.getBillCycle();
            
            if (bc.getId() == null || bc.getId().isBlank()) {
                throw new BadRequestException("billCycle.id is mandatory", CB_TYPE, "NA");
            }
            BillCycle billCycle = customerBill.getBillCycle() != null ? customerBill.getBillCycle() : new BillCycle();
            
            billCycle.setId(bc.getId());
            billCycle.setHref(bc.getHref());
            billCycle.setName(bc.getName());
            //billCycle.setReferredType(bc.getReferredType() == null ? "BillCycle" : bc.getReferredType());
            billCycle.setType(bc.getType() == null ? "BillCycleRef" : bc.getType());
            billCycle.setBaseType(bc.getBaseType());
            billCycle.setSchemaLocation(bc.getSchemaLocation());
            customerBill.setBillCycle(billCycle);
        }

        customerBill.setLastUpdate(java.time.LocalDateTime.now());

        CustomerBill saved = customerBillRepository.save(customerBill);
        log.info("Patched CustomerBill id={}", saved.getId());
        return mapper.toDto(saved);
    }

    @Override
    public CustomerBillDto patchRemainingAmt(String id, MoneyDto remainingAmt) {
        log.info("PATCH /customerBill/{}/remainingAmount", id);

        if (remainingAmt == null || remainingAmt.getUnit() == null || remainingAmt.getUnit().isBlank()
                || remainingAmt.getValue() == null || remainingAmt.getValue().signum() < 0) {
            throw new BadRequestException(
                    "remainingAmount must include a currency unit and a non-negative value", CB_TYPE, "NA");
        }

        CustomerBill customerBill = customerBillRepository.findById(id).orElseThrow(() -> {
            log.warn("CustomerBill not found with given Id: {}", id);
            return new ResourceNotFoundException("CustomerBill not found with given Id: " + id, CB_TYPE, "Null");
        });

        customerBill.setRemainingAmount(new com.wpits.customerbill.entities.Money(remainingAmt.getUnit(), remainingAmt.getValue()));
        customerBill.setLastUpdate(LocalDateTime.now());

        CustomerBill saved = customerBillRepository.save(customerBill);
        log.info("Patched remainingAmount for CustomerBill id={}", saved.getId());
        return mapper.toDto(saved);
    }

    @Override
    public void delete(String id) {
        if (!customerBillRepository.existsById(id)) {
            throw new ResourceNotFoundException("CustomerBill not found with given Id: " + id, CB_TYPE, "Null");
        }
        customerBillRepository.deleteById(id);
        log.info("Deleted CustomerBill id={}", id);
    }
}
