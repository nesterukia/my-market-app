package com.github.nesterukia.payment_service.http;

import com.github.nesterukia.payment_service.http.dto.BalanceInfo;
import com.github.nesterukia.payment_service.service.PaymentService;
import com.github.nesterukia.payment_service.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import reactor.core.publisher.Mono;

@Controller
@RequestMapping(value = "/api/balance")
public class BalanceContoller {
    private final UserService userService;
    private final PaymentService paymentService;

    public BalanceContoller(UserService userService, PaymentService paymentService) {
        this.userService = userService;
        this.paymentService = paymentService;
    }

    @GetMapping(value = "/{userId}")
    public Mono<BalanceInfo> getBalanceInfo(@PathVariable Long userId) {
        return userService.findById(userId)
                .flatMap(user -> paymentService.findAccountByUserId(user.getId()))
                .map(account -> new BalanceInfo(
                        account.getUserId(),
                        account.getCurrentBalance()
                ));
    }
}
