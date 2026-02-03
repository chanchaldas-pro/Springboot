package com.example.demo.service.impl;

import com.example.demo.entity.*;
import com.example.demo.exception.BadRequestException;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.PaymentAttemptRepository;
import com.example.demo.repository.PaymentRepository;
import com.razorpay.Utils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentWebhookServiceImplTest {

    @Mock
    private PaymentAttemptRepository paymentAttemptRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private PaymentWebhookServiceImpl paymentWebhookService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(
                paymentWebhookService,
                "webhookSecret",
                "test_secret"
        );
    }

    @Test
    void handleWebhook_paymentCaptured_successFlow() {

        String payload = """
        {
          "event": "payment.captured",
          "payload": {
            "payment": {
              "entity": {
                "id": "pay_123",
                "order_id": "order_123"
              }
            }
          }
        }
        """;

        byte[] rawPayload = payload.getBytes(StandardCharsets.UTF_8);

        Order order = new Order();
        order.setStatus(OrderStatus.PENDING);

        Payment payment = new Payment();
        payment.setStatus(PaymentStatus.PENDING);
        payment.setOrder(order);

        PaymentAttempt attempt = new PaymentAttempt();
        attempt.setPayment(payment);

        when(paymentAttemptRepository.findByExternalOrderId("order_123"))
                .thenReturn(Optional.of(attempt));

        try (MockedStatic<Utils> utilsMock = mockStatic(Utils.class)) {

            utilsMock.when(() ->
                    Utils.verifyWebhookSignature(any(), any(), any())
            ).thenReturn(true);

            paymentWebhookService.handleWebhook(rawPayload, "signature");
        }

        assertEquals(PaymentStatus.SUCCESS, payment.getStatus());
        assertEquals(OrderStatus.COMPLETED, order.getStatus());
        assertTrue(attempt.isSuccess());

        verify(paymentAttemptRepository).save(attempt);
        verify(paymentRepository).save(payment);
        verify(orderRepository).save(order);
    }

    @Test
    void handleWebhook_paymentFailed_failureFlow() {

        String payload = """
        {
          "event": "payment.failed",
          "payload": {
            "payment": {
              "entity": {
                "id": "pay_999",
                "order_id": "order_999",
                "error_description": "Insufficient balance"
              }
            }
          }
        }
        """;

        byte[] rawPayload = payload.getBytes(StandardCharsets.UTF_8);

        Order order = new Order();
        order.setStatus(OrderStatus.PENDING);

        Payment payment = new Payment();
        payment.setStatus(PaymentStatus.PENDING);
        payment.setOrder(order);

        PaymentAttempt attempt = new PaymentAttempt();
        attempt.setPayment(payment);

        when(paymentAttemptRepository.findByExternalOrderId("order_999"))
                .thenReturn(Optional.of(attempt));

        try (MockedStatic<Utils> utilsMock = mockStatic(Utils.class)) {

            utilsMock.when(() ->
                    Utils.verifyWebhookSignature(any(), any(), any())
            ).thenReturn(true);

            paymentWebhookService.handleWebhook(rawPayload, "signature");
        }

        assertEquals(PaymentStatus.FAILED, payment.getStatus());
        assertEquals(OrderStatus.FAILED, order.getStatus());
        assertFalse(attempt.isSuccess());

        verify(paymentAttemptRepository).save(attempt);
        verify(paymentRepository).save(payment);
        verify(orderRepository).save(order);
    }

    @Test
    void handleWebhook_invalidSignature_shouldThrowException() {

        byte[] rawPayload = "{}".getBytes(StandardCharsets.UTF_8);

        try (MockedStatic<Utils> utilsMock = mockStatic(Utils.class)) {

            utilsMock.when(() ->
                    Utils.verifyWebhookSignature(any(), any(), any())
            ).thenThrow(new RuntimeException("invalid"));

            assertThrows(
                    BadRequestException.class,
                    () -> paymentWebhookService.handleWebhook(rawPayload, "bad-signature")
            );
        }
    }
}

