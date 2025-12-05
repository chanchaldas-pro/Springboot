package com.example.demo.dto;

import com.example.demo.entity.CustomerRole;

public class CustomerSignupRequest {
    public String name;
    public String email;
    public String phone;
    public String address;
    private String password;
    private CustomerRole customerRole;

    public String getEmail(){
        return this.email;
    }
    public String getPassword(){
        return this.password;
    }

    public CustomerRole getCustomerRole(){return this.customerRole;}



}

