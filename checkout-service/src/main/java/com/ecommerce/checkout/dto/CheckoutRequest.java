package com.ecommerce.checkout.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * Payload sent by the Main (cart) service to initiate a checkout.
 * Includes the full cart contents so checkout-service can store the snapshot.
 */
@Data
public class CheckoutRequest {

    @NotBlank(message = "sessionId is required")
    private String sessionId;

    @NotBlank(message = "shippingAddress is required")
    private String shippingAddress;

    @NotBlank(message = "paymentMethod is required")
    private String paymentMethod;

    private String userEmail;

    @NotEmpty(message = "items cannot be empty")
    private List<CartItemDto> items;

    private BigDecimal totalAmount;
    private int totalItems;
}
