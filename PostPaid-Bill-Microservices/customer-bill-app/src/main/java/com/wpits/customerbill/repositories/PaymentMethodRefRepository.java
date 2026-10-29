package com.wpits.customerbill.repositories;

import com.wpits.customerbill.entities.PaymentMethodRef;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentMethodRefRepository extends JpaRepository<PaymentMethodRef, String> {
}
