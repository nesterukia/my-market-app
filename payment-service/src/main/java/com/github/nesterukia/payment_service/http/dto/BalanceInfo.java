package com.github.nesterukia.payment_service.http.dto;

public record BalanceInfo(
        Long userId,
        Double currentBalance
) {}
