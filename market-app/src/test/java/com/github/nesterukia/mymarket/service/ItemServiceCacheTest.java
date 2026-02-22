package com.github.nesterukia.mymarket.service;

import com.github.nesterukia.mymarket.dao.ItemRepository;
import com.github.nesterukia.mymarket.dao.OrderItemRepository;
import com.github.nesterukia.mymarket.domain.Item;
import com.github.nesterukia.mymarket.utils.RedisContainerTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.stream.LongStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest
public class ItemServiceCacheTest extends RedisContainerTest {

    @MockitoBean
    private ItemRepository itemRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private ItemService itemService;

    @Test
    void getItemById_cacheHit_returnsCachedItem() {
        Long itemId = 1L;
        Item testItem = Item.builder()
                .id(itemId)
                .title("Test Item")
                .description("Test Description")
                .imgPath("/img/test.jpg")
                .price(1000L)
                .build();
        when(itemRepository.findById(eq(itemId))).thenReturn(Mono.just(testItem));

        Item firstCall = itemService.getItemById(itemId).block();

        assertThat(firstCall).isNotNull();
        assertThat(firstCall.getId()).isEqualTo(itemId);

        Item secondCall = itemService.getItemById(itemId).block();

        assertThat(secondCall).isEqualTo(firstCall);
        verify(itemRepository, times(1)).findById(itemId);
    }

    @Test
    void getItems_cacheWorksForDifferentPages() {
        List<Item> allItems = LongStream.range(1, 31).
                mapToObj(i -> Item.builder()
                        .id(i)
                        .title("Item " + i)
                        .description("Description " + i)
                        .price(1000 + i)
                        .imgPath("/fake/path")
                        .build()
                ).toList();

        Pageable firstPage = PageRequest.of(0, 10, Sort.unsorted());
        Pageable secondPage = PageRequest.of(1, 10, Sort.unsorted());

        when(itemRepository.findByTitleOrDescriptionContainingIgnoreCase(eq(""), any(Pageable.class)))
                .thenReturn(Flux.fromIterable(allItems));

        List<Item> firstPageResult = itemService.getItems("", firstPage).block();

        assertThat(firstPageResult).hasSize(10);
        assertThat(firstPageResult.getFirst().getId()).isEqualTo(1L);
        List<Item> secondPageResult = itemService.getItems("", secondPage).block();

        assertThat(secondPageResult).hasSize(10);
        assertThat(secondPageResult.getFirst().getId()).isEqualTo(11L);

        List<?> firstPageAgain = itemService.getItems("", firstPage).block();

        assertThat(firstPageAgain).hasSize(10);
        assertThat(((Map<?, ?>) firstPageAgain.getFirst()).get("id")).isEqualTo(1);

        verify(itemRepository, times(2)).findByTitleOrDescriptionContainingIgnoreCase(eq(""), any(Pageable.class));
    }

    @Test
    void getItems_cacheMissOnSearchChange() {
        Item appleIPhone = Item.builder().id(1L).title("Apple iPhone").price(999L).build();
        Item samsungGalaxy = Item.builder().id(2L).title("Samsung Galaxy").price(899L).build();
        Pageable firstPage = PageRequest.of(0, 10);

        when(itemRepository.findByTitleOrDescriptionContainingIgnoreCase(eq("iphone"), eq(firstPage)))
                .thenReturn(Flux.just(appleIPhone));
        when(itemRepository.findByTitleOrDescriptionContainingIgnoreCase(eq("samsung"), eq(firstPage)))
                .thenReturn(Flux.just(samsungGalaxy));

        List<Item> iphoneResult = itemService.getItems("iphone", firstPage).block();

        assertThat(iphoneResult).hasSize(1);
        assertThat(iphoneResult.getFirst().getTitle()).contains("iPhone");

        List<Item> samsungResult = itemService.getItems("samsung", firstPage).block();

        assertThat(samsungResult).hasSize(1);
        assertThat(samsungResult.getFirst().getTitle()).contains("Samsung");
    }

    @Test
    void cacheWorksTogetherWithDatabase() {
        Item testItem = Item.builder()
                .id(999L)
                .title("New Cached Item")
                .description("Fresh from DB")
                .price(500L)
                .build();

        Pageable pageable = PageRequest.of(0, 20);

        when(itemRepository.findById(eq(testItem.getId()))).thenReturn(Mono.just(testItem));
        when(itemRepository.findByTitleOrDescriptionContainingIgnoreCase(any(String.class), eq(pageable)))
                .thenReturn(Flux.just(testItem));

        List<Item> firstResult = itemService.getItems("Cached", pageable).block();
        Item firstItemCall = itemService.getItemById(999L).block();

        assertThat(firstResult).hasSize(1);
        assertThat(firstItemCall.getTitle()).isEqualTo("New Cached Item");

        List<Item> cachedResult = itemService.getItems("Cached", pageable).block();
        Item cachedItemCall = itemService.getItemById(999L).block();

        assertThat(cachedResult.size()).isEqualTo(firstResult.size());
        assertThat(cachedItemCall).isEqualTo(firstItemCall);
    }
}
