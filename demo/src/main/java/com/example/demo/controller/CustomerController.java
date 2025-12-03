package com.example.demo.controller;

import com.example.demo.dto.CustomerResponse;
import com.example.demo.dto.CustomerSignupRequest;
import com.example.demo.entity.Customer;
import com.example.demo.dto.CustomerLoginRequest;
import com.example.demo.dto.AuthResponse;
import com.example.demo.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customer")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    // SIGNUP
    @PostMapping("/signup")
    public CustomerResponse signup(@RequestBody CustomerSignupRequest request) {
        return customerService.signup(request);
    }

    // LOGIN
    @PostMapping("/login")
    public AuthResponse login(@RequestBody CustomerLoginRequest request) {
        return customerService.login(request);
    }
}
