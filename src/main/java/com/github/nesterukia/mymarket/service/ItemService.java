package com.github.nesterukia.mymarket.service;

import com.github.nesterukia.mymarket.dao.ItemRepository;
import com.github.nesterukia.mymarket.domain.Item;
import com.github.nesterukia.mymarket.domain.SortType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
public class ItemService {
    private final ItemRepository itemRepository;

    @Autowired
    public ItemService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    public Page<Item> getItems(String search, SortType sortType, int pageNumber, int pageSize) {
        Sort sort = switch (sortType) {
            case ALPHA  -> Sort.by(Sort.Direction.ASC, "title");
            case PRICE -> Sort.by(Sort.Direction.ASC, "price");
            case NO -> Sort.unsorted();
        };

        return itemRepository.findByTitleOrDescriptionContainingIgnoreCase(
                search,
                PageRequest.of(pageNumber - 1, pageSize, sort)
        );
    }

    public Item getItemById(Long id) {
        return itemRepository.findById(id).orElseThrow();
    }
}
