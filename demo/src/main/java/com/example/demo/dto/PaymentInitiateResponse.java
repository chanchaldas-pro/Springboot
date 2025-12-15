package com.example.demo.dto;

public class PaymentInitiateResponse {

    private Long paymentId;             // internal DB payment ID
    private String externalPaymentId;   // UUID or provider ID
    private String providerSessionId;   // from payment provider
    private String redirectUrl;         // where frontend should redirect user

    // -----------------------
    // GETTERS & SETTERS
    // -----------------------

    public Long getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Long paymentId) {
        this.paymentId = paymentId;
    }

    public String getExternalPaymentId() {
        return externalPaymentId;
    }

    public void setExternalPaymentId(String externalPaymentId) {
        this.externalPaymentId = externalPaymentId;
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
}
