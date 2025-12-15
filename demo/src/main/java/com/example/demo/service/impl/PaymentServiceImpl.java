package com.example.demo.service.impl;

import com.example.demo.entity.Order;
import com.example.demo.entity.OrderStatus;
import com.example.demo.entity.Payment;
import com.example.demo.entity.PaymentStatus;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.PaymentRepository;
import com.example.demo.service.PaymentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.UUID;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    public PaymentServiceImpl(PaymentRepository paymentRepository,
                              OrderRepository orderRepository) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
    }

    // --------------------
    // INITIATE PAYMENT
    // --------------------
    @Override
    @Transactional
    public Payment initiatePayment(Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setAmount(order.getTotalAmount());
        payment.setExternalPaymentId(UUID.randomUUID().toString());
        payment.setStatus(PaymentStatus.INITIATED);

        return paymentRepository.save(payment);
    }

    // --------------------
    // UPDATE PAYMENT STATUS AFTER CALLBACK
    // --------------------
    @Override
    @Transactional
    public Payment updatePaymentStatus(String externalPaymentId, String status) {

        Payment payment = paymentRepository.findByExternalPaymentId(externalPaymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        payment.setStatus(PaymentStatus.valueOf(status));

        // Update order also
        Order order = payment.getOrder();

        if (status.equals("SUCCESS")) {
            order.setStatus(OrderStatus.PENDING);
        } else if (status.equals("FAILED")) {
            order.setStatus(OrderStatus.FAILED);
        }

        return paymentRepository.save(payment);

    }
}
