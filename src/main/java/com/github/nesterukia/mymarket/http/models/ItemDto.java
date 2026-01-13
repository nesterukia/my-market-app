package com.github.nesterukia.mymarket.http.models;

import com.github.nesterukia.mymarket.domain.CartItem;
import com.github.nesterukia.mymarket.domain.Item;
import com.github.nesterukia.mymarket.domain.OrderItem;

public record ItemDto(
        Long id,
        String title,
        String description,
        String imgPath,
        Long price,
        int count
) {
    public static ItemDto fromItem(Item item) {
        int quantity = 0;
        if (!item.getCartItems().isEmpty()) {
            quantity = item.getCartItems().getFirst().getQuantity();
        }
        return new ItemDto(
                item.getId(),
                item.getTitle(),
                item.getDescription(),
                item.getImgPath(),
                item.getPrice(),
                quantity
        );
    }

    public static ItemDto fromOrderItem(OrderItem orderItem) {
        return new ItemDto(
                orderItem.getItem().getId(),
                orderItem.getItem().getTitle(),
                orderItem.getItem().getDescription(),
                orderItem.getItem().getImgPath(),
                orderItem.getItem().getPrice(),
                orderItem.getQuantity()
        );
    }

    public static ItemDto fromCartItem(CartItem cartItem) {
        return new ItemDto(
                cartItem.getItem().getId(),
                cartItem.getItem().getTitle(),
                cartItem.getItem().getDescription(),
                cartItem.getItem().getImgPath(),
                cartItem.getItem().getPrice(),
                cartItem.getQuantity()
        );
    }
}
