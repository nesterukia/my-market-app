package com.github.nesterukia.mymarket.domain.exceptions;

import com.github.nesterukia.mymarket.http.dto.payment.TransactionInfo;
import lombok.Getter;

@Getter
public class PaymentServerException extends RuntimeException {
    private final TransactionInfo transactionInfo;
    public PaymentServerException(TransactionInfo transactionInfo) {
        super("");
        this.transactionInfo = transactionInfo;
    }

    public PaymentServerException() {
        super("");
        this.transactionInfo = TransactionInfo.internalPaymentServerError();
    }
}
