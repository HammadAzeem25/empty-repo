package com.example.ecommerce.service;

import com.example.ecommerce.model.Product;
import com.example.ecommerce.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> findAll() {
        return productRepository.findAll();
    }

    public Product findById(String id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + id));
    }

    public Product save(Product product) {
        return productRepository.save(product);
    }

    public Product update(String id, Product product) {
        Product existing = findById(id);
        existing.setName(product.getName());
        existing.setDescription(product.getDescription());
        existing.setPrice(product.getPrice());
        existing.setStock(product.getStock());
        return productRepository.save(existing);
    }

    public void delete(String id) {
        productRepository.deleteById(id);
    }

    public void decrementStock(String productId, int quantity) {
        Product product = findById(productId);
        int remaining = product.getStock() - quantity;
        if (remaining < 0) {
            throw new IllegalStateException("Insufficient stock for product " + product.getName());
        }
        product.setStock(remaining);
        productRepository.save(product);
    }
}
