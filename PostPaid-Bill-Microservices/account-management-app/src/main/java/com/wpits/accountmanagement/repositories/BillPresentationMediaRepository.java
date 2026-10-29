package com.wpits.accountmanagement.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.wpits.accountmanagement.entities.BillPresentationMedia;

@Repository
public interface BillPresentationMediaRepository extends JpaRepository<BillPresentationMedia, String> {

    boolean existsById(String id);

    @Query(value = "SELECT * FROM bill_presentation_media ORDER BY id ASC LIMIT :limit OFFSET :offset", nativeQuery = true)
    List<BillPresentationMedia> findAllWithExactOffset(@Param("offset") int offset, @Param("limit") int limit);

    @Query(value = "SELECT COUNT(*) FROM bill_presentation_media", nativeQuery = true)
    long countAll();
}
