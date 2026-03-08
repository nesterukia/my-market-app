package com.github.nesterukia.mymarket.http.dto.payment;

public record PaymentRequest(
        String userId,
        Double amount
){}
