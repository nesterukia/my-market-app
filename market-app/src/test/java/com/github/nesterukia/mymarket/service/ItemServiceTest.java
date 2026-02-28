package com.github.nesterukia.mymarket.service;

import com.github.nesterukia.mymarket.dao.ItemRepository;
import com.github.nesterukia.mymarket.dao.OrderItemRepository;
import com.github.nesterukia.mymarket.domain.Item;
import com.github.nesterukia.mymarket.domain.OrderItem;
import com.github.nesterukia.mymarket.domain.exceptions.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @InjectMocks
    private ItemService itemService;

    private Item testItem1;
    private Item testItem2;
    private Item testItem3;
    private OrderItem testOrderItem1;
    private OrderItem testOrderItem2;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        testItem1 = Item.builder()
                .id(1L)
                .title("Test Item 1")
                .description("Description 1")
                .imgPath("/img1.jpg")
                .price(100.0)
                .build();

        testItem2 = Item.builder()
                .id(2L)
                .title("Test Item 2")
                .description("Description 2")
                .imgPath("/img2.jpg")
                .price(200.0)
                .build();

        testItem3 = Item.builder()
                .id(3L)
                .title("Test Item 3")
                .description("Description 3")
                .imgPath("/img3.jpg")
                .price(300.0)
                .build();

        testOrderItem1 = OrderItem.builder()
                .id(1L)
                .orderId(1L)
                .itemId(testItem1.getId())
                .quantity(2)
                .build();

        testOrderItem2 = OrderItem.builder()
                .id(2L)
                .orderId(1L)
                .itemId(testItem2.getId())
                .quantity(1)
                .build();

        pageable = PageRequest.of(0, 10);
    }

    @Test
    void getItems_ShouldReturnPageWithItems() {
        List<Item> itemList = List.of(testItem1, testItem2, testItem3);
        Flux<Item> itemsFlux = Flux.fromIterable(itemList);

        when(itemRepository.findByTitleOrDescriptionContainingIgnoreCase(eq(""), any(Pageable.class)))
                .thenReturn(itemsFlux);

        StepVerifier.create(itemService.getItems("", pageable))
                .expectNextMatches(list ->
                        list.size() == 3 &&
                                list.get(0).getId().equals(1L) &&
                                list.get(1).getId().equals(2L) &&
                                list.get(2).getId().equals(3L)
                )
                .verifyComplete();
    }

    @Test
    void getItems_WithSearch_ShouldReturnFilteredItems() {
        List<Item> itemList = List.of(testItem1, testItem2);
        Flux<Item> itemsFlux = Flux.fromIterable(itemList);

        when(itemRepository.findByTitleOrDescriptionContainingIgnoreCase(eq("test"), any(Pageable.class)))
                .thenReturn(itemsFlux);

        StepVerifier.create(itemService.getItems("test", pageable))
                .expectNextMatches(page ->
                        page.size() == 2
                )
                .verifyComplete();
    }

    @Test
    void getItems_WithEmptyResult_ShouldReturnEmptyPage() {
        Flux<Item> itemsFlux = Flux.empty();
        when(itemRepository.findByTitleOrDescriptionContainingIgnoreCase(eq("nonexistent"), any(Pageable.class)))
                .thenReturn(itemsFlux);

        StepVerifier.create(itemService.getItems("nonexistent", pageable))
                .expectNextMatches(List::isEmpty)
                .verifyComplete();
    }

    @Test
    void getItemById_ShouldReturnItemWhenExists() {
        when(itemRepository.findById(1L))
                .thenReturn(Mono.just(testItem1));

        StepVerifier.create(itemService.getItemById(1L))
                .expectNext(testItem1)
                .verifyComplete();
    }

    @Test
    void getItemById_ShouldThrowExceptionWhenNotFound() {
        when(itemRepository.findById(999L))
                .thenReturn(Mono.empty());

        StepVerifier.create(itemService.getItemById(999L))
                .expectErrorMatches(throwable ->
                        throwable instanceof EntityNotFoundException &&
                                throwable.getMessage().equals("Item with id = '999' wasn't found.")
                )
                .verify();
    }

    @Test
    void findAllByOrderId_ShouldReturnItemDtos() {
        when(orderItemRepository.findAllByOrderId(1L))
                .thenReturn(Flux.just(testOrderItem1, testOrderItem2));

        when(itemRepository.findById(1L))
                .thenReturn(Mono.just(testItem1));
        when(itemRepository.findById(2L))
                .thenReturn(Mono.just(testItem2));

        StepVerifier.create(itemService.findAllByOrderId(1L).collectList())
                .expectNextMatches(itemDtos ->
                        itemDtos.size() == 2 &&
                                itemDtos.get(0).id().equals(1L) &&
                                itemDtos.get(0).count() == 2 &&
                                itemDtos.get(1).id().equals(2L) &&
                                itemDtos.get(1).count() == 1
                )
                .verifyComplete();
    }

    @Test
    void findAllByOrderId_ShouldReturnEmptyFluxWhenNoOrderItems() {
        when(orderItemRepository.findAllByOrderId(999L))
                .thenReturn(Flux.empty());

        StepVerifier.create(itemService.findAllByOrderId(999L))
                .verifyComplete();
    }

    @Test
    void findAllByOrderId_ShouldHandleMissingItem() {
        when(orderItemRepository.findAllByOrderId(1L))
                .thenReturn(Flux.just(testOrderItem1));

        when(itemRepository.findById(1L))
                .thenReturn(Mono.empty());

        StepVerifier.create(itemService.findAllByOrderId(1L))
                .verifyComplete();
    }

    @Test
    void findAllByOrderId_ShouldHandleMultipleItemsWithSameId() {
        OrderItem duplicateOrderItem = OrderItem.builder()
                .id(3L)
                .orderId(1L)
                .itemId(testItem1.getId())
                .quantity(3)
                .build();

        when(orderItemRepository.findAllByOrderId(1L))
                .thenReturn(Flux.just(testOrderItem1, duplicateOrderItem));

        when(itemRepository.findById(1L))
                .thenReturn(Mono.just(testItem1));

        StepVerifier.create(itemService.findAllByOrderId(1L).collectList())
                .expectNextMatches(itemDtos ->
                        itemDtos.size() == 2 &&
                                itemDtos.get(0).id().equals(1L) &&
                                itemDtos.get(0).count() == 2 &&
                                itemDtos.get(1).id().equals(1L) &&
                                itemDtos.get(1).count() == 3
                )
                .verifyComplete();
    }
}