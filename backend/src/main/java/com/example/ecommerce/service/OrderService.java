package com.example.ecommerce.service;

import com.example.ecommerce.model.Cart;
import com.example.ecommerce.model.CartItem;
import com.example.ecommerce.model.Order;
import com.example.ecommerce.model.OrderItem;
import com.example.ecommerce.model.Product;
import com.example.ecommerce.repository.CustomerRepository;
import com.example.ecommerce.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ProductService productService;
    private final CartService cartService;

    public OrderService(OrderRepository orderRepository, CustomerRepository customerRepository, ProductService productService, CartService cartService) {
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.productService = productService;
        this.cartService = cartService;
    }

    public List<Order> findAll() {
        return orderRepository.findAll();
    }

    public List<Order> findByCustomer(String customerId) {
        return orderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
    }

    @Transactional
    public Order checkout(String customerId) {
        if (!customerRepository.existsById(customerId)) {
            throw new IllegalArgumentException("Customer not found: " + customerId);
        }
        Cart cart = cartService.getOrCreateCart(customerId);
        if (cart.getItems().isEmpty()) {
            throw new IllegalStateException("Cannot checkout with an empty cart");
        }

        cart.getItems().forEach(cartItem -> {
            Product product = productService.findById(cartItem.getProductId());
            if (product.getStock() < cartItem.getQuantity()) {
                throw new IllegalStateException("Not enough stock to complete the order for product " + product.getName());
            }
        });

        List<OrderItem> items = cart.getItems().stream()
                .map(this::createOrderItem)
                .collect(Collectors.toList());

        BigDecimal total = items.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Order order = new Order(customerId, items, total);
        Order saved = orderRepository.save(order);

        cart.getItems().forEach(cartItem -> productService.decrementStock(cartItem.getProductId(), cartItem.getQuantity()));
        cartService.emptyCart(cart);

        return saved;
    }

    private OrderItem createOrderItem(CartItem cartItem) {
        Product product = productService.findById(cartItem.getProductId());
        return new OrderItem(product.getId(), product.getName(), cartItem.getQuantity(), product.getPrice());
    }
}
