package com.github.nesterukia.payment_service.http.dto;

public record PaymentRequest(
        Long userId,
        Double amount
){}
