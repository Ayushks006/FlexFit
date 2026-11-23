package com.flexfit.backend.service;

import com.flexfit.backend.dto.HealthResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.lang.management.ManagementFactory;
import java.sql.Connection;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Health verification service checking backend operational status,
 * database connectivity, and JVM environment metrics.
 */
@Service
public class HealthService {

    private static final Logger logger = LoggerFactory.getLogger(HealthService.class);

    @Value("${spring.application.name:flexfit-backend}")
    private String applicationName;

    @Value("${flexfit.api.version:v1}")
    private String apiVersion;

    private final DataSource dataSource;

    @Autowired
    public HealthService(@Autowired(required = false) DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public HealthResponse getSystemHealth() {
        String dbStatus = checkDatabaseConnectivity();
        String mlStatus = "STANDBY (Phase 2 Integration)";

        Map<String, Object> systemDetails = new LinkedHashMap<>();
        systemDetails.put("javaVersion", System.getProperty("java.version"));
        systemDetails.put("osName", System.getProperty("os.name"));
        systemDetails.put("osArch", System.getProperty("os.arch"));
        systemDetails.put("availableProcessors", Runtime.getRuntime().availableProcessors());
        systemDetails.put("freeMemoryMb", Runtime.getRuntime().freeMemory() / (1024 * 1024));
        systemDetails.put("totalMemoryMb", Runtime.getRuntime().totalMemory() / (1024 * 1024));
        systemDetails.put("jvmUptimeSeconds", ManagementFactory.getRuntimeMXBean().getUptime() / 1000);

        return new HealthResponse(
                "UP",
                applicationName,
                "1.0.0-SNAPSHOT (" + apiVersion + ")",
                Instant.now(),
                dbStatus,
                mlStatus,
                systemDetails
        );
    }

    private String checkDatabaseConnectivity() {
        if (dataSource == null) {
            return "NOT_CONFIGURED";
        }
        try (Connection connection = dataSource.getConnection()) {
            boolean valid = connection.isValid(2);
            return valid ? "CONNECTED" : "UNREACHABLE";
        } catch (Exception ex) {
            logger.warn("Database connection check failed: {}", ex.getMessage());
            return "UNAVAILABLE (Verify MySQL Service & Environment Variables)";
        }
    }
}
