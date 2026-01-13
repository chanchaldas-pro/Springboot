package com.example.demo.service.impl;

import com.example.demo.dto.PaymentInitiateRequest;
import com.example.demo.dto.PaymentInitiateResponse;
import com.example.demo.entity.*;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ExternalServiceException;
import com.example.demo.exception.NotFoundException;
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

        // 1️⃣ Fetch Order

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("NOT_FOUND","Order not found"));

        if (order.getStatus() == OrderStatus.COMPLETED) {
            throw new BadRequestException("ORDER_PAID","Order already paid");
        }

        // 2️⃣ Find or Create Payment (ONLY ONCE PER ORDER)
        Payment payment = paymentRepository.findByOrder(order)
                .orElseGet(() -> {
                    Payment p = new Payment();
                    p.setOrder(order);
                    p.setAmount(order.getTotalAmount());
                    p.setStatus(PaymentStatus.PENDING);
                    return paymentRepository.save(p);
                });

        // 3️⃣ ALWAYS create PaymentAttempt
        PaymentAttempt attempt = new PaymentAttempt();
        attempt.setPayment(payment);
        attempt.setSuccess(false);
        attempt.setPayment(payment);
        paymentAttemptRepository.save(attempt);

        // 4️⃣ Convert amount to paise
        BigDecimal amountInPaise = order.getTotalAmount()
                .multiply(BigDecimal.valueOf(100));

        // 5️⃣ Build Razorpay Order Request
        Map<String, Object> razorpayRequest = new HashMap<>();
        razorpayRequest.put("amount", amountInPaise.intValueExact());
        razorpayRequest.put("currency", "INR");
        razorpayRequest.put("receipt", order.getId().toString());
        razorpayRequest.put("payment_capture", 1);

        // 6️⃣ Create Razorpay Order
        Map<String, Object> razorpayResponse =
                razorpayWebClient.post()
                        .uri("/orders")
                        .bodyValue(razorpayRequest)
                        .retrieve()
                        .onStatus(HttpStatusCode::is4xxClientError,
                                res -> Mono.error(new BadRequestException("RAZORPAY_4XX", "Invalid payment request")))
                        .onStatus(HttpStatusCode::is5xxServerError,
                                res -> Mono.error(new ExternalServiceException("RAZORPAY_DOWN", "Razorpay is unavailable")))
                        .bodyToMono(Map.class)
                        .retryWhen(Retry.fixedDelay(2, Duration.ofMillis(500)))
                        .block();

        String externalOrderId = (String) razorpayResponse.get("id");

        // 7️⃣ Attach Razorpay Order ID to Attempt
        attempt.setExternalOrderId(externalOrderId);
        attempt.setProviderResponse("Payment initiated");
        paymentAttemptRepository.save(attempt);



        // 9️⃣ Response to Frontend
        PaymentInitiateResponse response = new PaymentInitiateResponse();
        response.setExternalOrderId(externalOrderId);
        response.setAmount(amountInPaise.intValueExact());


        return response;
    }


    // ✅ Retry only for network / timeout issues
    private boolean isRetryableError(Throwable ex) {
        return ex instanceof WebClientRequestException
                || ex instanceof TimeoutException;
    }


}
