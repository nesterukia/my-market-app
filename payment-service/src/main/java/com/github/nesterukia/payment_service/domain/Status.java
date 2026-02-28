package com.github.nesterukia.payment_service.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Status {
    SUCCESS("Succesfull payment"),
    ERROR("Payment is declined.");

    private final String message;
}
