package com.example.demo.service.impl;

import com.example.demo.client.PaymentClient;
import com.example.demo.dto.PaymentInitiateRequest;
import com.example.demo.dto.PaymentInitiateResponse;
import com.example.demo.entity.*;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.PaymentAttemptRepository;
import com.example.demo.repository.PaymentRepository;
import com.example.demo.service.PaymentService;
import com.razorpay.RazorpayException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
@Transactional




public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentAttemptRepository attemptRepository;
    private final PaymentClient paymentClient;
    private final OrderRepository orderRepository;
    private final PaymentAttemptRepository paymentAttemptRepository;

    @Transactional
    public PaymentInitiateResponse initiatePayment(Long orderId) {

        // 1. Fetch order
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // 2. Block already paid orders
        if (order.getStatus() == OrderStatus.COMPLETED) {
            throw new IllegalStateException("Order already paid");
        }

        Customer customer = order.getCustomer();
        String email = customer.getEmail();

        // 3. Fetch payment if exists
        Payment payment = paymentRepository.findByOrder(order).orElse(null);

        // 4. ALWAYS create payment attempt (later)
        PaymentAttempt attempt = new PaymentAttempt();

        // 5. If payment exists and NOT successful → reuse Razorpay order
        if (payment != null && payment.getStatus() != PaymentStatus.SUCCESS) {

            PaymentInitiateResponse response = new PaymentInitiateResponse();
            response.setExternalOrderId(order.getExternalOrderId());
            response.setAmount(convertToPaise(order.getTotalAmount()));


            attempt.setPayment(payment);
            attempt.setSuccess(false);
            attempt.setProviderResponse("Reused existing Razorpay order");

            paymentAttemptRepository.save(attempt);

            return response;
        }

        // 6. If payment does NOT exist → create payment
        if (payment == null) {
            payment = createPayment(order);
        }

        try {
            // 7. Create Razorpay order
            PaymentInitiateResponse response =
                    paymentClient.initiatePayment(buildRequest(order, email));

            // 8. Save Razorpay order id in Order
            order.setExternalOrderId(response.getExternalOrderId());
            orderRepository.save(order);

            // 9. Update payment
            payment.setStatus(PaymentStatus.INITIATED);
            paymentRepository.save(payment);

            // 10. Save payment attempt
            attempt.setPayment(payment);
            attempt.setSuccess(false);
            attempt.setProviderResponse("Razorpay order created");

            paymentAttemptRepository.save(attempt);

            return response;

        } catch (Exception ex) {

            // 11. Save failed attempt
            attempt.setPayment(payment);
            attempt.setSuccess(false);
            attempt.setProviderResponse(ex.getMessage());

            paymentAttemptRepository.save(attempt);


        }
        return null;
    }


    private int convertToPaise(BigDecimal amount) {
        return amount.multiply(BigDecimal.valueOf(100)).intValueExact();
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

    private PaymentInitiateRequest buildRequest(Order order,String email) {
        PaymentInitiateRequest request = new PaymentInitiateRequest();
        request.setOrderId(order.getId());
        request.setAmount(order.getTotalAmount());
        request.setEmail(email);
        request.setCallbackUrl("https://yourapp.com/payments/callback");
        return request;
    }
}

