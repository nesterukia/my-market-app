package com.github.nesterukia.mymarket.domain;


import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "cart_item")
@Getter
@Setter
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class CartItem {

    @Id
    private Long id;

    @Column("cart_id")
    private Long cartId;

    @Column("item_id")
    private Long itemId;

    @Column("quantity")
    @Builder.Default
    private Integer quantity = 0;

    public void increaseQuantity() {
        this.quantity++;
    }

    public void decreaseQuantity() {
        if (this.quantity > 0) {
            this.quantity--;
        }
    }
}

