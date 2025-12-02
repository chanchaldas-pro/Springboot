package com.example.demo.service.impl;

import com.example.demo.dto.*;
import com.example.demo.entity.Customer;
import com.example.demo.repository.CustomerRepository;
import com.example.demo.security.JwtService;
import com.example.demo.service.CustomerService;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    public Customer signup(CustomerSignupRequest request) {
        if (customerRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already registered!");
        }

        Customer customer = Customer.builder()
                .name(request.name)
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        return customerRepository.save(customer);
    }

    @Override
    public Customer login(CustomerLoginRequest request) {

        Customer customer = customerRepository.findByEmail(request.email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), customer.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        String token = jwtService.generateToken(customer.getUuid());

        return new AuthResponse(token, customer.getUuid());
    }

    @Override
    public Customer getCustomerByUuid(String uuid) {
        return customerRepository.findByUuid(uuid)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
    }
}

