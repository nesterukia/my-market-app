package com.github.nesterukia.mymarket.http;

import com.github.nesterukia.mymarket.dao.CartRepository;
import com.github.nesterukia.mymarket.domain.Cart;
import com.github.nesterukia.mymarket.domain.Item;
import com.github.nesterukia.mymarket.domain.User;
import com.github.nesterukia.mymarket.service.CartService;
import com.github.nesterukia.mymarket.service.ItemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@WebFluxTest(ItemController.class)
class ItemControllerTest {

    private static final String ITEMS_TITLE = "<title>Витрина магазина</title>";
    private static final String ITEM_TITLE = "<title>Товар</title>";

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private ItemService itemService;

    @MockitoBean
    private CartService cartService;

    private User testUser;
    private Cart testCart;
    private Item testItem1;
    private Item testItem2;
    private Item testItem3;
    private Item testItem4;
    private Item testItem5;
    private List<Item> testItems;

    @BeforeEach
    void setUp() {
        testUser = User.builder().id(1L).build();
        testCart = Cart.builder().userId(testUser.getId()).build();

        testItem1 = Item.builder()
                .id(1L)
                .title("Test Item 1")
                .description("Description 1")
                .imgPath("/img1.jpg")
                .price(100L)
                .build();

        testItem2 = Item.builder()
                .id(2L)
                .title("Test Item 2")
                .description("Description 2")
                .imgPath("/img2.jpg")
                .price(200L)
                .build();

        testItem3 = Item.builder()
                .id(3L)
                .title("Test Item 3")
                .description("Description 3")
                .imgPath("/img3.jpg")
                .price(300L)
                .build();

        testItem4 = Item.builder()
                .id(4L)
                .title("Test Item 4")
                .description("Description 4")
                .imgPath("/img4.jpg")
                .price(400L)
                .build();

        testItem5 = Item.builder()
                .id(5L)
                .title("Test Item 5")
                .description("Description 5")
                .imgPath("/img5.jpg")
                .price(500L)
                .build();

        testItems = List.of(testItem1, testItem2, testItem3, testItem4, testItem5);
    }

    @Test
    void getItems_ShouldReturnItemsViewWithDefaultParameters() {
        Page<Item> itemPage = new PageImpl<>(testItems.subList(0, 5), Pageable.ofSize(5), 5);

        when(itemService.getItems(eq(""), any(Pageable.class)))
                .thenReturn(Mono.just(itemPage));

        when(cartService.countCartItemsByUserIdAndItemId(isNull(), eq(1L)))
                .thenReturn(Mono.just(0));
        when(cartService.countCartItemsByUserIdAndItemId(isNull(), eq(2L)))
                .thenReturn(Mono.just(2));
        when(cartService.countCartItemsByUserIdAndItemId(isNull(), eq(3L)))
                .thenReturn(Mono.just(1));
        when(cartService.countCartItemsByUserIdAndItemId(isNull(), eq(4L)))
                .thenReturn(Mono.just(0));
        when(cartService.countCartItemsByUserIdAndItemId(isNull(), eq(5L)))
                .thenReturn(Mono.just(3));

        webTestClient.get()
                .uri("/items")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(content -> {
                    assert content.contains(ITEMS_TITLE);
                    assert content.contains("Test Item 1");
                    assert content.contains("Test Item 2");
                    assert content.contains("Test Item 3");
                    assert content.contains("Test Item 4");
                    assert content.contains("Test Item 5");
                    assert content.contains("NO");
                });
    }

    @Test
    void getItems_ShouldReturnItemsViewWithSearchParameter() {
        Page<Item> itemPage = new PageImpl<>(List.of(testItem1, testItem2), Pageable.ofSize(5), 2);

        when(itemService.getItems(eq("test"), any(Pageable.class)))
                .thenReturn(Mono.just(itemPage));

        when(cartService.countCartItemsByUserIdAndItemId(isNull(), eq(1L)))
                .thenReturn(Mono.just(1));
        when(cartService.countCartItemsByUserIdAndItemId(isNull(), eq(2L)))
                .thenReturn(Mono.just(0));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/items")
                        .queryParam("search", "test")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(content -> {
                    assert content.contains(ITEMS_TITLE);
                    assert content.contains("Test Item 1");
                    assert content.contains("Test Item 2");
                    assert !content.contains("Test Item 3");
                    assert content.contains("test");
                });
    }

    @Test
    void getItems_ShouldReturnItemsViewWithAlphaSort() {
        Page<Item> itemPage = new PageImpl<>(testItems.subList(0, 5), Pageable.ofSize(5), 5);

        when(itemService.getItems(eq(""), any(Pageable.class)))
                .thenReturn(Mono.just(itemPage));

        when(cartService.countCartItemsByUserIdAndItemId(isNull(), eq(1L)))
                .thenReturn(Mono.just(0));
        when(cartService.countCartItemsByUserIdAndItemId(isNull(), eq(2L)))
                .thenReturn(Mono.just(0));
        when(cartService.countCartItemsByUserIdAndItemId(isNull(), eq(3L)))
                .thenReturn(Mono.just(0));
        when(cartService.countCartItemsByUserIdAndItemId(isNull(), eq(4L)))
                .thenReturn(Mono.just(0));
        when(cartService.countCartItemsByUserIdAndItemId(isNull(), eq(5L)))
                .thenReturn(Mono.just(0));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/items")
                        .queryParam("sort", "ALPHA")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(content -> {
                    assert content.contains(ITEMS_TITLE);
                    assert content.contains("ALPHA");
                });
    }

