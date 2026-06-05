/*
 * Copyright (c) 2025 Resilient Order Enricher
 *
 * Licensed under the MIT License.
 */
package com.resilient.orderworker.infrastructure.adapter.in.rest;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.resilient.orderworker.application.port.in.QueryOrdersUseCase;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Paginated response wrapper")
public record PageResponse<T>(
        @JsonProperty("content") List<T> content,
        @JsonProperty("page") int page,
        @JsonProperty("size") int size,
        @JsonProperty("totalElements") long totalElements,
        @JsonProperty("totalPages") int totalPages,
        @JsonProperty("first") boolean first,
        @JsonProperty("last") boolean last) {

    public static <D, R> PageResponse<R> from(
            QueryOrdersUseCase.Page<D> page, java.util.function.Function<D, R> mapper) {
        return new PageResponse<>(
                page.content().stream().map(mapper).toList(),
                page.page(),
                page.size(),
                page.totalElements(),
                page.totalPages(),
                page.first(),
                page.last());
    }
}
