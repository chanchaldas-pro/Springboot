package com.example.demo.service;

public interface PaymentWebhookService {

    public void handleWebhook(byte[] rawPayload, String razorpaySignature);
}
