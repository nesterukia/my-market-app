package com.github.nesterukia.mymarket.http;

import com.github.nesterukia.mymarket.domain.Cart;
import com.github.nesterukia.mymarket.domain.Order;
import com.github.nesterukia.mymarket.domain.User;
import com.github.nesterukia.mymarket.service.CartService;
import com.github.nesterukia.mymarket.service.OrderService;
import com.github.nesterukia.mymarket.service.UserService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static com.github.nesterukia.mymarket.utils.UserUtils.USER_ID_COOKIE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.spy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
public class OrderControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private OrderService orderService;
    @MockitoBean
    private CartService cartService;
    @MockitoBean
    private UserService userService;

    @Test
    public void getAllOrders_CallsServiceAndReturnsView() throws Exception {
        Long userId = 1234L;
        List<Order> mockOrders = List.of(new Order());
        when(orderService.getAllOrders(userId)).thenReturn(mockOrders);

        mockMvc.perform(get("/orders").cookie(
                new Cookie(USER_ID_COOKIE, userId.toString())
                ))
                .andExpect(status().isOk())
                .andExpect(view().name("orders"));

        verify(orderService).getAllOrders(any());
    }

    @Test
    public void getAllOrders_NullUserIdCookie_isOK() throws Exception {
        List<Order> mockOrders = List.of(new Order());
        when(orderService.getAllOrders(any())).thenReturn(mockOrders);

        mockMvc.perform(get("/orders"))
                .andExpect(status().isOk())
                .andExpect(view().name("orders"));

        verify(orderService, never()).getAllOrders(any());
    }

    @Test
    public void getOrder_CallsServiceAndReturnsView() throws Exception {
        Long orderId = 1L;
        Long userId = 12345L;
        Order mockOrder = spy(Order.class);
        User mockUser = spy(User.builder().id(userId).build());
        when(mockOrder.getUser()).thenReturn(mockUser);
        when(orderService.getOrderById(orderId)).thenReturn(mockOrder);

        mockMvc.perform(get("/orders/{id}", orderId)
                        .cookie(new Cookie(USER_ID_COOKIE, userId.toString()))
                )
                .andExpect(status().isOk())
                .andExpect(view().name("order"));

        verify(orderService).getOrderById(orderId);
    }

    @Test
    public void getOrderWithNewOrderParam_CallsServiceAndReturnsView() throws Exception {
        Long orderId = 1L;
        Long userId = 12345L;
        Order mockOrder = spy(Order.class);
        User mockUser = spy(User.builder().id(userId).build());
        when(mockOrder.getUser()).thenReturn(mockUser);
        when(orderService.getOrderById(orderId)).thenReturn(mockOrder);

        mockMvc.perform(get("/orders/{id}", orderId)
                        .param("newOrder", "true")
                        .cookie(new Cookie(USER_ID_COOKIE, userId.toString()))
                )
                .andExpect(status().isOk())
                .andExpect(view().name("order"));

        verify(orderService).getOrderById(orderId);
    }

    @Test
    public void buy_CallsServicesAndRedirects() throws Exception {
        Long userId = 12345L;
        User mockUser = spy(User.builder().id(userId).build());
        when(userService.getOrCreate(any(), any())).thenReturn(mockUser);

        Cart mockCart = new Cart();
        when(cartService.create(any())).thenReturn(mockCart);

        Order mockOrder = new Order();
        mockOrder.setId(1L);
        when(orderService.createOrderFromCart(mockCart)).thenReturn(mockOrder);

        mockMvc.perform(post("/buy"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders/1?newOrder=true"));

        verify(cartService).create(any());
        verify(orderService).createOrderFromCart(mockCart);
        verify(cartService).clearCartAndDelete(mockCart);
    }
}

