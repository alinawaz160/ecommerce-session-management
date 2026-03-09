package com.ecommerce.order.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class PlaceOrderRequest {

    @NotBlank(message = "cartId is required")
    private String cartId;

    private String sessionId;
    private String userEmail;
    private BigDecimal totalAmount;
    private List<ItemDto> items;

    @Data
    public static class ItemDto {
        private String productId;
        private String productName;
        private BigDecimal price;
        private int quantity;
        private BigDecimal subtotal;
    }
}
