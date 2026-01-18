package com.github.nesterukia.mymarket.utils;

import com.github.nesterukia.mymarket.domain.Item;
import com.github.nesterukia.mymarket.http.models.ItemDto;

import java.util.ArrayList;
import java.util.List;

public class ItemUtils {
    private static Long MOCK_ITEM_ID = -1L;

    public static ItemDto getItemMockDto() {
        return ItemDto.fromItem(new Item(
                MOCK_ITEM_ID,
                "MOCK",
                "MOCK",
                "MOCK_IMG_PATH",
                -1L,
                new ArrayList<>(),
                new ArrayList<>()
        ));
    }

    public static Long calculateTotalSum(List<ItemDto> listOfItemDtos) {
        if (listOfItemDtos.isEmpty()) {
            return 0L;
        } else {
            return listOfItemDtos.stream()
                    .mapToLong(item -> item.count() * item.price())
                    .sum();
        }
    }
}
