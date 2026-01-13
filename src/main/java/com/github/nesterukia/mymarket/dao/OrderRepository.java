package com.github.nesterukia.mymarket.dao;

import com.github.nesterukia.mymarket.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {}
