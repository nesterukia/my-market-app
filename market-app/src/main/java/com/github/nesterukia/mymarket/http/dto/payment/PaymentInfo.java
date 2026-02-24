package com.github.nesterukia.mymarket.http.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PaymentInfo {
    private boolean isEnoughMoney;
    private boolean isServiceAvailable;

    public static PaymentInfo serviceUnavailable() {
        return new PaymentInfo(false, false);
    }
}
