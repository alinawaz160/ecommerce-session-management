package com.ecommerce.order.consumer;

import com.ecommerce.order.config.KafkaConfig;
import com.ecommerce.order.event.OrderEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Simulates sending a confirmation email when an order is placed.
 * In production this would call an email service (SES, SendGrid, etc.).
 */
@Slf4j
@Component
public class OrderKafkaConsumer {

    @KafkaListener(
        topics = KafkaConfig.ORDER_TOPIC,
        groupId = "${spring.kafka.consumer.group-id}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void onOrderEvent(OrderEvent event) {
        log.info("[EMAIL] Sending order confirmation to: {}", event.getUserEmail());
        log.info("[EMAIL] Order Number: {} | Total: {} | Status: {}",
            event.getOrderNumber(), event.getTotalAmount(), event.getStatus());
        log.info("[EMAIL] Confirmation email sent for order: {}", event.getOrderNumber());
    }
}
