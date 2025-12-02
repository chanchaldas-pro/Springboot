package com.example.demo.service;

import com.example.demo.dto.CustomerSignupRequest;
import com.example.demo.dto.CustomerLoginRequest;

import com.example.demo.entity.Customer;

public interface CustomerService {
    Customer signup(CustomerSignupRequest request);
    Customer login(CustomerLoginRequest request);
    Customer getCustomerByUuid(String uuid);
}

