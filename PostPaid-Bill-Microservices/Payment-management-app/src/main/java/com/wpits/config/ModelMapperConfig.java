package com.wpits.config;

import com.wpits.dtos.PaymentDto;
import com.wpits.entities.Payment;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.typeMap(PaymentDto.class, Payment.class)
                .addMappings(mapping -> mapping.skip(Payment::setPaymentItem));
        return modelMapper;
    }
}
