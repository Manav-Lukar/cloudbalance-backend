package com.cloudbalance.Exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class CloudAccountException extends RuntimeException {
    public CloudAccountException(String message) {
        super(message);
    }

    public CloudAccountException(String cloudProvider, String operation, String reason) {
        super(String.format("Cloud account operation failed for %s during %s: %s", cloudProvider, operation, reason));
    }
}