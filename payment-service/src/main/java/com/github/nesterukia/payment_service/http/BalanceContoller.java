package com.github.nesterukia.payment_service.http;

import com.github.nesterukia.payment_service.http.dto.BalanceInfo;
import com.github.nesterukia.payment_service.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class BalanceContoller {
    private final PaymentService paymentService;

    public BalanceContoller(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping(value = "/api/balanceInfo/{userId}")
    @PreAuthorize("hasAuthority('SERVICE')")
    public Mono<ResponseEntity<BalanceInfo>> getBalanceInfo(@PathVariable String userId) {
        return paymentService.findAccountByUserId(userId)
                .map(account -> ResponseEntity.ok(new BalanceInfo(
                        account.getUserId(),
                        account.getCurrentBalance()
                )));
    }
}
