package com.rently.rently.shared;

import org.springframework.data.domain.Page;

import java.util.function.Function;

public class PagedResponseMapper {

    private PagedResponseMapper() {}

    public static <E, R> PagedResponse<R> toPagedResponse(Page<E> page, Function<E, R> mapper) {
        return new PagedResponse<>(
                page.getContent().stream().map(mapper).toList(),
                page.getNumber() + 1,
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );
    }
}
