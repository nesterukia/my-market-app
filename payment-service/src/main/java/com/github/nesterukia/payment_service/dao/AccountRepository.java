package com.github.nesterukia.payment_service.dao;

import com.github.nesterukia.payment_service.domain.Account;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface AccountRepository extends ReactiveCrudRepository<Account, Long> {
    Mono<Account> findByUserId(Long userId);
}
