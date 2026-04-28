package com.faisal.dev.atsanalyzer.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        boolean success,
        String message,
        T data,
        LocalDateTime timestamp
) {

    public static <T> ApiResponse<T> success(
            String message,
            T data
    ) {

        return new ApiResponse<>(
                true,
                message,
                data,
                LocalDateTime.now()
        );
    }

    public static <T> ApiResponse<T> failure(
            String message,
            T data
    ) {

        return new ApiResponse<>(
                false,
                message,
                data,
                LocalDateTime.now()
        );
    }

    public static ApiResponse<Void> failure(
            String message
    ) {

        return failure(message, null);
    }
}
