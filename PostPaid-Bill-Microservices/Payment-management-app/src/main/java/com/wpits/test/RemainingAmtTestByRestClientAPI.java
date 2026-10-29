package com.wpits.test;

import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class RemainingAmtTestByRestClientAPI {

    public static void main(String[] args) {

        // Create a new instance of the RestClientAPI class
        RestClient restClient = RestClient.create();

        String href = "http://localhost:8081/tmf-api/Customer_Bill_Management/v5/customerBill/CB_0001";
        MoneyDto amount = new MoneyDto("INR", new BigDecimal("658.90"));
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("@type", "CustomerBill");
        requestBody.put("state", "NEW");
        requestBody.put("remainingAmount", amount);

        System.out.println("Request body for remaining amount API: " + requestBody);
        // Call the method to test remaining amount
        String response = restClient.patch()
                .uri(href)
                .body(requestBody)
                .retrieve()
                .body(String.class);
        System.out.println("Response from remaining amount API: " + response);

        //parse response and get @type and state
        if (response != null) {
            //System.out.println("RestClient response: " + response.getBytes());
            org.json.JSONObject json = new org.json.JSONObject(response);
            String type = (String) json.get("@type");
            String state = (String) json.get("state");
            System.out.println("Response @type: " + type);
            System.out.println("Response state: " + state);
            //get remainingAmount from response
            org.json.JSONObject remainingAmount = (org.json.JSONObject) json.get("remainingAmount");
            String unit = (String) remainingAmount.get("unit");
            BigDecimal value = (BigDecimal) remainingAmount.get("value");
            System.out.println("Response remainingAmount unit: " + unit);
            System.out.println("Response remainingAmount value: " + value);
        } else {
            System.out.println("No response from remaining amount API");
        }
    }
}
class MoneyDto {
    private String unit;
    private BigDecimal value;

    public MoneyDto(String unit, BigDecimal value) {
        this.unit = unit;
        this.value = value;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }
}