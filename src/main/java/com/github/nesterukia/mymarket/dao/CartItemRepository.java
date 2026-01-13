package com.github.nesterukia.mymarket.dao;

import com.github.nesterukia.mymarket.domain.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    @Query("""
            SELECT ci FROM CartItem ci
            WHERE ci.cart.id = :cartId
            AND ci.item.id = :itemId
    """)
    Optional<CartItem> findByCartIdAndItemId(@Param("cartId") Long cartId, @Param("itemId") Long itemId);
}
