package com.ecommerce.checkout.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Payload sent by the Main (cart) service to initiate a checkout.
 * Includes the full cart contents so checkout-service can store the snapshot.
 */
@Data
public class CheckoutRequest {

    private UUID existingCartId;   // non-null = overwrite existing snapshot

    @NotBlank(message = "sessionId is required")
    private String sessionId;

    private String shippingAddress;

    private String paymentMethod;

    private String userEmail;

    @NotEmpty(message = "items cannot be empty")
    private List<CartItemDto> items;

    private BigDecimal totalAmount;
    private int totalItems;
}
