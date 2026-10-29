package com.wpits.customerbill.repositories;

import com.wpits.customerbill.entities.CustomerBillOnDemand;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerBillOnDemandRepository extends JpaRepository<CustomerBillOnDemand, String> {
}
