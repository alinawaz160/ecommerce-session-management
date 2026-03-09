package com.ecommerce.checkout.repository;

import com.ecommerce.checkout.entity.OrderItem;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderItemRepository extends CassandraRepository<OrderItem, UUID> {

    @Query("SELECT * FROM order_items WHERE order_id = ?0")
    List<OrderItem> findByOrderId(UUID orderId);

    @Query("DELETE FROM order_items WHERE order_id = ?0")
    void deleteByOrderId(UUID orderId);
}
