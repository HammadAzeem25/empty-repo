package com.example.ecommerce.service;

import com.example.ecommerce.model.Cart;
import com.example.ecommerce.model.CartItem;
import com.example.ecommerce.model.Product;
import com.example.ecommerce.repository.CartRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final ProductService productService;

    public CartService(CartRepository cartRepository, ProductService productService) {
        this.cartRepository = cartRepository;
        this.productService = productService;
    }

    public Cart getOrCreateCart(String customerId) {
        Optional<Cart> cartOptional = cartRepository.findByCustomerId(customerId);
        return cartOptional.orElseGet(() -> cartRepository.save(new Cart(customerId)));
    }

    @Transactional
    public Cart addItem(String customerId, String productId, int quantity) {
        Cart cart = getOrCreateCart(customerId);
        Product product = productService.findById(productId);
        if (product.getStock() < quantity) {
            throw new IllegalStateException("Cannot add more items than available in stock");
        }
        List<CartItem> items = cart.getItems();
        CartItem existing = items.stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst()
                .orElse(null);
        if (existing == null) {
            items.add(new CartItem(productId, product.getName(), quantity, product.getPrice()));
        } else {
            int newQuantity = existing.getQuantity() + quantity;
            if (newQuantity > product.getStock()) {
                throw new IllegalStateException("Cannot exceed stock level");
            }
            existing.setQuantity(newQuantity);
            existing.setProductName(product.getName());
            existing.setPriceSnapshot(product.getPrice());
        }
        cart.setItems(items);
        return cartRepository.save(cart);
    }

    @Transactional
    public Cart updateQuantity(String customerId, String productId, int quantity) {
        Cart cart = getOrCreateCart(customerId);
        if (quantity <= 0) {
            return removeItem(customerId, productId);
        }
        Product product = productService.findById(productId);
        if (quantity > product.getStock()) {
            throw new IllegalStateException("Cannot exceed stock level");
        }
        cart.getItems().stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst()
                .ifPresentOrElse(item -> {
                    item.setQuantity(quantity);
                    item.setProductName(product.getName());
                    item.setPriceSnapshot(product.getPrice());
                }, () -> {
                    throw new IllegalArgumentException("Product " + productId + " not found in cart");
                });
        return cartRepository.save(cart);
    }

    @Transactional
    public Cart removeItem(String customerId, String productId) {
        Cart cart = getOrCreateCart(customerId);
        cart.getItems().removeIf(item -> item.getProductId().equals(productId));
        return cartRepository.save(cart);
    }

    public BigDecimal calculateTotal(Cart cart) {
        return cart.getItems().stream()
                .map(item -> {
                    BigDecimal price = item.getPriceSnapshot() != null ? item.getPriceSnapshot() : BigDecimal.ZERO;
                    return price.multiply(BigDecimal.valueOf(item.getQuantity()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void emptyCart(Cart cart) {
        cart.getItems().clear();
        cartRepository.save(cart);
    }
}
