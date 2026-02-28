package com.github.nesterukia.mymarket.service;

import com.github.nesterukia.mymarket.dao.CartItemRepository;
import com.github.nesterukia.mymarket.dao.CartRepository;
import com.github.nesterukia.mymarket.dao.ItemRepository;
import com.github.nesterukia.mymarket.domain.ActionType;
import com.github.nesterukia.mymarket.domain.Cart;
import com.github.nesterukia.mymarket.domain.CartItem;
import com.github.nesterukia.mymarket.domain.Item;
import com.github.nesterukia.mymarket.domain.User;
import com.github.nesterukia.mymarket.domain.exceptions.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@Transactional
public class CartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ItemRepository itemRepository;

    @Autowired
    public CartService(CartRepository cartRepository, CartItemRepository cartItemRepository, ItemRepository itemRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.itemRepository = itemRepository;
    }

    public Mono<Cart> findByUserId(Long userId) {
        return cartRepository.findByUserId(userId);
    }

    public Mono<Cart> create(User user) {
        Cart newCart = Cart.builder().userId(user.getId()).build();
        log.debug("New cart for user[{}]: {}", user.getId(), newCart);
        return cartRepository.save(newCart);
    }

    public Mono<Void> updateItemQuantityInCart(ActionType actionType, Cart cart, Item item, boolean isDeleteAllowed) {
        return switch (actionType) {
            case MINUS -> decreaseItemQuantityInCart(cart, item);
            case PLUS -> increaseItemQuantityInCart(cart, item);
            case DELETE -> isDeleteAllowed ? removeItemFromCart(cart, item) : Mono.empty();
        };
    }

    public Mono<Double> calculateTotalSum(Cart cart) {
        return cartItemRepository.findAllByCartId(cart.getId())
                .flatMap(cartItem -> itemRepository.findById(cartItem.getItemId())
                            .map(Item::getPrice)
                            .map(price -> price * cartItem.getQuantity())
                )
                .reduce(0.0, Double::sum);
    }

    public Mono<Void> clearCartAndDelete(Cart cart) {
        return cartItemRepository.deleteAllByCartId(cart.getId()).then(cartRepository.delete(cart));
    }

    public Flux<CartItem> findAllCartItemsByCart(Cart cart) {
        return cartItemRepository.findAllByCartId(cart.getId());
    }

    public Mono<Integer> countCartItemsByCartIdAndItemId(Long cartId, Long itemId) {
        return cartItemRepository.findByCartIdAndItemId(cartId, itemId)
                .map(CartItem::getQuantity)
                .switchIfEmpty(Mono.just(0));
    }

    public Mono<Integer> countCartItemsByUserIdAndItemId(Long userId, Long itemId) {
        return cartRepository.findByUserId(userId)
                .flatMap(cart -> cartItemRepository.findByCartIdAndItemId(cart.getId(), itemId))
                .map(CartItem::getQuantity)
                .switchIfEmpty(Mono.just(0));
    }

    private Mono<Void> increaseItemQuantityInCart(Cart cart, Item item) {
        return cartItemRepository.findByCartIdAndItemId(cart.getId(), item.getId())
                .switchIfEmpty(
                        cartItemRepository.save(CartItem.builder().cartId(cart.getId())
                                .itemId(item.getId())
                                .build()
                        )
                )
                .map(cartItem -> {
                    cartItem.increaseQuantity();
                    return cartItem;
                })
                .flatMap(cartItemRepository::save).then();
    }

    private Mono<Void> decreaseItemQuantityInCart(Cart cart, Item item) {
        return cartItemRepository.findByCartIdAndItemId(cart.getId(), item.getId())
                .map(cartItem -> {
                    cartItem.decreaseQuantity();
                    return cartItem;
                })
                .flatMap(cartItem -> {
                    if (cartItem.getQuantity() < 1) {
                        return cartItemRepository.delete(cartItem);
                    } else {
                        return cartItemRepository.save(cartItem).then();
                    }
                });
    }

    private Mono<Void> removeItemFromCart(Cart cart, Item item) {
        return cartItemRepository.findByCartIdAndItemId(cart.getId(), item.getId())
                .switchIfEmpty(
                        Mono.error(EntityNotFoundException.itemInCartNotFound(item.getId(), cart.getId()))
                )
                .flatMap(cartItemRepository::delete);
    }
}
