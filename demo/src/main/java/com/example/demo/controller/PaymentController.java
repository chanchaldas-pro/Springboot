package com.example.demo.controller;

import com.example.demo.entity.Payment;
import com.example.demo.service.PaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")   // Base API version
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    // POST /api/v1/payment/{orderId}/initiate
    @PostMapping("/payment/{orderId}/initiate")
    public ResponseEntity<Payment> initiatePayment(@PathVariable Long orderId) {

        // SECURITY NOTE: Do NOT log sensitive info anywhere here.
        Payment payment = paymentService.initiatePayment(orderId);

        return ResponseEntity.status(HttpStatus.CREATED).body(payment);
    }

    // POST /api/v1/payment/callback
    @PostMapping("/payment/callback")
    public ResponseEntity<String> paymentCallback(
            @RequestParam("externalPaymentId") String externalPaymentId,
            @RequestParam("status") String status
    ) {
        paymentService.updatePaymentStatus(externalPaymentId, status);
        return ResponseEntity.ok("Callback processed");
    }

    // Optional but helpful:
    // GET /api/v1/payment/{id}
//    @GetMapping("/payment/{id}")
//    public ResponseEntity<Payment> getPaymentById(@PathVariable Long id) {
//        Payment payment = paymentService.getPaymentById(id);
//        return ResponseEntity.ok(payment);
//    }
}
