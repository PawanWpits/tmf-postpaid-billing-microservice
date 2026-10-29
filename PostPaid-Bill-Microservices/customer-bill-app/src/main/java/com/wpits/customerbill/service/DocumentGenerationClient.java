package com.wpits.customerbill.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
public class DocumentGenerationClient {
    private final RestClient client;

    public DocumentGenerationClient(RestClient.Builder builder,
                                    @Value("${document-generation.base-url}") String baseUrl) {
        this.client = builder.clone().baseUrl(baseUrl).build();
    }

    public DocumentGenerationResult generatePdf(String billId) {
        try {
            DocumentGenerationResult result = client.post()
                    .uri("/documents/customer-bills/{billId}/generate", billId)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (request, response) -> {
                        throw new DocumentGenerationException(
                                "Document service returned " + response.getStatusCode()
                                        + " for CustomerBill " + billId);
                    })
                    .body(DocumentGenerationResult.class);
            if (result == null || !"generated".equalsIgnoreCase(result.status())) {
                throw new DocumentGenerationException(
                        "Document service did not confirm PDF generation for CustomerBill " + billId);
            }
            return result;
        } catch (RestClientException ex) {
            throw new DocumentGenerationException(
                    "Document service is unavailable while generating PDF for CustomerBill " + billId,
                    ex);
        }
    }

    public record DocumentGenerationResult(
            String billId,
            String billNo,
            String status,
            String path,
            String message) {
    }

    public static class DocumentGenerationException extends RuntimeException {
        public DocumentGenerationException(String message) {
            super(message);
        }

        public DocumentGenerationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
