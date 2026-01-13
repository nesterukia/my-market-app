package com.github.nesterukia.mymarket.dao;

import com.github.nesterukia.mymarket.domain.Cart;
import com.github.nesterukia.mymarket.domain.CartItem;
import com.github.nesterukia.mymarket.domain.Item;
import com.github.nesterukia.mymarket.utils.PostgresContainerTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class CartItemRepositoryTest extends PostgresContainerTest {
    @Autowired
    private CartItemRepository cartItemRepository;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private CartRepository cartRepository;

    private Cart testCart;
    private Item testItem;

    @BeforeEach
    public void prepareEntities() {
        testCart = cartRepository.save(new Cart());
        testItem = itemRepository.save(new Item());
    }

    @Test
    public void findByCartIdAndItemIdIsOk() {
        CartItem expectedCartItem = cartItemRepository.save(new CartItem(testCart, testItem));
        Optional<CartItem> actualCartItem = cartItemRepository.findByCartIdAndItemId(testCart.getId(), testItem.getId());
        assertTrue(actualCartItem.isPresent());
        assertEquals(expectedCartItem.getId(), actualCartItem.get().getId());
        assertEquals(testItem.getId(), actualCartItem.get().getItem().getId());
        assertEquals(testCart.getId(), actualCartItem.get().getCart().getId());
    }

    @Test
    public void findByCartIdAndItemIdNotExistingIsOk() {
        Long unknownCartId = 88888L;
        Long unknownItemId = 99999L;
        Optional<CartItem> actualCartItem = cartItemRepository.findByCartIdAndItemId(unknownCartId, unknownItemId);
        assertTrue(actualCartItem.isEmpty());
    }

    @AfterEach
    public void clearRepository() {
        cartItemRepository.deleteAll();
        cartRepository.deleteAll();
        itemRepository.deleteAll();
    }
}
