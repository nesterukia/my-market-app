package com.github.nesterukia.mymarket.service;

import com.github.nesterukia.mymarket.dao.ItemRepository;
import com.github.nesterukia.mymarket.dao.OrderItemRepository;
import com.github.nesterukia.mymarket.domain.Item;
import com.github.nesterukia.mymarket.domain.exceptions.EntityNotFoundException;
import com.github.nesterukia.mymarket.http.models.ItemDto;
import com.github.nesterukia.mymarket.utils.EntityType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


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

    public Mono<Page<Item>> getItems(String search, Pageable pageable) {
        Flux<Item> items = itemRepository.findByTitleOrDescriptionContainingIgnoreCase(search, pageable)
                .skip(pageable.getOffset())
                .take(pageable.getPageSize())
                .map(item -> {
                    log.debug("ItemService: {}", item);
                    return item;
                });

        Mono<Long> countOfItems = itemRepository.countByTitleOrDescriptionContainingIgnoreCase(search);

        return Mono.zip(items.collectList(), countOfItems)
                .map(tuple -> new PageImpl<>(tuple.getT1(), pageable, tuple.getT2()));
    }

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
