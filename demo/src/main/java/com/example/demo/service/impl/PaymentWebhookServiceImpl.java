package com.example.demo.service.impl;

import com.example.demo.entity.*;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.NotFoundException;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.PaymentAttemptRepository;
import com.example.demo.repository.PaymentRepository;
import com.example.demo.service.PaymentWebhookService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.razorpay.Utils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class PaymentWebhookServiceImpl implements PaymentWebhookService {

    private final PaymentAttemptRepository paymentAttemptRepository;
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${razorpay.webhook.secret}")
    private String webhookSecret;

    @Override
    public void handleWebhook(byte[] rawPayload, String razorpaySignature) {

        // 1️⃣ Verify Signature
        verifySignature(rawPayload, razorpaySignature);

        try {
            // 2️⃣ Parse JSON
            JsonNode root = objectMapper.readTree(rawPayload);

            String event = root.get("event").asText();

            JsonNode paymentEntity =
                    root.path("payload")
                            .path("payment")
                            .path("entity");

            String razorpayOrderId = paymentEntity.get("order_id").asText();
            String razorpayPaymentId = paymentEntity.get("id").asText();

            String failureReason = paymentEntity.has("error_description")
                    ? paymentEntity.get("error_description").asText()
                    : null;

            // 3️⃣ Fetch PaymentAttempt
            PaymentAttempt attempt =
                    paymentAttemptRepository.findByExternalOrderId(razorpayOrderId)
                            .orElseThrow(() -> new NotFoundException("NOT_FOUND","PaymentAttempt not found"));

            Payment payment = attempt.getPayment();
            Order order = payment.getOrder();

            if (payment.getStatus() == PaymentStatus.SUCCESS) {
                return; // Ignore duplicate webhook
            }

            // 4️⃣ Event Handling
            if ("payment.captured".equals(event)) {

                attempt.setSuccess(true);
                attempt.setExternalPaymentId(razorpayPaymentId);
                attempt.setProviderResponse("Payment captured");

                payment.setStatus(PaymentStatus.SUCCESS);
                payment.setExternalPaymentId(razorpayPaymentId);

                order.setStatus(OrderStatus.COMPLETED);

            } else if ("payment.failed".equals(event)) {

                attempt.setSuccess(false);
                attempt.setExternalPaymentId(razorpayPaymentId);
                attempt.setProviderResponse(failureReason);

                payment.setStatus(PaymentStatus.FAILED);
                order.setStatus(OrderStatus.FAILED);
            }

            // 5️⃣ Persist
            paymentAttemptRepository.save(attempt);
            paymentRepository.save(payment);
            orderRepository.save(order);

        } catch (Exception e) {
            throw new BadRequestException("WEBHOOK_ERROR","Webhook processing failed");
        }
    }

    // 🔐 Razorpay HMAC SHA256 Verification
    private void verifySignature(byte[] rawPayload, String razorpaySignature) {
        try {
            String payload = new String(rawPayload, StandardCharsets.UTF_8);
            Utils.verifyWebhookSignature(payload, razorpaySignature, webhookSecret);
        } catch (Exception e) {
            throw new BadRequestException("RAZORPAY_SECURITY_EXCEPTION","Invalid Razorpay webhook signature");
        }
    }



}
