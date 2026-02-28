package com.github.nesterukia.payment_service.service;

import com.github.nesterukia.payment_service.dao.AccountRepository;
import com.github.nesterukia.payment_service.dao.TransactionRepository;
import com.github.nesterukia.payment_service.domain.Account;
import com.github.nesterukia.payment_service.domain.Status;
import com.github.nesterukia.payment_service.domain.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@Transactional
@Slf4j
public class PaymentService {
    private AccountRepository accountRepository;
    private TransactionRepository transactionRepository;

    @Autowired
    public PaymentService(AccountRepository accountRepository, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    public Mono<Account> findAccountByUserId(Long userId) {
        return accountRepository.findByUserId(userId)
                .doOnNext(user -> log.info("Account FOUND: {}", user.getId()))
                .switchIfEmpty(Mono.defer(() -> {
                    log.info("Account NOT FOUND, creating: {}", userId);
                    return accountRepository.save(Account.builder().userId(userId).build());
                }))
                .doOnSuccess(account -> log.info("Returning Account: {}", account.getId()));
    }

    public Mono<Transaction> commitPayment(Long userId, Double amount) {
        return accountRepository.findByUserId(userId)
                .flatMap(account -> {
                    double finalBalance = account.getCurrentBalance() - amount;
                    boolean isTransactionAllowed = finalBalance >= 0;
                    Transaction.TransactionBuilder transactionBuilder = Transaction.builder()
                            .id(UUID.randomUUID())
                            .userId(userId)
                            .accountId(account.getId());
                    if (isTransactionAllowed) {
                        account.setCurrentBalance(finalBalance);
                        log.debug("Account[{}] current balance: {}", account.getId(), account.getCurrentBalance());
                        transactionBuilder.status(Status.SUCCESS);
                    } else {
                        transactionBuilder.status(Status.ERROR);
                    }
                    return accountRepository.save(account).then(transactionRepository.save(transactionBuilder.build()));
                });
    }
}
