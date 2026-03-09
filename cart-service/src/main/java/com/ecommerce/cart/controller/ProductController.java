package com.ecommerce.cart.controller;

import lombok.Builder;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private static final List<ProductDto> PRODUCTS = List.of(
        ProductDto.builder().productId("P001").productName("Luxury Sofa").price(new BigDecimal("499.99")).imageUrl("https://example.com/sofa.jpg").stock(100).build(),
        ProductDto.builder().productId("P002").productName("TV Stand").price(new BigDecimal("149.99")).imageUrl("https://example.com/tvstand.jpg").stock(50).build(),
        ProductDto.builder().productId("P003").productName("Coffee Table").price(new BigDecimal("89.99")).imageUrl("https://example.com/coffee-table.jpg").stock(75).build(),
        ProductDto.builder().productId("P004").productName("Bookshelf").price(new BigDecimal("119.99")).imageUrl("https://example.com/bookshelf.jpg").stock(30).build(),
        ProductDto.builder().productId("P005").productName("Dining Table Set").price(new BigDecimal("699.99")).imageUrl("https://example.com/dining-set.jpg").stock(20).build()
    );

    @GetMapping
    public ResponseEntity<List<ProductDto>> getProducts() {
        return ResponseEntity.ok(PRODUCTS);
    }

    @Data
    @Builder
    public static class ProductDto {
        private String productId;
        private String productName;
        private BigDecimal price;
        private String imageUrl;
        private int stock;
    }
}
