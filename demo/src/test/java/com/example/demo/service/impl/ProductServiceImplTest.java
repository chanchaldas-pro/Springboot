package com.example.demo.service.impl;

import com.example.demo.dto.ProductRequest;
import com.example.demo.dto.ProductResponse;
import com.example.demo.entity.Customer;
import com.example.demo.entity.Product;
import com.example.demo.exception.NotFoundException;
import com.example.demo.repository.CustomerRepository;
import com.example.demo.repository.ProductRepository;
import com.example.demo.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private ProductServiceImpl productService;

    @Test
    void getAllProducts_shouldReturnProductResponses() {

        Product product1 = new Product();
        product1.setId(1L);
        product1.setName("Phone");
        product1.setDescription("Smart phone");
        product1.setPrice(BigDecimal.valueOf(500));
        product1.setStock(10);

        Product product2 = new Product();
        product2.setId(2L);
        product2.setName("Laptop");
        product2.setDescription("Gaming laptop");
        product2.setPrice(BigDecimal.valueOf(1500));
        product2.setStock(5);

        when(productRepository.findAll())
                .thenReturn(List.of(product1, product2));

        List<ProductResponse> responses = productService.getAllProducts();

        assertEquals(2, responses.size());
        assertEquals("Phone", responses.get(0).getName());
        assertEquals("Laptop", responses.get(1).getName());
    }

    @Test
    void getProductById_shouldReturnProduct() {

        Product product = new Product();
        product.setId(1L);
        product.setName("Tablet");
        product.setDescription("Android tablet");
        product.setPrice(BigDecimal.valueOf(300));
        product.setStock(7);

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        ProductResponse response = productService.getProductById(1L);

        assertEquals(1L, response.getId());
        assertEquals("Tablet", response.getName());
    }

    @Test
    void getProductById_notFound_shouldThrowException() {

        when(productRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> productService.getProductById(99L)
        );
    }

    @Test
    void createProduct_shouldCreateSuccessfully() {

        ProductRequest request = new ProductRequest();
        request.setName("Headphones");
        request.setDescription("Wireless");
        request.setPrice(BigDecimal.valueOf(200));
        request.setStock(20);

        Customer customer = new Customer();
        customer.setCustomerUuid("cust-123");

        when(customerRepository.findByCustomerUuid("cust-123"))
                .thenReturn(Optional.of(customer));

        Product savedProduct = new Product();
        savedProduct.setId(1L);
        savedProduct.setName("Headphones");
        savedProduct.setDescription("Wireless");
        savedProduct.setPrice(BigDecimal.valueOf(200));
        savedProduct.setStock(20);

        when(productRepository.save(any(Product.class)))
                .thenReturn(savedProduct);

        ProductResponse response =
                productService.createProduct(request, "cust-123");

        assertEquals("Headphones", response.getName());
        assertEquals(BigDecimal.valueOf(200), response.getPrice());

        verify(productRepository).save(any(Product.class));
    }

    @Test
    void createProduct_customerNotFound_shouldThrowException() {

        ProductRequest request = new ProductRequest();
        request.setName("Mouse");

        when(customerRepository.findByCustomerUuid("missing"))
                .thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> productService.createProduct(request, "missing")
        );

        verify(productRepository, never()).save(any());
    }
}
