package com.github.nesterukia.payment_service.http;

import com.github.nesterukia.payment_service.http.dto.PaymentRequest;
import com.github.nesterukia.payment_service.http.dto.TransactionInfo;
import com.github.nesterukia.payment_service.service.PaymentService;
import com.github.nesterukia.payment_service.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class PaymentController {
    private final PaymentService paymentService;
    private final UserService userService;

    public PaymentController(PaymentService paymentService, UserService userService) {
        this.paymentService = paymentService;
        this.userService = userService;
    }

    @PostMapping(value = "/api/payment")
    public Mono<ResponseEntity<TransactionInfo>> commitPayment(@RequestBody PaymentRequest paymentRequest) {
        return userService.findById(paymentRequest.userId())
                .flatMap(user -> paymentService.commitPayment(user.getId(), paymentRequest.amount()))
                .map(transaction -> {
                    HttpStatus status = switch (transaction.getStatus()) {
                        case SUCCESS -> HttpStatus.OK;
                        case ERROR -> HttpStatus.FORBIDDEN;
                    };
                    return ResponseEntity.status(status)
                            .body(new TransactionInfo(
                                    transaction.getId().toString(),
                                    transaction.getStatus().getMessage()
                            ));
                });
    }
}
