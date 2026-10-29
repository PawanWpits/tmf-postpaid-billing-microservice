package com.wpits.customerbill.service.impls;

import com.wpits.customerbill.dtos.MoneyDto;
import com.wpits.customerbill.dtos.TaxItemDto;
import com.wpits.customerbill.entities.Money;
import com.wpits.customerbill.entities.TaxItem;
import com.wpits.customerbill.exceptions.BadRequestException;
import com.wpits.customerbill.exceptions.ResourceNotFoundException;
import com.wpits.customerbill.repositories.TaxItemRepository;
import com.wpits.customerbill.service.TaxItemService;
import com.wpits.customerbill.utils.IdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TaxItemServiceImpl implements TaxItemService {
    private static final String TYPE = "TaxItem";
    private final TaxItemRepository repository;
    private final IdGenerator idGenerator;

    @Value("${app.base-url}")
    private String baseUrl;

    @Override
    public TaxItemDto create(TaxItemDto dto) {
        validate(dto);
        String id = nextId();
        /*if (id == null || id.isBlank()) {
            id = idGenerator.generate("TAX");
        } else*/ if (repository.existsById(id)) {
            throw new BadRequestException("TaxItem already exists with id: " + id, TYPE, "NA");
        }
        TaxItem entity = toEntity(dto, id);
        return toDto(repository.save(entity));
    }
    private String nextId() {
        long next = repository.count() + 1;
        return String.format("TA_%04d", next);
    }
    @Override
    @Transactional(readOnly = true)
    public TaxItemDto getById(String id) {
        return toDto(repository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("TaxItem not found with id: " + id, TYPE, "Null")));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaxItemDto> list() {
        return repository.findAll().stream().map(this::toDto).toList();
    }

    @Override
    public TaxItemDto update(String id, TaxItemDto dto) {
        validate(dto);
        TaxItem entity = repository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("TaxItem not found with id: " + id, TYPE, "Null"));
        entity.setTaxCategory(dto.getTaxCategory());
        entity.setTaxRate(dto.getTaxRate());
        entity.setTaxAmount(toMoney(dto.getTaxAmount()));
        entity.setBaseType(dto.getBaseType());
        entity.setSchemaLocation(dto.getSchemaLocation());
        entity.setType(dto.getType() == null ? TYPE : dto.getType());
        return toDto(repository.save(entity));
    }

    @Override
    public void delete(String id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("TaxItem not found with id: " + id, TYPE, "Null");
        }
        repository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public TaxItemDto getBillingTax() {
        return repository.findFirstByOrderByIdAsc().map(this::toDto).orElseThrow(() ->
                new ResourceNotFoundException("No billing TaxItem is configured", TYPE, "Null"));
    }

    private void validate(TaxItemDto dto) {
        if (dto == null || dto.getTaxRate() == null || dto.getTaxRate().signum() < 0) {
            throw new BadRequestException("taxRate must be non-negative", TYPE, "NA");
        }
    }

    private TaxItem toEntity(TaxItemDto dto, String id) {
        return TaxItem.builder().id(id).taxCategory(dto.getTaxCategory()).taxRate(dto.getTaxRate())
                .taxAmount(toMoney(dto.getTaxAmount())).baseType(dto.getBaseType())
                .schemaLocation(dto.getSchemaLocation()).type(dto.getType() == null ? TYPE : dto.getType())
                .build();
    }

    private TaxItemDto toDto(TaxItem entity) {
        return TaxItemDto.builder().id(entity.getId()).taxCategory(entity.getTaxCategory())
                .taxRate(entity.getTaxRate()).taxAmount(toMoneyDto(entity.getTaxAmount()))
                .baseType(entity.getBaseType()).schemaLocation(entity.getSchemaLocation())
                .type(entity.getType()).build();
    }

    private Money toMoney(MoneyDto dto) {
        return dto == null ? null : new Money(dto.getUnit(), dto.getValue());
    }

    private MoneyDto toMoneyDto(Money money) {
        return money == null ? null : MoneyDto.builder().unit(money.getUnit()).value(money.getValue()).build();
    }
}
