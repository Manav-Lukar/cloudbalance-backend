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


    @PostMapping("/dynamic-cost-data")
    public ResponseEntity<List<Map<String, Object>>> getDynamicCostData(
            @RequestBody DynamicCostRequest request) {
        List<Map<String, Object>> result = snowflakeCostExplorerService.fetchDynamicCostData(
                request // Pass the entire request object
        );

        return ResponseEntity.ok(result);
    }

    @GetMapping("/groupby")
    public List<ColumnResponse> getAllColumns() {
        List<Columns> columnsList = snowflakeCostExplorerService.getAllColumns();
        return columnsList.stream()
                .map(c -> new ColumnResponse(c.getDisplayName(), c.getActualName()))
                .collect(Collectors.toList());
    }

    @GetMapping("/filters")
    public ResponseEntity<Map<String, List<String>>> getFilterValuesForAllGroupByColumns() {
        Map<String, List<String>> filterValues = snowflakeCostExplorerService.getFilterValuesForAllGroupByColumns();
        return ResponseEntity.ok(filterValues);
    }
    @GetMapping("/filters/{groupBy}")
    public ResponseEntity<List<String>> getFilterValuesForGroupBy(@PathVariable String groupBy) {
        List<String> filters = snowflakeCostExplorerService.getFilterValuesForGroupBy(groupBy);
        return ResponseEntity.ok(filters);
    }

}
