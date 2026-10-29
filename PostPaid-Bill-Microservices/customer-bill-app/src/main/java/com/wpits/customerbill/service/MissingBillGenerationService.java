package com.wpits.customerbill.service;

import com.wpits.customerbill.dtos.BillingCycleClosedEvent;
import com.wpits.customerbill.dtos.BillingCycleSpecificationView;
import com.wpits.customerbill.exceptions.BadRequestException;
import com.wpits.customerbill.exceptions.ResourceNotFoundException;
import com.wpits.customerbill.repositories.CustomerBillRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MissingBillGenerationService {

    private final AccountManagementClient accountManagementClient;
    private final CustomerBillRepository customerBillRepository;
    private final BillingCycleEventPublisher eventPublisher;

    @Transactional
    public MissingBillRecoveryResult reprocess(String billingCycleSpecificationId) {
        log.info("[MISSING-BILL] Starting missing-bill recovery for specification={}",
                billingCycleSpecificationId);

        if (billingCycleSpecificationId == null || billingCycleSpecificationId.isBlank()) {
            throw new BadRequestException("billingCycleSpecificationId is required to reprocess missing bills","MissingBillRecovery", "NA");
        }

        BillingCycleSpecificationView specification = accountManagementClient
                .getBillingCycleSpecification()
                .stream()
                .filter(candidate -> billingCycleSpecificationId.equals(candidate.getId()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "BillingCycleSpecification '" + billingCycleSpecificationId
                                + "' was not found in Account-management-app",
                        "BillingCycleSpecification", "NA"));

        LocalDate cycleBoundaryDate = latestCompletedPeriodEnd(specification, LocalDate.now().minusDays(1));
        LocalDate periodStart = cycleBoundaryDate.minusMonths(1);
        LocalDate periodEnd = cycleBoundaryDate.minusDays(1);
        log.info("[MISSING-BILL] Recovery period determined for specification={}: {} to {}",
                billingCycleSpecificationId, periodStart, periodEnd);

        List<AccountManagementClient.BillingAccountView> accounts =
                accountManagementClient.listBillingAccounts(billingCycleSpecificationId);
        if (accounts == null) {
            throw new AccountManagementClient.AccountManagementException(
                    "TMF666 returned no billing-account response for BillingCycleSpecification '"
                            + billingCycleSpecificationId + "'");
        }
        log.info("[MISSING-BILL] Found {} eligible billing accounts for specification={}",
                accounts.size(), billingCycleSpecificationId);

        int alreadyBilled = 0;
        int eventsPublished = 0;
        for (AccountManagementClient.BillingAccountView account : accounts) {
            if (account.getId() == null || account.getId().isBlank()) {
                log.warn("[MISSING-BILL] Skipping billing account without an id for specification={}",
                        billingCycleSpecificationId);
                continue;
            }
            if (customerBillRepository.existsForBillingCycle(
                    account.getId(),
                    billingCycleSpecificationId,
                    periodStart.minusDays(1).atStartOfDay(),
                    periodStart.plusDays(1).atStartOfDay(),
                    periodEnd.atStartOfDay(),
                    periodEnd.plusDays(1).atStartOfDay())) {
                alreadyBilled++;
                log.info("[MISSING-BILL] Skipping account={} because the bill already exists for period {} to {}",
                    account.getId(), periodStart, periodEnd);
                continue;
            }

            eventPublisher.publish(BillingCycleClosedEvent.builder()
                    .eventType("BILLING_CYCLE_CLOSED")
                    .billingAccountId(account.getId())
                    .billingCycleSpecificationId(billingCycleSpecificationId)
                    .periodStart(periodStart)
                    .periodEnd(periodEnd)
                    .build());
            eventsPublished++;
            log.info("[MISSING-BILL] Published BILLING_CYCLE_CLOSED for account={} specification={} period={} to {}",
                    account.getId(), billingCycleSpecificationId, periodStart, periodEnd);
        }

        MissingBillRecoveryResult result = new MissingBillRecoveryResult(
                billingCycleSpecificationId, periodStart, periodEnd, accounts.size(),
                alreadyBilled, eventsPublished);
        log.info("[MISSING-BILL] Recovery completed for specification={}: accountsFound={}, "
                        + "alreadyBilled={}, eventsPublished={}",
                billingCycleSpecificationId, result.accountsFound(), result.alreadyBilled(),
                result.eventsPublished());
        return result;
    }

    private LocalDate latestCompletedPeriodEnd(BillingCycleSpecificationView specification,
                                               LocalDate candidateDate) {
        if (specification.getBillingPeriod() == null
                || specification.getBillingPeriod().isBlank()) {
            throw new BadRequestException(
                    "BillingCycleSpecification '" + specification.getId()
                            + "' has no numeric billingPeriod day",
                    "BillingCycleSpecification", "NA");
        }

        int configuredDay;
        try {
            configuredDay = Integer.parseInt(specification.getBillingPeriod());
        } catch (NumberFormatException ex) {
            throw new BadRequestException(
                    "BillingCycleSpecification '" + specification.getId()
                            + "' has invalid billingPeriod '" + specification.getBillingPeriod()
                            + "'; expected a day between 1 and 31",
                    "BillingCycleSpecification", "NA");
        }
        if (configuredDay < 1 || configuredDay > 31) {
            throw new BadRequestException(
                    "BillingCycleSpecification '" + specification.getId()
                            + "' has billingPeriod '" + configuredDay
                            + "'; expected a day between 1 and 31",
                    "BillingCycleSpecification", "NA");
        }

        YearMonth month = YearMonth.from(candidateDate);
        LocalDate end = month.atDay(Math.min(configuredDay, month.lengthOfMonth()));
        if (end.isAfter(candidateDate)) {
            month = month.minusMonths(1);
            end = month.atDay(Math.min(configuredDay, month.lengthOfMonth()));
        }
        return end;
    }

    public record MissingBillRecoveryResult(
            String billingCycleSpecificationId,
            LocalDate periodStart,
            LocalDate periodEnd,
            int accountsFound,
            int alreadyBilled,
            int eventsPublished) {
    }
}
