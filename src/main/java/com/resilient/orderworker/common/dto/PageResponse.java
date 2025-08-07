/*
 * Copyright (c) 2025 Resilient Order Enricher
 *
 * Licensed under the MIT License.
 */
package com.resilient.orderworker.common.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Generic paginated response wrapper for API endpoints.
 *
 * <p>This record provides a consistent structure for paginated responses across all API endpoints.
 * It includes the actual data, pagination metadata, and navigation information.
 *
 * @param <T> Type of the content items
 * @param content List of items for the current page
 * @param page Current page number (0-based)
 * @param size Number of items per page
 * @param totalElements Total number of items across all pages
 * @param totalPages Total number of pages
 * @param first Whether this is the first page
 * @param last Whether this is the last page
 * @param hasNext Whether there is a next page
 * @param hasPrevious Whether there is a previous page
 * @author Resilient Order Enricher Team
 * @since 1.0.0
 */
@Schema(
        description = "Paginated response wrapper with metadata",
        example =
                """
                {
                  "content": [...],
                  "page": 0,
                  "size": 20,
                  "totalElements": 150,
                  "totalPages": 8,
                  "first": true,
                  "last": false,
                  "hasNext": true,
                  "hasPrevious": false
                }
                """)
public record PageResponse<T>(
        @Schema(description = "List of items for the current page") @JsonProperty("content")
                List<T> content,
        @Schema(description = "Current page number (0-based)", example = "0", minimum = "0")
                @JsonProperty("page")
                int page,
        @Schema(
                        description = "Number of items per page",
                        example = "20",
                        minimum = "1",
                        maximum = "100")
                @JsonProperty("size")
                int size,
        @Schema(
                        description = "Total number of items across all pages",
                        example = "150",
                        minimum = "0")
                @JsonProperty("totalElements")
                long totalElements,
        @Schema(description = "Total number of pages", example = "8", minimum = "0")
                @JsonProperty("totalPages")
                int totalPages,
        @Schema(description = "Whether this is the first page", example = "true")
                @JsonProperty("first")
                boolean first,
        @Schema(description = "Whether this is the last page", example = "false")
                @JsonProperty("last")
                boolean last,
        @Schema(description = "Whether there is a next page", example = "true")
                @JsonProperty("hasNext")
                boolean hasNext,
        @Schema(description = "Whether there is a previous page", example = "false")
                @JsonProperty("hasPrevious")
                boolean hasPrevious) {

    /**
     * Creates a PageResponse from content and pagination metadata.
     *
     * @param <T> Type of the content items
     * @param content List of items for the current page
     * @param page Current page number (0-based)
     * @param size Number of items per page
     * @param totalElements Total number of items across all pages
     * @return PageResponse with calculated metadata
     */
    public static <T> PageResponse<T> of(List<T> content, int page, int size, long totalElements) {
        int totalPages = (int) Math.ceil((double) totalElements / size);
        boolean first = page == 0;
        boolean last = page >= totalPages - 1;
        boolean hasNext = page < totalPages - 1;
        boolean hasPrevious = page > 0;

        return new PageResponse<>(
                content, page, size, totalElements, totalPages, first, last, hasNext, hasPrevious);
    }

    /**
     * Creates an empty PageResponse.
     *
     * @param <T> Type of the content items
     * @param page Current page number
     * @param size Number of items per page
     * @return Empty PageResponse
     */
    public static <T> PageResponse<T> empty(int page, int size) {
        return of(List.of(), page, size, 0L);
    }
}
