package com.github.nesterukia.payment_service.dao;

import com.github.nesterukia.payment_service.domain.User;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface UserRepository extends ReactiveCrudRepository<User, Long> {
}
