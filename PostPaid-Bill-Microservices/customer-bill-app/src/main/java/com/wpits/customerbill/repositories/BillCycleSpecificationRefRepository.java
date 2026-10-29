package com.wpits.customerbill.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.wpits.customerbill.entities.BillCycleSpecificationRef;

public interface BillCycleSpecificationRefRepository extends JpaRepository<BillCycleSpecificationRef, String>{

	boolean existsById(String id);

}
