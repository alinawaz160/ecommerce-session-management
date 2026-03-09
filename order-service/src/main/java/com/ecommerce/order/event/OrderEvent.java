package com.ecommerce.order.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Kafka event published when an order is placed.
 * Consumers can use this to send confirmation emails, update inventory, etc.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderEvent {

    private String orderNumber;
    private String cartId;
    private String sessionId;
    private String userEmail;
    private BigDecimal totalAmount;
    private String status;
    private LocalDateTime createdAt;
}
