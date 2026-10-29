package com.wpits.service;

import com.wpits.dtos.PaymentDto;

import java.util.List;

public interface PaymentService {

    PaymentDto create(PaymentDto paymentDto);

    PaymentDto getById(String id);

    List<PaymentDto> getAll();

    PaymentDto update(String id, PaymentDto paymentDto);

    void delete(String id);
}
