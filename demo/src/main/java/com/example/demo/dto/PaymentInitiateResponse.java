package com.example.demo.dto;

import java.math.BigDecimal;

public class PaymentInitiateResponse {

    private String paymentId;             // internal DB payment ID
    private String externalOrderId;   // UUID or provider ID
    private String providerSessionId;   // from payment provider
    private String redirectUrl;         // where frontend should redirect user
    private int amount;
    // -----------------------
    // GETTERS & SETTERS
    // -----------------------

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public String getExternalOrderId() {
        return externalOrderId;
    }

    public void setExternalOrderId(String externalOrderId) {
        this.externalOrderId = externalOrderId;
    }

    public String getProviderSessionId() {
        return providerSessionId;
    }

    public void setProviderSessionId(String providerSessionId) {
        this.providerSessionId = providerSessionId;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }
}
