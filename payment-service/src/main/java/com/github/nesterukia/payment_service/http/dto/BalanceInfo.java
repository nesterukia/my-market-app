package com.github.nesterukia.payment_service.http.dto;

public record BalanceInfo(
        String userId,
        Double currentBalance
) {}
