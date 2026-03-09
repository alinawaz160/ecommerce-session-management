package com.ecommerce.checkout.controller;

import com.ecommerce.checkout.dto.*;
import com.ecommerce.checkout.service.CheckoutService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * CheckoutController — two-step checkout flow:
 *
 *  POST /api/checkout            → Step 1: store cart snapshot, return cartId
 *  POST /api/checkout/place-order → Step 2: create order via Order service, return orderNumber
 *  GET  /api/checkout/{cartId}   → Retrieve snapshot
 *  GET  /api/checkout/session/{sessionId} → All snapshots for session
 */
@Slf4j
@RestController
@RequestMapping("/api/checkout")
@RequiredArgsConstructor
public class CheckoutController {

    private final CheckoutService checkoutService;

    /**
     * Step 1: Receives cart from Main service.
     * Stores snapshot in Cassandra. Returns cartId.
     */
    @PostMapping
    public ResponseEntity<CheckoutResponse> initiateCheckout(
            @Valid @RequestBody CheckoutRequest request) {
        log.info("Checkout initiated for session: {}", request.getSessionId());
        CheckoutResponse response = checkoutService.initiateCheckout(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Step 2: Place order — calls Order service.
     * Returns orderNumber.
     */
    @PostMapping("/place-order")
    public ResponseEntity<PlaceOrderResponse> placeOrder(
            @Valid @RequestBody PlaceOrderRequest request) {
        log.info("Place order request for cartId: {}", request.getCartId());
        PlaceOrderResponse response = checkoutService.placeOrder(request);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/checkout/{cartId}
     * Retrieve a stored cart snapshot.
     */
    @GetMapping("/{cartId}")
    public ResponseEntity<CheckoutResponse> getCartSnapshot(@PathVariable UUID cartId) {
        return ResponseEntity.ok(checkoutService.getCartSnapshot(cartId));
    }

    /**
     * GET /api/checkout/session/{sessionId}
     * All snapshots for a guest session.
     */
    @GetMapping("/session/{sessionId}")
    public ResponseEntity<List<CheckoutResponse>> getSnapshotsBySession(
            @PathVariable String sessionId) {
        return ResponseEntity.ok(checkoutService.getSnapshotsBySession(sessionId));
    }
}
