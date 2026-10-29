package com.wpits.customerbill.service;

import com.wpits.customerbill.billing.*;
import com.wpits.customerbill.dtos.BillingCycleClosedEvent;
import com.wpits.customerbill.dtos.BillingCycleSpecificationView;
import com.wpits.customerbill.dtos.PartyRefOrPartyRoleRefDto;
import com.wpits.customerbill.dtos.RelatedPartyRefOrPartyRoleRefDto;
import com.wpits.customerbill.entities.*;
import com.wpits.customerbill.enums.CustomerBillRunType;
import com.wpits.customerbill.enums.CustomerBillStateType;
import com.wpits.customerbill.exceptions.ConflictException;
import com.wpits.customerbill.repositories.*;
import com.wpits.customerbill.utils.IdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerBillGenerationService {
    private final CustomerBillRepository billRepository;
    private final CustomerBillItemRepository customerBillItemRepository;
    private final TaxItemRepository taxItemRepository;
    private final BillCycleRepository billCycleRepository;
    private final BillCycleSpecificationRefRepository specificationRefRepository;
    private final BillingInputProvider inputProvider;
    private final BillingCalculationService calculationService;
    private final AccountManagementClient accountManagementClient;
    private final InvoiceNumberService invoiceNumberService;
    private final IdGenerator idGenerator;
    private final PartyRefOrPartyRoleRefRepository partyRefRepository;

    @Value("${app.base-url}")
    private String baseUrl;

    @Transactional
    public void generate(BillingCycleClosedEvent event) {

        log.info("[BILL-GENERATION] Received on-cycle event account={} specification={} period={} to {}",event.getBillingAccountId(), event.getBillingCycleSpecificationId(),
                event.getPeriodStart(), event.getPeriodEnd());

        if (billRepository.existsForBillingCycle(event.getBillingAccountId(),
                event.getBillingCycleSpecificationId(),
                event.getPeriodStart().minusDays(1).atStartOfDay(),
                event.getPeriodStart().plusDays(1).atStartOfDay(),
                event.getPeriodEnd().atStartOfDay(),
                event.getPeriodEnd().plusDays(1).atStartOfDay())) {

            log.info("[BILL-GENERATION] Duplicate event ignored for account={}", event.getBillingAccountId());
            return;
        }

        AccountManagementClient.BillingAccountView account = accountManagementClient.getBillingAccount(event.getBillingAccountId());
        log.info("[BILL-GENERATION] Loaded account={} state={} ratingType={}", account.getId(), account.getState(), account.getRatingType());

        BillingCycleSpecificationView specification = accountManagementClient.getBillingCycleSpecification()
                .stream()
                .filter(candidate -> event.getBillingCycleSpecificationId().equals(candidate.getId()))
                .findFirst()
                .orElseThrow(() -> new AccountManagementClient.AccountManagementException("BillingCycleSpecification not found in TMF666: " + event.getBillingCycleSpecificationId()));

        log.info("[BILL-GENERATION] Using specification={} paymentDueDateOffset={}", specification.getId(), specification.getPaymentDueDateOffset());
        BillingInput input = inputProvider.getBillingInput(event.getBillingAccountId(), event.getPeriodStart(), event.getPeriodEnd());

        BillCalculation calculation = calculationService.calculate(input);
        log.info("[BILL-GENERATION] Calculated on-cycle charges account={} currency={} subtotal={} tax={} amountDue={}",event.getBillingAccountId(), input.currency(), calculation.subtotal(),calculation.tax(), calculation.amountDue());

        createBill(account, specification, event.getPeriodStart(), event.getPeriodEnd(),CustomerBillRunType.ON_CYCLE, input, calculation, false);
    }

    @Transactional
    public CustomerBill generateOnDemand(String billingAccountId) {
        log.info("[ON-DEMAND-BILL] Starting generation for account={}", billingAccountId);
        AccountManagementClient.BillingAccountView account =
                accountManagementClient.getBillingAccount(billingAccountId);
        log.info("[ON-DEMAND-BILL] Loaded account={} state={} ratingType={}",
                account == null ? billingAccountId : account.getId(),
                account == null ? null : account.getState(),
                account == null ? null : account.getRatingType());
        if (!isActivePostpaid(account)) {
            log.warn("[ON-DEMAND-BILL] Rejected account={} because it is not active postpaid",
                    billingAccountId);
            throw new BillGenerationRejectedException(
                    "Billing account must be active and postpaid: " + billingAccountId);
        }

        String specificationId = account.getBillStructure() == null
                || account.getBillStructure().getCycleSpecification() == null
                ? null
                : account.getBillStructure().getCycleSpecification().getId();

        if (specificationId == null || specificationId.isBlank()) {
            log.error("[ON-DEMAND-BILL] Account={} has no billing cycle specification",
                    billingAccountId);
            throw new IllegalStateException("Billing account has no billing cycle specification: " + billingAccountId);
        }

        log.info("[ON-DEMAND-BILL] Resolved account={} specification={}", billingAccountId, specificationId);
        BillingCycleSpecificationView specification = accountManagementClient.getBillingCycleSpecification()
                .stream()
                .filter(candidate -> specificationId.equals(candidate.getId()))
                .findFirst()
                .orElseThrow(() -> new AccountManagementClient.AccountManagementException("BillingCycleSpecification not found in TMF666: " + specificationId));

        log.info("[ON-DEMAND-BILL] Loaded specification={} paymentDueDateOffset={}",
                specification.getId(), specification.getPaymentDueDateOffset());
        LocalDate requestDate = LocalDate.now();
        LocalDate periodEnd = requestDate.minusDays(1);
        int billingCycleDay = billingCycleDay(specification);
        LocalDate previousCycleDate = cycleDateBefore(requestDate, billingCycleDay);
        LocalDate nextCycleDate = cycleDateOnOrAfter(requestDate, billingCycleDay);
        LocalDate cyclePeriodStart = previousCycleDate.plusDays(1);

        log.info("[ON-DEMAND-BILL] Account={} cycleDay={} previousCycleDate={} nextCycleDate={} "
                        + "cyclePeriodStart={} periodEnd={}",
                billingAccountId, billingCycleDay, previousCycleDate, nextCycleDate,
                cyclePeriodStart, periodEnd);
        rejectDuplicateInterimBill(billingAccountId, cyclePeriodStart, nextCycleDate);

        var latestBill = billRepository.findFirstByBillingAccount_IdOrderByBillDateDesc(billingAccountId);
        var latestBillPeriodEnd = latestBill
                .map(CustomerBill::getBillingPeriod)
                .map(TimePeriod::getEndDateTime)
                .map(LocalDateTime::toLocalDate)
                .map(endDate -> endDate.plusDays(1));
        LocalDate periodStart = latestBillPeriodEnd
                .map(endExclusive -> endExclusive.isAfter(cyclePeriodStart) ? endExclusive : cyclePeriodStart)
                .orElse(cyclePeriodStart);
        if (latestBillPeriodEnd.isPresent()) {
            log.info("[ON-DEMAND-BILL] Latest bill={} billingPeriodEnd={} determines next period start={}",
                    latestBill.map(CustomerBill::getId).orElse(null),
                    latestBill.map(CustomerBill::getBillingPeriod)
                            .map(TimePeriod::getEndDateTime).orElse(null), periodStart);
        } else if (latestBill.isPresent()) {
            log.info("[ON-DEMAND-BILL] Latest bill={} has no billing-period end; using cycle-period start={}",
                    latestBill.map(CustomerBill::getId).orElse(null), cyclePeriodStart);
        } else {
            log.info("[ON-DEMAND-BILL] No previous bill found; using cycle-aligned start={}", periodStart);
        }
        if (periodStart.isAfter(periodEnd)) {
            log.warn("[ON-DEMAND-BILL] Invalid period for account={}: start={} end={}",
                    billingAccountId, periodStart, periodEnd);
            throw new IllegalStateException("On-demand billing period start is after its end");
        }

        log.info("[ON-DEMAND-BILL] Loading billing inputs account={} period={} to {}",
                billingAccountId, periodStart, periodEnd);
        BillingInput input = inputProvider.getBillingInput(billingAccountId, periodStart, periodEnd);
        log.info("[ON-DEMAND-BILL] Loaded billing inputs account={} currency={}",
                billingAccountId, input.currency());
        BillCalculation calculation = calculationService.calculate(input);
        log.info("[ON-DEMAND-BILL] Calculated charges account={} currency={} subtotal={} tax={} amountDue={}",
                billingAccountId, input.currency(), calculation.subtotal(),
                calculation.tax(), calculation.amountDue());
        log.info("[ON-DEMAND-BILL] Creating off-cycle interim bill account={} period={} to {}",
                billingAccountId, periodStart, periodEnd);
        return createBill(account, specification, periodStart, periodEnd, CustomerBillRunType.OFF_CYCLE,
                input, calculation, true);
    }

    public void validateNoInterimBillForCurrentCycle(String billingAccountId) {
        AccountManagementClient.BillingAccountView account = accountManagementClient.getBillingAccount(billingAccountId);
        String specificationId = account.getBillStructure() == null
                || account.getBillStructure().getCycleSpecification() == null
                ? null
                : account.getBillStructure().getCycleSpecification().getId();

        if (specificationId == null || specificationId.isBlank()) {
            log.warn("[ON-DEMAND-BILL] Account={} has no billing cycle specification", billingAccountId);
            throw new IllegalStateException("Billing account has no billing cycle specification: " + billingAccountId);
        }

        BillingCycleSpecificationView specification = accountManagementClient.getBillingCycleSpecification()
                .stream()
                .filter(candidate -> specificationId.equals(candidate.getId()))
                .findFirst()
                .orElseThrow(() -> new AccountManagementClient.AccountManagementException(
                        "BillingCycleSpecification not found in TMF666: " + specificationId));

        LocalDate requestDate = LocalDate.now();
        int cycleDay = billingCycleDay(specification);
        LocalDate cycleStart = cycleDateBefore(requestDate, cycleDay).plusDays(1);
        LocalDate nextCycleDate = cycleDateOnOrAfter(requestDate, cycleDay);
        rejectDuplicateInterimBill(billingAccountId, cycleStart, nextCycleDate);
    }

    private void rejectDuplicateInterimBill(String billingAccountId,
                                            LocalDate cycleStart,
                                            LocalDate nextCycleDate) {
        boolean interimBillExists = billRepository
                .existsByBillingAccount_IdAndCategoryAndBillingPeriod_StartDateTimeGreaterThanEqualAndBillingPeriod_StartDateTimeLessThan(
                        billingAccountId, "interim",
                        cycleStart.atStartOfDay(), nextCycleDate.atStartOfDay());
        if (interimBillExists) {
            throw new ConflictException(
                    "An interim CustomerBill already exists for billing account " + billingAccountId
                            + " in the current billing cycle",
                    "CustomerBillOnDemand", "NA");
        }
    }

    private int billingCycleDay(BillingCycleSpecificationView specification) {

        String configuredPeriod = specification.getBillingPeriod();

        if (configuredPeriod != null && !configuredPeriod.isBlank()) {

            try {
                int configuredDay = Integer.parseInt(configuredPeriod);
                if (configuredDay < 1 || configuredDay > 31) {
                    throw new IllegalStateException("BillingCycleSpecification " + specification.getId()
                            + " has billingPeriod day outside 1-31: " + configuredDay);
                }
                return configuredDay;
            } catch (NumberFormatException ignored) {
                if (!"month".equalsIgnoreCase(configuredPeriod)
                        && !"monthly".equalsIgnoreCase(specification.getFrequency())) {
                    throw new IllegalStateException("Unsupported billingPeriod '"
                            + configuredPeriod + "' for BillingCycleSpecification "
                            + specification.getId());
                }
            }
        }

        log.info("[ON-DEMAND-BILL] Specification={} does not define a numeric cycle day; "
                        + "using month-end, consistent with scheduled-cycle handling",
                specification.getId());

        return 31;
    }

    private LocalDate cycleDateBefore(LocalDate referenceDate, int cycleDay) {
        YearMonth month = YearMonth.from(referenceDate);
        LocalDate dateInCurrentMonth = cycleDate(month, cycleDay);
        YearMonth previousCycleMonth = dateInCurrentMonth.isBefore(referenceDate)
                ? month : month.minusMonths(1);
        return cycleDate(previousCycleMonth, cycleDay);
    }

    private LocalDate cycleDateOnOrAfter(LocalDate referenceDate, int cycleDay) {
        YearMonth month = YearMonth.from(referenceDate);
        LocalDate dateInCurrentMonth = cycleDate(month, cycleDay);
        YearMonth nextCycleMonth = dateInCurrentMonth.isBefore(referenceDate)
                ? month.plusMonths(1) : month;
        return cycleDate(nextCycleMonth, cycleDay);
    }

    private LocalDate cycleDate(YearMonth month, int cycleDay) {
        return month.atDay(Math.min(cycleDay, month.lengthOfMonth()));
    }

    private CustomerBill createBill(AccountManagementClient.BillingAccountView account,
                                    BillingCycleSpecificationView specification,
                                    LocalDate periodStart,
                                    LocalDate periodEnd,
                                    CustomerBillRunType runType,
                                    BillingInput input,
                                    BillCalculation calculation,
                                    boolean onDemand) {
        LocalDate billDate = periodEnd.plusDays(1);
        LocalDateTime billDateTime = billDate.atStartOfDay();
        LocalDateTime periodEndDateTime = onDemand
                ? periodEnd.atTime(23, 59, 59)
                : periodEnd.atStartOfDay();
        LocalDateTime nextBillDate = billDate.plusMonths(1).atStartOfDay();
        int dueOffset = specification.getPaymentDueDateOffset() == null ? 0 : specification.getPaymentDueDateOffset();
        String billId = nextId();
        String cycleId = onDemand
                ? "BCOD-" + billId
                : "BC" + YearMonth.from(periodStart).toString().replace("-", "") + "-" + account.getId();

        log.info("[BILL-GENERATION] Creating {} bill id={} account={} period={} to {} cycle={}",
                onDemand ? "on-demand" : "on-cycle", billId, account.getId(),
                periodStart, periodEnd, cycleId);
        BillCycle cycle = BillCycle.builder()
                .id(cycleId)
                .href(baseUrl + "/billcycle/" + cycleId)
                .name(YearMonth.from(periodStart).getMonth() + " " + periodStart.getYear() + " Billing Cycle")
                .billingDate(billDateTime)
                .billingPeriod(YearMonth.from(periodStart).toString())
                .paymentDueDate(billDate.plusDays(dueOffset).atStartOfDay())
                .billCycleSpecification(specificationRef(specification.getId(), specification))
                .validFor(new TimePeriod(periodStart.atStartOfDay(), periodEndDateTime))
                .baseType("BillCycle").type("BillCycle").build();
        billCycleRepository.save(cycle);

        Random random = new Random();
        CustomerBill bill = CustomerBill.builder()
                .id(billId)
                //.billNo(invoiceNumberService.next(billDate))
                .billNo("NR" + random.nextInt(999999999) + "" + (char) ('A' + random.nextInt(26)))
                .href(baseUrl + "/customerBill/" + billId)
                .billDate(billDateTime)
                .nextBillDate(nextBillDate)
                .paymentDueDate(billDate.plusDays(dueOffset).atStartOfDay())
                .billingPeriod(new TimePeriod(periodStart.atStartOfDay(), periodEndDateTime))
                .amountDue(new Money(input.currency(), calculation.amountDue()))
                .taxExcludedAmount(new Money(input.currency(), calculation.subtotal()))
                .taxIncludedAmount(new Money(input.currency(), calculation.currentBill()))
                .remainingAmount(new Money(input.currency(), calculation.amountDue()))
                .runType(runType).state(CustomerBillStateType.NEW)
                .billCycle(cycle)
                .category(onDemand ? "last" : "normal") //last/interim/normal
                .billingAccount(BillingAccountRef.builder().id(account.getId()).href(account.getHref())
                        .name(account.getName()).ratingType(account.getRatingType()).referredType("BillingAccount")
                        .type("BillingAccountRef").build())
                .financialAccount(account.getFinancialAccount() == null ? null : FinancialAccountRef.builder()
                        .id(account.getFinancialAccount().getId()).href(account.getFinancialAccount().getHref())
                        .name(account.getFinancialAccount().getName()).referredType("FinancialAccount")
                        .type("FinancialAccountRef").build())
                .paymentMethod(account.getDefaultPaymentMethod() == null ? null : PaymentMethodRef.builder()
                        .id(account.getDefaultPaymentMethod().getId())
                        .href(account.getDefaultPaymentMethod().getHref())
                        .name(account.getDefaultPaymentMethod().getName())
                        .referredType(account.getDefaultPaymentMethod().getReferredType())
                        .baseType(account.getDefaultPaymentMethod().getBaseType())
                        .schemaLocation(account.getDefaultPaymentMethod().getSchemaLocation())
                        .type(account.getDefaultPaymentMethod().getType())
                        .build())
                .baseType("CustomerBill").type("CustomerBill").build();
        bill.setRelatedParty(toRelatedParty(account.getRelatedParty(), bill));

        long firstItemNumber = customerBillItemRepository.count() + 1;
        List<CustomerBillItem> items = List.of(
                item(firstItemNumber, "Monthly Plan", input.planName(), input.monthlyPlanCharge(),
                        input.currency(), bill),
                item(firstItemNumber + 1, "Additional Data", null, input.usageCharge(), input.currency(), bill),
                item(firstItemNumber + 2, "VAS", null, input.vasCharge(), input.currency(), bill),
                item(firstItemNumber + 3, "Discount", null, input.discount().negate(), input.currency(), bill),
                item(firstItemNumber + 4, "GST", null, calculation.tax(), input.currency(), bill));
        bill.setBillItem(items);
        bill.setTaxItem(List.of(TaxItem.builder()
                .id(String.format("TA_%04d", taxItemRepository.count() + 1))
                .taxCategory("GST")
                .taxRate(input.taxRate())
                .taxAmount(new Money(input.currency(), calculation.tax()))
                .baseType("TaxItem")
                .type("TaxItem")
                .customerBill(bill)
                .build()));

        log.info("[BILL-GENERATION] Persisting bill id={} runType={} category={} billDate={} "
                        + "period={} to {} itemCount={} relatedPartyCount={}",
                billId, runType, bill.getCategory(), billDate,
                periodStart, periodEnd, bill.getBillItem().size(),
                bill.getRelatedParty().size());

        billRepository.save(bill);

        log.info("[BILL-GENERATION] CustomerBill created id={} invoice={} account={} runType={} "
                        + "category={} billDate={} period={} to {}",
                bill.getId(), bill.getBillNo(), account.getId(), runType,
                bill.getCategory(), billDate, periodStart, periodEnd);

        return bill;
    }

    private boolean isActivePostpaid(AccountManagementClient.BillingAccountView account) {
        return account != null
                && "postpaid".equalsIgnoreCase(account.getRatingType())
                && (account.getState() == null || "active".equalsIgnoreCase(account.getState()));
    }

    public static class BillGenerationRejectedException extends RuntimeException {
        public BillGenerationRejectedException(String message) {
            super(message);
        }
    }

    private String nextId() {
        long next = billRepository.count() + 1;
        return String.format("CB_%04d", next);
    }

    private List<RelatedPartyRefOrPartyRoleRef> toRelatedParty(
            List<RelatedPartyRefOrPartyRoleRefDto> relatedParty, CustomerBill bill) {
        if (relatedParty == null || relatedParty.isEmpty()) {
            return new ArrayList<>();
        }

        return relatedParty.stream()
                .map(related -> RelatedPartyRefOrPartyRoleRef.builder()
                        .role(related.getRole())
                        .baseType(related.getBaseType())
                        .schemaLocation(related.getSchemaLocation())
                        .type(related.getType() == null
                                ? "RelatedPartyRefOrPartyRoleRef" : related.getType())
                        .partyOrPartyRole(toPartyRefOrPartyRoleRef(
                                related.getPartyOrPartyRole(), bill.getBillingAccount().getId()))
                        .customerBill(bill)
                        .build())
                .collect(Collectors.toList());
    }

    private PartyRefOrPartyRoleRef toPartyRefOrPartyRoleRef(PartyRefOrPartyRoleRefDto party,
                                                            String billingAccountId) {
        if (party == null) {
            return null;
        }

        String externalPartyId = party.getPartyId() == null || party.getPartyId().isBlank()
                ? party.getId()
                : party.getPartyId();
        if (externalPartyId == null || externalPartyId.isBlank()) {
            throw new IllegalArgumentException("CustomerBill related party must have a party id");
        }

        return partyRefRepository.findFirstByBillingAccountIdAndPartyId(billingAccountId, externalPartyId)
                .orElseGet(() -> partyRefRepository.save(
                        createPartyRefOrPartyRoleRef(party, billingAccountId, externalPartyId)));
    }

    private PartyRefOrPartyRoleRef createPartyRefOrPartyRoleRef(PartyRefOrPartyRoleRefDto party,
                                                                 String billingAccountId,
                                                                 String externalPartyId) {
        return PartyRefOrPartyRoleRef.builder()
                .id(UUID.randomUUID().toString())
                .billingAccountId(billingAccountId)
                .href(party.getHref())
                .name(party.getName())
                .partyId(externalPartyId)
                .partyName(party.getPartyName())
                .referredType(party.getReferredType())
                .baseType(party.getBaseType())
                .schemaLocation(party.getSchemaLocation())
                .type(party.getType() == null ? "PartyRef" : party.getType())
                .build();
    }

    private CustomerBillItem item(long sequence, String description, String planName, BigDecimal amount,
                                  String currency, CustomerBill bill) {
        return CustomerBillItem.builder()
                .id(String.format("CBI_%02d", sequence))
                .description(description)
                .planName(planName)
                .amount(amount.setScale(2)).unit(currency)
                .customerBill(bill)
                .build();
    }

    private BillCycleSpecificationRef specificationRef(String id, BillingCycleSpecificationView specification) {

        BillCycleSpecificationRef ref = specificationRefRepository.findById(id).orElseGet(BillCycleSpecificationRef::new);
        ref.setId(id);
        ref.setHref(specification.getHref());
        ref.setName(specification.getName());
        ref.setReferredType("BillingCycleSpecification");
        ref.setType("BillCycleSpecificationRef");

        return specificationRefRepository.save(ref);
    }
}
