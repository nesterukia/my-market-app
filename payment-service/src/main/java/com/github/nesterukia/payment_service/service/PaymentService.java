package com.github.nesterukia.payment_service.service;

import com.github.nesterukia.payment_service.dao.AccountRepository;
import com.github.nesterukia.payment_service.dao.TransactionRepository;
import com.github.nesterukia.payment_service.domain.Account;
import com.github.nesterukia.payment_service.domain.Status;
import com.github.nesterukia.payment_service.domain.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

@Service
@Transactional
public class PaymentService {
    private AccountRepository accountRepository;
    private TransactionRepository transactionRepository;

    @Autowired
    public PaymentService(AccountRepository accountRepository, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    public Mono<Account> findAccountByUserId(Long userId) {
        return accountRepository.findByUserId(userId);
    }

    public Mono<Transaction> commitPayment(Long userId, Double amount) {
        return accountRepository.findByUserId(userId)
                .flatMap(account -> {
                    double finalBalance = account.getCurrentBalance() - amount;
                    boolean isTransactionAllowed = finalBalance >= 0;
                    Transaction.TransactionBuilder transactionBuilder = Transaction.builder()
                            .accountId(account.getId());
                    if (isTransactionAllowed) {
                        account.setCurrentBalance(finalBalance);
                        accountRepository.save(account);
                        transactionBuilder.status(Status.SUCCESS);
                    } else {
                        transactionBuilder.status(Status.ERROR);
                    }
                    return transactionRepository.save(transactionBuilder.build());
                });
    }
}
