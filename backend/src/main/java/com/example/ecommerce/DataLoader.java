package com.example.ecommerce;

import com.example.ecommerce.model.Customer;
import com.example.ecommerce.model.Product;
import com.example.ecommerce.repository.CustomerRepository;
import com.example.ecommerce.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

@Configuration
public class DataLoader {

    private static final Logger log = LoggerFactory.getLogger(DataLoader.class);

    @Bean
    CommandLineRunner loadSampleData(ProductRepository productRepository, CustomerRepository customerRepository) {
        return args -> {
            if (productRepository.count() == 0) {
                log.info("Loading sample products");
                productRepository.save(new Product("Modern Sofa", "Comfortable three-seat sofa", new BigDecimal("899.99"), 5));
                productRepository.save(new Product("Coffee Table", "Walnut coffee table", new BigDecimal("249.99"), 10));
                productRepository.save(new Product("Floor Lamp", "Minimalist LED floor lamp", new BigDecimal("129.99"), 15));
            }

            if (customerRepository.count() == 0) {
                log.info("Loading sample customers");
                customerRepository.save(new Customer("alex", "Alex Johnson", "alex@example.com"));
                customerRepository.save(new Customer("taylor", "Taylor Smith", "taylor@example.com"));
            }
        };
    }
}
