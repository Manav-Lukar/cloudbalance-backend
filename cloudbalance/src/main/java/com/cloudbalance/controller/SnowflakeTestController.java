package com.cloudbalance.controller;

import com.cloudbalance.dto.ColumnResponse;
import com.cloudbalance.dto.DynamicCostRequest;
import com.cloudbalance.entity.Columns;
import com.cloudbalance.service.CostExplorerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/snowflake")
public class SnowflakeTestController {

    private final CostExplorerService costExplorerService;




    @PostMapping("/dynamic-cost-data")
    public ResponseEntity<List<Map<String, Object>>> getDynamicCostData(
            @RequestBody DynamicCostRequest request) {
        List<Map<String, Object>> result = costExplorerService.fetchDynamicCostData(
                request // Pass the entire request object
        );

        return ResponseEntity.ok(result);
    }

    @GetMapping("/groupby")
    public List<ColumnResponse> getAllColumns() {
        List<Columns> columnsList = costExplorerService.getAllColumns();
        return columnsList.stream()
                .map(c -> new ColumnResponse(c.getDisplayName(), c.getActualName()))
                .collect(Collectors.toList());
    }

    @GetMapping("/filters")
    public ResponseEntity<Map<String, List<String>>> getFilterValuesForAllGroupByColumns() {
        Map<String, List<String>> filterValues = costExplorerService.getFilterValuesForAllGroupByColumns();
        return ResponseEntity.ok(filterValues);
    }
    @GetMapping("/filters/{groupBy}")
    public ResponseEntity<List<String>> getFilterValuesForGroupBy(@PathVariable String groupBy) {
        List<String> filters = costExplorerService.getFilterValuesForGroupBy(groupBy);
        return ResponseEntity.ok(filters);
    }

}
