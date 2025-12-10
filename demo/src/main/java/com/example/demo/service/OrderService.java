package com.example.demo.service;

import com.example.demo.dto.CreateOrderRequest;
import com.example.demo.dto.OrderResponse;

import java.util.List;

public interface OrderService {

    OrderResponse createOrder(CreateOrderRequest request, String jwtToken);

    OrderResponse getOrderById(Long id);
    List<OrderResponse> getAllOrders();

}
