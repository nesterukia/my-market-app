package com.github.nesterukia.payment_service.service;

import com.github.nesterukia.payment_service.dao.UserRepository;
import com.github.nesterukia.payment_service.domain.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

@Service
@Transactional
@Slf4j
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public Mono<User> findById(Long userId) {
        log.info("Finding user by ID: {}", userId);
        return userRepository.findById(userId)
                .doOnNext(user -> log.info("User FOUND: {}", user.getId()))
                .switchIfEmpty(Mono.defer(() -> {
                    log.info("User NOT FOUND, creating: {}", userId);
                    return create(userId);
                }))
                .doOnSuccess(user -> log.info("Returning user: {}", user.getId()));
    }

    private Mono<User> create(Long userId) {
        log.warn("CREATING user with ID: {}", userId);
        User user = User.builder()
                .id(userId)
                .build();

        return userRepository.save(user)
                .doOnSuccess(saved -> log.info("SAVED user: {}", saved.getId()))
                .doOnError(err -> log.error("SAVE ERROR: ", err));
    }
}
