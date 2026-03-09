package com.ecommerce.cart.controller;

import com.ecommerce.cart.client.CheckoutServiceClient;
import com.ecommerce.cart.dto.*;
import com.ecommerce.cart.service.CartService;
import com.ecommerce.cart.service.UserServiceImpl;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
        log.debug("POST addItem - productId: {}", request.getProductId());
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
        cartService.clearCart(session);
        return ResponseEntity.noContent().build();
    }

    // ── Checkout Flow ─────────────────────────────────────────────────────────

    /**
     * POST /api/cart/checkout
     *
     * Reads the current cart (session or DB), sends it to Checkout service.
     * Checkout service stores snapshot in Cassandra and returns a cartId.
     * The cartId is stored in the HttpSession for subsequent place-order call.
     */
    @PostMapping("/checkout")
    public ResponseEntity<Map<String, String>> checkout(
            @Valid @RequestBody CheckoutInitRequest request,
            HttpSession session) {
        log.info("Initiating checkout for session: {}", session.getId());

        CartDto cart = cartService.getOrCreateCart(session);
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Cart is empty"));
        }

        // Build request for checkout-service
        CheckoutServiceClient.CheckoutRequest checkoutReq = new CheckoutServiceClient.CheckoutRequest();
        checkoutReq.setSessionId(session.getId());
        checkoutReq.setShippingAddress(request.getShippingAddress());
        checkoutReq.setPaymentMethod(request.getPaymentMethod());
        checkoutReq.setUserEmail((String) session.getAttribute(UserServiceImpl.SESSION_USER_EMAIL));
        checkoutReq.setTotalAmount(cart.getTotalAmount());
        checkoutReq.setTotalItems(cart.getTotalItems());
        checkoutReq.setItems(CheckoutServiceClient.fromDb(cart.getItems()));

        UUID cartId = checkoutServiceClient.initiateCheckout(checkoutReq);

        // Store cartId in session for place-order step
        session.setAttribute("CART_ID", cartId.toString());
        log.info("Cart snapshot created — cartId: {}", cartId);

        return ResponseEntity.ok(Map.of("cartId", cartId.toString()));
    }

    /**
     * POST /api/cart/place-order
     *
     * Uses the cartId stored from the checkout step.
     * Calls Checkout service → Order service → Kafka.
     * Returns HTTP 200 with the generated order number.
     */
    @PostMapping("/place-order")
    public ResponseEntity<Map<String, String>> placeOrder(HttpSession session) {
        String cartIdStr = (String) session.getAttribute("CART_ID");
        if (cartIdStr == null) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "No pending checkout found. Call /checkout first."));
        }

        log.info("Placing order for cartId: {}", cartIdStr);

        CheckoutServiceClient.PlaceOrderRequest orderReq = new CheckoutServiceClient.PlaceOrderRequest();
        orderReq.setCartId(UUID.fromString(cartIdStr));
        orderReq.setSessionId(session.getId());
        orderReq.setUserEmail((String) session.getAttribute(UserServiceImpl.SESSION_USER_EMAIL));

        String orderNumber = checkoutServiceClient.placeOrder(orderReq);

        // Clear cart and checkout state after successful order
        session.removeAttribute("CART_ID");
        cartService.clearCart(session);

        log.info("Order placed successfully — orderNumber: {}", orderNumber);
        return ResponseEntity.ok(Map.of("orderNumber", orderNumber));
    }
}
