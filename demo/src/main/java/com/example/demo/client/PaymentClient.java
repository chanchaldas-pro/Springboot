package com.example.demo.client;

import com.example.demo.dto.PaymentInitiateRequest;
import com.example.demo.dto.PaymentInitiateResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.util.retry.Retry;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentClient {

    private final WebClient paymentWebClient;

    @CircuitBreaker(name = "paymentGateway", fallbackMethod = "fallback")
    @Retry(name = "paymentGateway")
    public PaymentInitiateResponse initiatePayment(PaymentInitiateRequest request) {

        return paymentWebClient.post()
                .uri("/payments/initiate")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(PaymentInitiateResponse.class)
                .block();
    }

    private PaymentInitiateResponse fallback(
            PaymentInitiateRequest request,
            Throwable ex
    ) {
        throw new RuntimeException("Gateway unavailable", ex);
    }
}


