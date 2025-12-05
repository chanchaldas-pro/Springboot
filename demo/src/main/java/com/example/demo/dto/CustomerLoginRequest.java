package com.example.demo.dto;

import com.example.demo.entity.CustomerRole;

public class CustomerLoginRequest {
    public String email;
    private String password;
    private CustomerRole role;

    public String getPassword(){
        return this.password;
    }

    public CustomerRole getRole(){
        return this.role;
    }
}

