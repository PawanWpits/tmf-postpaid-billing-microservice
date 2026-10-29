package com.wpits.document.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import tools.jackson.databind.JsonNode;

import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
public class CustomerBillClient {
    private final RestClient client;

    public CustomerBillClient(RestClient.Builder builder,
                              @Value("${customer-bill.base-url}") String baseUrl) {
        this.client = builder.baseUrl(baseUrl).build();
    }

    public JsonNode getBill(String id) {
        try {
            return client.get()
                    .uri("/customerBill/{id}", id)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (request, response) -> {
                        throw new CustomerBillClientException(
                                "CustomerBill API returned " + response.getStatusCode());
                    })
                    .body(JsonNode.class);
        } catch (RestClientException ex) {
            throw new CustomerBillClientException(
                    "CustomerBill API is unavailable while loading bill " + id, ex);
        }
    }

    public List<JsonNode> getAllCustomerBillsByBillDate(LocalDate date) {
        try {
            List<JsonNode> bills = client.get()
                    .uri("/customerBill/byBillDate?date={date}", date)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (request, response) -> {
                        throw new CustomerBillClientException("CustomerBill API returned " + response.getStatusCode() + " while loading bills for " + date);
                    })
                    .body(new ParameterizedTypeReference<>() { });

            if (bills == null) {
                throw new CustomerBillClientException("CustomerBill API returned an empty response for " + date);
            }
            log.info("from customer-bill-app Today bills count: {}", bills.size());
            return bills;
        } catch (RestClientException ex) {
            throw new CustomerBillClientException("CustomerBill API is unavailable while loading bills for " + date, ex);
        }
    }

    public static class CustomerBillClientException extends RuntimeException {
        public CustomerBillClientException(String message) {
            super(message);
        }

        public CustomerBillClientException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
