package com.wpits.customerbill.repositories;

import com.wpits.customerbill.entities.CustomerBillItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CustomerBillItemRepository extends JpaRepository<CustomerBillItem, String> {

    List<CustomerBillItem> findByCustomerBill_IdIn(List<String> customerBillIds);
}
