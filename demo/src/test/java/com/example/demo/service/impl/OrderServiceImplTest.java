package com.example.demo.service.impl;

import com.example.demo.dto.CreateOrderRequest;
import com.example.demo.dto.OrderItemRequest;
import com.example.demo.dto.OrderResponse;
import com.example.demo.entity.Customer;
import com.example.demo.entity.Order;
import com.example.demo.entity.Product;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.NotFoundException;
import com.example.demo.repository.CustomerRepository;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    // ---------------------------
    // CREATE ORDER SUCCESS
    // ---------------------------
    @Test
    void createOrder_shouldCreateSuccessfully() {

        String customerUuid = "cust-123";

        Customer customer = new Customer();
        customer.setId(1L);
        customer.setCustomerUuid(customerUuid);

        when(customerRepository.findByCustomerUuid(customerUuid))
                .thenReturn(Optional.of(customer));

        Product product = new Product();
        product.setId(10L);
        product.setPrice(BigDecimal.valueOf(100));
        product.setStock(10);

        when(productRepository.findById(10L))
                .thenReturn(Optional.of(product));

        OrderItemRequest itemRequest = new OrderItemRequest();
        itemRequest.setProductId(10L);
        itemRequest.setQuantity(2);

        CreateOrderRequest request = new CreateOrderRequest();
        request.setItems(List.of(itemRequest));

        OrderResponse response = orderService.createOrder(request, customerUuid);

        assertEquals(1L, response.getCustomerId());
        assertEquals(1, response.getItems().size());
        assertEquals(2, response.getItems().get(0).getQuantity());

        verify(orderRepository).save(any(Order.class));
        assertEquals(8, product.getStock());
    }

    // ---------------------------
    // CUSTOMER NOT FOUND
    // ---------------------------
    @Test
    void createOrder_shouldFailIfCustomerMissing() {

        when(customerRepository.findByCustomerUuid("bad"))
                .thenReturn(Optional.empty());

        CreateOrderRequest request = new CreateOrderRequest();
        request.setItems(List.of(
                new OrderItemRequest()
        ));

        assertThrows(NotFoundException.class,
                () -> orderService.createOrder(request, "bad"));

        verify(orderRepository, never()).save(any());
    }

    // ---------------------------
    // PRODUCT NOT FOUND
    // ---------------------------
    @Test
    void createOrder_shouldFailIfProductMissing() {

        Customer customer = new Customer();
        customer.setCustomerUuid("cust");

        when(customerRepository.findByCustomerUuid("cust"))
                .thenReturn(Optional.of(customer));

        when(productRepository.findById(99L))
                .thenReturn(Optional.empty());

        OrderItemRequest itemRequest = new OrderItemRequest();
        itemRequest.setProductId(99L);
        itemRequest.setQuantity(1);

        CreateOrderRequest request = new CreateOrderRequest();
        request.setItems(List.of(itemRequest));

        assertThrows(NotFoundException.class,
                () -> orderService.createOrder(request, "cust"));
    }

    // ---------------------------
    // INSUFFICIENT STOCK
    // ---------------------------
    @Test
    void createOrder_shouldFailIfStockInsufficient() {

        Customer customer = new Customer();
        customer.setCustomerUuid("cust");

        when(customerRepository.findByCustomerUuid("cust"))
                .thenReturn(Optional.of(customer));

        Product product = new Product();
        product.setId(1L);
        product.setStock(1);
        product.setPrice(BigDecimal.TEN);

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        OrderItemRequest itemRequest = new OrderItemRequest();
        itemRequest.setProductId(1L);
        itemRequest.setQuantity(5);

        CreateOrderRequest request = new CreateOrderRequest();
        request.setItems(List.of(itemRequest));

        assertThrows(BadRequestException.class,
                () -> orderService.createOrder(request, "cust"));
    }

    // ---------------------------
    // GET ORDER BY ID
    // ---------------------------
    @Test
    void getOrderById_shouldReturnOrder() {

        Order order = new Order();
        order.setId(1L);

        Customer customer = new Customer();
        customer.setId(2L);
        order.setCustomer(customer);

        // 🔧 FIX: set empty items list (or real one)
        order.setItems(List.of());

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        OrderResponse response = orderService.getOrderById(1L);

        assertEquals(1L, response.getId());
        assertEquals(2L, response.getCustomerId());
        assertNotNull(response.getItems());
    }


    // ---------------------------
    // GET ORDER BY ID NOT FOUND
    // ---------------------------
    @Test
    void getOrderById_shouldFailIfMissing() {

        when(orderRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> orderService.getOrderById(1L));
    }
}
