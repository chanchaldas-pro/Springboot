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
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found: " + id));
    }

    @Override
    public Product createProduct(Product product, String jwtToken) {

        // 1. Token must exist
        if (jwtToken == null || jwtToken.isBlank()) {
            throw new UnauthorizedException("You must be logged in to create a product");
        }

        // 2. Validate + extract customer UUID from token
        String customerUuid;
        try {
            JwtService jwtService= new JwtService();
            customerUuid =  jwtService.extractCustomerUuid(jwtToken);
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

        // 5. Optional validation on product fields
        if (product.getName() == null || product.getName().isBlank()) {
            throw new BadRequestException("Product name is required");
        }

        if (product.getPrice() == null || product.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Product price must be greater than 0");
        }


        // 6. Save product
        return productRepository.save(product);
    }

}
