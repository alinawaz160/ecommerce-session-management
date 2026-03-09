package com.ecommerce.checkout.repository;

import com.ecommerce.checkout.entity.Order;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderRepository extends CassandraRepository<Order, UUID> {

    List<Order> findBySessionId(String sessionId);
}
