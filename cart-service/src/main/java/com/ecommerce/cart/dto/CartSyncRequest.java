package com.ecommerce.cart.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * Sent by the Checkout service to sync any cart changes
 * (e.g. price corrections, qty adjustments) back to the Cart service.
 */
@Data
public class CartSyncRequest {

    private String sessionId;
    private List<SyncItem> items;

    @Data
    public static class SyncItem {
        private String productId;
        private Integer quantity;       // updated quantity (0 = remove)
        private BigDecimal price;       // updated price (e.g. after stock check)
    }
}
