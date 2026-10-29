package com.wpits.accountmanagement.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.wpits.accountmanagement.entities.FinancialAccount;

@Repository
public interface FinancialAccountRepository extends JpaRepository<FinancialAccount, String> {

    boolean existsById(String id);

    @Query(value = "SELECT * FROM financial_account ORDER BY id ASC LIMIT :limit OFFSET :offset", nativeQuery = true)
    List<FinancialAccount> findAllWithExactOffset(@Param("offset") int offset, @Param("limit") int limit);

    @Query(value = "SELECT COUNT(*) FROM financial_account", nativeQuery = true)
    long countAll();
}
