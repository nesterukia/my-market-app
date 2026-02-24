package com.github.nesterukia.mymarket.http.dto.payment;

public record BalanceInfo(
        Long userId,
        Double currentBalance
) {}
