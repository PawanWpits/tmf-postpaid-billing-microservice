package com.wpits;

import com.wpits.config.ModelMapperConfig;
import com.wpits.dtos.MoneyDto;
import com.wpits.dtos.PaymentDto;
import com.wpits.dtos.PaymentItemDto;
import com.wpits.dtos.PaymentMethodRefOrValueDto;
import com.wpits.entities.Payment;
import com.wpits.entities.PaymentItem;
import com.wpits.entities.PaymentMethodRefOrValue;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaymentDtoMappingTest {

    private final ModelMapper modelMapper = new ModelMapperConfig().modelMapper();

    @Test
    void mapsPaymentDtoFieldsAndNestedTypesToEntities() {
        PaymentDto dto = new PaymentDto();
        dto.setName("Payment");
        dto.setAmount(new MoneyDto("USD", new BigDecimal("12.50")));

        PaymentMethodRefOrValueDto paymentMethod = new PaymentMethodRefOrValueDto();
        paymentMethod.setId("method-1");
        paymentMethod.setPreferred(true);
        dto.setPaymentMethod(paymentMethod);

        PaymentItemDto paymentItem = new PaymentItemDto();
        paymentItem.setId("item-1");
        paymentItem.setAmount(new MoneyDto("USD", new BigDecimal("10.00")));
        dto.setPaymentItem(List.of(paymentItem));

        Payment entity = modelMapper.map(dto, Payment.class);
        PaymentMethodRefOrValue mappedMethod = entity.getPaymentMethod();
        PaymentItem mappedItem = modelMapper.map(paymentItem, PaymentItem.class);

        assertEquals("Payment", entity.getName());
        assertEquals(new BigDecimal("12.50"), entity.getAmount().getValue());
        assertTrue(mappedMethod.isPreferred());
        assertEquals("method-1", mappedMethod.getId());
        assertEquals(new BigDecimal("10.00"), mappedItem.getAmount().getValue());
    }

}
