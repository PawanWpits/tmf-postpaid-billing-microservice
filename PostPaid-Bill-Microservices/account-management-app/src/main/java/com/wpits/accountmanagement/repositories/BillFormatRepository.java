package com.wpits.accountmanagement.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.wpits.accountmanagement.entities.BillFormat;

@Repository
public interface BillFormatRepository extends JpaRepository<BillFormat, String> {

    boolean existsById(String id);

    @Query(value = "SELECT * FROM bill_format ORDER BY id ASC LIMIT :limit OFFSET :offset", nativeQuery = true)
    List<BillFormat> findAllWithExactOffset(@Param("offset") int offset, @Param("limit") int limit);

    @Query(value = "SELECT COUNT(*) FROM bill_format", nativeQuery = true)
    long countAll();
}
