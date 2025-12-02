package com.example.demo.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.net.ProtocolFamily;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Builder
@Table(name = "customers")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;   // INTERNAL ID (DB only, never exposed)

    @Column(unique = true, updatable = false, nullable = false)
    private String customerUuid = UUID.randomUUID().toString();   // EXTERNAL ID

    @NotBlank(message = "Name is required")
    private String name;

    @Email(message = "Email is not valid")
    @Column(unique = true)
    private String email;


    private String phone;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;


    private String address;

    @Enumerated(EnumType.STRING)
    private CustomerRole customerRole = CustomerRole.USER;

    // ---------------------------
    // ONE CUSTOMER → MANY ORDERS
    // ---------------------------
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Order> orders = new ArrayList<>();





    // -------------------------
    //    GETTERS & SETTERS
    // -------------------------

    public Long getId() {
        return id;
    }

    public String getCustomerUuid() {
        return customerUuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public CustomerRole getCustomerRole() {
        return customerRole;
    }

    public void setCustomerRole(CustomerRole customerRole) {
        this.customerRole = customerRole;
    }

    public List<Order> getOrders() {
        return orders;
    }
}
