package com.ecommerce.cart.client;

import com.ecommerce.cart.dto.CartDto;
import com.ecommerce.cart.dto.CartItemDto;
import com.ecommerce.cart.dto.SessionCartItem;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * HTTP client for calling the Checkout service from the Main (cart) service.
 */
@Slf4j
@Component
public class CheckoutServiceClient {

    private final RestTemplate restTemplate;

    public CheckoutServiceClient(@Qualifier("checkoutRestTemplate") RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Step 1 of checkout: sends cart data to Checkout service.
     * Checkout service stores snapshot in Cassandra and returns a cartId.
     */
    public UUID initiateCheckout(CheckoutRequest request) {
        log.debug("Sending cart to checkout-service for session: {}", request.getSessionId());
        CheckoutResponse response = restTemplate.postForObject(
            "/api/checkout", request, CheckoutResponse.class);
        if (response == null) {
            throw new RuntimeException("No response from checkout-service");
        }
        log.info("Checkout initiated — cartId: {}", response.getCartId());
        return response.getCartId();
    }

    /**
     * Step 2: triggers order creation in Order service via Checkout service.
     */
    public String placeOrder(PlaceOrderRequest request) {
        log.debug("Placing order via checkout-service for cartId: {}", request.getCartId());
        PlaceOrderResponse response = restTemplate.postForObject(
            "/api/checkout/place-order", request, PlaceOrderResponse.class);
        if (response == null) {
            throw new RuntimeException("No response from checkout-service place-order");
        }
        log.info("Order placed — orderNumber: {}", response.getOrderNumber());
        return response.getOrderNumber();
    }

    // ── Inner DTOs (cart-service side) ───────────────────────────────────────

    @Data
    public static class CheckoutRequest {
        private String sessionId;
        private String shippingAddress;
        private String paymentMethod;
        private String userEmail;
        private List<ItemDto> items;
        private BigDecimal totalAmount;
        private int totalItems;

        @Data
        public static class ItemDto {
            private String productId;
            private String productName;
            private BigDecimal price;
            private Integer quantity;
            private String imageUrl;
        }
    }

    @Data
    public static class CheckoutResponse {
        private UUID cartId;
        private String sessionId;
        private String status;
        private BigDecimal totalAmount;
    }

    @Data
    public static class PlaceOrderRequest {
        private UUID cartId;
        private String sessionId;
        private String userEmail;
    }

    @Data
    public static class PlaceOrderResponse {
        private String orderNumber;
        private String status;
    }

    // ── Conversion helpers ────────────────────────────────────────────────────

    public static List<CheckoutRequest.ItemDto> fromSession(List<SessionCartItem> items) {
        return items.stream().map(i -> {
            CheckoutRequest.ItemDto dto = new CheckoutRequest.ItemDto();
            dto.setProductId(i.getProductId());
            dto.setProductName(i.getProductName());
            dto.setPrice(i.getPrice());
            dto.setQuantity(i.getQuantity());
            dto.setImageUrl(i.getImageUrl());
            return dto;
        }).toList();
    }

    public static List<CheckoutRequest.ItemDto> fromDb(List<CartItemDto> items) {
        return items.stream().map(i -> {
            CheckoutRequest.ItemDto dto = new CheckoutRequest.ItemDto();
            dto.setProductId(i.getProductId());
            dto.setProductName(i.getProductName());
            dto.setPrice(i.getPrice());
            dto.setQuantity(i.getQuantity());
            dto.setImageUrl(i.getImageUrl());
            return dto;
        }).toList();
    }
}
