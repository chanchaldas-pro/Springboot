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


        public ProductResponse createProduct(ProductRequest request, String customerUuid) {

            // (Optional) business validation: ensure customer exists
            Customer customer = customerRepository.findByCustomerUuid(customerUuid)
                    .orElseThrow(() -> new NotFoundException("Customer not found"));

            // Business logic only
            Product product = new Product();
            product.setName(request.getName());
            product.setDescription(request.getDescription());
            product.setPrice(request.getPrice());
            product.setStock(request.getStock());

            Product saved = productRepository.save(product);
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
