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
        return userRepository.findById(userId).switchIfEmpty(create(userId));
    }

    private Mono<User> create(Long userId) {
        log.debug("UserService: new user with id='{}' created", userId);
        return userRepository.save(User.builder().id(userId).build());
    }
}
