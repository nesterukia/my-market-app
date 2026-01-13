package com.github.nesterukia.mymarket.service;

import com.github.nesterukia.mymarket.dao.CartItemRepository;
import com.github.nesterukia.mymarket.dao.CartRepository;
import com.github.nesterukia.mymarket.domain.Cart;
import com.github.nesterukia.mymarket.domain.CartItem;
import com.github.nesterukia.mymarket.domain.Item;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

    public Cart getOrCreate() {
        List<Cart> allCarts = cartRepository.findAll();

        if (allCarts.isEmpty()) {
            Cart newCart = new Cart();
            return cartRepository.save(newCart);
        } else {
            return allCarts.getFirst();
        }
    }

    public void increaseItemQuantityInCart(Cart cart, Item item) {
        CartItem cartItem = cartItemRepository.findByCartIdAndItemId(cart.getId(), item.getId())
                .orElseGet(() -> cartItemRepository.save(new CartItem(cart, item)));
        cartItem.increaseQuantity();
        cartItemRepository.save(cartItem);
    }

    public void decreaseItemQuantityInCart(Cart cart, Item item) {
        CartItem cartItem = cartItemRepository.findByCartIdAndItemId(cart.getId(), item.getId())
                .orElseThrow();
        cartItem.decreaseQuantity();

        if (cartItem.getQuantity() < 1) {
            cartItemRepository.delete(cartItem);
        } else {
            cartItemRepository.save(cartItem);
        }
    }

    public void removeItemFromCart(Cart cart, Item item) {
        CartItem cartItem = cartItemRepository.findByCartIdAndItemId(cart.getId(), item.getId())
                .orElseThrow();
        cartItemRepository.delete(cartItem);
    }

    public void clearCartAndDelete(Cart cart) {
        cartItemRepository.deleteAll();
        cartRepository.delete(cart);
    }
}
