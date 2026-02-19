package com.github.nesterukia.mymarket.http.models;

import com.github.nesterukia.mymarket.domain.Item;

public record ItemDto(
        Long id,
        String title,
        String description,
        String imgPath,
        Long price,
        int count
) {
    public static ItemDto fromItem(Item item, int quantity) {
        return new ItemDto(
                item.getId(),
                item.getTitle(),
                item.getDescription(),
                item.getImgPath(),
                item.getPrice(),
                quantity
        );
    }
}
