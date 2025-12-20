package com.example.demo.client;

import com.example.demo.dto.PaymentInitiateRequest;
import com.example.demo.dto.PaymentInitiateResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.util.retry.Retry;

import java.time.Duration;

@Slf4j
@Component
public class PaymentClient {

    private final WebClient paymentWebClient;

    public PaymentClient(WebClient paymentWebClient) {
        this.paymentWebClient = paymentWebClient;
    }

    // -------------------------------
    // INITIATE PAYMENT
    // ------------------- ------------
    public PaymentInitiateResponse initiatePayment(PaymentInitiateRequest request) {

        log.info("Calling Payment Gateway for order {}", request.getOrderId());

        return paymentWebClient
                .post()
                .uri("/initiate")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(PaymentInitiateResponse.class)
                .timeout(Duration.ofSeconds(3))          // TIMEOUT as per PDF
                .retryWhen(Retry.backoff(2, Duration.ofMillis(300))) // RETRY as per PDF
                .doOnError(ex -> log.error("Payment gateway error: {}", ex.getMessage()))
                .block();
    }

    // -------------------------------
    // VERIFY CALLBACK SIGNATURE
    // -------------------------------
    public boolean verifyCallback(String payload, String signature) {

        return paymentWebClient
                .post()
                .uri("/verify")
                .header("X-PAYMENT-SIGNATURE", signature)
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(Boolean.class)
                .timeout(Duration.ofSeconds(2))
                .block();
    }
}

