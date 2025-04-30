package com.cloudbalance.service;

import com.cloudbalance.dto.DynamicCostRequest;
import com.cloudbalance.entity.Columns;
import com.cloudbalance.repository.ColumnRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.sql.Date;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j

public class SnowflakeCostExplorerService {

    private final Connection snowflakeConnection;
    private final ColumnRepository columnRepository;

    public List<Map<String, Object>> fetchDynamicCostData(DynamicCostRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }
        if (request.getStartDate() == null || request.getEndDate() == null) {
            throw new IllegalArgumentException("Start date and end date must not be null");
        }

        List<Map<String, Object>> results = new ArrayList<>();
        List<Object> params = new ArrayList<>();

        String startDateFormatted = request.getStartDate() + "-01";
        String endDateFormatted = request.getEndDate() + "-31";

        List<Columns> columnsList = getAllColumns();
        Map<String, String> displayToActualMap = new HashMap<>();
        for (Columns col : columnsList) {
            displayToActualMap.put(col.getDisplayName().toUpperCase(), col.getActualName());
        }

        String actualGroupBy = null;

        if (request.getGroupBy() != null && !request.getGroupBy().isEmpty()) {
            actualGroupBy = displayToActualMap.get(request.getGroupBy().toUpperCase());
            if (actualGroupBy == null) {
                throw new RuntimeException("Group By field not found in Columns table: " + request.getGroupBy());
            }
        }

        StringBuilder query = new StringBuilder();
        query.append("SELECT ");

        if (actualGroupBy != null) {
            query.append(actualGroupBy).append(", ");
        } else {
            query.append("'' AS GROUP_BY, ");
        }

        query.append("SUM(LINEITEM_UNBLENDEDCOST) AS TOTAL_USAGE_COST ")
                .append("FROM COST_EXPLORER ")
                .append("WHERE USAGESTARTDATE BETWEEN ? AND ? ");

        params.add(Date.valueOf(startDateFormatted));
        params.add(Date.valueOf(endDateFormatted));

        if (request.getAccountId() != null && !request.getAccountId().isEmpty()) {
            query.append("AND LINKEDACCOUNTID = ? ");
            params.add(request.getAccountId());
        }

        if (request.getFilters() != null && !request.getFilters().isEmpty()) {
            for (Map.Entry<String, Object> entry : request.getFilters().entrySet()) {
                String displayKey = entry.getKey().toUpperCase();
                Object value = entry.getValue();
                String actualKey = displayToActualMap.get(displayKey);

                if (actualKey == null) {
                    throw new RuntimeException("Filter field not found in Columns table: " + displayKey);
                }

                if (value instanceof List<?> list && !list.isEmpty()) {
                    query.append("AND ").append(actualKey).append(" IN (")
                            .append(String.join(", ", Collections.nCopies(list.size(), "?")))
                            .append(") ");
                    params.addAll(list);
                } else if (value != null) {
                    query.append("AND ").append(actualKey).append(" = ? ");
                    params.add(value);
                }
            }
        }

        if (actualGroupBy != null) {
            query.append("GROUP BY ").append(actualGroupBy).append(" ");
        }

        query.append("ORDER BY TOTAL_USAGE_COST DESC");

        log.info("Generated Snowflake query: {}", query);

        try (PreparedStatement stmt = snowflakeConnection.prepareStatement(query.toString())) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                ResultSetMetaData meta = rs.getMetaData();
                int colCount = meta.getColumnCount();

                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    for (int i = 1; i <= colCount; i++) {
                        row.put(meta.getColumnLabel(i), rs.getObject(i));
                    }
                    results.add(row);
                }
            }
        } catch (Exception e) {
            log.error("Error fetching dynamic Snowflake cost data: ", e);
            throw new RuntimeException("Error executing Snowflake query: " + e.getMessage(), e);
        }

        if (results.isEmpty()) {
            return results;
        }

        results.sort((a, b) -> {
            Double costB = getDoubleSafely(b.get("TOTAL_USAGE_COST"));
            Double costA = getDoubleSafely(a.get("TOTAL_USAGE_COST"));
            return Double.compare(costB, costA);
        });

        List<Map<String, Object>> finalResult = new ArrayList<>();
        double othersSum = 0.0;

        for (int i = 0; i < results.size(); i++) {
            if (i < 5) {
                Map<String, Object> result = new LinkedHashMap<>();
                if (actualGroupBy != null) {
                    result.put(request.getGroupBy(), results.get(i).get(actualGroupBy));
                } else {
                    result.put("Group", "");
                }
                result.put("Total Usage", getDoubleSafely(results.get(i).get("TOTAL_USAGE_COST")));
                finalResult.add(result);
            } else {
                othersSum += getDoubleSafely(results.get(i).get("TOTAL_USAGE_COST"));
            }
        }

        if (results.size() > 5) {
            Map<String, Object> othersEntry = new LinkedHashMap<>();
            othersEntry.put(request.getGroupBy() != null ? request.getGroupBy() : "Group", "Others");
            othersEntry.put("Total Usage", othersSum);
            finalResult.add(othersEntry);
        }

        return finalResult;
    }

    private Double getDoubleSafely(Object value) {
        if (value == null) return 0.0;
        if (value instanceof Number) return ((Number) value).doubleValue();
        try {
            return Double.parseDouble(value.toString());
        } catch (Exception e) {
            return 0.0;
        }
    }

    public List<Columns> getAllColumns() {
        return columnRepository.findAll();
    }

    public Map<String, List<String>> getFilterValuesForAllGroupByColumns() {
        Map<String, List<String>> filterMap = new LinkedHashMap<>();
        List<Columns> columnsList = getAllColumns();

        for (Columns column : columnsList) {
            String displayName = column.getDisplayName();
            String actualName = column.getActualName();
            String query = "SELECT DISTINCT " + actualName + " FROM COST_EXPLORER WHERE " + actualName + " IS NOT NULL";

            try (PreparedStatement stmt = snowflakeConnection.prepareStatement(query);
                 ResultSet rs = stmt.executeQuery()) {

                List<String> values = new ArrayList<>();
                while (rs.next()) {
                    Object val = rs.getObject(1);
                    if (val != null) {
                        values.add(val.toString());
                    }
                }
                filterMap.put(displayName, values);

            } catch (SQLException e) {
                log.error("Error fetching filter values for column: {}", actualName, e);
                // You can choose to throw or continue with other columns
            }
        }

        return filterMap;
    }
    public List<String> getFilterValuesForGroupBy(String groupByDisplayName) {
        if (groupByDisplayName == null || groupByDisplayName.trim().isEmpty()) {
            throw new IllegalArgumentException("GroupBy display name cannot be null or empty");
        }

        List<Columns> columnsList = getAllColumns();
        Optional<Columns> column = columnsList.stream()
                .filter(c -> c.getDisplayName().equalsIgnoreCase(groupByDisplayName))
                .findFirst();

        if (column.isEmpty()) {
            throw new RuntimeException("GroupBy column not found: " + groupByDisplayName);
        }

        String actualName = column.get().getActualName();
        String query = "SELECT DISTINCT " + actualName + " FROM COST_EXPLORER WHERE " + actualName + " IS NOT NULL";

        List<String> values = new ArrayList<>();
        try (PreparedStatement stmt = snowflakeConnection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Object val = rs.getObject(1);
                if (val != null) {
                    values.add(val.toString());
                }
            }
        } catch (SQLException e) {
            log.error("Error fetching filter values for column: {}", actualName, e);
            throw new RuntimeException("Error fetching filters for: " + groupByDisplayName, e);
        }

        return values;
    }


}