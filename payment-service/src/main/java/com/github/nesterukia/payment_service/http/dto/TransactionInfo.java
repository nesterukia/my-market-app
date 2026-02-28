package com.github.nesterukia.payment_service.http.dto;

public record TransactionInfo(
        String transactionId,
        String message
) {}
