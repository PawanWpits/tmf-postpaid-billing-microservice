package com.wpits.customerbill.service;

import com.wpits.customerbill.dtos.BillingCycleSpecificationView;
import com.wpits.customerbill.repositories.CustomerBillRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;

import static org.mockito.Mockito.*;

class BillingCycleOrchestrationServiceLoadTest {

    //check loops and functionilty Orchestration for 50k billing account process kr kes
    @Test
    void publishesOneEventFor50000EligibleBillingAccounts() {
        CustomerBillRepository billRepository = mock(CustomerBillRepository.class);
        BillingCycleEventPublisher eventPublisher = mock(BillingCycleEventPublisher.class);
        BillCycleSpecificationRefService refService = mock(BillCycleSpecificationRefService.class);
        AccountManagementClient accountManagementClient = mock(AccountManagementClient.class);

        BillingCycleSpecificationView specification = new BillingCycleSpecificationView();
        specification.setId("BCS-50000");
        specification.setBillingPeriod(String.valueOf(LocalDate.now().minusDays(1).getDayOfMonth()));
        specification.setFrequency("monthly");

        List<AccountManagementClient.BillingAccountView> accounts = IntStream.rangeClosed(1, 50_000)
                .mapToObj(index -> {
                    AccountManagementClient.BillingAccountView account =
                            new AccountManagementClient.BillingAccountView();
                    account.setId("BA-" + index);
                    return account;
                })
                .toList();

        when(accountManagementClient.getBillingCycleSpecification()).thenReturn(List.of(specification));
        when(accountManagementClient.listBillingAccounts("BCS-50000")).thenReturn(accounts);
        when(billRepository.existsForBillingCycle(anyString(), eq("BCS-50000"),
                any(LocalDateTime.class), any(LocalDateTime.class),
                any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(false);

        BillingCycleOrchestrationService service = new BillingCycleOrchestrationService(
                billRepository, eventPublisher, refService, accountManagementClient);

        service.processScheduled();

        verify(accountManagementClient).listBillingAccounts("BCS-50000");
        verify(billRepository, times(50_000))
                .existsForBillingCycle(anyString(), eq("BCS-50000"),
                        any(LocalDateTime.class), any(LocalDateTime.class),
                        any(LocalDateTime.class), any(LocalDateTime.class));
        verify(eventPublisher, times(50_000)).publish(any());
    }
}
