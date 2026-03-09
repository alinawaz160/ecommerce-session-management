package com.ecommerce.cart.dto;

import com.ecommerce.cart.entity.CartItem;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CartItemDto {

    private Long id;
    private String productId;
    private String productName;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal subtotal;
    private String imageUrl;

    public static CartItemDto from(CartItem item) {
        CartItemDto dto = new CartItemDto();
        dto.setId(item.getId());
        dto.setProductId(item.getProductId());
        dto.setProductName(item.getProductName());
        dto.setPrice(item.getPrice());
        dto.setQuantity(item.getQuantity());
        dto.setSubtotal(item.getSubtotal());
        dto.setImageUrl(item.getImageUrl());
        return dto;
    }
}
