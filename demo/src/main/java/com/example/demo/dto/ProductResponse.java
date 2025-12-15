package com.example.demo.dto;

import java.math.BigDecimal;
import java.util.UUID;

public class ProductResponse {

    private Long id;
    private UUID internalId;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stock;

    // Constructor
    public ProductResponse(Long id, UUID internalId, String name, String description,
                           BigDecimal price, Integer stock) {
        this.id = id;
        this.internalId = internalId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
    }

    // Getters ONLY (immutable DTO)
    public Long getId() { return id; }
    public UUID getInternalId() { return internalId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public BigDecimal getPrice() { return price; }
    public Integer getStock() { return stock; }
}

