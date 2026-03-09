package com.ecommerce.checkout.dto;

import com.ecommerce.checkout.entity.Order;
import com.ecommerce.checkout.entity.OrderItem;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
public class CheckoutResponse {

    @JsonProperty("cartId")
    private UUID cartId;

    private String sessionId;
    private String status;
    private BigDecimal totalAmount;
    private int totalItems;
    private String shippingAddress;
    private String paymentMethod;
    private List<OrderItemDto> items;
    private Instant createdAt;

    @Data
    public static class OrderItemDto {
        private String productId;
        private String productName;
        private BigDecimal price;
        private int quantity;
        private BigDecimal subtotal;
        private String imageUrl;

        public static OrderItemDto from(OrderItem item) {
            OrderItemDto dto = new OrderItemDto();
            dto.setProductId(item.getProductId());
            dto.setProductName(item.getProductName());
            dto.setPrice(item.getPrice());
            dto.setQuantity(item.getQuantity());
            dto.setSubtotal(item.getSubtotal());
            dto.setImageUrl(item.getImageUrl());
            return dto;
        }
    }

    public static CheckoutResponse from(Order order, List<OrderItem> items) {
        CheckoutResponse res = new CheckoutResponse();
        res.setCartId(order.getOrderId());
        res.setSessionId(order.getSessionId());
        res.setStatus(order.getStatus());
        res.setTotalAmount(order.getTotalAmount());
        res.setTotalItems(order.getTotalItems());
        res.setShippingAddress(order.getShippingAddress());
        res.setPaymentMethod(order.getPaymentMethod());
        res.setCreatedAt(order.getCreatedAt());
        res.setItems(items.stream().map(OrderItemDto::from).toList());
        return res;
    }
}
