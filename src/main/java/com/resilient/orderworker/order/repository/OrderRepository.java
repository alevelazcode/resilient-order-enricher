package com.resilient.orderworker.order.repository;

import com.resilient.orderworker.order.entity.Order;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

/**
 * Reactive MongoDB repository for Order entities.
 */
@Repository
public interface OrderRepository extends ReactiveMongoRepository<Order, String> {
    
    /**
     * Find an order by its orderId.
     * @param orderId the order ID
     * @return Mono containing the order if found
     */
    Mono<Order> findByOrderId(String orderId);
    
    /**
     * Check if an order exists by orderId.
     * @param orderId the order ID
     * @return Mono containing true if exists
     */
    Mono<Boolean> existsByOrderId(String orderId);
}