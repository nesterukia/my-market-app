package com.github.nesterukia.mymarket.http.dto.payment;

public record TransactionInfo(
        String transactionId,
        String message
) {
    public static TransactionInfo internalPaymentServerError() {
        return new TransactionInfo("-1", "Internal payment server error. Try later.");
    }
}
