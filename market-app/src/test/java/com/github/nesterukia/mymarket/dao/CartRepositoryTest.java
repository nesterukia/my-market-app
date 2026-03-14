package com.github.nesterukia.mymarket.dao;

import com.github.nesterukia.mymarket.domain.Cart;
import com.github.nesterukia.mymarket.utils.CachedDbContainerTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.test.StepVerifier;


@SpringBootTest
class CartRepositoryTest extends CachedDbContainerTest {

    @Autowired
    private CartRepository cartRepository;

    private String testUserId;
    private Cart testCart;

    @BeforeEach
    void setUp() {
        cartRepository.deleteAll().block();

        testUserId = "testUserId_123456";

        testCart = Cart.builder()
                .userId(testUserId)
                .build();
    }

    @Test
    void findByUserId_ShouldReturnCartWhenExists() {
        Cart savedCart = cartRepository.save(testCart).block();

        StepVerifier.create(cartRepository.findByUserId(testUserId))
                .expectNextMatches(cart ->
                        cart.getUserId().equals(testUserId) &&
                                cart.getId().equals(savedCart.getId())
                )
                .verifyComplete();
    }

    @Test
    void findByUserId_ShouldReturnEmptyWhenNoCartForUser() {
        cartRepository.save(testCart).block();

        StepVerifier.create(cartRepository.findByUserId(testUserId))
                .verifyComplete();
    }

    @Test
    void save_ShouldCreateNewCartWithGeneratedId() {
        StepVerifier.create(cartRepository.save(testCart))
                .expectNextMatches(cart ->
                        cart.getId() != null &&
                                cart.getUserId().equals(testUserId)
                )
                .verifyComplete();
    }

    @Test
    void findById_ShouldReturnCartWhenExists() {
        Cart savedCart = cartRepository.save(testCart).block();

        StepVerifier.create(cartRepository.findById(savedCart.getId()))
                .expectNextMatches(cart ->
                        cart.getId().equals(savedCart.getId()) &&
                                cart.getUserId().equals(testUserId)
                )
                .verifyComplete();
    }

    @Test
    void delete_ShouldRemoveCartFromDatabase() {
        Cart savedCart = cartRepository.save(testCart).block();

        StepVerifier.create(cartRepository.delete(savedCart))
                .verifyComplete();

        StepVerifier.create(cartRepository.findById(savedCart.getId()))
                .verifyComplete();
    }
}