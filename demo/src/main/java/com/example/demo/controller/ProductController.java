package com.example.demo.controller;

import com.example.demo.entity.Product;
import com.example.demo.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/api/v1")   // BASE PATH ONLY
public class ProductController {

    private final ProductService productService;


    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // GET /api/v1/products  → list all products
    @GetMapping("/products")
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    // GET /api/v1/product/{id}  → single product
    @GetMapping("/product/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        Product product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }









    // POST /api/v1/product  → create product
    @PostMapping("/products/new")
    public ResponseEntity<Product> createProduct(
            @RequestBody Product product,
            @CookieValue(name = "jwt_token", required = true) String jwtToken
    ) {
        System.out.println(jwtToken);
        Product saved = productService.createProduct(product, null);
        return ResponseEntity.ok(saved);
    }



}
