package com.wpits.customerbill.repositories;

import com.wpits.customerbill.entities.BillingAccountRef;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BillingAccountRefRepository extends JpaRepository<BillingAccountRef, String> {
}
