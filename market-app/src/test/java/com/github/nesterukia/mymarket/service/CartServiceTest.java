package com.github.nesterukia.mymarket.service;

import com.github.nesterukia.mymarket.dao.CartItemRepository;
import com.github.nesterukia.mymarket.dao.CartRepository;
import com.github.nesterukia.mymarket.domain.ActionType;
import com.github.nesterukia.mymarket.domain.Cart;
import com.github.nesterukia.mymarket.domain.CartItem;
import com.github.nesterukia.mymarket.domain.Item;
import com.github.nesterukia.mymarket.domain.User;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @InjectMocks
    private CartService cartService;

    private User testUser;
    private Cart testCart;
    private Item testItem;
    private CartItem testCartItem;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .build();

        testCart = Cart.builder()
                .id(1L)
                .userId(testUser.getId())
                .build();

        testItem = Item.builder()
                .id(1L)
                .title("Test Item")
                .description("Description")
                .imgPath("/img.jpg")
                .price(100.0)
                .build();

        testCartItem = CartItem.builder()
                .id(1L)
                .cartId(testCart.getId())
                .itemId(testItem.getId())
                .quantity(2)
                .build();
    }

    @Test
    void findByUserId_ShouldReturnCartWhenExists() {
        when(cartRepository.findByUserId(testUser.getId()))
                .thenReturn(Mono.just(testCart));

        StepVerifier.create(cartService.findByUserId(testUser.getId()))
                .expectNext(testCart)
                .verifyComplete();
    }

    @Test
    void findByUserId_ShouldReturnEmptyWhenCartNotFound() {
        when(cartRepository.findByUserId(testUser.getId()))
                .thenReturn(Mono.empty());

        StepVerifier.create(cartService.findByUserId(testUser.getId()))
                .verifyComplete();
    }

    @Test
    void create_ShouldCreateNewCart() {
        when(cartRepository.save(any(Cart.class)))
                .thenReturn(Mono.just(testCart));

        StepVerifier.create(cartService.create(testUser))
                .expectNext(testCart)
                .verifyComplete();

        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    void updateItemQuantityInCart_WithMinusAction_ShouldDecreaseQuantity() {
        when(cartItemRepository.findByCartIdAndItemId(testCart.getId(), testItem.getId()))
                .thenReturn(Mono.just(testCartItem));

        when(cartItemRepository.save(any(CartItem.class)))
                .thenReturn(Mono.just(testCartItem));

        StepVerifier.create(cartService.updateItemQuantityInCart(ActionType.MINUS, testCart, testItem, true))
                .verifyComplete();

        verify(cartItemRepository).findByCartIdAndItemId(testCart.getId(), testItem.getId());
        verify(cartItemRepository).save(any(CartItem.class));
    }

    @Test
    void updateItemQuantityInCart_WithMinusAction_ShouldDeleteItemWhenQuantityBecomesZero() {
        CartItem cartItemWithQuantityOne = CartItem.builder()
                .id(1L)
                .cartId(testCart.getId())
                .itemId(testItem.getId())
                .quantity(1)
                .build();

        when(cartItemRepository.findByCartIdAndItemId(testCart.getId(), testItem.getId()))
                .thenReturn(Mono.just(cartItemWithQuantityOne));

        when(cartItemRepository.delete(any(CartItem.class)))
                .thenReturn(Mono.empty());

        StepVerifier.create(cartService.updateItemQuantityInCart(ActionType.MINUS, testCart, testItem, true))
                .verifyComplete();

        verify(cartItemRepository).delete(any(CartItem.class));
        verify(cartItemRepository, never()).save(any(CartItem.class));
    }

    @Test
    void updateItemQuantityInCart_WithDeleteAction_ShouldRemoveItem() {
        when(cartItemRepository.findByCartIdAndItemId(testCart.getId(), testItem.getId()))
                .thenReturn(Mono.just(testCartItem));

        when(cartItemRepository.delete(any(CartItem.class)))
                .thenReturn(Mono.empty());

        StepVerifier.create(cartService.updateItemQuantityInCart(ActionType.DELETE, testCart, testItem, true))
                .verifyComplete();

        verify(cartItemRepository).findByCartIdAndItemId(testCart.getId(), testItem.getId());
        verify(cartItemRepository).delete(testCartItem);
    }

    @Test
    void updateItemQuantityInCart_WithDeleteAction_ShouldThrowExceptionWhenItemNotFound() {
        when(cartItemRepository.findByCartIdAndItemId(testCart.getId(), testItem.getId()))
                .thenReturn(Mono.empty());

        StepVerifier.create(cartService.updateItemQuantityInCart(ActionType.DELETE, testCart, testItem, true))
                .expectError(EntityNotFoundException.class)
                .verify();

        verify(cartItemRepository).findByCartIdAndItemId(testCart.getId(), testItem.getId());
        verify(cartItemRepository, never()).delete(any(CartItem.class));
    }

    @Test
    void updateItemQuantityInCart_WithDeleteAction_ShouldDoNothingWhenNotAllowed() {
        StepVerifier.create(cartService.updateItemQuantityInCart(ActionType.DELETE, testCart, testItem, false))
                .verifyComplete();

        verify(cartItemRepository, never()).findByCartIdAndItemId(anyLong(), anyLong());
        verify(cartItemRepository, never()).delete(any(CartItem.class));
    }

    @Test
    void clearCartAndDelete_ShouldDeleteAllItemsAndCart() {
        when(cartItemRepository.deleteAllByCartId(testCart.getId()))
                .thenReturn(Mono.empty());

        when(cartRepository.delete(testCart))
                .thenReturn(Mono.empty());

        StepVerifier.create(cartService.clearCartAndDelete(testCart))
                .verifyComplete();

        verify(cartItemRepository).deleteAllByCartId(testCart.getId());
        verify(cartRepository).delete(testCart);
    }

    @Test
    void findAllCartItemsByCart_ShouldReturnAllItems() {
        when(cartItemRepository.findAllByCartId(testCart.getId()))
                .thenReturn(Flux.just(testCartItem));

        StepVerifier.create(cartService.findAllCartItemsByCart(testCart))
                .expectNext(testCartItem)
                .verifyComplete();
    }

    @Test
    void countCartItemsByCartIdAndItemId_ShouldReturnQuantityWhenItemExists() {
        when(cartItemRepository.findByCartIdAndItemId(testCart.getId(), testItem.getId()))
                .thenReturn(Mono.just(testCartItem));

        StepVerifier.create(cartService.countCartItemsByCartIdAndItemId(testCart.getId(), testItem.getId()))
                .expectNext(2)
                .verifyComplete();
    }

    @Test
    void countCartItemsByCartIdAndItemId_ShouldReturnZeroWhenItemNotExists() {
        when(cartItemRepository.findByCartIdAndItemId(testCart.getId(), testItem.getId()))
                .thenReturn(Mono.empty());

        StepVerifier.create(cartService.countCartItemsByCartIdAndItemId(testCart.getId(), testItem.getId()))
                .expectNext(0)
                .verifyComplete();
    }

    @Test
    void countCartItemsByUserIdAndItemId_ShouldReturnZeroWhenUserIdIsNull() {
        when(cartRepository.findByUserId(isNull())).thenReturn(Mono.empty());
        StepVerifier.create(cartService.countCartItemsByUserIdAndItemId(null, testItem.getId()))
                .expectNext(0)
                .verifyComplete();

        verify(cartRepository, never()).findByUserId(anyLong());
    }
}