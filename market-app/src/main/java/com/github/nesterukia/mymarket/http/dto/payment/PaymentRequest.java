package com.github.nesterukia.mymarket.http.dto.payment;

public record PaymentRequest(
        Long userId,
        Double amount
){}
