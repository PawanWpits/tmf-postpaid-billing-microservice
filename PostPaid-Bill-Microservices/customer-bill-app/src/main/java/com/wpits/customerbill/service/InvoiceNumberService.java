package com.wpits.customerbill.service;

import com.wpits.customerbill.repositories.CustomerBillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class InvoiceNumberService {
    private final CustomerBillRepository repository;

    public synchronized String next(LocalDate billDate) {
        int year = billDate.getYear();
        String prefix = "INV-" + year + "-";
        long sequence = repository.countByBillNoStartingWith(prefix) + 1;
        String candidate = prefix + String.format("%06d", sequence);
        while (repository.existsByBillNo(candidate)) {
            candidate = prefix + String.format("%06d", ++sequence);
        }
        return candidate;
    }
}
