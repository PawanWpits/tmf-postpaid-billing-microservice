package com.wpits.customerbill.repositories;

import com.wpits.customerbill.entities.CustomerBill;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface CustomerBillRepository extends JpaRepository<CustomerBill, String> {

    boolean existsById(String id);
    boolean existsByBillNo(String billNo);
    long countByBillNoStartingWith(String prefix);

    @Query("""
            select count(b) > 0 from CustomerBill b
            where b.billingAccount.id = :billingAccountId
              and b.billCycle.billCycleSpecification.id = :billingCycleSpecificationId
              and b.billingPeriod.startDateTime >= :periodStartLower
              and b.billingPeriod.startDateTime < :periodStartUpper
              and b.billingPeriod.endDateTime >= :periodEndLower
              and b.billingPeriod.endDateTime < :periodEndUpper
            """)
    boolean existsForBillingCycle(@Param("billingAccountId") String billingAccountId,
                                  @Param("billingCycleSpecificationId") String billingCycleSpecificationId,
                                  @Param("periodStartLower") LocalDateTime periodStartLower,
                                  @Param("periodStartUpper") LocalDateTime periodStartUpper,
                                  @Param("periodEndLower") LocalDateTime periodEndLower,
                                  @Param("periodEndUpper") LocalDateTime periodEndUpper);

    @Query(value = "SELECT * FROM customer_bill ORDER BY id ASC LIMIT :limit OFFSET :offset", nativeQuery = true)
    List<CustomerBill> findAllWithExactOffset(@Param("offset") int offset, @Param("limit") int limit);

    List<CustomerBill> findByBillDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    Optional<CustomerBill> findFirstByBillingAccount_IdOrderByBillDateDesc(String billingAccountId);

    boolean existsByBillingAccount_IdAndCategoryAndBillingPeriod_StartDateTimeGreaterThanEqualAndBillingPeriod_StartDateTimeLessThan(
            String billingAccountId,
            String category,
            LocalDateTime periodStartInclusive,
            LocalDateTime periodStartExclusive);

    @Query(value = "SELECT COUNT(*) FROM customer_bill", nativeQuery = true)
    long countAll();
}
