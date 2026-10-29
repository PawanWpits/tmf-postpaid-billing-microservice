package com.wpits.customerbill.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.wpits.customerbill.entities.BillCycle;

@Repository
public interface BillCycleRepository extends JpaRepository<BillCycle, String> {

	//Optional<BillCycle> findByBillCycleCode(String id);

	boolean existsById(String id);

	@Query(value = "SELECT * FROM bill_cycle ORDER BY id ASC LIMIT :limit OFFSET :offset", nativeQuery = true)
	List<BillCycle> findAllWithExactOffset(@Param("offset") int offset, @Param("limit") int limit);

	@Query(value = "SELECT COUNT(*) FROM bill_cycle", nativeQuery = true)
	long countAll();

}
