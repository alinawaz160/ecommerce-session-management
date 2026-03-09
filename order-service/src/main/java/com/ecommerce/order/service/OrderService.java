package com.ecommerce.order.service;

import com.ecommerce.order.dto.PlaceOrderRequest;
import com.ecommerce.order.dto.PlaceOrderResponse;

public interface OrderService {

    /** Creates an order, publishes a Kafka event, returns the order response. */
    PlaceOrderResponse placeOrder(PlaceOrderRequest request);

    /** Retrieve a single order by its order number. */
    PlaceOrderResponse getOrder(String orderNumber);
}
