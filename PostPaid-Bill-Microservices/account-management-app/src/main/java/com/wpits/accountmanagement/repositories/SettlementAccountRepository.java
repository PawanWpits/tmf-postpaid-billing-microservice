package com.wpits.accountmanagement.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.wpits.accountmanagement.entities.SettlementAccount;

@Repository
public interface SettlementAccountRepository extends JpaRepository<SettlementAccount, String> {

    boolean existsById(String id);

    @Query(value = "SELECT * FROM settlement_account ORDER BY id ASC LIMIT :limit OFFSET :offset", nativeQuery = true)
    List<SettlementAccount> findAllWithExactOffset(@Param("offset") int offset, @Param("limit") int limit);

    @Query(value = "SELECT COUNT(*) FROM settlement_account", nativeQuery = true)
    long countAll();
}
