package com.ecommerce.cart.dto;

import lombok.Data;

/**
 * Request to initiate checkout (snapshot cart in Cassandra).
 * No body required — cart is read server-side from session/DB.
 */
@Data
public class CheckoutInitRequest {
}
