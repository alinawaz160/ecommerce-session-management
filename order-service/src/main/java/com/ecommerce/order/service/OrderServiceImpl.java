package com.ecommerce.order.service;

import com.ecommerce.order.dto.PlaceOrderRequest;
import com.ecommerce.order.dto.PlaceOrderResponse;
import com.ecommerce.order.entity.Order;
import com.ecommerce.order.event.OrderEvent;
import com.ecommerce.order.producer.OrderKafkaProducer;
import com.ecommerce.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository    orderRepository;
    private final OrderKafkaProducer kafkaProducer;

    @Override
    @Transactional
    public PlaceOrderResponse placeOrder(PlaceOrderRequest request) {
        String orderNumber = generateOrderNumber();

        BigDecimal totalAmount = request.getTotalAmount() != null
            ? request.getTotalAmount()
            : BigDecimal.ZERO;

        Order order = Order.builder()
            .orderNumber(orderNumber)
            .cartId(request.getCartId())
            .sessionId(request.getSessionId())
            .userEmail(request.getUserEmail())
            .totalAmount(totalAmount)
            .status(Order.STATUS_PLACED)
            .build();

        Order saved = orderRepository.save(order);
        log.info("Order saved to DB — orderNumber: {}, cartId: {}", orderNumber, request.getCartId());

        // Publish Kafka event
        OrderEvent event = new OrderEvent(
            saved.getOrderNumber(),
            saved.getCartId(),
            saved.getSessionId(),
            saved.getUserEmail(),
            saved.getTotalAmount(),
            saved.getStatus(),
            saved.getCreatedAt()
        );
        kafkaProducer.publishOrderEvent(event);

        return PlaceOrderResponse.from(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PlaceOrderResponse getOrder(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
            .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderNumber));
        return PlaceOrderResponse.from(order);
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private String generateOrderNumber() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int random = ThreadLocalRandom.current().nextInt(100000, 999999);
        return "ORD-" + date + "-" + random;
    }
}
