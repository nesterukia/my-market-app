package com.github.nesterukia.payment_service.http;

import com.github.nesterukia.payment_service.http.dto.PaymentRequest;
import com.github.nesterukia.payment_service.http.dto.TransactionInfo;
import com.github.nesterukia.payment_service.service.PaymentService;
import com.github.nesterukia.payment_service.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import reactor.core.publisher.Mono;

@Controller
@RequestMapping("/api/payment")
public class PaymentController {
    private final PaymentService paymentService;
    private final UserService userService;

    public PaymentController(PaymentService paymentService, UserService userService) {
        this.paymentService = paymentService;
        this.userService = userService;
    }

    @PostMapping
    public Mono<TransactionInfo> commitPayment(@RequestBody PaymentRequest paymentRequest) {
        return userService.findById(paymentRequest.userId())
                .flatMap(user -> paymentService.commitPayment(user.getId(), paymentRequest.amount()))
                .map(transaction -> new TransactionInfo(
                        transaction.getId().toString(),
                        transaction.getStatus().getMessage()
                ));
    }
}
