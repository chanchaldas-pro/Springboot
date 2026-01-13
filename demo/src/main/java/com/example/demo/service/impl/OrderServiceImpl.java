package com.example.demo.service.impl;
import com.example.demo.dto.CreateOrderRequest;
import com.example.demo.dto.OrderItemResponse;
import com.example.demo.dto.OrderResponse;
import com.example.demo.entity.Customer;
import com.example.demo.entity.Order;
import com.example.demo.entity.OrderItem;
import com.example.demo.entity.Product;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.NotFoundException;
import com.example.demo.exception.UnauthorizedException;
import com.example.demo.repository.CustomerRepository;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.ProductRepository;
import com.example.demo.security.JwtService;
import com.example.demo.service.OrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;

    public OrderServiceImpl(OrderRepository orderRepository,
                            CustomerRepository customerRepository,
                            ProductRepository productRepository) {

        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
    }

    // --------------------
    // CREATE ORDER LOGIC
    // --------------------
    @Override

    public OrderResponse createOrder(CreateOrderRequest request,String customerUuid) {
        // 3. Fetch customer from DB
        Customer customer = customerRepository.findByCustomerUuid(customerUuid)
                .orElseThrow(() -> new NotFoundException("NOT_FOUND","Customer not found"));

        // 2️⃣ Create Order
        Order order = new Order();
        order.setCustomer(customer);

        // 3️⃣ Convert items
        var orderItems = request.getItems().stream().map(itemDto -> {

            Product product = productRepository.findById(itemDto.getProductId())
                    .orElseThrow(() -> new NotFoundException("NOT_FOUND","Product not found: " + itemDto.getProductId()));

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProduct(product);
            item.setQuantity(itemDto.getQuantity());
            item.setPrice(product.getPrice()); // Lock price

            int NetStock =product.getStock()-itemDto.getQuantity();
            if(NetStock<0){
                throw new BadRequestException("BAD_REQUEST","Order Quantity can not greater than Product in Stock");
            }

            product.setStock(NetStock);



            return item;

        }).collect(Collectors.toList());

        order.setItems(orderItems);

        // 4️⃣ Compute total
        order.recomputeTotal();

        // 5️⃣ Save order + items (cascade)
        orderRepository.save(order);

        // 6️⃣ Return DTO response
        return convertToResponse(order);
    }

    // --------------------
    // GET ORDER BY ID
    // --------------------
    @Override
    public OrderResponse getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("NOT_FOUND","Order not found: " + id));

        return convertToResponse(order);
    }

    @Override
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll()
                .stream()
                .map(this::convertToResponse)
                .toList();
    }



    // --------------------
    // ENTITY → DTO
    // --------------------
    private OrderResponse convertToResponse(Order order) {

        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setInternalId(order.getInternalId());
        response.setCustomerId(order.getCustomer().getId());
        response.setOrderDate(order.getOrderDate());
        response.setStatus(order.getStatus().name());
        response.setTotalAmount(order.getTotalAmount());

        var itemDtos = order.getItems().stream().map(i -> {
            OrderItemResponse item = new OrderItemResponse();
            item.setProductId(i.getProduct().getId());
            item.setQuantity(i.getQuantity());
            item.setPrice(i.getPrice());
            return item;
        }).collect(Collectors.toList());

        response.setItems(itemDtos);

        return response;
    }
}
