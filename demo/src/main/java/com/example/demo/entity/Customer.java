package com.example.demo.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "customers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_uuid", unique = true, updatable = false, nullable = false)
    private String customerUuid;


    private String name;

    @Email(message = "Email is not valid")
    @Column(unique = true)
    @NotBlank(message = "Email is required")
    private String email;

    private String phone;

    @NotBlank(message = "Password is required")
    @Size(min = 7, message = "Password must be at least 6 characters")
    private String password;

    private String address;

    @Enumerated(EnumType.STRING)
    private CustomerRole customerRole ;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Order> orders = new ArrayList<>();

    // Auto-generate UUID BEFORE saving
    @PrePersist
    public void generateUuid() {
        if (this.customerUuid == null || this.customerUuid.isEmpty()) {
            this.customerUuid = UUID.randomUUID().toString();
        }
    }
    public  String getUuid(){
        return customerUuid;
    }

    public List<Order> getOrders() {
        return orders;
    }
}
