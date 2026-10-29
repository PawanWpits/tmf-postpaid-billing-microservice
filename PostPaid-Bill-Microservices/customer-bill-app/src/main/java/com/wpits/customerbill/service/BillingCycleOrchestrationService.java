package com.wpits.customerbill.service;

import com.wpits.customerbill.dtos.*;
import com.wpits.customerbill.repositories.BillCycleSpecificationRefRepository;
import com.wpits.customerbill.repositories.CustomerBillRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BillingCycleOrchestrationService {
    private final CustomerBillRepository customerBillRepository;
    private final BillingCycleEventPublisher eventPublisher;
    private final BillCycleSpecificationRefService billCycleSpecificationRefService;
    private final AccountManagementClient accountManagementClient;
/*
    @Transactional
    public boolean process(BillingCycleProcessRequest request) {

        validate(request);

        AccountManagementClient.BillingAccountView account = accountManagementClient.getBillingAccount(request.getBillingAccountId());

        if (request.getBillingCycleSpecificationId() == null || request.getBillingCycleSpecificationId().isBlank()) {
            request.setBillingCycleSpecificationId(specificationId(account));
        }

        if (customerBillRepository.existsForBillingCycle(request.getBillingAccountId(),
                request.getBillingCycleSpecificationId(),
                request.getPeriodStart().minusDays(1).atStartOfDay(),
                request.getPeriodStart().plusDays(1).atStartOfDay(),
                request.getPeriodEnd().atStartOfDay(),
                request.getPeriodEnd().plusDays(1).atStartOfDay())) {
            log.info("[BILLING-CYCLE] Duplicate cycle ignored for account={} period={}",
                    request.getBillingAccountId(), request.getPeriodStart() + " to " + request.getPeriodEnd());
            return false;
        }

        if (account == null || !"postpaid".equalsIgnoreCase(account.getRatingType())
                || (account.getState() != null && !"active".equalsIgnoreCase(account.getState()))) {
            throw new IllegalArgumentException("BillingAccount is not an active postpaid account");
        }
        accountManagementClient.getBillingCycleSpecification(request.getBillingCycleSpecificationId());

        eventPublisher.publish(BillingCycleClosedEvent.builder()
                .eventType("BILLING_CYCLE_CLOSED")
                .billingAccountId(request.getBillingAccountId())
                .billingCycleSpecificationId(request.getBillingCycleSpecificationId())
                .periodStart(request.getPeriodStart())
                .periodEnd(request.getPeriodEnd())
                .build());
        return true;
    }*/

    @Transactional
    public void processScheduled() {

        LocalDate cycleBoundaryDate = LocalDate.now().minusDays(1);

        log.info("[BILLING-CYCLE] Daily completion check for cycle boundary {}", cycleBoundaryDate);

        List<BillingCycleSpecificationView> billingCycleSpecifications = accountManagementClient.getBillingCycleSpecification();
        log.info("BillCycleSpecification count: {}", billingCycleSpecifications.size());

        for (BillingCycleSpecificationView bcs : billingCycleSpecifications) {

            BillingCycleSpecificationView completedSpecification = completedPeriod(bcs, cycleBoundaryDate);
            if (completedSpecification == null) {
                continue;
            }

            LocalDate periodStart = cycleBoundaryDate.minusMonths(1);
            LocalDate periodEnd = cycleBoundaryDate.minusDays(1);
            log.info("[BILLING-CYCLE] Resolved specification={} billing day={} bill period={} to {}",
                    completedSpecification.getId(), cycleBoundaryDate.getDayOfMonth(),
                    periodStart, periodEnd);
            List<AccountManagementClient.BillingAccountView> billingAccounts =
                    accountManagementClient.listBillingAccounts(completedSpecification.getId());

            log.info("[BILLING-CYCLE] Found {} active postpaid accounts for specification={}",
                    billingAccounts.size(), completedSpecification.getId());

            for (AccountManagementClient.BillingAccountView account : billingAccounts) {

                if (account.getId() == null || account.getId().isBlank()) {
                    log.warn("[BILLING-CYCLE] Skipping account without an id for specification={}", completedSpecification.getId());
                    continue;
                }

                if (customerBillRepository.existsForBillingCycle(
                        account.getId(),
                        completedSpecification.getId(),
                        periodStart.minusDays(1).atStartOfDay(),
                        periodStart.plusDays(1).atStartOfDay(),
                        periodEnd.atStartOfDay(),
                        periodEnd.plusDays(1).atStartOfDay())) {
                    log.info("[BILLING-CYCLE] Duplicate cycle ignored for account={} period={}", account.getId(), periodStart + " to " + periodEnd);
                    continue;
                }

                eventPublisher.publish(BillingCycleClosedEvent.builder()
                        .eventType("BILLING_CYCLE_CLOSED")
                        .billingAccountId(account.getId())
                        .billingCycleSpecificationId(completedSpecification.getId())
                        .periodStart(periodStart)
                        .periodEnd(periodEnd)
                        .build());
            }
        }

    }

    private BillingCycleSpecificationView completedPeriod(BillingCycleSpecificationView specification, LocalDate periodEnd) {

        //Monthly
        if (isMonthly(specification)) {

            int billingDay = billingDay(specification);
            log.info("billingDay: {}", billingDay);
            log.info("billingPeriodEnd: {}, ", periodEnd);
            if (periodEnd.getDayOfMonth() == billingDay) {

                return specification;
            }

        }

        log.warn("[BILLING-CYCLE] Unsupported billing period={} frequency={} for specification={}", specification.getBillingPeriod(), specification.getFrequency(), specification.getId());
        return null;
    }

    private boolean isMonthly(BillingCycleSpecificationView specification) {
        return "month".equalsIgnoreCase(specification.getBillingPeriod()) || "monthly".equalsIgnoreCase(specification.getFrequency());
    }

    private int billingDay(BillingCycleSpecificationView specification) {
        try {
            int configuredDay = Integer.parseInt(specification.getBillingPeriod());
            if (configuredDay < 1 || configuredDay > 31) {
                throw new IllegalArgumentException("billingPeriod day must be between 1 and 31");
            }
            return configuredDay;
        } catch (NumberFormatException ex) {
            return YearMonth.from(LocalDate.now()).lengthOfMonth();
        }
    }
}



