package com.example.demo.service;

import com.example.demo.entity.Order;
import com.example.demo.entity.Payment;

public interface PaymentService {

    Payment initiatePayment(Order order);

    Payment updatePaymentStatus(String externalPaymentId, String status);
}
