package com.github.nesterukia.mymarket.service;

import com.github.nesterukia.mymarket.dao.CartItemRepository;
import com.github.nesterukia.mymarket.dao.CartRepository;
import com.github.nesterukia.mymarket.domain.Cart;
import com.github.nesterukia.mymarket.domain.CartItem;
import com.github.nesterukia.mymarket.domain.Item;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {
    @Mock
    private CartRepository cartRepository;
    @Mock
    private CartItemRepository cartItemRepository;
    @Spy
    private Cart mockCart;
    @Spy
    private Item mockItem;
    @Spy
    private CartItem mockCartItem;

    @InjectMocks
    private CartService cartService;

    @Test
    void getOrCreate_NoCarts_CreatesAndSavesNewCart() {
        when(cartRepository.findAll()).thenReturn(List.of());
        Cart newCart = new Cart();
        when(cartRepository.save(any(Cart.class))).thenReturn(newCart);

        Cart result = cartService.getOrCreate();

        assertThat(result).isEqualTo(newCart);
        verify(cartRepository).findAll();
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    void getOrCreate_ExistingCarts_ReturnsFirstCart() {
        Cart existingCart = new Cart();
        when(cartRepository.findAll()).thenReturn(List.of(existingCart));

        Cart result = cartService.getOrCreate();

        assertThat(result).isEqualTo(existingCart);
        verify(cartRepository).findAll();
        verify(cartRepository, never()).save(any());
    }

    @Test
    void increaseItemQuantityInCart_NoExistingItem_CreatesNewCartItem() {
        Long cartId = 1L, itemId = 2L;
        when(mockCart.getId()).thenReturn(cartId);
        when(mockItem.getId()).thenReturn(itemId);
        when(cartItemRepository.findByCartIdAndItemId(cartId, itemId)).thenReturn(Optional.empty());
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(mockCartItem);

        cartService.increaseItemQuantityInCart(mockCart, mockItem);

        verify(cartItemRepository).findByCartIdAndItemId(cartId, itemId);
        verify(cartItemRepository, times(2)).save(any(CartItem.class));  // create + increase
        verify(mockCartItem).increaseQuantity();
    }

    @Test
    void increaseItemQuantityInCart_ExistingItem_IncreasesQuantity() {
        Long cartId = 1L, itemId = 2L;
        when(mockCart.getId()).thenReturn(cartId);
        when(mockItem.getId()).thenReturn(itemId);
        when(cartItemRepository.findByCartIdAndItemId(cartId, itemId)).thenReturn(Optional.of(mockCartItem));

        cartService.increaseItemQuantityInCart(mockCart, mockItem);

        verify(mockCartItem).increaseQuantity();
        verify(cartItemRepository).save(mockCartItem);
    }

    @Test
    void decreaseItemQuantityInCart_ExistingItemWithQuantityGt1_SavesUpdated() {
        Long cartId = 1L, itemId = 2L;
        when(mockCart.getId()).thenReturn(cartId);
        when(mockItem.getId()).thenReturn(itemId);
        when(mockCartItem.getQuantity()).thenReturn(2).thenReturn(1);  // before/after
        when(cartItemRepository.findByCartIdAndItemId(cartId, itemId)).thenReturn(Optional.of(mockCartItem));

        cartService.decreaseItemQuantityInCart(mockCart, mockItem);

        verify(mockCartItem).decreaseQuantity();
        verify(cartItemRepository).save(mockCartItem);
    }

    @Test
    void decreaseItemQuantityInCart_QuantityBecomesZero_DeletesItem() {
        Long cartId = 1L, itemId = 2L;
        when(mockCart.getId()).thenReturn(cartId);
        when(mockItem.getId()).thenReturn(itemId);
        mockCartItem.setQuantity(1);
        when(cartItemRepository.findByCartIdAndItemId(cartId, itemId)).thenReturn(Optional.of(mockCartItem));

        assertEquals(1, mockCartItem.getQuantity());
        cartService.decreaseItemQuantityInCart(mockCart, mockItem);

        assertEquals(0, mockCartItem.getQuantity());
        verify(mockCartItem).decreaseQuantity();
        verify(cartItemRepository).delete(mockCartItem);
        verify(cartItemRepository, never()).save(any());
    }

    @Test
    void decreaseItemQuantityInCart_NoExistingItem_ThrowsException() {
        Long cartId = 1L, itemId = 2L;
        when(mockCart.getId()).thenReturn(cartId);
        when(mockItem.getId()).thenReturn(itemId);
        when(cartItemRepository.findByCartIdAndItemId(cartId, itemId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.decreaseItemQuantityInCart(mockCart, mockItem))
                .isInstanceOf(RuntimeException.class);  // or specific exception

        verify(cartItemRepository, never()).save(any());
        verify(cartItemRepository, never()).delete(any());
    }

    @Test
    void removeItemFromCart_ExistingItem_DeletesIt() {
        Long cartId = 1L, itemId = 2L;
        when(mockCart.getId()).thenReturn(cartId);
        when(mockItem.getId()).thenReturn(itemId);
        when(cartItemRepository.findByCartIdAndItemId(cartId, itemId)).thenReturn(Optional.of(mockCartItem));

        cartService.removeItemFromCart(mockCart, mockItem);

        verify(cartItemRepository).findByCartIdAndItemId(cartId, itemId);
        verify(cartItemRepository).delete(mockCartItem);
    }

    @Test
    void removeItemFromCart_NoExistingItem_ThrowsException() {
        Long cartId = 1L, itemId = 2L;
        when(mockCart.getId()).thenReturn(cartId);
        when(mockItem.getId()).thenReturn(itemId);
        when(cartItemRepository.findByCartIdAndItemId(cartId, itemId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.removeItemFromCart(mockCart, mockItem))
                .isInstanceOf(RuntimeException.class);

        verify(cartItemRepository, never()).delete(any());
    }

    @Test
    void clearCartAndDelete_ClearsItemsAndDeletesCart() {
        cartService.clearCartAndDelete(mockCart);

        verify(cartItemRepository).deleteAll();
        verify(cartRepository).delete(mockCart);
    }
}

