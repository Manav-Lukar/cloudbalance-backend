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
        List<Map<String, Object>> results = new ArrayList<>();
        List<Object> params = new ArrayList<>();

        // Format start and end date
        String startDateFormatted = request.getStartDate() + "-01";
        String endDateFormatted = request.getEndDate() + "-31";

        // Fetch actual group by column name from MySQL if provided
        // Initialize actualGroupBy to null
        String actualGroupBy = null;

// Check if groupBy is provided in the request
        if (request.getGroupBy() != null && !request.getGroupBy().isEmpty()) {

            // Retrieve all columns
            List<Columns> columnsList = getAllColumns();

            // Try to find the column with the displayName matching the groupBy
            Optional<Columns> column = columnsList.stream()
                    .filter(c -> c.getDisplayName().equalsIgnoreCase(request.getGroupBy()))
                    .findFirst();

            // If the column is found, get the actualName, else throw an exception
            if (column.isPresent()) {
                actualGroupBy = column.get().getActualName();
            } else {
                throw new RuntimeException("Group By field not found in Columns table: " + request.getGroupBy());
            }
        }

// Now actualGroupBy will have the value or an exception will be thrown if not found


        StringBuilder query = new StringBuilder();
        query.append("SELECT ")
                .append("PRODUCT_PRODUCTNAME, ");

        if (actualGroupBy != null) {
            query.append(actualGroupBy).append(", ");
        } else {
            query.append("'' AS LINEITEM_USAGETYPE, ");
        }

        query.append("SUM(LINEITEM_UNBLENDEDCOST) AS TOTAL_USAGE_COST ")
                .append("FROM COST_EXPLORER ")
                .append("WHERE USAGESTARTDATE BETWEEN ? AND ? ");

        // Add date params
        params.add(Date.valueOf(startDateFormatted));
        params.add(Date.valueOf(endDateFormatted));

        // Account ID filter
        if (request.getAccountId() != null && !request.getAccountId().isEmpty()) {
            query.append("AND LINKEDACCOUNTID = ? ");
            params.add(request.getAccountId());
        }

        // Additional filters
        if (request.getFilters() != null) {
            for (Map.Entry<String, Object> entry : request.getFilters().entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();

                if (value instanceof List<?> list && !list.isEmpty()) {
                    query.append("AND ").append(key).append(" IN (")
                            .append(String.join(", ", Collections.nCopies(list.size(), "?")))
                            .append(") ");
                    params.addAll(list);
                } else {
                    query.append("AND ").append(key).append(" = ? ");
                    params.add(value);
                }
            }
        }

        // Group By
        if (actualGroupBy != null) {
            query.append("GROUP BY PRODUCT_PRODUCTNAME, ").append(actualGroupBy).append(" ");
        } else {
            query.append("GROUP BY PRODUCT_PRODUCTNAME ");
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

        // ===== Process top 5 + Others =====
        if (results.isEmpty()) {
            return results;
        }

        // Sort descending
        results.sort((a, b) -> {
            Double costB = getDoubleSafely(b.get("TOTAL_USAGE_COST"));
            Double costA = getDoubleSafely(a.get("TOTAL_USAGE_COST"));
            return Double.compare(costB, costA);
        });

        List<Map<String, Object>> finalResult = new ArrayList<>();
        double othersSum = 0.0;

        for (int i = 0; i < results.size(); i++) {
            if (i < 5) {
                finalResult.add(results.get(i));
            } else {
                othersSum += getDoubleSafely(results.get(i).get("TOTAL_USAGE_COST"));
            }
        }

        if (results.size() > 5) {
            Map<String, Object> othersEntry = new LinkedHashMap<>();
            othersEntry.put("PRODUCT_PRODUCTNAME", "Others");

            // Handle group by field if present
            if (request.getGroupBy() != null && !request.getGroupBy().isEmpty()) {
                othersEntry.put(actualGroupBy != null ? actualGroupBy : request.getGroupBy(), "Others");
            }

            othersEntry.put("TOTAL_USAGE_COST", othersSum);
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

}
