package com.github.nesterukia.mymarket.domain;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "order_item")
@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {

    @Id
    private Long id;

    @Column("order_id")
    private Long orderId;

    @Column("item_id")
    private Long itemId;

    @Column("quantity")
    @Builder.Default
    private Integer quantity = 0;

    public OrderItem(Long orderId, Long itemId, Integer quantity) {
        this.orderId = orderId;
        this.itemId = itemId;
        this.quantity = quantity;
    }
}
