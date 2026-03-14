package com.github.nesterukia.mymarket.service;

import com.github.nesterukia.mymarket.dao.CartItemRepository;
import com.github.nesterukia.mymarket.dao.OrderItemRepository;
import com.github.nesterukia.mymarket.dao.OrderRepository;
import com.github.nesterukia.mymarket.domain.Cart;
import com.github.nesterukia.mymarket.domain.CartItem;
import com.github.nesterukia.mymarket.domain.Order;
import com.github.nesterukia.mymarket.domain.OrderItem;
import com.github.nesterukia.mymarket.domain.exceptions.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @InjectMocks
    private OrderService orderService;

    private Cart testCart;
    private Order testOrder;
    private CartItem testCartItem1;
    private CartItem testCartItem2;
    private OrderItem testOrderItem1;
    private OrderItem testOrderItem2;

    @BeforeEach
    void setUp() {
        testCart = Cart.builder()
                .id(1L)
                .userId("test-user-id")
                .build();

        testOrder = Order.builder()
                .id(1L)
                .userId("test-user-id")
                .build();

        testCartItem1 = CartItem.builder()
                .id(1L)
                .cartId(testCart.getId())
                .itemId(1L)
                .quantity(2)
                .build();

        testCartItem2 = CartItem.builder()
                .id(2L)
                .cartId(testCart.getId())
                .itemId(2L)
                .quantity(1)
                .build();

        testOrderItem1 = OrderItem.builder()
                .id(1L)
                .orderId(testOrder.getId())
                .itemId(1L)
                .quantity(2)
                .build();

        testOrderItem2 = OrderItem.builder()
                .id(2L)
                .orderId(testOrder.getId())
                .itemId(2L)
                .quantity(1)
                .build();
    }

    @Test
    void getAllOrders_ShouldReturnOrdersWhenUserIdExists() {
        when(orderRepository.findAllByUserId("test-user-id"))
                .thenReturn(Flux.just(testOrder));

        StepVerifier.create(orderService.getAllOrders("test-user-id"))
                .expectNext(testOrder)
                .verifyComplete();
    }

    @Test
    void getAllOrders_ShouldReturnMultipleOrders() {
        Order testOrder2 = Order.builder()
                .id(2L)
                .userId("test-user-id")
                .build();

        when(orderRepository.findAllByUserId("test-user-id"))
                .thenReturn(Flux.just(testOrder, testOrder2));

        StepVerifier.create(orderService.getAllOrders("test-user-id"))
                .expectNext(testOrder)
                .expectNext(testOrder2)
                .verifyComplete();
    }

    @Test
    void getAllOrders_ShouldReturnEmptyFluxWhenUserIdIsNull() {
        StepVerifier.create(orderService.getAllOrders(null))
                .verifyComplete();
    }

    @Test
    void getAllOrders_ShouldReturnEmptyFluxWhenNoOrders() {
        when(orderRepository.findAllByUserId("test-user-id"))
                .thenReturn(Flux.empty());

        StepVerifier.create(orderService.getAllOrders("test-user-id"))
                .verifyComplete();
    }

    @Test
    void getOrderByUserIdAndId_ShouldReturnOrderWhenExists() {
        when(orderRepository.findByIdAndUserId(1L, "test-user-id"))
                .thenReturn(Mono.just(testOrder));

        StepVerifier.create(orderService.getOrderByUserIdAndId("test-user-id", 1L))
                .expectNext(testOrder)
                .verifyComplete();
    }

    @Test
    void getOrderByUserIdAndId_ShouldThrowExceptionWhenNotFound() {
        when(orderRepository.findByIdAndUserId(999L, "test-user-id"))
                .thenReturn(Mono.empty());

        StepVerifier.create(orderService.getOrderByUserIdAndId("test-user-id", 999L))
                .expectErrorMatches(throwable ->
                        throwable instanceof EntityNotFoundException &&
                                throwable.getMessage().equals("Order with id = '999' wasn't found.")
                )
                .verify();
    }

    @Test
    void createOrderFromCart_ShouldCreateOrderWithEmptyCart() {
        when(orderRepository.save(any(Order.class)))
                .thenReturn(Mono.just(testOrder));

        when(cartItemRepository.findAllByCartId(testCart.getId()))
                .thenReturn(Flux.empty());

        StepVerifier.create(orderService.createOrderFromCart(testCart))
                .expectNext(testOrder)
                .verifyComplete();

        verify(orderRepository).save(any(Order.class));
        verify(cartItemRepository).findAllByCartId(testCart.getId());
    }

    @Test
    void createOrderFromCart_ShouldHandleRepositorySaveError() {
        when(orderRepository.save(any(Order.class)))
                .thenReturn(Mono.error(new RuntimeException("Database error")));

        StepVerifier.create(orderService.createOrderFromCart(testCart))
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void createOrderFromCart_ShouldPreserveCorrectUserId() {
        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> {
                    Order orderToSave = invocation.getArgument(0);
                    return Mono.just(Order.builder()
                            .id(1L)
                            .userId(orderToSave.getUserId())
                            .build());
                });

        when(cartItemRepository.findAllByCartId(testCart.getId()))
                .thenReturn(Flux.just(testCartItem1));

        when(orderItemRepository.save(any(OrderItem.class)))
                .thenReturn(Mono.just(testOrderItem1));

        StepVerifier.create(orderService.createOrderFromCart(testCart))
                .expectNextMatches(order ->
                        order.getUserId().equals(testCart.getUserId())
                )
                .verifyComplete();
    }
}