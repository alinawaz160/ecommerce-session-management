package com.ecommerce.cart.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request from the client to initiate checkout.
 * sessionId is derived server-side from HttpSession.
 */
@Data
public class CheckoutInitRequest {

    @NotBlank(message = "shippingAddress is required")
    private String shippingAddress;

    @NotBlank(message = "paymentMethod is required")
    private String paymentMethod;
}
