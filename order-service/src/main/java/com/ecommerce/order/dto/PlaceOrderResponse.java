package com.ecommerce.order.dto;

import com.ecommerce.order.entity.Order;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PlaceOrderResponse {

    private String orderNumber;
    private String cartId;
    private String sessionId;
    private String userEmail;
    private BigDecimal totalAmount;
    private String status;
    private LocalDateTime createdAt;

    public static PlaceOrderResponse from(Order order) {
        PlaceOrderResponse res = new PlaceOrderResponse();
        res.setOrderNumber(order.getOrderNumber());
        res.setCartId(order.getCartId());
        res.setSessionId(order.getSessionId());
        res.setUserEmail(order.getUserEmail());
        res.setTotalAmount(order.getTotalAmount());
        res.setStatus(order.getStatus());
        res.setCreatedAt(order.getCreatedAt());
        return res;
    }
}
