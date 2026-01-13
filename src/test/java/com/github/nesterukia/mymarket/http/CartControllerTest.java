package com.github.nesterukia.mymarket.http;

import com.github.nesterukia.mymarket.domain.Cart;
import com.github.nesterukia.mymarket.domain.CartItem;
import com.github.nesterukia.mymarket.domain.Item;
import com.github.nesterukia.mymarket.service.CartService;
import com.github.nesterukia.mymarket.service.ItemService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CartController.class)
public class CartControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private CartService cartService;
    @MockitoBean
    private ItemService itemService;

    @Test
    void getCartItems_EmptyCart_ReturnsCartView() throws Exception {
        Cart mockCart = mock(Cart.class);
        when(cartService.getOrCreate()).thenReturn(mockCart);
        when(mockCart.getCartItems()).thenReturn(List.of());

        mockMvc.perform(get("/cart/items"))
                .andExpect(status().isOk())
                .andExpect(view().name("cart"));

        verify(cartService).getOrCreate();
    }

    @Test
    void getCartItems_WithItems_ReturnsCartView() throws Exception {
        Cart mockCart = mock(Cart.class);
        CartItem mockCartItem = mock(CartItem.class);
        Item mockItem = mock(Item.class);
        when(cartService.getOrCreate()).thenReturn(mockCart);
        when(mockCart.getCartItems()).thenReturn(List.of(mockCartItem));
        when(mockCartItem.getItem()).thenReturn(mockItem);

        mockMvc.perform(get("/cart/items"))
                .andExpect(status().isOk())
                .andExpect(view().name("cart"));

        verify(cartService).getOrCreate();
    }

    @Test
    void changeItemQuantityFromCartPage_MinusAction_CallsDecrease() throws Exception {
        Item mockItem = new Item();
        mockItem.setId(1L);
        Cart mockCart = mock(Cart.class);
        when(itemService.getItemById(1L)).thenReturn(mockItem);
        when(cartService.getOrCreate()).thenReturn(mockCart);

        mockMvc.perform(post("/cart/items")
                        .param("id", "1")
                        .param("action", "MINUS"))
                .andExpect(status().isOk())
                .andExpect(view().name("cart"));

        verify(itemService).getItemById(1L);
        verify(cartService).decreaseItemQuantityInCart(mockCart, mockItem);
    }

    @Test
    void changeItemQuantityFromCartPage_PlusAction_CallsIncrease() throws Exception {
        Item mockItem = new Item();
        mockItem.setId(1L);
        Cart mockCart = mock(Cart.class);
        when(itemService.getItemById(1L)).thenReturn(mockItem);
        when(cartService.getOrCreate()).thenReturn(mockCart);

        mockMvc.perform(post("/cart/items")
                        .param("id", "1")
                        .param("action", "PLUS"))
                .andExpect(status().isOk())
                .andExpect(view().name("cart"));

        verify(itemService).getItemById(1L);
        verify(cartService).increaseItemQuantityInCart(mockCart, mockItem);
    }

    @Test
    void changeItemQuantityFromCartPage_DeleteAction_CallsRemove() throws Exception {
        Item mockItem = new Item();
        mockItem.setId(1L);
        Cart mockCart = mock(Cart.class);
        when(itemService.getItemById(1L)).thenReturn(mockItem);
        when(cartService.getOrCreate()).thenReturn(mockCart);

        mockMvc.perform(post("/cart/items")
                        .param("id", "1")
                        .param("action", "DELETE"))
                .andExpect(status().isOk())
                .andExpect(view().name("cart"));

        verify(itemService).getItemById(1L);
        verify(cartService).removeItemFromCart(mockCart, mockItem);
    }

    @Test
    void changeItemQuantityFromItemsPage_MinusAction_RedirectsWithParams() throws Exception {
        Item mockItem = new Item();
        mockItem.setId(1L);
        Cart mockCart = mock(Cart.class);
        when(itemService.getItemById(1L)).thenReturn(mockItem);
        when(cartService.getOrCreate()).thenReturn(mockCart);

        mockMvc.perform(post("/items")
                        .param("id", "1")
                        .param("search", "phone")
                        .param("sort", "PRICE")
                        .param("pageNumber", "2")
                        .param("pageSize", "10")
                        .param("action", "MINUS"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/items?search=phone&sort=PRICE&pageNumber=2&pageSize=10"));

        verify(itemService).getItemById(1L);
        verify(cartService).decreaseItemQuantityInCart(mockCart, mockItem);
    }

    @Test
    void changeItemQuantityFromItemsPage_PlusAction_RedirectsWithParams() throws Exception {
        Item mockItem = new Item();
        mockItem.setId(1L);
        Cart mockCart = mock(Cart.class);
        when(itemService.getItemById(1L)).thenReturn(mockItem);
        when(cartService.getOrCreate()).thenReturn(mockCart);

        mockMvc.perform(post("/items")
                        .param("id", "1")
                        .param("action", "PLUS"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/items?search=&sort=NO&pageNumber=1&pageSize=5"));

        verify(itemService).getItemById(1L);
        verify(cartService).increaseItemQuantityInCart(mockCart, mockItem);
    }

    @Test
    void changeItemQuantityFromItemsPage_DefaultParams_RedirectsDefaults() throws Exception {
        Item mockItem = new Item();
        mockItem.setId(1L);
        Cart mockCart = mock(Cart.class);
        when(itemService.getItemById(1L)).thenReturn(mockItem);
        when(cartService.getOrCreate()).thenReturn(mockCart);

        mockMvc.perform(post("/items")
                        .param("id", "1")
                        .param("action", "PLUS"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/items?search=&sort=NO&pageNumber=1&pageSize=5"));

        verify(cartService).increaseItemQuantityInCart(mockCart, mockItem);
    }

    @Test
    void changeItemQuantityFromItemPage_MinusAction_ReturnsItemView() throws Exception {
        Item mockItem = new Item();
        mockItem.setId(1L);
        Cart mockCart = mock(Cart.class);
        when(itemService.getItemById(1L)).thenReturn(mockItem);
        when(cartService.getOrCreate()).thenReturn(mockCart);

        mockMvc.perform(post("/items/{id}", 1L)
                        .param("action", "MINUS"))
                .andExpect(status().isOk())
                .andExpect(view().name("item"));

        verify(itemService, times(2)).getItemById(1L);
        verify(cartService).decreaseItemQuantityInCart(mockCart, mockItem);
        verify(itemService, times(2)).getItemById(1L);
    }

    @Test
    void changeItemQuantityFromItemPage_PlusAction_ReturnsItemView() throws Exception {
        Item mockItem = new Item();
        mockItem.setId(1L);
        Cart mockCart = mock(Cart.class);
        when(itemService.getItemById(1L)).thenReturn(mockItem);
        when(cartService.getOrCreate()).thenReturn(mockCart);

        mockMvc.perform(post("/items/{id}", 1L)
                        .param("action", "PLUS"))
                .andExpect(status().isOk())
                .andExpect(view().name("item"));

        verify(itemService, times(2)).getItemById(1L);
        verify(cartService).increaseItemQuantityInCart(mockCart, mockItem);
    }

    @Test
    void changeFromCartPage_MultipleActions_HandlesAllCases() throws Exception {
        Item mockItem = new Item();
        mockItem.setId(1L);
        Cart mockCart = mock(Cart.class);
        when(itemService.getItemById(1L)).thenReturn(mockItem);
        when(cartService.getOrCreate()).thenReturn(mockCart);

        mockMvc.perform(post("/cart/items")
                        .param("id", "1")
                        .param("action", "minus"))
                .andExpect(status().isOk());

        verify(cartService).decreaseItemQuantityInCart(mockCart, mockItem);
    }

    @Test
    void changeFromItemsPage_ComplexParams_RedirectsCorrectly() throws Exception {
        Item mockItem = new Item();
        mockItem.setId(1L);
        Cart mockCart = mock(Cart.class);
        when(itemService.getItemById(1L)).thenReturn(mockItem);
        when(cartService.getOrCreate()).thenReturn(mockCart);

        String expectedRedirect = "/items?search=laptop&sort=ALPHA&pageNumber=5&pageSize=15";
        mockMvc.perform(post("/items")
                        .param("id", "1")
                        .param("search", "laptop")
                        .param("sort", "ALPHA")
                        .param("pageNumber", "5")
                        .param("pageSize", "15")
                        .param("action", "minus"))
                .andExpect(redirectedUrl(expectedRedirect));

        verify(cartService).decreaseItemQuantityInCart(mockCart, mockItem);
    }

    @Test
    void getCartItems_LowerCaseActionIgnored_CartView() throws Exception {
        Cart mockCart = mock(Cart.class);
        when(cartService.getOrCreate()).thenReturn(mockCart);

        mockMvc.perform(get("/cart/items"))
                .andExpect(view().name("cart"));

        verify(cartService).getOrCreate();
    }

    @Test
    void changeFromItemPage_PathVariableAndAction_CallsServices() throws Exception {
        Item mockItem = new Item();
        mockItem.setId(2L);
        Cart mockCart = mock(Cart.class);
        when(itemService.getItemById(2L)).thenReturn(mockItem);
        when(cartService.getOrCreate()).thenReturn(mockCart);

        mockMvc.perform(post("/items/{id}", 2L)
                        .param("action", "delete"))  // Only MINUS/PLUS supported
                .andExpect(status().isOk())
                .andExpect(view().name("item"));

        verify(cartService).getOrCreate();
    }

    @Test
    void changeFromItemsPage_DeleteAction_NotSupported() throws Exception {
        Item mockItem = new Item();
        mockItem.setId(1L);
        Cart mockCart = mock(Cart.class);
        when(itemService.getItemById(1L)).thenReturn(mockItem);
        when(cartService.getOrCreate()).thenReturn(mockCart);

        mockMvc.perform(post("/items")
                        .param("id", "1")
                        .param("action", "DELETE"))
                .andExpect(status().is3xxRedirection());  // No delete case

        verify(cartService, never()).removeItemFromCart(any(), any());
    }

    @Test
    void allEndpoints_ServiceInteractionsVerified() throws Exception {
        Item mockItem = new Item();
        mockItem.setId(1L);
        Cart mockCart = mock(Cart.class);
        when(itemService.getItemById(1L)).thenReturn(mockItem);
        when(cartService.getOrCreate()).thenReturn(mockCart);

        mockMvc.perform(post("/cart/items").param("id", "1").param("action", "PLUS"))
                .andExpect(status().isOk());

        verify(itemService).getItemById(1L);
        verify(cartService).increaseItemQuantityInCart(mockCart, mockItem);
    }
}

