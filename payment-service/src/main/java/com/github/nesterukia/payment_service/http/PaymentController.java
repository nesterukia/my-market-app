package com.github.nesterukia.payment_service.http;

import com.github.nesterukia.payment_service.http.dto.PaymentRequest;
import com.github.nesterukia.payment_service.http.dto.TransactionInfo;
import com.github.nesterukia.payment_service.service.PaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class PaymentController {
    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping(value = "/api/payment")
    @PreAuthorize("hasAuthority('SERVICE')")
    public Mono<ResponseEntity<TransactionInfo>> commitPayment(@RequestBody PaymentRequest paymentRequest) {
        return paymentService.commitPayment(paymentRequest.userId(), paymentRequest.amount())
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
