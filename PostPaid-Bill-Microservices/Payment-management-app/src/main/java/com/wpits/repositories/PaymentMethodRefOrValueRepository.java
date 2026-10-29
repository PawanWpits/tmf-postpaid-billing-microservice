package com.wpits.repositories;

import com.wpits.entities.PaymentMethodRefOrValue;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentMethodRefOrValueRepository extends JpaRepository<PaymentMethodRefOrValue, String> {
}
