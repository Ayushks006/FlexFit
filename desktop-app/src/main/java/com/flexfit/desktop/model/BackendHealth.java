package com.flexfit.desktop.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.Instant;
import java.util.Map;

/**
 * Client-side model representing the health response received from the FlexFit Backend.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BackendHealth {
    private String status;
    private String service;
    private String version;
    private Instant timestamp;
    private String databaseStatus;
    private String mlServiceStatus;
    private Map<String, Object> systemDetails;

    public BackendHealth() {
    }

    public BackendHealth(String status, String service, String version, Instant timestamp,
                         String databaseStatus, String mlServiceStatus, Map<String, Object> systemDetails) {
        this.status = status;
        this.service = service;
        this.version = version;
        this.timestamp = timestamp;
        this.databaseStatus = databaseStatus;
        this.mlServiceStatus = mlServiceStatus;
        this.systemDetails = systemDetails;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public String getDatabaseStatus() {
        return databaseStatus;
    }

    public void setDatabaseStatus(String databaseStatus) {
        this.databaseStatus = databaseStatus;
    }

    public String getMlServiceStatus() {
        return mlServiceStatus;
    }

    public void setMlServiceStatus(String mlServiceStatus) {
        this.mlServiceStatus = mlServiceStatus;
    }

    public Map<String, Object> getSystemDetails() {
        return systemDetails;
    }

    public void setSystemDetails(Map<String, Object> systemDetails) {
        this.systemDetails = systemDetails;
    }
}
