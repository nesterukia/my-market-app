package com.github.nesterukia.mymarket.domain;

import lombok.Getter;

@Getter
public enum SortType {
    ALPHA("title"),
    NO(""),
    PRICE("price");

    private final String property;

    SortType(String property) {
        this.property = property;
    }
}
