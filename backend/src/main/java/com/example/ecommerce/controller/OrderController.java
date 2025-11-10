package com.example.ecommerce.controller;

import com.example.ecommerce.dto.CheckoutRequest;
import com.example.ecommerce.model.Order;
import com.example.ecommerce.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public List<Order> listOrders() {
        return orderService.findAll();
    }

    @GetMapping("/customers/{customerId}")
    public List<Order> listOrdersForCustomer(@PathVariable String customerId) {
        return orderService.findByCustomer(customerId);
    }

    @PostMapping("/checkout")
    @ResponseStatus(HttpStatus.CREATED)
    public Order checkout(@Valid @RequestBody CheckoutRequest request) {
        return orderService.checkout(request.getCustomerId());
    }
}
