package com.github.nesterukia.mymarket.dao;

import com.github.nesterukia.mymarket.domain.Cart;
import com.github.nesterukia.mymarket.domain.CartItem;
import com.github.nesterukia.mymarket.domain.Item;
import com.github.nesterukia.mymarket.domain.User;
import com.github.nesterukia.mymarket.utils.PostgresContainerTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

@SpringBootTest
class CartItemRepositoryTest extends PostgresContainerTest {

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ItemRepository itemRepository;    
    
    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser1;
    private User testUser2;
    private Item savedItem1;
    private Item savedItem2;
    private Item savedItem3;
    private Cart savedCart1;
    private Cart savedCart2;

    @BeforeEach
    void setUp() {
        cartItemRepository.deleteAll().block();
        itemRepository.deleteAll().block();

        Item item1 = Item.builder()
                .title("Test Item 1")
                .description("Description 1")
                .imgPath("/img1.jpg")
                .price(100L)
                .build();

        Item item2 = Item.builder()
                .title("Test Item 2")
                .description("Description 2")
                .imgPath("/img2.jpg")
                .price(200L)
                .build();

        Item item3 = Item.builder()
                .title("Test Item 3")
                .description("Description 3")
                .imgPath("/img3.jpg")
                .price(300L)
                .build();

        testUser1 = userRepository.save(User.builder().build()).block();
        testUser2 = userRepository.save(User.builder().build()).block();
        Cart cart1 = Cart.builder().userId(testUser1.getId()).build();
        Cart cart2 = Cart.builder().userId(testUser2.getId()).build();

        savedItem1 = itemRepository.save(item1).block();
        savedItem2 = itemRepository.save(item2).block();
        savedItem3 = itemRepository.save(item3).block();
        savedCart1 = cartRepository.save(cart1).block();
        savedCart2 = cartRepository.save(cart2).block();
    }

    @Test
    void findByCartIdAndItemId_ShouldReturnCartItemWhenExists() {
        CartItem cartItem = CartItem.builder()
                .cartId(savedCart1.getId())
                .itemId(savedItem1.getId())
                .quantity(2)
                .build();

        Mono<CartItem> saveItem = cartItemRepository.save(cartItem);

        StepVerifier.create(saveItem.then(cartItemRepository.findByCartIdAndItemId(savedCart1.getId(), savedItem1.getId())))
                .expectNextMatches(item ->
                        item.getCartId().equals(savedCart1.getId()) &&
                                item.getItemId().equals(savedItem1.getId()) &&
                                item.getQuantity() == 2L
                )
                .verifyComplete();
    }

    @Test
    void findByCartIdAndItemId_ShouldReturnEmptyWhenNotExists() {
        
        CartItem cartItem = CartItem.builder()
                .cartId(savedCart1.getId())
                .itemId(savedItem1.getId())
                .quantity(2)
                .build();

        Mono<CartItem> saveItem = cartItemRepository.save(cartItem);

        StepVerifier.create(saveItem.then(cartItemRepository.findByCartIdAndItemId(999L, savedItem1.getId())))
                .verifyComplete();

        StepVerifier.create(cartItemRepository.findByCartIdAndItemId(savedCart1.getId(), 999L))
                .verifyComplete();
    }

    @Test
    void deleteAllByCartId_ShouldDeleteAllItemsFromCart() {
        
        CartItem cartItem1 = CartItem.builder()
                .cartId(savedCart1.getId())
                .itemId(savedItem1.getId())
                .quantity(2)
                .build();

        CartItem cartItem2 = CartItem.builder()
                .cartId(savedCart1.getId())
                .itemId(savedItem2.getId())
                .quantity(3)
                .build();

        CartItem cartItem3 = CartItem.builder()
                .cartId(savedCart1.getId())
                .itemId(savedItem3.getId())
                .quantity(1)
                .build();

        Flux<CartItem> saveAll = cartItemRepository.saveAll(List.of(cartItem1, cartItem2, cartItem3));

        StepVerifier.create(saveAll.then(cartItemRepository.deleteAllByCartId(savedCart1.getId())))
                .verifyComplete();

        StepVerifier.create(cartItemRepository.findAllByCartId(savedCart1.getId()))
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    void deleteAllByCartId_ShouldOnlyDeleteItemsFromSpecifiedCart() {
        CartItem cartItem1 = CartItem.builder()
                .cartId(savedCart1.getId())
                .itemId(savedItem1.getId())
                .quantity(2)
                .build();

        CartItem cartItem2 = CartItem.builder()
                .cartId(savedCart1.getId())
                .itemId(savedItem2.getId())
                .quantity(3)
                .build();

        CartItem cartItem3 = CartItem.builder()
                .cartId(savedCart2.getId())
                .itemId(savedItem3.getId())
                .quantity(1)
                .build();

        Flux<CartItem> saveAll = cartItemRepository.saveAll(List.of(cartItem1, cartItem2, cartItem3));

        StepVerifier.create(saveAll.then(cartItemRepository.deleteAllByCartId(savedCart1.getId())))
                .verifyComplete();

        StepVerifier.create(cartItemRepository.findAllByCartId(savedCart1.getId()))
                .expectNextCount(0)
                .verifyComplete();

        StepVerifier.create(cartItemRepository.findAllByCartId(savedCart2.getId()))
                .expectNextMatches(item ->
                        item.getItemId().equals(savedItem3.getId()) &&
                                item.getQuantity() == 1L
                )
                .verifyComplete();
    }

    @Test
    void findAllByCartId_ShouldReturnAllItemsInCart() {
        
        CartItem cartItem1 = CartItem.builder()
                .cartId(savedCart1.getId())
                .itemId(savedItem1.getId())
                .quantity(2)
                .build();

        CartItem cartItem2 = CartItem.builder()
                .cartId(savedCart1.getId())
                .itemId(savedItem2.getId())
                .quantity(3)
                .build();

        Flux<CartItem> saveAll = cartItemRepository.saveAll(List.of(cartItem1, cartItem2));

        StepVerifier.create(saveAll.thenMany(cartItemRepository.findAllByCartId(savedCart1.getId())))
                .expectNextCount(2)
                .verifyComplete();

        StepVerifier.create(saveAll.thenMany(cartItemRepository.findAllByCartId(savedCart1.getId()))
                        .map(CartItem::getItemId)
                        .collectList())
                .expectNextMatches(ids -> ids.containsAll(List.of(savedItem1.getId(), savedItem2.getId())))
                .verifyComplete();
    }

    @Test
    void findAllByCartId_ShouldReturnEmptyForNonExistentCart() {
        
        CartItem cartItem = CartItem.builder()
                .cartId(savedCart1.getId())
                .itemId(savedItem1.getId())
                .quantity(2)
                .build();

        Mono<CartItem> saveItem = cartItemRepository.save(cartItem);

        StepVerifier.create(saveItem.thenMany(cartItemRepository.findAllByCartId(999L)))
                .expectNextCount(0)
                .verifyComplete();
    }
}