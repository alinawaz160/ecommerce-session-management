package com.ecommerce.cart.controller;

import com.ecommerce.cart.client.CheckoutServiceClient;
import com.ecommerce.cart.dto.*;
import com.ecommerce.cart.service.cart.CartService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final CheckoutServiceClient checkoutServiceClient;

    // ── Cart CRUD ─────────────────────────────────────────────────────────────

    @GetMapping
    public ResponseEntity<CartDto> getCart(HttpSession session) {
        log.debug("GET cart - session: {}", session.getId());
        return ResponseEntity.ok(cartService.getOrCreateCart(session));
    }

    @PostMapping("/items")
    public ResponseEntity<CartDto> addItem(
            @Valid @RequestBody AddItemRequest request,
            HttpSession session) {
        log.debug("POST addItem - productId: {}", request.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(cartService.addItem(session, request));
    }

    @PutMapping("/items/{productId}")
    public ResponseEntity<CartDto> updateItem(
            @PathVariable String productId,
            @Valid @RequestBody UpdateItemRequest request,
            HttpSession session) {
        log.debug("PUT updateItem - productId: {}, qty: {}", productId, request.getQuantity());
        return ResponseEntity.ok(cartService.updateItem(session, productId, request));
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<CartDto> removeItem(
            @PathVariable String productId,
            HttpSession session) {
        log.debug("DELETE removeItem - productId: {}", productId);
        return ResponseEntity.ok(cartService.removeItem(session, productId));
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCart(HttpSession session) {
        log.debug("DELETE clearCart");
        cartService.clearPendingCartId(session);
        cartService.clearCart(session);
        return ResponseEntity.noContent().build();
    }

    // ── Checkout Flow ─────────────────────────────────────────────────────────

    /**
     * POST /api/cart/checkout
     *
     * Step 1: Called when user clicks "Proceed to Checkout".
     * Reads the current cart (session or DB), sends snapshot to Checkout service.
     * Checkout service stores it in Cassandra and returns a cartId.
     * No shipping/payment required at this stage.
     */
    @PostMapping("/checkout")
    public ResponseEntity<Map<String, String>> checkout(HttpSession session) {
        log.info("Initiating checkout for session: {}", session.getId());

        CartDto cart = cartService.getOrCreateCart(session);
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Cart is empty"));
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = (auth != null && auth.getCredentials() instanceof String email) ? email : null;

        // Pass existing cartId so checkout-service overwrites the same Cassandra row
        String existingCartId = cartService.getPendingCartId(session);

        CheckoutServiceClient.CheckoutRequest checkoutReq = new CheckoutServiceClient.CheckoutRequest();
        if (existingCartId != null) {
            checkoutReq.setExistingCartId(UUID.fromString(existingCartId));
        }
        checkoutReq.setSessionId(session.getId());
        checkoutReq.setUserEmail(userEmail);
        checkoutReq.setTotalAmount(cart.getTotalAmount());
        checkoutReq.setTotalItems(cart.getTotalItems());
        checkoutReq.setItems(CheckoutServiceClient.fromDb(cart.getItems()));

        UUID cartId = checkoutServiceClient.initiateCheckout(checkoutReq);

        cartService.storePendingCartId(session, cartId.toString());
        log.info("Cart snapshot {} — cartId: {}", existingCartId != null ? "updated" : "created", cartId);

        return ResponseEntity.ok(Map.of("cartId", cartId.toString()));
    }

    /**
     * POST /api/cart/place-order
     *
     * Step 2: Called when user submits the checkout form.
     * Accepts shipping address and payment method.
     * Calls Checkout service → Order service → Kafka.
     */
    @PostMapping("/place-order")
    public ResponseEntity<Map<String, String>> placeOrder(
            @Valid @RequestBody PlaceOrderInitRequest request,
            HttpSession session) {
        String cartIdStr = cartService.getPendingCartId(session);
        if (cartIdStr == null) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "No pending checkout found. Call /checkout first."));
        }

        log.info("Placing order for cartId: {}", cartIdStr);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String jwtEmail = (auth != null && auth.getCredentials() instanceof String e) ? e : null;
        // Logged-in users: use JWT email. Guests: use email entered on checkout form.
        String userEmail = jwtEmail != null ? jwtEmail : request.getEmail();

        CheckoutServiceClient.PlaceOrderRequest orderReq = new CheckoutServiceClient.PlaceOrderRequest();
        orderReq.setCartId(UUID.fromString(cartIdStr));
        orderReq.setSessionId(session.getId());
        orderReq.setUserEmail(userEmail);
        orderReq.setShippingAddress(request.getShippingAddress());
        orderReq.setPaymentMethod(request.getPaymentMethod());

        String orderNumber = checkoutServiceClient.placeOrder(orderReq);

        cartService.clearPendingCartId(session);
        cartService.clearCart(session);

        log.info("Order placed successfully — orderNumber: {}", orderNumber);
        return ResponseEntity.ok(Map.of("orderNumber", orderNumber));
    }
}
