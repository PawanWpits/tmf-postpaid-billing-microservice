package com.wpits.document.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import tools.jackson.databind.JsonNode;

@Service
public class SimulatorAddressClient {
    private final RestClient client;

    public SimulatorAddressClient(RestClient.Builder builder,
                                  @Value("${billing-simulator.base-url}") String baseUrl) {
        this.client = builder.clone().baseUrl(baseUrl).build();
    }

    public JsonNode getAddressDetails(String partyId) {
        try {
            JsonNode address = client.get()
                    .uri("/address-details/{partyId}", partyId)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (request, response) -> {
                        throw new AddressLookupException(
                                "Address API returned " + response.getStatusCode()
                                        + " for party " + partyId);
                    })
                    .body(JsonNode.class);
            if (address == null || address.isNull()) {
                throw new AddressLookupException(
                        "Address API returned an empty address for party " + partyId);
            }
            return address;
        } catch (RestClientException ex) {
            throw new AddressLookupException(
                    "Address API is unavailable while loading party " + partyId, ex);
        }
    }

    public static class AddressLookupException extends RuntimeException {
        public AddressLookupException(String message) {
            super(message);
        }

        public AddressLookupException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
