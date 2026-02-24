package com.github.nesterukia.mymarket.http.dto;

import com.github.nesterukia.mymarket.domain.Item;

public record ItemDto(
        Long id,
        String title,
        String description,
        String imgPath,
        Double price,
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
