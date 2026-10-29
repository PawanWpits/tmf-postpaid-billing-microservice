package com.wpits.accountmanagement.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.wpits.accountmanagement.entities.BillingCycleSpecification;

@Repository
public interface BillingCycleSpecificationRepository extends JpaRepository<BillingCycleSpecification, String> {

    boolean existsById(String id);

    @Query(value = "SELECT * FROM billing_cycle_specification ORDER BY id ASC LIMIT :limit OFFSET :offset", nativeQuery = true)
    List<BillingCycleSpecification> findAllWithExactOffset(@Param("offset") int offset, @Param("limit") int limit);

    @Query(value = "SELECT COUNT(*) FROM billing_cycle_specification", nativeQuery = true)
    long countAll();

    boolean existsByBillingPeriod(String billingPeriod);
}
