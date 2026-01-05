package com.example.demo.repository;

import com.example.demo.entity.PaymentAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentAttemptRepository extends JpaRepository<PaymentAttempt, Long> {

    List<PaymentAttempt> findByPaymentId(Long paymentId);
    Optional<PaymentAttempt> findByExternalOrderId(String externalOrderId);
}
