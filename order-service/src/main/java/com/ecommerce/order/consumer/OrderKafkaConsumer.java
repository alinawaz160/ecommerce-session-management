package com.ecommerce.order.consumer;

import com.ecommerce.order.config.KafkaConfig;
import com.ecommerce.order.event.OrderEvent;
import com.ecommerce.order.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderKafkaConsumer {

    private final EmailService emailService;

    @KafkaListener(
        topics = KafkaConfig.ORDER_TOPIC,
        groupId = "${spring.kafka.consumer.group-id}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void onOrderEvent(OrderEvent event) {
        log.info("Order event received — orderNumber: {}, email: {}", event.getOrderNumber(), event.getUserEmail());
        emailService.sendOrderConfirmation(event);
    }
}
