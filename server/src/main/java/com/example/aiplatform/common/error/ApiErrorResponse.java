package com.example.aiplatform.common.error;

import java.util.Map;

public record ApiErrorResponse(
        String code,
        String message,
        Map<String, String> details,
        String traceId
) {
}
