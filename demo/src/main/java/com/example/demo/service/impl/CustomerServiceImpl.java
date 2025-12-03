package com.example.demo.service.impl;

import com.example.demo.dto.AuthResponse;
import com.example.demo.dto.CustomerLoginRequest;
import com.example.demo.dto.CustomerResponse;
import com.example.demo.dto.CustomerSignupRequest;
import com.example.demo.entity.Customer;
import com.example.demo.repository.CustomerRepository;
import com.example.demo.security.JwtService;
import com.example.demo.service.CustomerService;
import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.demo.entity.CustomerRole;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    public CustomerResponse signup(CustomerSignupRequest request) {
        if (customerRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already registered!");
        }

        Customer customer = Customer.builder()
                .name(request.name)
                .email(request.email)
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.phone)
                .address(request.address)
                .customerRole(
                        request.getCustomerRole() != null
                                ? request.getCustomerRole()
                                : CustomerRole.USER
                )
                .build();

        Customer cs = customerRepository.save(customer);

        CustomerResponse csr = new CustomerResponse();
        csr.customerUuid = cs.getCustomerUuid();  // FIX 1
        csr.name = cs.getName();
        csr.email = cs.getEmail();
        csr.phone = cs.getPhone();                // FIX 2
        csr.address = cs.getAddress();
        csr.role = cs.getCustomerRole();

        return csr;
    }




    @Override
    public AuthResponse login(CustomerLoginRequest request) {

        Customer customer = customerRepository.findByEmail(request.email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), customer.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        String token = jwtService.generateToken(customer.getUuid(),customer.getCustomerRole());

        return new AuthResponse(token, customer.getUuid());
    }

    @Override
    public Customer getCustomerByEmail(String email) {
        return customerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
    }
}

