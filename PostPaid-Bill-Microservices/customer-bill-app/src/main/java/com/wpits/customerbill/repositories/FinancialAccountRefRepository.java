package com.wpits.customerbill.repositories;

import com.wpits.customerbill.entities.FinancialAccountRef;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FinancialAccountRefRepository extends JpaRepository<FinancialAccountRef, String> {
}
