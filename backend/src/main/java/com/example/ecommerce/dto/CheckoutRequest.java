package com.example.ecommerce.dto;

import jakarta.validation.constraints.NotBlank;

public class CheckoutRequest {

    @NotBlank
    private String customerId;

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }
}
