package com.github.nesterukia.mymarket.service;

import com.github.nesterukia.mymarket.dao.ItemRepository;
import com.github.nesterukia.mymarket.dao.OrderItemRepository;
import com.github.nesterukia.mymarket.domain.Item;
import com.github.nesterukia.mymarket.domain.SortType;
import com.github.nesterukia.mymarket.domain.exceptions.EntityNotFoundException;
import com.github.nesterukia.mymarket.http.models.ItemDto;
import com.github.nesterukia.mymarket.utils.EntityType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;


@Service
@Slf4j
@Transactional
public class ItemService {
    private final ItemRepository itemRepository;
    private final OrderItemRepository orderItemRepository;

    @Autowired
    public ItemService(ItemRepository itemRepository, OrderItemRepository orderItemRepository) {
        this.itemRepository = itemRepository;
        this.orderItemRepository = orderItemRepository;
    }

    @Cacheable(value = "items:list", key = "'page:' + #pageable.pageNumber + ':' + #pageable.pageSize + ':' + #search")
    public Mono<List<Item>> getItems(String search, Pageable pageable) {
        Comparator<Item> byPriceAscending = Comparator.comparingLong(Item::getPrice);
        Comparator<Item> byPriceDescending = byPriceAscending.reversed();

        Comparator<Item> byTitleAscending = Comparator.comparing(Item::getTitle);
        Comparator<Item> byTitleDescending = byTitleAscending.reversed();

        Comparator<Item> byId = Comparator.comparingLong(Item::getId);

        Iterator<Sort.Order> sortPropertyIterator = pageable.getSort().iterator();
        final Comparator<Item> byRequiredOrder;
        if (sortPropertyIterator.hasNext()) {
            Sort.Order firstOrder = sortPropertyIterator.next();
            String property = firstOrder.getProperty();
            Sort.Direction direction = firstOrder.getDirection();

            if (property.equals(SortType.ALPHA.getProperty())) {
                byRequiredOrder = direction.isAscending() ? byTitleAscending : byTitleDescending;
            } else {
                byRequiredOrder = direction.isAscending() ? byPriceAscending : byPriceDescending;
            }
        } else {
            byRequiredOrder = byId;
        }

        return itemRepository.findByTitleOrDescriptionContainingIgnoreCase(search, pageable)
                .skip(pageable.getOffset())
                .take(pageable.getPageSize())
                .sort(byRequiredOrder)
                .collectList();
    }

    public Mono<Page<Item>> formItemsPage(List<Item> items, String search, Pageable pageable) {
        return itemRepository.countByTitleOrDescriptionContainingIgnoreCase(search)
                .map(countOfItems -> new PageImpl<>(items, pageable, countOfItems));
    }

    @Cacheable(value = "items:card", key = "#id")
    public Mono<Item> getItemById(Long id) {
        return itemRepository.findById(id).switchIfEmpty(
                Mono.error(new EntityNotFoundException(EntityType.Item, id))
        );
    }

    public Flux<ItemDto> findAllByOrderId(Long orderId) {
        return orderItemRepository.findAllByOrderId(orderId)
                .flatMap(orderItem -> Mono.zip(
                        itemRepository.findById(orderItem.getItemId()),
                        Mono.just(orderItem.getQuantity()))
                )
                .map(itemQuantityTuple -> ItemDto.fromItem(
                        itemQuantityTuple.getT1(),
                        itemQuantityTuple.getT2()
                ));
    }
}
