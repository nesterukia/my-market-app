package com.github.nesterukia.mymarket.http;

import com.github.nesterukia.mymarket.domain.Cart;
import com.github.nesterukia.mymarket.domain.Order;
import com.github.nesterukia.mymarket.service.CartService;
import com.github.nesterukia.mymarket.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
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

    @Test
    public void getAllOrders_CallsServiceAndReturnsView() throws Exception {
        List<Order> mockOrders = List.of(new Order());
        when(orderService.getAllOrders()).thenReturn(mockOrders);

        mockMvc.perform(get("/orders"))
                .andExpect(status().isOk())
                .andExpect(view().name("orders"));

        verify(orderService).getAllOrders();
    }

    @Test
    public void getOrder_CallsServiceAndReturnsView() throws Exception {
        Long orderId = 1L;
        Order mockOrder = new Order();
        when(orderService.getOrderById(orderId)).thenReturn(mockOrder);

        mockMvc.perform(get("/orders/{id}", orderId))
                .andExpect(status().isOk())
                .andExpect(view().name("order"));

        verify(orderService).getOrderById(orderId);
    }

    @Test
    public void getOrderWithNewOrderParam_CallsServiceAndReturnsView() throws Exception {
        Long orderId = 1L;
        Order mockOrder = new Order();
        when(orderService.getOrderById(orderId)).thenReturn(mockOrder);

        mockMvc.perform(get("/orders/{id}", orderId).param("newOrder", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("order"));

        verify(orderService).getOrderById(orderId);
    }

    @Test
    public void buy_CallsServicesAndRedirects() throws Exception {
        Cart mockCart = new Cart();
        Order mockOrder = new Order();
        mockOrder.setId(1L);
        when(cartService.getOrCreate()).thenReturn(mockCart);
        when(orderService.createOrderFromCart(mockCart)).thenReturn(mockOrder);

        mockMvc.perform(post("/buy"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders/1?newOrder=true"));

        verify(cartService).getOrCreate();
        verify(orderService).createOrderFromCart(mockCart);
        verify(cartService).clearCartAndDelete(mockCart);
    }
}

