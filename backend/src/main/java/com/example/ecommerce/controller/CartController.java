package com.example.ecommerce.controller;

import com.example.ecommerce.dto.AddCartItemRequest;
import com.example.ecommerce.dto.UpdateCartItemRequest;
import com.example.ecommerce.model.Cart;
import com.example.ecommerce.service.CartService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/carts")
@CrossOrigin(origins = "*")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping("/{customerId}")
    public Cart getCart(@PathVariable String customerId) {
        return cartService.getOrCreateCart(customerId);
    }

    @PostMapping("/{customerId}/items")
    public Cart addItem(@PathVariable String customerId, @Valid @RequestBody AddCartItemRequest request) {
        return cartService.addItem(customerId, request.getProductId(), request.getQuantity());
    }

    @PatchMapping("/{customerId}/items/{productId}")
    public Cart updateQuantity(@PathVariable String customerId,
                               @PathVariable String productId,
                               @Valid @RequestBody UpdateCartItemRequest request) {
        return cartService.updateQuantity(customerId, productId, request.getQuantity());
    }

    @DeleteMapping("/{customerId}/items/{productId}")
    public Cart removeItem(@PathVariable String customerId, @PathVariable String productId) {
        return cartService.removeItem(customerId, productId);
    }
}
