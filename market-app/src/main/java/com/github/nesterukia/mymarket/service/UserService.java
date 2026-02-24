package com.github.nesterukia.mymarket.service;

import com.github.nesterukia.mymarket.dao.UserRepository;
import com.github.nesterukia.mymarket.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Duration;
import java.util.Optional;

import static com.github.nesterukia.mymarket.utils.UserUtils.USER_ID_COOKIE;

@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public Mono<User> getOrCreate(Long userId, ServerWebExchange exchange) {
        return Optional.ofNullable(userId)
                .map(id -> userRepository.findById(id)
                        .switchIfEmpty(
                                Mono.defer(() -> createAndSetCookieUser(exchange))
                        )
                )
                .orElseGet(() -> createAndSetCookieUser(exchange));
    }

    private Mono<User> createAndSetCookieUser(ServerWebExchange exchange) {
        return userRepository.save(new User())
                .doOnNext(newUser -> {
                    ResponseCookie cookie = ResponseCookie.from(USER_ID_COOKIE, newUser.getId().toString())
                            .path("/")
                            .httpOnly(true)
                            .maxAge(Duration.ofDays(1))
                            .build();
                    exchange.getResponse().addCookie(cookie);
                });
    }
}
