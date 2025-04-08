package com.cloudbalance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
public class CloudbalanceApplication {

	public static void main(String[] args) {
		SpringApplication.run(CloudbalanceApplication.class, args);
	}

}
