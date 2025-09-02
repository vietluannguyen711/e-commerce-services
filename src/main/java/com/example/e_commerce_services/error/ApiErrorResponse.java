package com.example.e_commerce_services.error;

import java.time.OffsetDateTime;
import java.util.Map;

public record ApiErrorResponse(
        ErrorPayload error
        ) {

    public static ApiErrorResponse of(String code, String message, Map<String, Object> details, String path) {
        return new ApiErrorResponse(new ErrorPayload(code, message, details, path, OffsetDateTime.now()));
    }

    public record ErrorPayload(
            String code,
            String message,
            Map<String, Object> details,
            String path,
            OffsetDateTime timestamp
            ) {

    }
}
