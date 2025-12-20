package com.example.demo.service;

import com.example.demo.dto.AuthResponse;
import com.example.demo.dto.CustomerLoginRequest;
import com.example.demo.dto.CustomerResponse;
import com.example.demo.dto.CustomerSignupRequest;
import com.example.demo.entity.Customer;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.Email;
import org.springframework.http.ResponseEntity;

public interface CustomerService {
    CustomerResponse signup(CustomerSignupRequest request);
    AuthResponse login(CustomerLoginRequest request);
    Customer getCustomerByEmail(String email);
    Void logout(HttpServletResponse response);
}