/*
        //List<BillCycleSpecificationRefDto> references =  billCycleSpecificationRefService.getBillCycleSpecificationRefs();
        List<AccountManagementClient.BillingAccountView> accounts =accountManagementClient.listBillingAccounts();

        List<BillCycleSpecificationRefDto> references = new ArrayList<>();

        for (BillCycleSpecificationRefDto reference : references) {
            try {
                BillingCycleSpecificationView specification =
                        accountManagementClient.getBillingCycleSpecification(*//*reference.getId()*//*);
                BillingPeriod period = completedPeriod(specification, periodEnd);
                if (period == null) {
                    continue;
                }

                for (AccountManagementClient.BillingAccountView account : accounts) {
                    AccountManagementClient.BillingAccountView details =
                                accountManagementClient.getBillingAccount(account.getId());
                    if (!isActivePostpaid(details)) {
                        log.warn("[BILLING-CYCLE] Skipping account={} because it is not an active postpaid account "
                                            + "(state={}, ratingType={})",
                                    account.getId(), details == null ? null : details.getState(),
                                    details == null ? null : details.getRatingType());
                        continue;
                    }
                    if (!usesSpecification(details, specification.getId())) {
                        log.warn("[BILLING-CYCLE] Skipping account={} because its BCS reference does not match {}",
                                    details.getId(), specification.getId());
                        continue;
                    }
                    if (process(new BillingCycleProcessRequest(details.getId(), specification.getId(),
                                period.periodStart(), period.periodEnd()))) {
                        log.info("[BILLING-CYCLE] Published closed event for account={} specification={}",
                                    details.getId(), specification.getId());
                    }
                }
            } catch (RuntimeException ex) {
                log.error("[BILLING-CYCLE] Failed scheduled cycle for specificationRef={}",
                        reference.getId(), ex);
            }*/
    /*    }
    }*/
/*
    private boolean usesSpecification(AccountManagementClient.BillingAccountView account,
                                      String specificationId) {
        try {
            return specificationId.equals(specificationId(account));
        } catch (IllegalArgumentException ex) {
            log.warn("[BILLING-CYCLE] BillingAccount {} has no valid BCS reference", account.getId());
            return false;
        }
    }

    private BillingPeriod completedPeriod(AccountManagementClient.BillingCycleSpecificationView specification,
                                          LocalDate periodEnd) {
        if (isMonthly(specification)) {
            int billingDay = billingDay(specification);
            if (periodEnd.getDayOfMonth() != billingDay) {
                return null;
            }
            LocalDate periodStart = periodEnd.minusMonths(1).plusDays(1);
            return new BillingPeriod(periodStart, periodEnd);
        }
        log.warn("[BILLING-CYCLE] Unsupported billing period={} frequency={} for specification={}",
                specification.getBillingPeriod(), specification.getFrequency(), specification.getId());
        return null;
    }

    private boolean isMonthly(AccountManagementClient.BillingCycleSpecificationView specification) {
        return "month".equalsIgnoreCase(specification.getBillingPeriod())
                || "monthly".equalsIgnoreCase(specification.getFrequency());
    }

    private int billingDay(AccountManagementClient.BillingCycleSpecificationView specification) {
        try {
            int configuredDay = Integer.parseInt(specification.getBillingPeriod());
            if (configuredDay < 1 || configuredDay > 31) {
                throw new IllegalArgumentException("billingPeriod day must be between 1 and 31");
            }
            return configuredDay;
        } catch (NumberFormatException ex) {
            return YearMonth.from(LocalDate.now()).lengthOfMonth();
        }
    }

    private boolean isActivePostpaid(AccountManagementClient.BillingAccountView account) {
        return account != null
                && "postpaid".equalsIgnoreCase(account.getRatingType())
                && (account.getState() == null || "active".equalsIgnoreCase(account.getState()));
    }

    private String specificationId(AccountManagementClient.BillingAccountView account) {
        if (account == null || account.getBillStructure() == null
                || account.getBillStructure().getCycleSpecification() == null
                || account.getBillStructure().getCycleSpecification().getId() == null
                || account.getBillStructure().getCycleSpecification().getId().isBlank()) {
            throw new IllegalArgumentException("BillingAccount has no BillingCycleSpecification reference");
        }
        return account.getBillStructure().getCycleSpecification().getId();
    }

    private void validate(BillingCycleProcessRequest request) {

        if (request == null || request.getBillingAccountId() == null
                || request.getPeriodStart() == null || request.getPeriodEnd() == null
                || request.getPeriodEnd().isBefore(request.getPeriodStart())) {

            throw new IllegalArgumentException("A valid billing account, specification, and billing period are required");
        }
    }

    private record BillingPeriod(LocalDate periodStart, LocalDate periodEnd) {
    }*/
//}
