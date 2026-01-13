package com.github.nesterukia.mymarket.dao;

import com.github.nesterukia.mymarket.domain.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {}
