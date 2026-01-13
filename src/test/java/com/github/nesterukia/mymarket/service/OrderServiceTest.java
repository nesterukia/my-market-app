package com.github.nesterukia.mymarket.service;

import com.github.nesterukia.mymarket.dao.OrderItemRepository;
import com.github.nesterukia.mymarket.dao.OrderRepository;
import com.github.nesterukia.mymarket.domain.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderItemRepository orderItemRepository;
    @Mock
    private Cart mockCart;
    @Mock
    private CartItem mockCartItem1;
    @Mock
    private CartItem mockCartItem2;
    @Mock
    private Order mockSavedOrder;

    @InjectMocks
    private OrderService orderService;

    @Test
    void getAllOrders_ReturnsAllOrdersFromRepository() {
        List<Order> expectedOrders = List.of(new Order(), new Order());
        when(orderRepository.findAll()).thenReturn(expectedOrders);

        List<Order> result = orderService.getAllOrders();

        assertThat(result).isEqualTo(expectedOrders);
        verify(orderRepository).findAll();
    }

    @Test
    void getAllOrders_EmptyList_ReturnsEmptyList() {
        when(orderRepository.findAll()).thenReturn(List.of());

        List<Order> result = orderService.getAllOrders();

        assertThat(result).isEmpty();
        verify(orderRepository).findAll();
    }

    @Test
    void getOrderById_ExistingOrder_ReturnsOrder() {
        Long orderId = 1L;
        Order expectedOrder = new Order();
        expectedOrder.setId(orderId);
        when(orderRepository.findById(orderId)).thenReturn(java.util.Optional.of(expectedOrder));

        Order result = orderService.getOrderById(orderId);

        assertThat(result).isSameAs(expectedOrder);
        verify(orderRepository).findById(orderId);
    }

    @Test
    void getOrderById_NonExistingOrder_ThrowsException() {
        Long orderId = 999L;
        when(orderRepository.findById(orderId)).thenReturn(java.util.Optional.empty());

        assertThatThrownBy(() -> orderService.getOrderById(orderId))
                .isInstanceOf(NoSuchElementException.class);

        verify(orderRepository).findById(orderId);
    }

    @Test
    void createOrderFromCart_EmptyCart_CreatesOrderOnly() {
        when(mockCart.getCartItems()).thenReturn(List.of());
        when(orderRepository.save(any(Order.class))).thenReturn(mockSavedOrder);

        Order result = orderService.createOrderFromCart(mockCart);

        assertThat(result).isEqualTo(mockSavedOrder);
        verify(orderRepository).save(any(Order.class));
        verify(orderItemRepository, never()).save(any());
        verify(mockCart).getCartItems();
    }

    @Test
    void createOrderFromCart_OneCartItem_CreatesOrderAndOrderItem() {
        Item mockItem = new Item();
        when(mockCart.getCartItems()).thenReturn(List.of(mockCartItem1));
        when(mockCartItem1.getItem()).thenReturn(mockItem);
        when(mockCartItem1.getQuantity()).thenReturn(3);
        when(orderRepository.save(any(Order.class))).thenReturn(mockSavedOrder);

        Order result = orderService.createOrderFromCart(mockCart);

        verify(orderRepository).save(argThat(order -> order instanceof Order));
        verify(orderItemRepository).save(argThat(orderItem ->
                orderItem.getOrder() == mockSavedOrder &&
                        orderItem.getItem() == mockItem &&
                        orderItem.getQuantity() == 3
        ));
    }

    @Test
    void createOrderFromCart_MultipleCartItems_CreatesMultipleOrderItems() {
        Item item1 = new Item(), item2 = new Item();
        when(mockCart.getCartItems()).thenReturn(List.of(mockCartItem1, mockCartItem2));
        when(mockCartItem1.getItem()).thenReturn(item1);
        when(mockCartItem1.getQuantity()).thenReturn(1);
        when(mockCartItem2.getItem()).thenReturn(item2);
        when(mockCartItem2.getQuantity()).thenReturn(5);
        when(orderRepository.save(any(Order.class))).thenReturn(mockSavedOrder);

        Order result = orderService.createOrderFromCart(mockCart);

        verify(orderRepository).save(any(Order.class));
        verify(orderItemRepository, times(2)).save(any(OrderItem.class));
    }

    @Test
    void getAllOrders_MultipleCalls_VerifiesRepositoryEachTime() {
        when(orderRepository.findAll()).thenReturn(List.of(new Order()));

        orderService.getAllOrders();
        orderService.getAllOrders();

        verify(orderRepository, times(2)).findAll();
    }

    @Test
    void createOrderFromCart_CompleteFlow_VerifiesAllInteractions() {
        when(mockCart.getCartItems()).thenReturn(List.of());
        when(orderRepository.save(any(Order.class))).thenReturn(mockSavedOrder);

        Order result = orderService.createOrderFromCart(mockCart);

        verify(mockCart).getCartItems();
        verify(orderRepository).save(any(Order.class));
        verifyNoMoreInteractions(orderItemRepository);
        assertThat(result).isNotNull();
    }
}
