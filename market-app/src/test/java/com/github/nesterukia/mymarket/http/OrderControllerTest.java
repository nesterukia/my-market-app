package com.github.nesterukia.mymarket.http;

import com.github.nesterukia.mymarket.domain.Cart;
import com.github.nesterukia.mymarket.domain.Item;
import com.github.nesterukia.mymarket.domain.Order;
import com.github.nesterukia.mymarket.http.dto.ItemDto;
import com.github.nesterukia.mymarket.http.dto.OrderDto;
import com.github.nesterukia.mymarket.http.dto.payment.TransactionInfo;
import com.github.nesterukia.mymarket.service.CartService;
import com.github.nesterukia.mymarket.service.ItemService;
import com.github.nesterukia.mymarket.service.OrderService;
import com.github.nesterukia.mymarket.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

@TestPropertySource(properties = {
        "KC_OUTER_REALM_URL=http://localhost:${KC_OUTER_PORT}/realms/nesterukia-realm",
        "KC_INNER_REALM_URI=http://keycloak-sso:8080/realms/nesterukia-realm"
})
@WebFluxTest(OrderController.class)
class OrderControllerTest {

    private static final String ORDERS_TITLE = "<title>Список заказов</title>";
    private static final String ORDER_TITLE = "<title>Заказ</title>";

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private CartService cartService;

    @MockitoBean
    private ItemService itemService;

    @MockitoBean
    private PaymentService paymentService;

    private String testUserId;
    private Cart testCart;
    private Order testOrder1;
    private Order testOrder2;
    private Item testItem1;
    private Item testItem2;
    private ItemDto testItemDto1;
    private ItemDto testItemDto2;
    private OrderDto testOrderDto1;
    private OrderDto testOrderDto2;

    @BeforeEach
    void setUp() {
        testUserId = "test-user-id";

        testCart = Cart.builder()
                .id(1L)
                .userId(testUserId)
                .build();

        testOrder1 = Order.builder()
                .id(1L)
                .userId(testUserId)
                .build();

        testOrder2 = Order.builder()
                .id(2L)
                .userId(testUserId)
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

        testItemDto1 = ItemDto.fromItem(testItem1, 2);
        testItemDto2 = ItemDto.fromItem(testItem2, 1);

        testOrderDto1 = OrderDto.fromOrder(testOrder1, List.of(testItemDto1, testItemDto2));
        testOrderDto2 = OrderDto.fromOrder(testOrder2, List.of(testItemDto1));
    }

    @Test
    void getAllOrders_ShouldReturnOrdersViewWithOrders() {
        when(orderService.getAllOrders(testUserId))
                .thenReturn(Flux.just(testOrder1, testOrder2));

        when(itemService.findAllByOrderId(testOrder1.getId()))
                .thenReturn(Flux.just(testItemDto1, testItemDto2));

        when(itemService.findAllByOrderId(testOrder2.getId()))
                .thenReturn(Flux.just(testItemDto1));

        webTestClient.get()
                .uri("/orders")
                .cookie("user_id", testUserId.toString())
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(content -> {
                    assert content.contains(ORDERS_TITLE);
                });
    }

    @Test
    void getAllOrders_ShouldReturnEmptyOrdersViewWhenNoOrders() {
        when(orderService.getAllOrders(eq(testUserId)))
                .thenReturn(Flux.empty());

        webTestClient.get()
                .uri("/orders")
                .cookie("user_id", testUserId.toString())
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(content -> {
                    assert content.contains(ORDERS_TITLE);
                    assert !content.contains("Test Item 1");
                });
    }

    @Test
    void getAllOrders_ShouldReturnEmptyOrdersViewWhenUserIdIsNull() {
        when(orderService.getAllOrders(isNull()))
                .thenReturn(Flux.empty());

        webTestClient.get()
                .uri("/orders")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(content -> {
                    assert content.contains(ORDERS_TITLE);
                    assert !content.contains("Test Item 1");
                });
    }

    @Test
    void getOrder_ShouldReturnOrderViewWithExistingOrder() {

        when(orderService.getOrderByUserIdAndId(testUserId, testOrder1.getId()))
                .thenReturn(Mono.just(testOrder1));

        when(itemService.findAllByOrderId(testOrder1.getId()))
                .thenReturn(Flux.just(testItemDto1, testItemDto2));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/orders/%d".formatted(testOrder1.getId()))
                        .queryParam("newOrder", "false")
                        .build())
                .cookie("user_id", testUserId.toString())
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(content -> {
                    assert content.contains(ORDER_TITLE);
                    assert content.contains("Test Item 1");
                    assert content.contains("Test Item 2");
                    assert content.contains("2");
                    assert content.contains("1");
                    assert content.contains("400");
                });
    }

    @Test
    void getOrder_ShouldReturnOrderViewWithNewOrderFlag() {

        when(orderService.getOrderByUserIdAndId(testUserId, testOrder1.getId()))
                .thenReturn(Mono.just(testOrder1));

        when(itemService.findAllByOrderId(testOrder1.getId()))
                .thenReturn(Flux.just(testItemDto1));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/orders/%d".formatted(testOrder1.getId()))
                        .queryParam("newOrder", "true")
                        .build())
                .cookie("user_id", testUserId.toString())
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(content -> {
                    assert content.contains(ORDER_TITLE);
                });
    }

    @Test
    void buy_ShouldCreateOrderAndRedirect() {
        Order newOrder = Order.builder()
                .id(3L)
                .userId(testUserId)
                .build();

        when(cartService.findByUserId(testUserId))
                .thenReturn(Mono.just(testCart));

        when(cartService.calculateTotalSum(eq(testCart)))
                .thenReturn(Mono.just(100.0));

        when(paymentService.commitPayment(eq(testUserId), eq(100.0))).thenReturn(
                Mono.just(new TransactionInfo(UUID.randomUUID().toString(), "Success"))
        );

        when(orderService.createOrderFromCart(testCart))
                .thenReturn(Mono.just(newOrder));

        when(cartService.clearCartAndDelete(testCart))
                .thenReturn(Mono.empty());

        webTestClient.post()
                .uri("/buy")
                .cookie("user_id", testUserId.toString())
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueMatches("Location", ".*/orders/3\\?newOrder=true.*");
    }
}