package com.cloudbalance.controller;

import com.cloudbalance.entity.GroupBy;
import com.cloudbalance.service.SnowflakeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class SnowflakeTestController {

    private final SnowflakeService snowflakeService;

    @GetMapping("/snowflake/test")
    public String testSnowflakeConnection() {
        return snowflakeService.getCurrentSnowflakeVersion();
    }

    @GetMapping("/snowflake/usage-summary")
    public String getLinkedAccountUsage() {
        return snowflakeService.getLinkedAccountUsageSummary();
    }

    @GetMapping("/snowflake/full-test")
    public String runWarehouseTableQuery() {
        return snowflakeService.runMultiStepWarehouseQuery();
    }

    // ✅ New Endpoint: Get All Unique Linked Account IDs
    @GetMapping("/snowflake/linked-accounts")
    public List<Long> getAllLinkedAccountIds() {
        return snowflakeService.getAllLinkedAccountIds();
    }

    @GetMapping("/api/usage-summary")
    public ResponseEntity<Map<String, Object>> getUsageSummary(@RequestParam Long accountId) {
        List<Double> totalUsage = snowflakeService.getUsageAmountsForAccount(accountId);
        Map<String, Object> response = new HashMap<>();
        response.put("totalUsage", totalUsage); // Returning the total usage in the response

        return ResponseEntity.ok(response);
    }
}
