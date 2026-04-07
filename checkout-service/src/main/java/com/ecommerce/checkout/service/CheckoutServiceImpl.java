package com.ecommerce.checkout.service;

import com.ecommerce.checkout.client.OrderServiceClient;
import com.ecommerce.checkout.dto.*;
import com.ecommerce.checkout.entity.Order;
import com.ecommerce.checkout.entity.OrderItem;
import com.ecommerce.checkout.exception.EmptyCartException;
import com.ecommerce.checkout.exception.OrderNotFoundException;
import com.ecommerce.checkout.repository.OrderItemRepository;
import com.ecommerce.checkout.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CheckoutServiceImpl implements CheckoutService {

    private final OrderRepository     orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderServiceClient  orderServiceClient;

    // ─────────────────────────────────────────────────────────────────────────
    //  Step 1: Store cart snapshot in Cassandra → return cartId
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public CheckoutResponse initiateCheckout(CheckoutRequest request) {
        log.info("Storing cart snapshot for session: {}", request.getSessionId());

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new EmptyCartException(request.getSessionId());
        }

        UUID cartId  = request.getExistingCartId() != null ? request.getExistingCartId() : UUID.randomUUID();
        Instant now  = Instant.now();

        BigDecimal totalAmount = request.getTotalAmount() != null
            ? request.getTotalAmount()
            : request.getItems().stream()
                .map(i -> i.getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int totalItems = request.getTotalItems() > 0
            ? request.getTotalItems()
            : request.getItems().stream().mapToInt(CartItemDto::getQuantity).sum();

        Order order = Order.builder()
            .orderId(cartId)
            .sessionId(request.getSessionId())
            .status(Order.STATUS_CART_RECEIVED)
            .totalAmount(totalAmount)
            .totalItems(totalItems)
            .shippingAddress(request.getShippingAddress())
            .paymentMethod(request.getPaymentMethod())
            .createdAt(now)
            .updatedAt(now)
            .build();

        orderRepository.save(order);

        // Delete stale items before re-inserting (handles item removals/qty changes)
        if (request.getExistingCartId() != null) {
            orderItemRepository.deleteByOrderId(cartId);
        }
        List<OrderItem> orderItems = buildOrderItems(cartId, request.getItems());
        orderItemRepository.saveAll(orderItems);

        log.info("Cart snapshot {} — cartId: {}", request.getExistingCartId() != null ? "updated" : "saved", cartId);
        return CheckoutResponse.from(order, orderItems);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Step 2: Place order → delegate to Order service
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public PlaceOrderResponse placeOrder(PlaceOrderRequest request) {
        log.info("Placing order for cartId: {}", request.getCartId());

        Order snapshot = orderRepository.findById(request.getCartId())
            .orElseThrow(() -> new OrderNotFoundException(request.getCartId().toString()));

        // Persist shipping/payment collected on the checkout form
        if (request.getShippingAddress() != null) {
            snapshot.setShippingAddress(request.getShippingAddress());
            snapshot.setPaymentMethod(request.getPaymentMethod());
            orderRepository.save(snapshot);
        }

        List<OrderItem> items = orderItemRepository.findByOrderId(request.getCartId());

        // Build request for order-service
        OrderServiceClient.OrderRequest orderReq = new OrderServiceClient.OrderRequest();
        orderReq.setCartId(request.getCartId().toString());
        orderReq.setSessionId(request.getSessionId() != null ? request.getSessionId() : snapshot.getSessionId());
        orderReq.setUserEmail(request.getUserEmail());
        orderReq.setTotalAmount(snapshot.getTotalAmount());
        orderReq.setItems(items.stream().map(i -> {
            OrderServiceClient.OrderRequest.ItemDto dto = new OrderServiceClient.OrderRequest.ItemDto();
            dto.setProductId(i.getProductId());
            dto.setProductName(i.getProductName());
            dto.setPrice(i.getPrice());
            dto.setQuantity(i.getQuantity());
            dto.setSubtotal(i.getSubtotal());
            return dto;
        }).toList());

        OrderServiceClient.OrderResponse response = orderServiceClient.placeOrder(orderReq);

        log.info("Order placed — orderNumber: {}", response.getOrderNumber());
        return new PlaceOrderResponse(response.getOrderNumber(), request.getCartId(), "PLACED");
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Query endpoints
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public CheckoutResponse getCartSnapshot(UUID cartId) {
        Order order = orderRepository.findById(cartId)
            .orElseThrow(() -> new OrderNotFoundException(cartId.toString()));
        List<OrderItem> items = orderItemRepository.findByOrderId(cartId);
        return CheckoutResponse.from(order, items);
    }

    @Override
    public List<CheckoutResponse> getSnapshotsBySession(String sessionId) {
        List<Order> orders = orderRepository.findBySessionId(sessionId);
        return orders.stream().map(order -> {
            List<OrderItem> items = orderItemRepository.findByOrderId(order.getOrderId());
            return CheckoutResponse.from(order, items);
        }).toList();
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Internal helpers
    // ─────────────────────────────────────────────────────────────────────────

    private List<OrderItem> buildOrderItems(UUID cartId, List<CartItemDto> cartItems) {
        return cartItems.stream().map(ci -> {
            BigDecimal subtotal = ci.getPrice().multiply(BigDecimal.valueOf(ci.getQuantity()));
            return OrderItem.builder()
                .orderId(cartId)
                .productId(ci.getProductId())
                .productName(ci.getProductName())
                .price(ci.getPrice())
                .quantity(ci.getQuantity())
                .subtotal(subtotal)
                .imageUrl(ci.getImageUrl())
                .build();
        }).toList();
    }
}
