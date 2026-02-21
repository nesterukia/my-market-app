package com.github.nesterukia.mymarket.dao;

import com.github.nesterukia.mymarket.domain.Item;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface ItemRepository extends ReactiveCrudRepository<Item, Long> {
    @Query("""
        SELECT * FROM items i
        WHERE LOWER(i.title) LIKE LOWER(CONCAT('%', :search, '%'))
        OR LOWER(i.description) LIKE LOWER(CONCAT('%', :search, '%'))
    """)
    Flux<Item> findByTitleOrDescriptionContainingIgnoreCase(@Param("search") String search,
                                                            Pageable pageable);

    @Query("""
        SELECT COUNT(*) FROM items i
        WHERE LOWER(i.title) LIKE LOWER(CONCAT('%', :search, '%'))
        OR LOWER(i.description) LIKE LOWER(CONCAT('%', :search, '%'))
    """)
    Mono<Long> countByTitleOrDescriptionContainingIgnoreCase(@Param("search") String search);
}
