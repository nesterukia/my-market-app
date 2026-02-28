package com.github.nesterukia.payment_service.repository;

import com.github.nesterukia.payment_service.dao.UserRepository;
import com.github.nesterukia.payment_service.domain.User;
import com.github.nesterukia.payment_service.utils.PostgresContainerTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.List;

@SpringBootTest
public class UserRepositoryTest extends PostgresContainerTest {

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll().block();
    }

    @Test
    void findByIdIsOk() {
        User user1 = User.builder().id(1L).build();
        User user2 = User.builder().id(2L).build();
        User user3 = User.builder().id(3L).build();

        StepVerifier.create(userRepository.saveAll(List.of(user1, user2, user3))
                        .thenMany(userRepository.findById(2L)))
                .expectNextCount(1)
                .verifyComplete();
    }
}