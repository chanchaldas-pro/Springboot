package com.example.demo.service.impl;

import com.example.demo.client.PaymentClient;
import com.example.demo.dto.PaymentInitiateRequest;
import com.example.demo.dto.PaymentInitiateResponse;
import com.example.demo.entity.*;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.PaymentAttemptRepository;
import com.example.demo.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional


public class PaymentServiceImpl {

    private final PaymentRepository paymentRepository;
    private final PaymentAttemptRepository attemptRepository;
    private final PaymentClient paymentClient;

    public String initiatePayment(Long orderId) {
       @Autowired
        Order order=OrderRepository.findByID(orderId);
        Payment payment = paymentRepository
                .findByOrder(order)
                .orElseGet(() -> createPayment(order));





        for (int i = 1; i <= 3; i++) {
            try {
                PaymentInitiateResponse response =
                        paymentClient.initiatePayment(buildRequest(order));

                payment.setExternalPaymentId(response.getPaymentId());
                paymentRepository.save(payment);

                saveAttempt(payment, response.toString(), true);

                return response.getRedirectUrl();

            } catch (Exception ex) {
                saveAttempt(payment, ex.getMessage(), false);
            }
        }

        payment.setStatus(PaymentStatus.UNKNOWN);
        order.setStatus(OrderStatus.FAILED);

        return null;
    }

    private Payment createPayment(Order order) {
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setAmount(order.getTotalAmount());
        return paymentRepository.save(payment);
    }

    private void saveAttempt(Payment payment, String response, boolean success) {
        PaymentAttempt attempt = new PaymentAttempt();
        attempt.setPayment(payment);
        attempt.setProviderResponse(response);
        attempt.setSuccess(success);
        attemptRepository.save(attempt);
    }

    private PaymentInitiateRequest buildRequest(Order order) {
        PaymentInitiateRequest request = new PaymentInitiateRequest();
        request.setOrderId(order.getId());
        request.setAmount(order.getTotalAmount());
        request.setCallbackUrl("https://yourapp.com/payments/callback");
        return request;
    }
}

