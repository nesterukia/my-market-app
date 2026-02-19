package com.github.nesterukia.mymarket.http.models;


public record ItemsRequestDto(
        Long id,
        String search,
        String sort,
        Integer pageNumber,
        Integer pageSize,
        String action
) {
    public ItemsRequestDto {
        search = search == null ? "" : search;
        sort = sort == null ? "NO" : sort;
        pageNumber = pageNumber == null  || pageNumber < 1 ? 1 : pageNumber;
        pageSize = pageSize == null ? 5 : pageSize;
    }
}
