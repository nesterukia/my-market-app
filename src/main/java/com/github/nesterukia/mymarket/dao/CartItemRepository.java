package com.github.nesterukia.mymarket.dao;

import com.github.nesterukia.mymarket.domain.CartItem;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CartItemRepository extends ReactiveCrudRepository<CartItem, Long> {
    @Query("""
            SELECT * FROM cart_item ci
            WHERE ci.cart_id = :cartId
            AND ci.item_id = :itemId
    """)
    Mono<CartItem> findByCartIdAndItemId(@Param("cartId") Long cartId, @Param("itemId") Long itemId);

    @Modifying
    @Query("DELETE FROM cart_item ci where ci.cart_id = :cartid")
    Mono<Void> deleteAllByCartId(@Param("cartId") Long cartId);

    Flux<CartItem> findAllByCartId(Long cartId);
}
