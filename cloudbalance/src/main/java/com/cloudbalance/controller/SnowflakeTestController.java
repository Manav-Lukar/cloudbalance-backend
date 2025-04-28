package com.cloudbalance.controller;

import com.cloudbalance.dto.ColumnResponse;
import com.cloudbalance.dto.DynamicCostRequest;
import com.cloudbalance.entity.Columns;
import com.cloudbalance.entity.GroupBy;
import com.cloudbalance.repository.ColumnRepository;
import com.cloudbalance.service.SnowflakeCostExplorerService;
import com.cloudbalance.service.SnowflakeCostExplorerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/snowflake")
public class SnowflakeTestController {

    private final SnowflakeCostExplorerService snowflakeCostExplorerService;

//    @GetMapping("/snowflake/test")
//    public String testSnowflakeConnection() {
//        return snowflakeService.getCurrentSnowflakeVersion();
//    }
//
//    @GetMapping("/snowflake/usage-summary")
//    public String getLinkedAccountUsage() {
//        return snowflakeService.getLinkedAccountUsageSummary();
//    }
//
//    @GetMapping("/snowflake/full-test")
//    public String runWarehouseTableQuery() {
//        return snowflakeService.runMultiStepWarehouseQuery();
//    }
//
//    // ✅ New Endpoint: Get All Unique Linked Account IDs
//    @GetMapping("/snowflake/linked-accounts")
//    public List<Long> getAllLinkedAccountIds() {
//        return snowflakeService.getAllLinkedAccountIds();
//    }
//
//    @GetMapping("/api/usage-summary")
//    public ResponseEntity<Map<String, Object>> getUsageSummary(@RequestParam Long accountId) {
//        List<Double> totalUsage = snowflakeService.getUsageAmountsForAccount(accountId);
//        Map<String, Object> response = new HashMap<>();
//        response.put("totalUsage", totalUsage); // Returning the total usage in the response
//
//        return ResponseEntity.ok(response);
//    }

//    @PostMapping("/dynamic-data")
//    public ResponseEntity<List<Map<String, Object>>> getDynamicCostData(
//            @RequestBody DynamicCostRequest request,
//            @RequestParam String groupBy) {
//
//        String startDate = request.getStartDate();
//        String endDate = request.getEndDate();
//        String accountId = request.getAccountId();
//        Map<String, Object> filters = request.getFilters();
//
//        List<Map<String, Object>> result = snowflakeService.fetchDynamicData(startDate, endDate, accountId, filters);
//        return ResponseEntity.ok(result);
//    }

    @PostMapping("/dynamic-cost-data")
    public ResponseEntity<List<Map<String, Object>>> getDynamicCostData(
            @RequestBody DynamicCostRequest request) {

        // Ensure the service method is updated to handle the full request properly
        List<Map<String, Object>> result = snowflakeCostExplorerService.fetchDynamicCostData(
                request // Pass the entire request object
        );

        return ResponseEntity.ok(result);
    }

    @GetMapping("/all")
    public List<ColumnResponse> getAllColumns() {
        List<Columns> columnsList = snowflakeCostExplorerService.getAllColumns();
        return columnsList.stream()
                .map(c -> new ColumnResponse(c.getDisplayName(), c.getActualName()))
                .collect(Collectors.toList());
    }

}
