package com.example.demo.service;

import com.example.demo.entity.Payment;

public interface PaymentService {

    Payment initiatePayment(Long orderId);

    Payment updatePaymentStatus(String externalPaymentId, String status);
}
