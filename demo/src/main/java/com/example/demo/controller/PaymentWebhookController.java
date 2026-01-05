package com.example.demo.controller;

import com.example.demo.service.PaymentWebhookService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/webhook")
public class PaymentWebhookController {

    private final PaymentWebhookService paymentWebhookService;

    public PaymentWebhookController(PaymentWebhookService paymentWebhookService) {
        this.paymentWebhookService = paymentWebhookService;
    }

    @PostMapping("/payment")
    public ResponseEntity<String> handleWebhook(HttpServletRequest request,
                                                @RequestHeader("x-razorpay-signature") String razorpaySignature) throws IOException {

        // ✅ Read RAW bytes
        byte[] rawBody = request.getInputStream().readAllBytes();

        paymentWebhookService.handleWebhook(rawBody, razorpaySignature);

        return ResponseEntity.ok("Webhook received");
    }


}

