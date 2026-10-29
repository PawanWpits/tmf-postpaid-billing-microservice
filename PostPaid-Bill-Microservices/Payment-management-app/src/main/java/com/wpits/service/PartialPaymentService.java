package com.wpits.service;

import com.wpits.dtos.PartialPaymentRequest;

public interface PartialPaymentService {

    String processPayment(PartialPaymentRequest request);
}
