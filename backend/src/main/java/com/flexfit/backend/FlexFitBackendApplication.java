package com.flexfit.backend;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * FlexFit Backend Application
 * Provides REST API services, business logic, authentication,
 * and database communication for the FlexFit ecosystem.
 */
@SpringBootApplication
public class FlexFitBackendApplication {

    private static final Logger logger = LoggerFactory.getLogger(FlexFitBackendApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(FlexFitBackendApplication.class, args);
        logger.info("=================================================");
        logger.info("  FlexFit Backend Server Started Successfully!   ");
        logger.info("  Health endpoint: http://localhost:8080/api/v1/health ");
        logger.info("=================================================");
    }
}
