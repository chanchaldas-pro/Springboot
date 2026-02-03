package com.example.demo.service.impl;

import com.example.demo.dto.PaymentInitiateResponse;
import com.example.demo.entity.*;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.NotFoundException;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.PaymentAttemptRepository;
import com.example.demo.repository.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    PaymentRepository paymentRepository;

    @Mock
    PaymentAttemptRepository paymentAttemptRepository;

    @Mock
    OrderRepository orderRepository;

    // 👇 deep stub for chained WebClient calls
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    WebClient razorpayWebClient;

    @InjectMocks
    PaymentServiceImpl paymentService;

    // ---------------------------
    // SUCCESS PAYMENT INIT
    // ---------------------------
    @Test
    void initiatePayment_shouldCreateSuccessfully() {

        Order order = new Order();
        order.setId(1L);
        order.setTotalAmount(BigDecimal.valueOf(500));
        order.setStatus(OrderStatus.PENDING);

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        when(paymentRepository.findByOrder(order))
                .thenReturn(Optional.empty());

        Payment savedPayment = new Payment();
        savedPayment.setOrder(order);
        savedPayment.setAmount(BigDecimal.valueOf(500));
        savedPayment.setStatus(PaymentStatus.PENDING);

        when(paymentRepository.save(any(Payment.class)))
                .thenReturn(savedPayment);

        Map<String, Object> razorpayResponse = Map.of(
                "id", "rzp_order_123"
        );

        when(razorpayWebClient.post()
                .uri("/orders")
                .bodyValue(any())
                .retrieve()
                .bodyToMono(Map.class)
                .block())
                .thenReturn(razorpayResponse);

        PaymentInitiateResponse response =
                paymentService.initiatePayment(1L);

        assertEquals("rzp_order_123", response.getExternalOrderId());
        assertEquals(50000, response.getAmount()); // paise

        verify(paymentAttemptRepository, times(2))
                .save(any(PaymentAttempt.class));
    }

    // ---------------------------
    // ORDER NOT FOUND
    // ---------------------------
    @Test
    void initiatePayment_shouldFailIfOrderMissing() {

        when(orderRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> paymentService.initiatePayment(1L));
    }

    // ---------------------------
    // ORDER ALREADY PAID
    // ---------------------------
    @Test
    void initiatePayment_shouldFailIfAlreadyPaid() {

        Order order = new Order();
        order.setStatus(OrderStatus.COMPLETED);

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        assertThrows(BadRequestException.class,
                () -> paymentService.initiatePayment(1L));
    }
}