    @Test
    void getItems_ShouldReturnItemsViewWithPriceSort() {
        Page<Item> itemPage = new PageImpl<>(testItems.subList(0, 5), Pageable.ofSize(5), 5);

        when(itemService.getItems(eq(""), any(Pageable.class)))
                .thenReturn(Mono.just(itemPage));

        when(cartService.countCartItemsByUserIdAndItemId(isNull(), eq(1L)))
                .thenReturn(Mono.just(0));
        when(cartService.countCartItemsByUserIdAndItemId(isNull(), eq(2L)))
                .thenReturn(Mono.just(0));
        when(cartService.countCartItemsByUserIdAndItemId(isNull(), eq(3L)))
                .thenReturn(Mono.just(0));
        when(cartService.countCartItemsByUserIdAndItemId(isNull(), eq(4L)))
                .thenReturn(Mono.just(0));
        when(cartService.countCartItemsByUserIdAndItemId(isNull(), eq(5L)))
                .thenReturn(Mono.just(0));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/items")
                        .queryParam("sort", "PRICE")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(content -> {
                    assert content.contains(ITEMS_TITLE);
                    assert content.contains("PRICE");
                });
    }

    @Test
    void getItems_ShouldReturnItemsViewWithPaginationParameters() {
        Page<Item> itemPage = new PageImpl<>(testItems.subList(2, 5), Pageable.ofSize(3).withPage(1), 5);

        when(itemService.getItems(eq(""), any(Pageable.class)))
                .thenReturn(Mono.just(itemPage));

        when(cartService.countCartItemsByUserIdAndItemId(isNull(), eq(3L)))
                .thenReturn(Mono.just(0));
        when(cartService.countCartItemsByUserIdAndItemId(isNull(), eq(4L)))
                .thenReturn(Mono.just(2));
        when(cartService.countCartItemsByUserIdAndItemId(isNull(), eq(5L)))
                .thenReturn(Mono.just(1));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/items")
                        .queryParam("pageNumber", 2)
                        .queryParam("pageSize", 3)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(content -> {
                    assert content.contains(ITEMS_TITLE);
                    assert !content.contains("Test Item 1");
                    assert !content.contains("Test Item 2");
                    assert content.contains("Test Item 3");
                    assert content.contains("Test Item 4");
                    assert content.contains("Test Item 5");
                });
    }

    @Test
    void getItems_ShouldHandleRootPath() {
        Page<Item> itemPage = new PageImpl<>(testItems.subList(0, 5), Pageable.ofSize(5), 5);

        when(itemService.getItems(eq(""), any(Pageable.class)))
                .thenReturn(Mono.just(itemPage));

        when(cartService.countCartItemsByUserIdAndItemId(isNull(), eq(1L)))
                .thenReturn(Mono.just(0));
        when(cartService.countCartItemsByUserIdAndItemId(isNull(), eq(2L)))
                .thenReturn(Mono.just(0));
        when(cartService.countCartItemsByUserIdAndItemId(isNull(), eq(3L)))
                .thenReturn(Mono.just(0));
        when(cartService.countCartItemsByUserIdAndItemId(isNull(), eq(4L)))
                .thenReturn(Mono.just(0));
        when(cartService.countCartItemsByUserIdAndItemId(isNull(), eq(5L)))
                .thenReturn(Mono.just(0));

        webTestClient.get()
                .uri("/")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(content -> {
                    assert content.contains(ITEMS_TITLE);
                });
    }

    @Test
    void getItemById_ShouldReturnItemView() {
        Long itemId = 1L;

        when(itemService.getItemById(itemId))
                .thenReturn(Mono.just(testItem1));

        when(cartService.countCartItemsByUserIdAndItemId(isNull(), eq(itemId)))
                .thenReturn(Mono.just(2));

        webTestClient.get()
                .uri("/items/{id}", itemId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(content -> {
                    assert content.contains(ITEM_TITLE);
                    assert content.contains("Test Item 1");
                    assert content.contains("Description 1");
                    assert content.contains("/img1.jpg");
                    assert content.contains("100");
                    assert content.contains("2");
                });
    }

    @Test
    void getItemById_ShouldHandleZeroQuantity() {
        Long itemId = 1L;

        when(itemService.getItemById(itemId))
                .thenReturn(Mono.just(testItem1));

        when(cartService.countCartItemsByUserIdAndItemId(isNull(), eq(itemId)))
                .thenReturn(Mono.just(0));

        webTestClient.get()
                .uri("/items/{id}", itemId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(content -> {
                    assert content.contains(ITEM_TITLE);
                    assert content.contains("Test Item 1");
                    assert content.contains("0");
                });
    }
}