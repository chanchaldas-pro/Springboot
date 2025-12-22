package com.example.demo.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "payment_attempts")
public class PaymentAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private UUID internalId = UUID.randomUUID();

    @ManyToOne
    @JoinColumn(name = "payment_id")
    private Payment payment;

    private LocalDateTime attemptTime = LocalDateTime.now();

    private String providerResponse;

    private boolean success;

    // Getters & Setters

    public void setPayment(Payment payment){
        this.payment=payment;
    }

    public void setProviderResponse(String response){
        this.providerResponse=response;
    }

    public void setSuccess(boolean success){
        this.success=success;
    }
}
