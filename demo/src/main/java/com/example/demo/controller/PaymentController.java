package com.example.demo.controller;

import com.example.demo.dto.PaymentInitiateResponse;
import com.example.demo.entity.Payment;
import com.example.demo.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/v1/payment")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/{orderId}/initiate")
    public ResponseEntity<PaymentInitiateResponse> initiatePayment(@PathVariable Long orderId) {
        PaymentInitiateResponse res = paymentService.initiatePayment(orderId);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

//    @PostMapping("/callback")
//    public ResponseEntity<String> paymentCallback(
//            @RequestParam String externalPaymentId,
//            @RequestParam String status
//    ) {
//        paymentService.updatePaymentStatus(externalPaymentId, status);
//        return ResponseEntity.ok("Callback processed");
//    }
}


    // Optional but helpful:
    // GET /api/v1/payment/{id}
//    @GetMapping("/payment/{id}")
//    public ResponseEntity<Payment> getPaymentById(@PathVariable Long id) {
//        Payment payment = paymentService.getPaymentById(id);
//        return ResponseEntity.ok(payment);
//    }

