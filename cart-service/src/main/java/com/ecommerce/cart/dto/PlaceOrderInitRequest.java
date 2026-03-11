package com.ecommerce.cart.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request from the frontend to place an order.
 * Contains shipping and payment details collected on the checkout page.
 */
@Data
public class PlaceOrderInitRequest {

    @NotBlank(message = "shippingAddress is required")
    private String shippingAddress;

    @NotBlank(message = "paymentMethod is required")
    private String paymentMethod;

    /** Email from the checkout form — used for guests; ignored for logged-in users (JWT email takes precedence). */
    private String email;
}
