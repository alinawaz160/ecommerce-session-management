package com.ecommerce.checkout.client;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;

/**
 * HTTP client for calling the Order service from the Checkout service.
 */
@Slf4j
@Component
public class OrderServiceClient {

    private final RestTemplate restTemplate;

    public OrderServiceClient(@Qualifier("orderRestTemplate") RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public OrderResponse placeOrder(OrderRequest request) {
        try {
            log.debug("Calling order-service to place order for cartId: {}", request.getCartId());
            OrderResponse response = restTemplate.postForObject(
                "/api/orders", request, OrderResponse.class);
            if (response == null) {
                throw new RuntimeException("No response from order-service");
            }
            return response;
        } catch (Exception e) {
            log.error("Failed to place order for cartId: {}", request.getCartId(), e);
            throw new RuntimeException("Order service unavailable", e);
        }
    }

    // ── Inner DTOs ────────────────────────────────────────────────────────────

    @Data
    public static class OrderRequest {
        private String cartId;
        private String sessionId;
        private String userEmail;
        private BigDecimal totalAmount;
        private List<ItemDto> items;

        @Data
        public static class ItemDto {
            private String productId;
            private String productName;
            private BigDecimal price;
            private int quantity;
            private BigDecimal subtotal;
        }
    }

    @Data
    public static class OrderResponse {
        private String orderNumber;
        private String cartId;
        private String status;
    }
}
