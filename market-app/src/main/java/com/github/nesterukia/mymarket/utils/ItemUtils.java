package com.github.nesterukia.mymarket.utils;

import com.github.nesterukia.mymarket.domain.Item;
import com.github.nesterukia.mymarket.http.dto.ItemDto;

import java.util.List;

public class ItemUtils {
    private static Long MOCK_ITEM_ID = -1L;

    public static ItemDto getItemMockDto() {
        return ItemDto.fromItem(new Item(
                MOCK_ITEM_ID,
                "MOCK",
                "MOCK",
                "MOCK_IMG_PATH",
                -1.0
        ), 0);
    }

    public static Double calculateTotalSum(List<ItemDto> listOfItemDtos) {
        if (listOfItemDtos.isEmpty()) {
            return 0.0;
        } else {
            return listOfItemDtos.stream()
                    .mapToDouble(item -> item.count() * item.price())
                    .sum();
        }
    }
}
