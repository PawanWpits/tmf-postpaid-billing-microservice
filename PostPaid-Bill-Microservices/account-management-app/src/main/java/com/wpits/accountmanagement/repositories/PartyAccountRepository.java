package com.wpits.accountmanagement.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.wpits.accountmanagement.entities.PartyAccount;

@Repository
public interface PartyAccountRepository extends JpaRepository<PartyAccount, String> {

    boolean existsById(String id);

    @Query(value = "SELECT * FROM party_account ORDER BY id ASC LIMIT :limit OFFSET :offset", nativeQuery = true)
    List<PartyAccount> findAllWithExactOffset(@Param("offset") int offset, @Param("limit") int limit);

    @Query(value = "SELECT COUNT(*) FROM party_account", nativeQuery = true)
    long countAll();
}
