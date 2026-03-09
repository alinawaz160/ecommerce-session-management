package com.ecommerce.order.producer;

import com.ecommerce.order.config.KafkaConfig;
import com.ecommerce.order.event.OrderEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderKafkaProducer {

    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;

    public void publishOrderEvent(OrderEvent event) {
        log.info("Publishing order event to Kafka — orderNumber: {}", event.getOrderNumber());

        CompletableFuture<SendResult<String, OrderEvent>> future =
            kafkaTemplate.send(KafkaConfig.ORDER_TOPIC, event.getOrderNumber(), event);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish order event for orderNumber: {}",
                    event.getOrderNumber(), ex);
            } else {
                log.info("Order event published — orderNumber: {}, offset: {}",
                    event.getOrderNumber(),
                    result.getRecordMetadata().offset());
            }
        });
    }
}
