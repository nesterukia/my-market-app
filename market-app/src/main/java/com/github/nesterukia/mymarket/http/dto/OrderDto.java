package com.github.nesterukia.mymarket.http.dto;

import com.github.nesterukia.mymarket.domain.Order;
import com.github.nesterukia.mymarket.utils.ItemUtils;

import java.util.List;

public record OrderDto(
        Long id,
        List<ItemDto> items,
        Double totalSum
) {
    public static OrderDto fromOrder(Order order, List<ItemDto> itemDtos) {
        return new OrderDto(
                order.getId(),
                itemDtos,
                ItemUtils.calculateTotalSum(itemDtos)
        );
    }
}
