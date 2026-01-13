package com.github.nesterukia.mymarket.http.models;

import com.github.nesterukia.mymarket.domain.Order;
import com.github.nesterukia.mymarket.utils.ItemUtils;

import java.util.List;

public record OrderDto(
        Long id,
        List<ItemDto> items,
        Long totalSum
) {
    public static OrderDto fromOrder(Order order) {
        List<ItemDto> listOfItemDtos = order.getOrderItems().stream()
                .map(ItemDto::fromOrderItem)
                .toList();
        return new OrderDto(
                order.getId(),
                listOfItemDtos,
                ItemUtils.calculateTotalSum(listOfItemDtos)
        );
    }
}
