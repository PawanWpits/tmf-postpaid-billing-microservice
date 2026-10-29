package com.wpits.customerbill.service.impls;

import com.wpits.customerbill.dtos.BillingAccountRefDto;
import com.wpits.customerbill.dtos.CustomerBillOnDemandDto;
import com.wpits.customerbill.dtos.CustomerBillRefDto;
import com.wpits.customerbill.dtos.PartyRefOrPartyRoleRefDto;
import com.wpits.customerbill.dtos.RelatedPartyRefOrPartyRoleRefDto;
import com.wpits.customerbill.entities.BillingAccountRef;
import com.wpits.customerbill.entities.CustomerBillOnDemand;
import com.wpits.customerbill.entities.CustomerBill;
import com.wpits.customerbill.entities.PartyRefOrPartyRoleRef;
import com.wpits.customerbill.entities.RelatedPartyRefOrPartyRoleRef;
import com.wpits.customerbill.enums.CustomerBillOnDemandStateType;
import com.wpits.customerbill.repositories.BillingAccountRefRepository;
import com.wpits.customerbill.repositories.CustomerBillOnDemandRepository;
import com.wpits.customerbill.repositories.PartyRefOrPartyRoleRefRepository;
import com.wpits.customerbill.service.AccountManagementClient;
import com.wpits.customerbill.service.CustomerBillGenerationService;
import com.wpits.customerbill.service.CustomerBillOnDemandService;
import com.wpits.customerbill.service.DocumentGenerationClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerBillOnDemandServiceImpl implements CustomerBillOnDemandService {

    private static final String RESOURCE_NAME = "CustomerBillOnDemand";

    private final CustomerBillOnDemandRepository repository;
    private final BillingAccountRefRepository billingAccountRefRepository;
    private final PartyRefOrPartyRoleRefRepository partyRefRepository;
    private final AccountManagementClient accountManagementClient;
    private final CustomerBillGenerationService billGenerationService;
    private final DocumentGenerationClient documentGenerationClient;

    @Value("${app.base-url}")
    private String baseUrl;

    @Override
    public CustomerBillOnDemandDto create(CustomerBillOnDemandDto request) {

        log.info("Received request to create on-demand bill: {}", request);
        if (request == null || request.getBillingAccount() == null || request.getBillingAccount().getId() == null || request.getBillingAccount().getId().isBlank()) {
            throw new IllegalArgumentException("billingAccount.id is required");
        }

        AccountManagementClient.BillingAccountView account = accountManagementClient.getBillingAccount(request.getBillingAccount().getId());
        log.info("Creating on-demand bill for billing account: {}", account.getId());

        billGenerationService.validateNoInterimBillForCurrentCycle(account.getId());
        log.info("Validated no interim bill for current cycle for billing account: {}", account.getId());

        String id = nextId();
        BillingAccountRef billingAccount = resolveBillingAccount(request.getBillingAccount(), account);

        CustomerBillOnDemand billOnDemand = CustomerBillOnDemand.builder()
                .id(id)
                .href(baseUrl + "/customerBillOnDemand/" + id)
                .description(request.getDescription())
                .lastUpdate(request.getLastUpdate() == null
                        ? LocalDateTime.now() : request.getLastUpdate().withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime())
                .name(request.getName())
                .state(CustomerBillOnDemandStateType.IN_PROGRESS)
                .billingAccount(billingAccount)
                .relatedParty(toRelatedParty(request.getRelatedParty(), billingAccount.getId()))
                .baseType(request.getBaseType())
                .schemaLocation(request.getSchemaLocation())
                .type(RESOURCE_NAME)
                .build();

        //request save ho gyi
        repository.save(billOnDemand);

        try {
            //invoice generate krega
            CustomerBill bill = billGenerationService.generateOnDemand(billingAccount.getId());
            billOnDemand.setCustomerBill(bill);
            repository.save(billOnDemand); //update ho gyi bill ka reference

            //pdf generate krega
            DocumentGenerationClient.DocumentGenerationResult document = documentGenerationClient.generatePdf(bill.getId());
            billOnDemand.setState(CustomerBillOnDemandStateType.DONE);
            repository.save(billOnDemand); //update ho gyi state DONE

            CustomerBillOnDemandDto response = toDto(billOnDemand);
            response.setDocumentStatus(document.status());
            response.setDocumentPath(document.path());
            return response;
        } catch (CustomerBillGenerationService.BillGenerationRejectedException ex) {
            billOnDemand.setState(CustomerBillOnDemandStateType.REJECTED);
            repository.save(billOnDemand);
            CustomerBillOnDemandDto response = toDto(billOnDemand);
            response.setErrorMessage(ex.getMessage());
            return response;
        } catch (RuntimeException ex) {
            billOnDemand.setState(CustomerBillOnDemandStateType.TERMINATED_WITH_ERROR);
            repository.save(billOnDemand);
            CustomerBillOnDemandDto response = toDto(billOnDemand);
            response.setErrorMessage(ex.getMessage());
            return response;
        }
    }

    private String nextId() {
        long next = repository.count() + 1;
        return String.format("D_%02d", next);
    }

    private BillingAccountRef resolveBillingAccount(BillingAccountRefDto request,
                                                     AccountManagementClient.BillingAccountView account) {
        return billingAccountRefRepository.findById(request.getId())
                .orElseGet(() -> billingAccountRefRepository.save(BillingAccountRef.builder()
                        .id(account.getId())
                        .href(account.getHref())
                        .name(account.getName())
                        .ratingType(account.getRatingType())
                        .referredType("BillingAccount")
                        .type("BillingAccountRef")
                        .build()));
    }

    private RelatedPartyRefOrPartyRoleRef toRelatedParty(RelatedPartyRefOrPartyRoleRefDto request,
                                                         String billingAccountId) {

        if (request == null) {
            return null;
        }

        PartyRefOrPartyRoleRefDto partyRequest = request.getPartyOrPartyRole();
        PartyRefOrPartyRoleRef party = null;
        if (partyRequest != null) {
            String externalPartyId = partyRequest.getPartyId() == null || partyRequest.getPartyId().isBlank()
                    ? partyRequest.getId()
                    : partyRequest.getPartyId();
            if (externalPartyId == null || externalPartyId.isBlank()) {
                throw new IllegalArgumentException("relatedParty.partyOrPartyRole.id is required");
            }
            party = partyRefRepository.findFirstByBillingAccountIdAndPartyId(billingAccountId, externalPartyId)
                    .orElseGet(() -> partyRefRepository.save(PartyRefOrPartyRoleRef.builder()
                            .id(UUID.randomUUID().toString())
                            .billingAccountId(billingAccountId)
                            .href(partyRequest.getHref())
                            .name(partyRequest.getName())
                            .partyId(externalPartyId)
                            .partyName(partyRequest.getPartyName())
                            .referredType(partyRequest.getReferredType())
                            .baseType(partyRequest.getBaseType())
                            .schemaLocation(partyRequest.getSchemaLocation())
                            .type(partyRequest.getType() == null ? "PartyRef" : partyRequest.getType())
                            .build()));
        }

        return RelatedPartyRefOrPartyRoleRef.builder()
                .role(request.getRole())
                .baseType(request.getBaseType())
                .schemaLocation(request.getSchemaLocation())
                .type(request.getType() == null
                        ? "RelatedPartyRefOrRelatedPartyRoleRef" : request.getType())
                .partyOrPartyRole(party)
                .build();
    }

    private CustomerBillOnDemandDto toDto(CustomerBillOnDemand entity) {
        return CustomerBillOnDemandDto.builder()
                .id(entity.getId())
                .href(entity.getHref())
                .description(entity.getDescription())
                .lastUpdate(entity.getLastUpdate() == null
                        ? null : entity.getLastUpdate().atOffset(ZoneOffset.UTC))
                .name(entity.getName())
                .state(entity.getState().getValue())
                .billingAccount(toBillingAccountDto(entity.getBillingAccount()))
                .customerBill(toCustomerBillDto(entity.getCustomerBill()))
                .relatedParty(toRelatedPartyDto(entity.getRelatedParty()))
                .baseType(entity.getBaseType())
                .schemaLocation(entity.getSchemaLocation())
                .type(entity.getType())
                .build();
    }

    private BillingAccountRefDto toBillingAccountDto(BillingAccountRef entity) {
        if (entity == null) {
            return null;
        }
        return BillingAccountRefDto.builder()
                .id(entity.getId())
                .href(entity.getHref())
                .name(entity.getName())
                .ratingType(entity.getRatingType())
                .referredType(entity.getReferredType())
                .baseType(entity.getBaseType())
                .schemaLocation(entity.getSchemaLocation())
                .type(entity.getType())
                .build();
    }

    private CustomerBillRefDto toCustomerBillDto(CustomerBill entity) {
        if (entity == null) {
            return null;
        }
        return CustomerBillRefDto.builder()
                .id(entity.getId())
                .href(entity.getHref())
                .name(entity.getBillNo())
                .referredType("CustomerBill")
                .type("CustomerBillRef")
                .build();
    }

    private RelatedPartyRefOrPartyRoleRefDto toRelatedPartyDto(RelatedPartyRefOrPartyRoleRef entity) {
        if (entity == null) {
            return null;
        }
        PartyRefOrPartyRoleRef party = entity.getPartyOrPartyRole();
        PartyRefOrPartyRoleRefDto partyDto = party == null ? null : PartyRefOrPartyRoleRefDto.builder()
                .id(party.getPartyId() == null || party.getPartyId().isBlank()
                        ? party.getId() : party.getPartyId())
                .href(party.getHref())
                .name(party.getName())
                .partyId(party.getPartyId())
                .partyName(party.getPartyName())
                .referredType(party.getReferredType())
                .baseType(party.getBaseType())
                .schemaLocation(party.getSchemaLocation())
                .type(party.getType())
                .build();

        return RelatedPartyRefOrPartyRoleRefDto.builder()
                .role(entity.getRole())
                .partyOrPartyRole(partyDto)
                .baseType(entity.getBaseType())
                .schemaLocation(entity.getSchemaLocation())
                .type(entity.getType())
                .build();
    }
}
