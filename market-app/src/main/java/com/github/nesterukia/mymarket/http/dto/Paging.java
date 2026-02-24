package com.github.nesterukia.mymarket.http.dto;

public record Paging(
        int pageSize,
        int pageNumber,
        boolean hasPrevious,
        boolean hasNext
) {}
