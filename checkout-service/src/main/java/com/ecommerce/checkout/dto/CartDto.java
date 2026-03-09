package com.ecommerce.checkout.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

// ─────────────────────────────────────────────────────────────────────────────
//  Cart service response mirror (consumed from Cart service REST call)
// ─────────────────────────────────────────────────────────────────────────────

@Data
public class CartDto {
    private Long cartId;
    private String sessionId;
    private String status;
    private List<CartItemDto> items;
    private BigDecimal totalAmount;
    private int totalItems;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
