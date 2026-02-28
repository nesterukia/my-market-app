package com.github.nesterukia.payment_service.dao;

import com.github.nesterukia.payment_service.domain.Transaction;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface TransactionRepository extends ReactiveCrudRepository<Transaction, Long> {
}
