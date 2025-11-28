package com.example.demo.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "customers")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private UUID internalId = UUID.randomUUID();

    @NotBlank
    private String fullName;

    @Email
    @Column(unique = true)
    private String email;

    private String phone;

    @OneToMany(mappedBy = "customer")
    private List<Order> orders;

    // Getters & Setters
}
