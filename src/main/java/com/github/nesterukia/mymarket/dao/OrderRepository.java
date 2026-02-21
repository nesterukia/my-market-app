package com.github.nesterukia.mymarket.dao;

import com.github.nesterukia.mymarket.domain.Order;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OrderRepository extends ReactiveCrudRepository<Order, Long> {
    Flux<Order> findAllByUserId(Long userId);
    Mono<Order> findByIdAndUserId(Long id, Long userId);
}
