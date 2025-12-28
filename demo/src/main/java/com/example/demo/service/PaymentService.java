package com.example.demo.service;

import com.example.demo.dto.PaymentInitiateResponse;
import com.example.demo.entity.Order;
import com.example.demo.entity.Payment;

public interface PaymentService {

    PaymentInitiateResponse initiatePayment(Long orderId);

//    Payment updatePaymentStatus(String externalPaymentId, String status);
}
