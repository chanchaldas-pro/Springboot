package com.example.demo.dto;

public class CustomerLoginRequest {
    public String email;
    private String password;

    public String getPassword(){
        return this.password;
    }
}

