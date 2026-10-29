package com.wpits.repositories;

import com.wpits.entities.PaymentItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentItemRepository extends JpaRepository<PaymentItem, String> {
}
