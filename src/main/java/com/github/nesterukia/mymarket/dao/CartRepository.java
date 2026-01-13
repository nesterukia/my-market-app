package com.github.nesterukia.mymarket.dao;

import com.github.nesterukia.mymarket.domain.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartRepository extends JpaRepository<Cart, Long> {}
