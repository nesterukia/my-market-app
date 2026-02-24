package com.github.nesterukia.mymarket.http;

import com.github.nesterukia.mymarket.domain.ActionType;
import com.github.nesterukia.mymarket.domain.Cart;
import com.github.nesterukia.mymarket.domain.CartItem;
import com.github.nesterukia.mymarket.domain.Item;
import com.github.nesterukia.mymarket.domain.User;
import com.github.nesterukia.mymarket.http.dto.payment.PaymentInfo;
import com.github.nesterukia.mymarket.service.CartService;
import com.github.nesterukia.mymarket.service.ItemService;
import com.github.nesterukia.mymarket.service.PaymentService;
import com.github.nesterukia.mymarket.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static com.github.nesterukia.mymarket.utils.UserUtils.USER_ID_COOKIE;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebFluxTest(CartController.class)
class CartControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private CartService cartService;

    @MockitoBean
    private ItemService itemService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private PaymentService paymentService;

    private User testUser;
    private Cart testCart;
    private Item testItem1;
    private Item testItem2;
    private CartItem testCartItem1;
    private CartItem testCartItem2;

    private static final String ITEM_TITLE = "<title>Товар</title>";
    private static final String ITEMS_TITLE = "<title>Витрина магазина</title>";
    private static final String CART_TITLE = "<title>Корзина</title>";
    private static final String ORDERS_TITLE = "<title>Список заказов</title>";
    private static final String ORDER_TITLE = "<title>Заказ</title>";

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .build();

        testCart = Cart.builder()
                .id(1L)
                .userId(testUser.getId())
                .build();

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

        testCartItem1 = CartItem.builder()
                .id(1L)
                .cartId(testCart.getId())
                .itemId(testItem1.getId())
                .quantity(2)
                .build();

        testCartItem2 = CartItem.builder()
                .id(2L)
                .cartId(testCart.getId())
                .itemId(testItem2.getId())
                .quantity(1)
                .build();
    }

    @Test
    void getCartItems_ShouldReturnCartViewWithItems() {
        when(userService.getOrCreate(eq(testUser.getId()), any(ServerWebExchange.class)))
                .thenReturn(Mono.just(testUser));

        when(cartService.findByUserId(testUser.getId()))
                .thenReturn(Mono.just(testCart));

        when(cartService.create(testUser))
                .thenReturn(Mono.just(testCart));

        when(cartService.findAllCartItemsByCart(testCart))
                .thenReturn(Flux.just(testCartItem1, testCartItem2));

        when(itemService.getItemById(testItem1.getId()))
                .thenReturn(Mono.just(testItem1));

        when(itemService.getItemById(testItem2.getId()))
                .thenReturn(Mono.just(testItem2));

        when(paymentService.checkUserBalance(anyLong(), anyDouble())).thenReturn(
                Mono.just(new PaymentInfo(true, true))
        );

        webTestClient.get()
                .uri("/cart/items")
                .cookie(USER_ID_COOKIE, testUser.getId().toString())
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(content -> {
                    assert content.contains(CART_TITLE);
                    assert content.contains("Test Item 1");
                    assert content.contains("Test Item 2");
                    assert content.contains("2");
                    assert content.contains("1");
                    assert content.contains("300");
                });
    }

    @Test
    void getCartItems_ShouldCreateNewCartWhenNotFound() {
        when(userService.getOrCreate(eq(testUser.getId()), any(ServerWebExchange.class)))
                .thenReturn(Mono.just(testUser));

        when(cartService.findByUserId(testUser.getId()))
                .thenReturn(Mono.empty());

        when(cartService.create(testUser))
                .thenReturn(Mono.just(testCart));

        when(cartService.findAllCartItemsByCart(testCart))
                .thenReturn(Flux.empty());

        when(paymentService.checkUserBalance(anyLong(), anyDouble())).thenReturn(
                Mono.just(new PaymentInfo(true, true))
        );

        webTestClient.get()
                .uri("/cart/items")
                .cookie(USER_ID_COOKIE, testUser.getId().toString())
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(content -> {
                    assert content.contains(CART_TITLE);
                    assert !content.contains("Test Item 1");
                });
    }

    @Test
    void changeItemQuantityInCartFromCartPage_ShouldIncreaseQuantity() {
        when(itemService.getItemById(testItem1.getId()))
                .thenReturn(Mono.just(testItem1));

        when(userService.getOrCreate(eq(testUser.getId()), any(ServerWebExchange.class)))
                .thenReturn(Mono.just(testUser));

        when(cartService.findByUserId(testUser.getId()))
                .thenReturn(Mono.just(testCart));

        when(cartService.create(testUser))
                .thenReturn(Mono.just(testCart));

        when(cartService.create(testUser))
                .thenReturn(Mono.just(testCart));

        when(cartService.updateItemQuantityInCart(eq(ActionType.PLUS), eq(testCart), eq(testItem1), eq(true)))
                .thenReturn(Mono.empty());

        when(cartService.findAllCartItemsByCart(testCart))
                .thenReturn(Flux.just(testCartItem1, testCartItem2));

        when(itemService.getItemById(testItem1.getId()))
                .thenReturn(Mono.just(testItem1));

        when(itemService.getItemById(testItem2.getId()))
                .thenReturn(Mono.just(testItem2));

        when(paymentService.checkUserBalance(eq(testUser.getId()), anyDouble())).thenReturn(
                Mono.just(new PaymentInfo(true, true))
        );

        webTestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/cart/items")
                        .queryParam("id", testItem1.getId())
                        .queryParam("action", "PLUS")
                        .queryParam("search", "")
                        .queryParam("sort", "NO")
                        .queryParam("pageNumber", 0)
                        .queryParam("pageSize", 10)
                        .build())
                .cookie(USER_ID_COOKIE, testUser.getId().toString())
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(content -> {
                    assert content.contains(CART_TITLE);
                    assert content.contains("Test Item 1");
                    assert content.contains("Test Item 2");
                });
    }

    @Test
    void changeItemQuantityInCartFromCartPage_ShouldDecreaseQuantity() {
        when(itemService.getItemById(testItem1.getId()))
                .thenReturn(Mono.just(testItem1));

        when(userService.getOrCreate(eq(testUser.getId()), any(ServerWebExchange.class)))
                .thenReturn(Mono.just(testUser));

        when(cartService.findByUserId(testUser.getId()))
                .thenReturn(Mono.just(testCart));

        when(cartService.create(testUser))
                .thenReturn(Mono.just(testCart));

        when(cartService.updateItemQuantityInCart(eq(ActionType.MINUS), eq(testCart), eq(testItem1), eq(true)))
                .thenReturn(Mono.empty());

        when(cartService.findAllCartItemsByCart(testCart))
                .thenReturn(Flux.just(testCartItem1, testCartItem2));

        when(itemService.getItemById(testItem1.getId()))
                .thenReturn(Mono.just(testItem1));

        when(itemService.getItemById(testItem2.getId()))
                .thenReturn(Mono.just(testItem2));

        when(paymentService.checkUserBalance(anyLong(), anyDouble())).thenReturn(
                Mono.just(new PaymentInfo(true, true))
        );

        when(paymentService.checkUserBalance(anyLong(), anyDouble())).thenReturn(
                Mono.just(new PaymentInfo(true, true))
        );

        webTestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/cart/items")
                        .queryParam("id", testItem1.getId())
                        .queryParam("action", "MINUS")
                        .queryParam("search", "")
                        .queryParam("sort", "NO")
                        .queryParam("pageNumber", 0)
                        .queryParam("pageSize", 10)
                        .build())
                .cookie(USER_ID_COOKIE, testUser.getId().toString())
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(content -> {
                    assert content.contains(CART_TITLE);
                    assert content.contains("Test Item 1");
                    assert content.contains("Test Item 2");
                });
    }

    @Test
    void changeItemQuantityInCartFromCartPage_ShouldDeleteItem() {
        when(itemService.getItemById(testItem1.getId()))
                .thenReturn(Mono.just(testItem1));

        when(userService.getOrCreate(eq(testUser.getId()), any(ServerWebExchange.class)))
                .thenReturn(Mono.just(testUser));

        when(cartService.findByUserId(testUser.getId()))
                .thenReturn(Mono.just(testCart));

        when(cartService.create(testUser))
                .thenReturn(Mono.just(testCart));

        when(cartService.updateItemQuantityInCart(eq(ActionType.DELETE), eq(testCart), eq(testItem1), eq(true)))
                .thenReturn(Mono.empty());

        when(cartService.findAllCartItemsByCart(testCart))
                .thenReturn(Flux.just(testCartItem2));

        when(itemService.getItemById(testItem2.getId()))
                .thenReturn(Mono.just(testItem2));

        when(paymentService.checkUserBalance(anyLong(), anyDouble())).thenReturn(
                Mono.just(new PaymentInfo(true, true))
        );

        webTestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/cart/items")
                        .queryParam("id", testItem1.getId())
                        .queryParam("action", "DELETE")
                        .queryParam("search", "")
                        .queryParam("sort", "NO")
                        .queryParam("pageNumber", 0)
                        .queryParam("pageSize", 10)
                        .build())
                .cookie(USER_ID_COOKIE, testUser.getId().toString())
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(content -> {
                    assert content.contains(CART_TITLE);
                    assert !content.contains("Test Item 1");
                    assert content.contains("Test Item 2");
                });
    }

    @Test
    void changeItemQuantityInCartFromCartPage_ShouldCreateCartWhenNotFound() {
        when(itemService.getItemById(testItem1.getId()))
                .thenReturn(Mono.just(testItem1));

        when(userService.getOrCreate(eq(testUser.getId()), any(ServerWebExchange.class)))
                .thenReturn(Mono.just(testUser));

        when(cartService.findByUserId(testUser.getId()))
                .thenReturn(Mono.empty());

        when(cartService.create(testUser))
                .thenReturn(Mono.just(testCart));

        when(cartService.updateItemQuantityInCart(eq(ActionType.PLUS), eq(testCart), eq(testItem1), eq(true)))
                .thenReturn(Mono.empty());

        when(cartService.findAllCartItemsByCart(testCart))
                .thenReturn(Flux.just(testCartItem1));

        when(itemService.getItemById(testItem1.getId()))
                .thenReturn(Mono.just(testItem1));

        when(paymentService.checkUserBalance(anyLong(), anyDouble())).thenReturn(
                Mono.just(new PaymentInfo(true, true))
        );

        webTestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/cart/items")
                        .queryParam("id", testItem1.getId())
                        .queryParam("action", "PLUS")
                        .queryParam("search", "")
                        .queryParam("sort", "NO")
                        .queryParam("pageNumber", 0)
                        .queryParam("pageSize", 10)
                        .build())
                .cookie(USER_ID_COOKIE, testUser.getId().toString())
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(content -> {
                    assert content.contains(CART_TITLE);
                    assert content.contains("Test Item 1");
                });
    }

    @Test
    void changeItemQuantityInCartFromItemsPage_ShouldReturnRedirectWithAlphaSort() {
        when(itemService.getItemById(testItem1.getId()))
                .thenReturn(Mono.just(testItem1));

        when(userService.getOrCreate(eq(testUser.getId()), any(ServerWebExchange.class)))
                .thenReturn(Mono.just(testUser));

        when(cartService.findByUserId(testUser.getId()))
                .thenReturn(Mono.just(testCart));

        when(cartService.create(testUser))
                .thenReturn(Mono.just(testCart));

        when(cartService.updateItemQuantityInCart(eq(ActionType.PLUS), eq(testCart), eq(testItem1), eq(false)))
                .thenReturn(Mono.empty());

        when(cartService.findAllCartItemsByCart(testCart))
                .thenReturn(Flux.just(testCartItem1));

        webTestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/items")
                        .queryParam("id", testItem1.getId())
                        .queryParam("action", "PLUS")
                        .queryParam("search", "test")
                        .queryParam("sort", "ALPHA")
                        .queryParam("pageNumber", 0)
                        .queryParam("pageSize", 10)
                        .build())
                .cookie(USER_ID_COOKIE, testUser.getId().toString())
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/items?search=test&sort=ALPHA&pageNumber=1&pageSize=10");
    }

    @Test
    void changeItemQuantityInCartFromItemsPage_ShouldReturnRedirectWithPriceSort() {
        when(itemService.getItemById(testItem1.getId()))
                .thenReturn(Mono.just(testItem1));

        when(userService.getOrCreate(eq(testUser.getId()), any(ServerWebExchange.class)))
                .thenReturn(Mono.just(testUser));

        when(cartService.findByUserId(testUser.getId()))
                .thenReturn(Mono.just(testCart));

        when(cartService.create(testUser))
                .thenReturn(Mono.just(testCart));

        when(cartService.updateItemQuantityInCart(eq(ActionType.MINUS), eq(testCart), eq(testItem1), eq(false)))
                .thenReturn(Mono.empty());

        when(cartService.findAllCartItemsByCart(testCart))
                .thenReturn(Flux.just(testCartItem1));

        webTestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/items")
                        .queryParam("id", testItem1.getId())
                        .queryParam("action", "MINUS")
                        .queryParam("search", "test")
                        .queryParam("sort", "PRICE")
                        .queryParam("pageNumber", 1)
                        .queryParam("pageSize", 5)
                        .build())
                .cookie(USER_ID_COOKIE, testUser.getId().toString())
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueMatches("Location", ".*/items\\?search=test&sort=PRICE&pageNumber=1&pageSize=5.*");
    }

    @Test
    void changeItemQuantityInCartFromItemsPage_ShouldCreateCartWhenNotFound() {
        when(itemService.getItemById(testItem1.getId()))
                .thenReturn(Mono.just(testItem1));

        when(userService.getOrCreate(eq(testUser.getId()), any(ServerWebExchange.class)))
                .thenReturn(Mono.just(testUser));

        when(cartService.findByUserId(testUser.getId()))
                .thenReturn(Mono.empty());

        when(cartService.create(testUser))
                .thenReturn(Mono.just(testCart));

        when(cartService.updateItemQuantityInCart(eq(ActionType.DELETE), eq(testCart), eq(testItem1), eq(false)))
                .thenReturn(Mono.empty());

        when(cartService.findAllCartItemsByCart(testCart))
                .thenReturn(Flux.empty());

        webTestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/items")
                        .queryParam("id", testItem1.getId())
                        .queryParam("action", "DELETE")
                        .queryParam("search", "test")
                        .queryParam("sort", "NO")
                        .queryParam("pageNumber", 0)
                        .queryParam("pageSize", 10)
                        .build())
                .cookie(USER_ID_COOKIE, testUser.getId().toString())
                .exchange()
                .expectStatus().is3xxRedirection();
    }

    @Test
    void changeItemQuantityInCartFromItemPage_ShouldReturnItemView() {
        when(itemService.getItemById(testItem1.getId()))
                .thenReturn(Mono.just(testItem1));

        when(userService.getOrCreate(eq(testUser.getId()), any(ServerWebExchange.class)))
                .thenReturn(Mono.just(testUser));

        when(cartService.findByUserId(testUser.getId()))
                .thenReturn(Mono.just(testCart));

        when(cartService.create(testUser))
                .thenReturn(Mono.just(testCart));

        when(cartService.updateItemQuantityInCart(eq(ActionType.PLUS), eq(testCart), eq(testItem1), eq(false)))
                .thenReturn(Mono.empty());

        when(cartService.countCartItemsByCartIdAndItemId(testCart.getId(), testItem1.getId()))
                .thenReturn(Mono.just(2));

        webTestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/items/%d".formatted(testItem1.getId()))
                        .queryParam("action", "PLUS")
                        .queryParam("search", "")
                        .queryParam("sort", "NO")
                        .queryParam("pageNumber", 0)
                        .queryParam("pageSize", 10)
                        .build())
                .cookie(USER_ID_COOKIE, testUser.getId().toString())
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(content -> {
                    assert content.contains(ITEM_TITLE);
                    assert content.contains("Test Item 1");
                    assert content.contains("2");
                    assert content.contains("100");
                });
    }
}