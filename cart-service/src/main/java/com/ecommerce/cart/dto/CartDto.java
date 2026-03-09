package com.ecommerce.cart.dto;

import com.ecommerce.cart.entity.Cart;
import com.ecommerce.cart.entity.CartItem;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CartDto {

    private Long cartId;
    private Long userId;
    private String sessionId;
    private String status;
    private List<CartItemDto> items;
    private BigDecimal totalAmount;
    private int totalItems;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static CartDto from(Cart cart) {
        CartDto dto = new CartDto();
        dto.setCartId(cart.getId());
        dto.setUserId(cart.getUserId());
        dto.setSessionId(cart.getSessionId());
        dto.setStatus(cart.getStatus().name());
        dto.setItems(cart.getItems().stream().map(CartItemDto::from).toList());
        dto.setTotalAmount(
            cart.getItems().stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
        );
        dto.setTotalItems(
            cart.getItems().stream().mapToInt(CartItem::getQuantity).sum()
        );
        dto.setCreatedAt(cart.getCreatedAt());
        dto.setUpdatedAt(cart.getUpdatedAt());
        return dto;
    }
}
