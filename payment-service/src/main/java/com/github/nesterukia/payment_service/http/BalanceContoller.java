package com.github.nesterukia.payment_service.http;

import com.github.nesterukia.payment_service.http.dto.BalanceInfo;
import com.github.nesterukia.payment_service.service.PaymentService;
import com.github.nesterukia.payment_service.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class BalanceContoller {
    private final UserService userService;
    private final PaymentService paymentService;

    public BalanceContoller(UserService userService, PaymentService paymentService) {
        this.userService = userService;
        this.paymentService = paymentService;
    }

    @GetMapping(value = "/api/balanceInfo/{userId}")
    public Mono<ResponseEntity<BalanceInfo>> getBalanceInfo(@PathVariable Long userId) {
        return userService.findById(userId)
                .flatMap(user -> paymentService.findAccountByUserId(user.getId()))
                .map(account -> ResponseEntity.ok(new BalanceInfo(
                        account.getUserId(),
                        account.getCurrentBalance()
                )));
    }
}
