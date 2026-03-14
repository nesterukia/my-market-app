package com.github.nesterukia.mymarket.http.dto.payment;

public record BalanceInfo(
        String userId,
        Double currentBalance
) {}
