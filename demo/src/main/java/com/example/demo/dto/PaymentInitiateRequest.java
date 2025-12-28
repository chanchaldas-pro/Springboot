package com.example.demo.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class PaymentInitiateRequest {

    @NotNull(message = "Order ID is required")
    private Long orderId;

    private String email;

    @NotNull(message = "Amount is required")
    private BigDecimal amount;

    private String callbackUrl;

    // Getters & Setters

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getEmail(){
        return email;
    }

    public void setEmail(String email){
        this.email=email;
    }

    public void setCallbackUrl(String Url){
        this.callbackUrl=Url;
    }
}
