package com.example.demo.controller;

import com.example.demo.dto.PaymentInitiateResponse;
import com.example.demo.entity.Payment;
import com.example.demo.service.PaymentService;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.reactive.result.view.RedirectView;

@RestController
@RequestMapping("/api/v1/payment")
public class PaymentController {

    @Value("${razorpay.key.id}")
    private String razorpayKeyId;


    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret; // Replace with your Key Secret

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/{orderId}/initiate")
    public ResponseEntity<PaymentInitiateResponse> initiatePayment(@PathVariable Long orderId) {
        PaymentInitiateResponse res = paymentService.initiatePayment(orderId);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }



    @PostMapping("/callback")
    public RedirectView paymentCallback(
            @RequestParam("razorpay_order_id") String razorpayOrderId,
            @RequestParam("razorpay_payment_id") String razorpayPaymentId,
            @RequestParam("razorpay_signature") String razorpaySignature) throws RazorpayException {
        try {
            // Verify the payment signature here
            String signature = razorpayOrderId + "|" + razorpayPaymentId;
            boolean isValid = Utils.verifySignature(signature, razorpaySignature, razorpayKeySecret);

            if (isValid) {
                // Payment successful
                RedirectView redirectView = new RedirectView("/success.html?orderId=" + razorpayOrderId);
                return redirectView;
            } else {
                // Payment failed
                return new RedirectView("/failure.html"); // Create failure.html if needed
            }
        } catch (RazorpayException e) {
            System.err.println("Razorpay Exception during callback: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.err.println("General Exception during callback: " + e.getMessage());
            throw new RazorpayException("General exception during callback");
        }
    }

    @PostMapping("/get-key")
    public String getKey() {
        return razorpayKeyId;
    }






}


    // Optional but helpful:
    // GET /api/v1/payment/{id}
//    @GetMapping("/payment/{id}")
//    public ResponseEntity<Payment> getPaymentById(@PathVariable Long id) {
//        Payment payment = paymentService.getPaymentById(id);
//        return ResponseEntity.ok(payment);
//    }

