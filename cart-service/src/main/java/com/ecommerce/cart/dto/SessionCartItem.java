package com.ecommerce.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Lightweight cart item stored in HttpSession for guest users.
 * Implements Serializable for session persistence.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SessionCartItem implements Serializable {

    private String productId;
    private String productName;
    private BigDecimal price;
    private Integer quantity;
    private String imageUrl;

    public BigDecimal getSubtotal() {
        return price.multiply(BigDecimal.valueOf(quantity));
    }
}
