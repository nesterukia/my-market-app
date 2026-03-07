package com.github.nesterukia.payment_service.http.dto;

public record PaymentRequest(
        String userId,
        Double amount
){}
