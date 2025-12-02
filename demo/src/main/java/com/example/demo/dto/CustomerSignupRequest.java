package com.example.demo.dto;

import jakarta.validation.constraints.Email;

public class CustomerSignupRequest {
    public String name;
    public String email;
    public String phone;
    public String address;
    private String password;

    public String getEmail(){
        return this.email;
    }
    public String getPassword(){
        return this.password;
    }

}

