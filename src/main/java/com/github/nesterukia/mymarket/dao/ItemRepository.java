package com.github.nesterukia.mymarket.dao;

import com.github.nesterukia.mymarket.domain.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    @Query("""
            SELECT i FROM Item i
            WHERE LOWER(i.title) LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(i.description) LIKE LOWER(CONCAT('%', :search, '%'))
    """)
    Page<Item> findByTitleOrDescriptionContainingIgnoreCase(@Param("search") String search,
                                                            Pageable pageable);
}
