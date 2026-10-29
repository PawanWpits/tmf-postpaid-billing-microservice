package com.wpits.customerbill.repositories;

import com.wpits.customerbill.entities.TaxItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TaxItemRepository extends JpaRepository<TaxItem, String> {
    Optional<TaxItem> findFirstByOrderByIdAsc();
}
