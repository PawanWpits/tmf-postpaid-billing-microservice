package com.wpits.customerbill.service.impls;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import com.wpits.customerbill.dtos.BillCycleDto;
import com.wpits.customerbill.entities.BillCycle;
import com.wpits.customerbill.exceptions.BadRequestException;
import com.wpits.customerbill.exceptions.ResourceNotFoundException;
import com.wpits.customerbill.repositories.BillCycleRepository;
import com.wpits.customerbill.repositories.BillCycleSpecificationRefRepository;
import com.wpits.customerbill.service.BillCycleService;
import com.wpits.customerbill.utils.BillCycleMapper;
import com.wpits.customerbill.utils.FieldFilterUtil;
import com.wpits.customerbill.utils.PaginationUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.JsonNode;

@Service
@RequiredArgsConstructor
@Slf4j
public class BillCycleServiceImpl implements BillCycleService{

	private final BillCycleRepository billCycleRepository;
	private final BillCycleSpecificationRefRepository billCycleSpecificationRefRepository;
	private final BillCycleMapper mapper;
	private final FieldFilterUtil fieldFilterUtil;
	
	private static final String BC_TYPE="BillCycle";
	private static final String BCS_TYPE="BillCycleSpecificationRef";
	private static final String TYPE="BillCycleSpecification";
    private static final int DEFAULT_OFFSET = 0;
    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 200;
    private static final Set<String> DEFAULT_GET_BY_ID_FIELDS = Set.of(
            "id",
            "href",
            "name",
            "description",
            "billingDate",
            "billingPeriod",
            "chargeDate",
            "creditDate",
            "mailingDate",
            "paymentDueDate",
            "billCycleSpecification",
            "validFor",
            "@type"
    );
  
    private static final Set<String> DEFAULT_LIST_FIELDS = Set.of(
            "id",
            "href",
            "billingDate",
            "@type"
    );
	
	@Value("${app.base-url}")
	private String href;
	
	@Override
	public BillCycleDto create(BillCycleDto dto) {
		
		 log.info("POST /billCycle/{} - fields=[{}]",dto);
		
		if(billCycleRepository.existsById(dto.getId())){
            throw new BadRequestException("BillCycle already exists",BC_TYPE,"NA");
        }
		
		if(!billCycleSpecificationRefRepository.existsById(dto.getBillCycleSpecification().getId())){
            throw new ResourceNotFoundException(
            		"BillCycleSpecificationRef not found with given Id: "
            		+ dto.getBillCycleSpecification().getId(), BCS_TYPE, "Null");
        }
		
		
		BillCycle billCycle = mapper.toEntity(dto);
		
		String billCycleId = dto.getId() == null || billCycle.getId().isBlank() ? nextBillCycleId() : dto.getId();
		billCycle.setHref(href+"/"+"billcycle"+"/"+billCycleId);
		
		billCycle.setBaseType(BC_TYPE);
		billCycle.setType(BC_TYPE);
		billCycle.setSchemaLocation("Implementing soon...");
		billCycle.setBillingPeriod(datetoMonthConversion(dto.getBillingPeriod()));
		
		String billCycleSpecificationId = dto.getBillCycleSpecification().getId() == null || dto.getBillCycleSpecification().getId().isBlank() ? nextBillCycleSpecificationId() : dto.getBillCycleSpecification().getId();
		billCycle.getBillCycleSpecification().setHref(href+"/"+"billCycleSpecification"+"/"+billCycleSpecificationId);
		billCycle.getBillCycleSpecification().setReferredType(TYPE);
		billCycle.getBillCycleSpecification().setType(BCS_TYPE);
		billCycle.getBillCycleSpecification().setSchemaLocation("Implementing soon...");
		billCycle.getBillCycleSpecification().setBaseType(BC_TYPE);
		
		BillCycle savedBillCycle = billCycleRepository.save(billCycle);
		
		
		return mapper.toDto(savedBillCycle);
	}
	
	private String datetoMonthConversion(String billingPeriod) {

		// Convert to LocalDate (appending "-01" for the first of the month)
		LocalDate localDate = LocalDate.parse(billingPeriod + "-01", DateTimeFormatter.ISO_LOCAL_DATE);
		return localDate.getMonth().toString();

	}

	private String nextBillCycleId() {
        long next = billCycleRepository.count() + 1;
        return String.format("BC_%04d", next);
	}
    
    private String nextBillCycleSpecificationId() {
        long next = billCycleRepository.count() + 1;
        return String.format("BCSPEC_%01d", next);
	}
	
	@Override
	public BillCycleDto update(Long id, BillCycleDto dto) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public JsonNode getById(String id, String fields) {

        log.info("GET /billCycle/{} - fields=[{}]", id, fields);
        
		BillCycle billCycle = billCycleRepository.findById(id).orElseThrow(() -> {
			log.warn("Bill cycle not found with given Id: {}",id);
			return new ResourceNotFoundException("Bill cycle not found with given Id: "+id, BC_TYPE,"Null");
		});
		
		BillCycleDto dto = mapper.toDto(billCycle);

		return fieldFilterUtil.applyFieldSelection(dto,fields,DEFAULT_GET_BY_ID_FIELDS);
	}
	@Override
	public List<JsonNode> list(String fields, Integer offset, Integer limit) {
        log.info("GET /billCycle - fields=[{}], offset=[{}], limit=[{}]", fields, offset, limit);
        
        int cycleLimit = (limit == null || limit <= 0) ? DEFAULT_LIMIT : Math.min(limit, MAX_LIMIT);
        int cycleOffset = (offset == null || offset < 0) ? DEFAULT_OFFSET : offset;

        log.info("GET /billCycle - fields=[{}], offset=[{}], limit=[{}]", fields, offset, limit);

        //int pageNumber = safeOffset / safeLimit;
        
        log.info("Listing BillCycle entities via native query - fields=[{}], offset=[{}], limit=[{}]",
                fields, cycleOffset, cycleLimit);

        List<BillCycle> records = billCycleRepository.findAllWithExactOffset(cycleOffset, cycleLimit);

        log.debug("BillCycle native offset query returned {} record(s) (offset=[{}], limit=[{}], total=[{}])",
                records.size(), cycleOffset, cycleLimit, records.size());

        return records.stream()
                .map(mapper::toDto)
                .map(dto -> fieldFilterUtil.applyFieldSelection(dto,fields,DEFAULT_LIST_FIELDS)).toList();
	}
	@Override
	public void delete(String id) {
		// TODO Auto-generated method stub
		
	}



}
