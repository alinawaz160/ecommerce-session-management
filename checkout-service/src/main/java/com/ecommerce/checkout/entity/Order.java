package com.ecommerce.checkout.entity;

import lombok.*;
import org.springframework.data.cassandra.core.mapping.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Cart snapshot stored in Cassandra at checkout time.
 * The orderId field IS the cartId returned to the Main service.
 * Partition key: order_id (UUID) for even distribution.
 */
@Table("orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @PrimaryKey
    @CassandraType(type = CassandraType.Name.UUID)
    private UUID orderId;

    @Column("session_id")
    @Indexed   // secondary index — fine for low-cardinality lookups
    private String sessionId;

    @Column("status")
    private String status;   // PENDING | CONFIRMED | FAILED | CANCELLED

    @Column("total_amount")
    private BigDecimal totalAmount;

    @Column("total_items")
    private int totalItems;

    /** JSON-serialised shipping address for simplicity. */
    @Column("shipping_address")
    private String shippingAddress;

    @Column("payment_method")
    private String paymentMethod;

    @Column("created_at")
    private Instant createdAt;

    @Column("updated_at")
    private Instant updatedAt;

    // ── Status constants ─────────────────────────────────────────────────────
    /** Cart snapshot received and stored — awaiting place-order call */
    public static final String STATUS_CART_RECEIVED = "CART_RECEIVED";
}
