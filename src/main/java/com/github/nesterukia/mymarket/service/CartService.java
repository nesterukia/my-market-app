package com.github.nesterukia.mymarket.service;

import com.github.nesterukia.mymarket.dao.CartItemRepository;
import com.github.nesterukia.mymarket.dao.CartRepository;
import com.github.nesterukia.mymarket.domain.Cart;
import com.github.nesterukia.mymarket.domain.CartItem;
import com.github.nesterukia.mymarket.domain.Item;
import com.github.nesterukia.mymarket.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    @Autowired
    public CartService(CartRepository cartRepository, CartItemRepository cartItemRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
    }

    public Cart findById(Long cartId) {
        return cartRepository.findById(cartId).orElseThrow();
    }

    public Cart create(User user) {
        return cartRepository.save(Cart.builder()
                .user(user)
                .cartItems(List.of())
                .build()
        );
    }

    public void increaseItemQuantityInCart(Cart cart, Item item) {
        CartItem cartItem = cartItemRepository.findByCartIdAndItemId(cart.getId(), item.getId())
                .orElseGet(() -> cartItemRepository.save(new CartItem(cart, item)));
        cartItem.increaseQuantity();
        cartItemRepository.save(cartItem);
    }

    public void decreaseItemQuantityInCart(Cart cart, Item item) {
        Optional<CartItem> cartItemOptional = cartItemRepository.findByCartIdAndItemId(cart.getId(), item.getId());

        if (cartItemOptional.isPresent()) {
            CartItem cartItem = cartItemOptional.get();
            cartItem.decreaseQuantity();
            if (cartItem.getQuantity() < 1) {
                cartItemRepository.delete(cartItem);
            } else {
                cartItemRepository.save(cartItem);
            }
        }
    }

    public void removeItemFromCart(Cart cart, Item item) {
        CartItem cartItem = cartItemRepository.findByCartIdAndItemId(cart.getId(), item.getId())
                .orElseThrow();
        cartItemRepository.delete(cartItem);
    }

    public void clearCartAndDelete(Cart cart) {
        cartItemRepository.deleteAllByCart(cart);
        cartRepository.delete(cart);
    }
}
