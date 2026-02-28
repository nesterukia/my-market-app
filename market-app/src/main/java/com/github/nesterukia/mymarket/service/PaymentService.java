package com.github.nesterukia.mymarket.service;

import com.github.nesterukia.mymarket.domain.exceptions.PaymentServerException;
import com.github.nesterukia.mymarket.http.dto.payment.BalanceInfo;
import com.github.nesterukia.mymarket.http.dto.payment.PaymentInfo;
import com.github.nesterukia.mymarket.http.dto.payment.PaymentRequest;
import com.github.nesterukia.mymarket.http.dto.payment.TransactionInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URI;

@Service
@Transactional
@Slf4j
public class PaymentService {
    private final WebClient webClient;
    @Value("${paymentService.baseUrl}")
    private String paymentServiceUrl;
    private static final String USER_BALANCE_URI_PATH = "/api/balanceInfo/%d";
    private static final String COMMIT_PAYMENT_URI_PATH = "/api/payment";

    @Autowired
    public PaymentService(WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<PaymentInfo> checkUserBalance(Long userId, Double orderSum) {
        String uriPath = USER_BALANCE_URI_PATH.formatted(userId);
        return webClient.get()
                .uri(URI.create(paymentServiceUrl.concat(uriPath)))
                .retrieve()
                .bodyToMono(BalanceInfo.class)
                .map(balanceInfo -> {
                    log.debug("PaymentService.checkUserBalance(): id={}, balance={}", balanceInfo.userId(), balanceInfo.currentBalance());
                    double balanceDifference = balanceInfo.currentBalance() - orderSum;
                    boolean isEnoughMoney = balanceDifference >= 0.0;
                    return new PaymentInfo(isEnoughMoney, true);
                })
                .onErrorResume(e -> Mono.just(PaymentInfo.serviceUnavailable()));
    }

    public Mono<TransactionInfo> commitPayment(Long userId, Double orderSum) {
        return webClient.post()
                .uri(URI.create(paymentServiceUrl.concat(COMMIT_PAYMENT_URI_PATH)))
                .body(Mono.just(new PaymentRequest(userId, orderSum)), PaymentRequest.class)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError,
                        clientResponse -> clientResponse.bodyToMono(TransactionInfo.class)
                                .flatMap(info -> {
                                    PaymentServerException exception = new PaymentServerException(info);
                                    return Mono.error(exception);
                                }))
                .onStatus(HttpStatusCode::is5xxServerError,
                        response -> Mono.error(new PaymentServerException()))
                .bodyToMono(TransactionInfo.class);
    }
}
