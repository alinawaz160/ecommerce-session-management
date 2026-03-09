package com.ecommerce.checkout.entity;

import lombok.*;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.*;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Individual line items for an order.
 *
 * Primary key: (order_id [partition], product_id [clustering])
 * This allows efficient retrieval of all items for a given order.
 */
@Table("order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {

    @PrimaryKeyColumn(name = "order_id", type = PrimaryKeyType.PARTITIONED,
                      ordinal = 0)
    @CassandraType(type = CassandraType.Name.UUID)
    private UUID orderId;

    @PrimaryKeyColumn(name = "product_id", type = PrimaryKeyType.CLUSTERED,
                      ordinal = 1)
    private String productId;

    @Column("product_name")
    private String productName;

    @Column("price")
    private BigDecimal price;

    @Column("quantity")
    private int quantity;

    @Column("subtotal")
    private BigDecimal subtotal;

    @Column("image_url")
    private String imageUrl;
}
