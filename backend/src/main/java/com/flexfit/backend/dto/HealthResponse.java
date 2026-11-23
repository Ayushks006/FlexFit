package com.flexfit.backend.dto;

import java.time.Instant;
import java.util.Map;

/**
 * Health response data transfer object containing operational status,
 * component statuses, and system diagnostic metadata.
 */
public record HealthResponse(
        String status,
        String service,
        String version,
        Instant timestamp,
        String databaseStatus,
        String mlServiceStatus,
        Map<String, Object> systemDetails
) {
}
