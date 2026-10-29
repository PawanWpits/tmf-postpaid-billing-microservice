package com.wpits.accountmanagement.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.wpits.accountmanagement.entities.BillingAccount;

@Repository
public interface BillingAccountRepository extends JpaRepository<BillingAccount, String> {

    boolean existsById(String id);

    @Query(value = "SELECT * FROM billing_account ORDER BY id ASC LIMIT :limit OFFSET :offset", nativeQuery = true)
    List<BillingAccount> findAllWithExactOffset(@Param("offset") int offset, @Param("limit") int limit);

    @Query(value = "SELECT COUNT(*) FROM billing_account", nativeQuery = true)
    long countAll();

    @Query(value = """
            SELECT *
            FROM billing_account
            WHERE cycle_spec_id = :cycle
              AND state = 'ACTIVE'
              AND rating_type = 'postpaid'
            """, nativeQuery = true)
    List<BillingAccount> fetchAllBillingAccountByBillCycleSpecification(@Param("cycle") String billCycleSpecificationId);
}
