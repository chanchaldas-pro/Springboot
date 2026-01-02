package com.example.demo.service.impl;

import com.example.demo.dto.PaymentInitiateRequest;
import com.example.demo.dto.PaymentInitiateResponse;
import com.example.demo.entity.*;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.PaymentAttemptRepository;
import com.example.demo.repository.PaymentRepository;
import com.example.demo.service.PaymentService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;

import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentAttemptRepository paymentAttemptRepository;
    private final OrderRepository orderRepository;
    private final WebClient razorpayWebClient;

    @Override
    @Transactional
    public PaymentInitiateResponse initiatePayment(Long orderId) {

        // 1️⃣ Fetch order from DB
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Order not found with id: " + orderId)
                );

        // Optional safety check
        if (order.getStatus() == OrderStatus.COMPLETED) {
            throw new IllegalStateException("Order already paid");
        }

        // 2️⃣ Convert amount to paise
        BigDecimal amountInPaise = order.getTotalAmount()
                .multiply(BigDecimal.valueOf(100));

        // 3️⃣ Build Razorpay request
        Map<String, Object> razorpayRequest = new HashMap<>();
        razorpayRequest.put("amount", amountInPaise.intValueExact());
        razorpayRequest.put("currency", "INR");
        razorpayRequest.put("receipt", "order_" + order.getId());
        razorpayRequest.put("payment_capture", 1);

        // 4️⃣ Call Razorpay Order API
        Map<String, Object> razorpayResponse =
                razorpayWebClient.post()
                        .uri("/orders")
                        .bodyValue(razorpayRequest)
                        .retrieve()
                        .onStatus(HttpStatusCode::is4xxClientError,
                                res -> Mono.error(new RuntimeException("Invalid payment request")))
                        .onStatus(HttpStatusCode::is5xxServerError,
                                res -> Mono.error(new RuntimeException("Razorpay server error")))
                        .bodyToMono(Map.class)
                        .retryWhen(
                                Retry.fixedDelay(2, Duration.ofMillis(500))
                                        .filter(this::isRetryableError)
                        )
                        .block();

        // 5️⃣ Extract Razorpay order id
        String externalOrderId = (String) razorpayResponse.get("id");



        // 6️⃣ Persist payment record (important!)
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setAmount(order.getTotalAmount());
        payment.setStatus(PaymentStatus.INITIATED);
        paymentRepository.save(payment);
        order.setExternalOrderId(externalOrderId);
        orderRepository.save(order);


        // 7️⃣ Build response for frontend
        PaymentInitiateResponse response = new PaymentInitiateResponse();
        response.setExternalOrderId(externalOrderId);
        response.setAmount(amountInPaise.intValueExact());
        response.setRedirectUrl(
                "https://checkout.razorpay.com/v1/checkout.js?order_id=" + externalOrderId
        );


        return response;
    }


    // ✅ Retry only for network / timeout issues
    private boolean isRetryableError(Throwable ex) {
        return ex instanceof WebClientRequestException
                || ex instanceof TimeoutException;
    }

    /* ================== Helper methods (existing logic) ================== */

//    private Payment createPayment(Order order) {
//        Payment payment = new Payment();
//        payment.setOrder(order);
//        payment.setAmount(order.getTotalAmount());
//        return paymentRepository.save(payment);
//    }
//
//    private void saveAttempt(Payment payment, String response, boolean success) {
//        PaymentAttempt attempt = new PaymentAttempt();
//        attempt.setPayment(payment);
//        attempt.setProviderResponse(response);
//        attempt.setSuccess(success);
//        paymentAttemptRepository.save(attempt);
//    }
//
//    private int convertToPaise(BigDecimal amount) {
//        return amount.multiply(BigDecimal.valueOf(100)).intValueExact();
//    }
}
