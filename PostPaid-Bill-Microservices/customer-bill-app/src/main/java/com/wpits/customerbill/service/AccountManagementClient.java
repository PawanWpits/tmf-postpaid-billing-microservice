package com.wpits.customerbill.service;

import com.wpits.customerbill.dtos.RelatedPartyRefOrPartyRoleRefDto;
import com.wpits.customerbill.dtos.BillingCycleSpecificationView;
import com.wpits.customerbill.entities.BillCycleSpecificationRef;
import com.wpits.customerbill.exceptions.ResourceNotFoundException;
import lombok.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Service
public class AccountManagementClient {

    private final RestClient client;
    private final String baseUrl;
    private final ObjectMapper objectMapper;

    public AccountManagementClient(RestClient accountManagementRestClient,
                                   @Value("${account-management.base-url:http://localhost:8080/tmf-api/accountManagement/v5}") String baseUrl,
                                   ObjectMapper objectMapper) {
        this.client = accountManagementRestClient;
        this.baseUrl = baseUrl;
        this.objectMapper = objectMapper;
    }

    public List<BillingCycleSpecificationView> getBillingCycleSpecification() {

        String fields = "id,billingDateShift,billingPeriod,chargeDateOffset,"
                + "creditDateOffset,frequency,mailingDateOffset,paymentDueDateOffset";
        JsonNode response = client.get()
                .uri(baseUrl + "/billingCycleSpecification?fields=" + fields)
                .header("Accept", "application/json")
                .retrieve().body(JsonNode.class);

        return readList(response, BillingCycleSpecificationView.class);
    }

    public BillingAccountView getBillingAccount(String id) {
        String uri = baseUrl + "/billingAccount/" + id;
        try {
            return client.get().uri(uri).retrieve()
                    .onStatus(status -> status.value() == 404, (request, response) -> {
                        throw new ResourceNotFoundException(
                                "BillingAccount not found with id: " + id,
                                "BillingAccount", "NA");
                    })
                    .onStatus(HttpStatusCode::isError, (request, response) -> {
                        throw new AccountManagementException(
                                "TMF666 returned " + response.getStatusCode()
                                        + " while looking up BillingAccount " + id);
                    })
                    .body(BillingAccountView.class);
        } catch (RestClientException ex) {
            throw new AccountManagementException(
                    "TMF666 unavailable while looking up BillingAccount " + id, ex);
        }
    }

    public List<BillingAccountView> listBillingAccounts() {
        JsonNode response = get("/billingAccount", JsonNode.class);
        return readList(response, BillingAccountView.class);
    }

    public List<BillingAccountView> listBillingAccounts(String billCycleSpecificationId) {
        JsonNode response = get("/billingAccount/billing-cycle-specification/" + billCycleSpecificationId, JsonNode.class);
        return readList(response, BillingAccountView.class);
    }

    private <T> T get(String path, Class<T> type) {
        return getAbsolute(baseUrl + path, type);
    }

    private <T> T getAbsolute(String uri, Class<T> type) {
        try {
            return client.get().uri(uri).retrieve()
                    .onStatus(HttpStatusCode::isError, (request, response) -> {
                        throw new AccountManagementException("TMF666 returned " + response.getStatusCode());
                    })
                    .body(type);
        } catch (RestClientException ex) {
            throw new AccountManagementException("TMF666 unavailable", ex);
        }
    }

    private <T> List<T> readList(JsonNode response, Class<T> itemType) {
        if (response == null || response.isNull()) {
            return List.of();
        }
        JsonNode values = response.isArray() ? response : response.get("value");
        if (values == null || !values.isArray()) {
            throw new AccountManagementException("TMF666 list response must be an array or contain a value array");
        }
        List<T> result = new java.util.ArrayList<>();
        for (JsonNode value : values) {
            result.add(objectMapper.convertValue(value, itemType));
        }
        return result;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class BillingAccountView {
        private String id;
        private String href;
        private String name;
        private String ratingType;
        private String state;
        private BillStructureView billStructure;
        private FinancialAccountView financialAccount;
        private PaymentMethodView defaultPaymentMethod;
        private List<RelatedPartyRefOrPartyRoleRefDto> relatedParty;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class BillStructureView {
        private BillingCycleSpecificationView cycleSpecification;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class FinancialAccountView {
        private String id;
        private String href;
        private String name;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class PaymentMethodView {
        private String id;
        private String href;
        private String name;
        private String referredType;
        private String baseType;
        private String schemaLocation;
        private String type;
    }

    public static class AccountManagementException extends RuntimeException {
        public AccountManagementException(String message) { super(message); }
        public AccountManagementException(String message, Throwable cause) { super(message, cause); }
    }
}
