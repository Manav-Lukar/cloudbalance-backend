package com.cloudbalance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootApplication
public class CloudbalanceApplication {

	private static final Logger logger = LoggerFactory.getLogger(CloudbalanceApplication.class);

	public static void main(String[] args) {
		logger.info("Starting Cloudbalance application...");
		SpringApplication.run(CloudbalanceApplication.class, args);
	}
}
