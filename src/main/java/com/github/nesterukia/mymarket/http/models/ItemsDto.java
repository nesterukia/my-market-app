package com.github.nesterukia.mymarket.http.models;

import com.github.nesterukia.mymarket.domain.Item;
import com.github.nesterukia.mymarket.domain.SortType;
import com.github.nesterukia.mymarket.utils.ItemUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class ItemsDto {
    private List<List<ItemDto>> items;
    private String search;
    private SortType sort;
    private Paging paging;

    public ItemsDto(Page<Item> pageOfItems, SortType sortType, String search) {
        Paging paging = new Paging(
                pageOfItems.getPageable().getPageSize(),
                pageOfItems.getPageable().getPageNumber() + 1,
                pageOfItems.hasPrevious(),
                pageOfItems.hasNext()
        );

        this.items = groupItemsByThree(pageOfItems.get().toList());
        this.search = search;
        this.sort = sortType;
        this.paging = paging;
    }

    private List<List<ItemDto>> groupItemsByThree(List<Item> items) {
        List<List<ItemDto>> result = new ArrayList<>();

        for (int i = 0; i < items.size(); i += 3) {
            List<ItemDto> group = new ArrayList<>(3);

            for (int j = i; j < i + 3 && j < items.size(); j++) {
                group.add(ItemDto.fromItem(items.get(j)));
            }

            while (group.size() < 3) {
                group.add(ItemUtils.getItemMockDto());
            }

            result.add(group);
        }
        return result;
    }
}
