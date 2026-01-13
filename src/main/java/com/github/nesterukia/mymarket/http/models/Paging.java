package com.github.nesterukia.mymarket.http.models;

public record Paging(
        int pageSize,
        int pageNumber,
        boolean hasPrevious,
        boolean hasNext
) {}
