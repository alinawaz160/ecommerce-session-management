package com.ecommerce.order.controller;

import com.ecommerce.order.dto.PlaceOrderRequest;
import com.ecommerce.order.dto.PlaceOrderResponse;
import com.ecommerce.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * POST /api/orders
     * Called by Checkout service to create an order.
     * Generates order number, saves to MySQL, publishes Kafka event.
     */
    @PostMapping
    public ResponseEntity<PlaceOrderResponse> placeOrder(
            @Valid @RequestBody PlaceOrderRequest request) {
        log.info("Place order request — cartId: {}", request.getCartId());
        PlaceOrderResponse response = orderService.placeOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /api/orders/{orderNumber}
     * Retrieve order details by order number.
     */
    @GetMapping("/{orderNumber}")
    public ResponseEntity<PlaceOrderResponse> getOrder(@PathVariable String orderNumber) {
        return ResponseEntity.ok(orderService.getOrder(orderNumber));
    }
}
