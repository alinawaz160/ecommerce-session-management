package com.ecommerce.checkout.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

/**
 * Request to place an order using a previously created cart snapshot.
 */
@Data
public class PlaceOrderRequest {

    @NotNull(message = "cartId is required")
    private UUID cartId;

    private String sessionId;
    private String userEmail;
    private String shippingAddress;
    private String paymentMethod;
}
