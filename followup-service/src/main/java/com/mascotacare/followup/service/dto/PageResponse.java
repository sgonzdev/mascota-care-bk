package com.mascotacare.followup.service.dto;

import org.springframework.data.domain.Page;
import java.util.List;

/** DTO uniforme de paginación para evitar exponer la estructura interna de Page de Spring. */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {
    public static <T> PageResponse<T> from(Page<T> p) {
        return new PageResponse<>(p.getContent(), p.getNumber(), p.getSize(),
                p.getTotalElements(), p.getTotalPages(), p.isFirst(), p.isLast());
    }

    public <R> PageResponse<R> map(java.util.function.Function<T, R> mapper) {
        return new PageResponse<>(
                content.stream().map(mapper).toList(),
                page, size, totalElements, totalPages, first, last);
    }
}
