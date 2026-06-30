package com.careeros.common.pagination;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

/**
 * Transport-friendly pagination wrapper.
 *
 * <p>Spring Data's {@link Page} leaks persistence concerns and is awkward to serialize stably.
 * {@link PaginationResponse} exposes only what an API client needs and supports mapping the
 * underlying entity page to a DTO page via {@link #from(Page, Function)}.
 *
 * @param <T> element type exposed to the client
 */
@Getter
@Builder
public class PaginationResponse<T> {

    private final List<T> content;
    private final int page;
    private final int size;
    private final long totalElements;
    private final int totalPages;
    private final boolean first;
    private final boolean last;
    private final boolean empty;

    public static <T> PaginationResponse<T> from(Page<T> page) {
        return PaginationResponse.<T>builder()
                .content(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .empty(page.isEmpty())
                .build();
    }

    public static <E, T> PaginationResponse<T> from(Page<E> page, Function<? super E, ? extends T> mapper) {
        return from(page.map(mapper));
    }
}
