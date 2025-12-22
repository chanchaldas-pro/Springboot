package com.example.demo.service.impl;

import com.example.demo.client.PaymentClient;
import com.example.demo.dto.PaymentInitiateRequest;
import com.example.demo.dto.PaymentInitiateResponse;
import com.example.demo.entity.*;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.PaymentAttemptRepository;
import com.example.demo.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
@Transactional


public class PaymentServiceImpl {

    private final PaymentRepository paymentRepository;
    private final PaymentAttemptRepository attemptRepository;
    private final PaymentClient paymentClient;

    public String initiatePayment(Long orderId) {
        OrderRepository orderrepo=new OrderRepository() {
            @Override
            public List<Order> findByCustomerId(Long customerId) {
                return List.of();
            }

            @Override
            public List<Order> findByStatus(OrderStatus status) {
                return List.of();
            }

            @Override
            public List<Order> findByID(Long orderId) {
                return List.of();
            }

            @Override
            public void flush() {

            }

            @Override
            public <S extends Order> S saveAndFlush(S entity) {
                return null;
            }

            @Override
            public <S extends Order> List<S> saveAllAndFlush(Iterable<S> entities) {
                return List.of();
            }

            @Override
            public void deleteAllInBatch(Iterable<Order> entities) {

            }

            @Override
            public void deleteAllByIdInBatch(Iterable<Long> longs) {

            }

            @Override
            public void deleteAllInBatch() {

            }

            @Override
            public Order getOne(Long aLong) {
                return null;
            }

            @Override
            public Order getById(Long aLong) {
                return null;
            }

            @Override
            public Order getReferenceById(Long aLong) {
                return null;
            }

            @Override
            public <S extends Order> List<S> findAll(Example<S> example) {
                return List.of();
            }

            @Override
            public <S extends Order> List<S> findAll(Example<S> example, Sort sort) {
                return List.of();
            }

            @Override
            public <S extends Order> List<S> saveAll(Iterable<S> entities) {
                return List.of();
            }

            @Override
            public List<Order> findAll() {
                return List.of();
            }

            @Override
            public List<Order> findAllById(Iterable<Long> longs) {
                return List.of();
            }

            @Override
            public <S extends Order> S save(S entity) {
                return null;
            }

            @Override
            public Optional<Order> findById(Long aLong) {
                return Optional.empty();
            }

            @Override
            public boolean existsById(Long aLong) {
                return false;
            }

            @Override
            public long count() {
                return 0;
            }

            @Override
            public void deleteById(Long aLong) {

            }

            @Override
            public void delete(Order entity) {

            }

            @Override
            public void deleteAllById(Iterable<? extends Long> longs) {

            }

            @Override
            public void deleteAll(Iterable<? extends Order> entities) {

            }

            @Override
            public void deleteAll() {

            }

            @Override
            public List<Order> findAll(Sort sort) {
                return List.of();
            }

            @Override
            public Page<Order> findAll(Pageable pageable) {
                return null;
            }

            @Override
            public <S extends Order> Optional<S> findOne(Example<S> example) {
                return Optional.empty();
            }

            @Override
            public <S extends Order> Page<S> findAll(Example<S> example, Pageable pageable) {
                return null;
            }

            @Override
            public <S extends Order> long count(Example<S> example) {
                return 0;
            }

            @Override
            public <S extends Order> boolean exists(Example<S> example) {
                return false;
            }

            @Override
            public <S extends Order, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
                return null;
            }
        };
        List<Order> order=orderrepo.findByID(orderId);
        Payment payment = paymentRepository
                .findByOrder(order.get(0))
                .orElseGet(() -> createPayment(order.get(0)));





        for (int i = 1; i <= 3; i++) {
            try {
                PaymentInitiateResponse response =
                        paymentClient.initiatePayment(buildRequest(order.get(0)));

                payment.setExternalPaymentId(response.getPaymentId());
                paymentRepository.save(payment);

                saveAttempt(payment, response.toString(), true);

                return response.getRedirectUrl();

            } catch (Exception ex) {
                saveAttempt(payment, ex.getMessage(), false);
            }
        }

        payment.setStatus(PaymentStatus.UNKNOWN);
        order.get(0).setStatus(OrderStatus.FAILED);

        return null;
    }

    private Payment createPayment(Order order) {
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setAmount(order.getTotalAmount());
        return paymentRepository.save(payment);
    }

    private void saveAttempt(Payment payment, String response, boolean success) {
        PaymentAttempt attempt = new PaymentAttempt();
        attempt.setPayment(payment);
        attempt.setProviderResponse(response);
        attempt.setSuccess(success);
        attemptRepository.save(attempt);
    }

    private PaymentInitiateRequest buildRequest(Order order) {
        PaymentInitiateRequest request = new PaymentInitiateRequest();
        request.setOrderId(order.getId());
        request.setAmount(order.getTotalAmount());
        request.setCallbackUrl("https://yourapp.com/payments/callback");
        return request;
    }
}

