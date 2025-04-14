package com.cloudbalance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootApplication
public class CloudbalanceApplication {

	// Create a Logger instance
	private static final Logger logger = LoggerFactory.getLogger(CloudbalanceApplication.class);

	public static void main(String[] args) {
		// Log an informational message before starting the application
		logger.info("Starting Cloudbalance application...");

		try {
			// Run the Spring Boot application
			SpringApplication.run(CloudbalanceApplication.class, args);

			// Log an informational message after the application has started successfully
			logger.info("Cloudbalance application started successfully.");
		} catch (Exception e) {
			// Log an error message if the application fails to start
			logger.error("Error starting Cloudbalance application: ", e);
		}
	}
}
