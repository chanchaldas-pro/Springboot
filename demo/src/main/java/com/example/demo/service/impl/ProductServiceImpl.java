package com.example.demo.service.impl;

import com.example.demo.entity.CustomerRole;
import com.example.demo.entity.Product;
import com.example.demo.entity.Customer;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.NotFoundException;
import com.example.demo.exception.UnauthorizedException;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.CustomerRepository;
import com.example.demo.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.demo.security.JwtService;
import com.example.demo.dto.ProductResponse;
import com.example.demo.dto.ProductRequest;


import java.math.BigDecimal;
import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private CustomerRepository customerRepository;



    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found: " + id));

        return mapToResponse(product);
    }

    @Override
    public ProductResponse createProduct(ProductRequest request, String jwtToken) {

        // 1. Token must exist
        if (jwtToken == null || jwtToken.isBlank()) {
            throw new UnauthorizedException("You must be logged in to create a product");
        }

        // 2. Validate + extract customer UUID from token
        String customerUuid;
        try {
            JwtService jwtService = new JwtService();
            customerUuid = jwtService.extractCustomerUuid(jwtToken);
        } catch (Exception e) {
            throw new UnauthorizedException("Invalid or expired token");
        }

        // 3. Fetch customer from DB
        Customer customer = customerRepository.findByCustomerUuid(customerUuid)
                .orElseThrow(() -> new NotFoundException("Customer not found"));

        // 4. Check role
        if (customer.getCustomerRole() != CustomerRole.ADMIN) {
            throw new UnauthorizedException("Only ADMIN can create products");
        }

        // 5. Map ProductRequest → Product entity
        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());

        // 6. Save product
        Product saved = productRepository.save(product);

        // 7. Return ProductResponse
        return mapToResponse(saved);
    }

    // -------------------------------------------------
// MAPPING METHOD
// -------------------------------------------------
    private ProductResponse mapToResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getInternalId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStock()
        );
    }


}
