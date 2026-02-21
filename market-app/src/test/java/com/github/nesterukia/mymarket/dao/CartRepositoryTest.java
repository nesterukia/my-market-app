package com.github.nesterukia.mymarket.dao;

import com.github.nesterukia.mymarket.domain.Cart;
import com.github.nesterukia.mymarket.domain.User;
import com.github.nesterukia.mymarket.utils.PostgresContainerTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.test.StepVerifier;


@SpringBootTest
class CartRepositoryTest extends PostgresContainerTest {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private Cart testCart;

    @BeforeEach
    void setUp() {
        cartRepository.deleteAll().block();

        testUser = userRepository.save(User.builder().build()).block();

        testCart = Cart.builder()
                .userId(testUser.getId())
                .build();
    }

    @Test
    void findByUserId_ShouldReturnCartWhenExists() {
        Cart savedCart = cartRepository.save(testCart).block();

        StepVerifier.create(cartRepository.findByUserId(testUser.getId()))
                .expectNextMatches(cart ->
                        cart.getUserId().equals(testUser.getId()) &&
                                cart.getId().equals(savedCart.getId())
                )
                .verifyComplete();
    }

    @Test
    void findByUserId_ShouldReturnEmptyWhenNoCartForUser() {
        cartRepository.save(testCart).block();

        StepVerifier.create(cartRepository.findByUserId(999L))
                .verifyComplete();
    }

    @Test
    void save_ShouldCreateNewCartWithGeneratedId() {
        StepVerifier.create(cartRepository.save(testCart))
                .expectNextMatches(cart ->
                        cart.getId() != null &&
                                cart.getUserId().equals(testUser.getId())
                )
                .verifyComplete();
    }

    @Test
    void findById_ShouldReturnCartWhenExists() {
        Cart savedCart = cartRepository.save(testCart).block();

        StepVerifier.create(cartRepository.findById(savedCart.getId()))
                .expectNextMatches(cart ->
                        cart.getId().equals(savedCart.getId()) &&
                                cart.getUserId().equals(testUser.getId())
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