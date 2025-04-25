package com.cloudbalance.service;

import com.cloudbalance.entity.GroupBy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class SnowflakeService {

    private final Connection snowflakeConnection;

    public String getCurrentSnowflakeVersion() {
        String result = "Connection failed or no result.";
        try (Statement stmt = snowflakeConnection.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT CURRENT_VERSION()");
            if (rs.next()) {
                result = "Snowflake version: " + rs.getString(1);
            }
        } catch (Exception e) {
            log.error("Error while querying Snowflake: ", e);
            result = "Error: " + e.getMessage();
        }
        return result;
    }

    public String runMultiStepWarehouseQuery() {
        StringBuilder result = new StringBuilder();

        String[] queries = {
                "SELECT * FROM COST_EXPLORER LIMIT 605"
        };

        try (Statement stmt = snowflakeConnection.createStatement()) {
            for (String query : queries) {
                log.info("Executing: {}", query);
                boolean hasResultSet = stmt.execute(query);

                if (hasResultSet) {
                    ResultSet rs = stmt.getResultSet();
                    int columnCount = rs.getMetaData().getColumnCount();

                    while (rs.next()) {
                        for (int j = 1; j <= columnCount; j++) {
                            result.append(rs.getString(j)).append(" | ");
                        }
                        result.append("\n");
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error executing Snowflake queries", e);
            return "Error: " + e.getMessage();
        }

        return result.toString();
    }

    public String getLinkedAccountUsageSummary() {
        StringBuilder result = new StringBuilder();
        String query = "SELECT * FROM COST_EXPLORER LIMIT 15";

        try (Statement stmt = snowflakeConnection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                long accountId = rs.getLong("LINKEDACCOUNTID");
                double usage = rs.getDouble(2);
                result.append("Account: ").append(accountId)
                        .append(" | Usage: ").append(usage).append("\n");
            }
        } catch (Exception e) {
            log.error("Error executing query: ", e);
            return "Error: " + e.getMessage();
        }

        return result.toString();
    }

    public List<Long> getAllLinkedAccountIds() {
        List<Long> accountIds = new ArrayList<>();
        String query = "SELECT LINKEDACCOUNTID FROM COST_EXPLORER LIMIT 50";

        try (Statement stmt = snowflakeConnection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                accountIds.add(rs.getLong("LINKEDACCOUNTID"));
            }
        } catch (Exception e) {
            log.error("Error fetching linked account IDs: ", e);
        }

        return accountIds;
    }

    public List<Double> getUsageAmountsForAccount(Long accountId) {
        String query = """
            SELECT LINEITEM_USAGEAMOUNT 
            FROM AWS.COST.COST_EXPLORER 
            WHERE LINKEDACCOUNTID = ? 
            LIMIT 100000
        """;

        List<Double> usageAmounts = new ArrayList<>();

        try (PreparedStatement stmt = snowflakeConnection.prepareStatement(query)) {
            stmt.setLong(1, accountId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                usageAmounts.add(rs.getDouble("LINEITEM_USAGEAMOUNT"));
            }
        } catch (Exception e) {
            log.error("Error fetching usage amounts for account: ", e);
        }

        return usageAmounts;
    }

    // ✅ NEW METHOD: Dynamic data fetch with filters and grouping
    public List<Map<String, Object>> fetchDynamicData(
            String startDate,
            String endDate,
            String groupByColumn,
            Map<String, Object> filters
    ) {
        List<Map<String, Object>> results = new ArrayList<>();
        List<Object> params = new ArrayList<>();

        StringBuilder query = new StringBuilder();
        query.append("SELECT TO_CHAR(USAGESTARTDATE, 'YYYY-MM') AS USAGE_MONTH, ")
                .append(groupByColumn).append(", ")
                .append("SUM(LINEITEM_UNBLENDEDCOST) AS TOTAL_USAGE_COST ")
                .append("FROM COST_EXPLORER WHERE USAGESTARTDATE BETWEEN ? AND ? ");

        // Add date parameters
        params.add(Date.valueOf(LocalDate.parse(startDate)));
        params.add(Date.valueOf(LocalDate.parse(endDate)));

        // Dynamically add filters to the query
        for (Map.Entry<String, Object> entry : filters.entrySet()) {
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

        query.append("GROUP BY TO_CHAR(USAGESTARTDATE, 'YYYY-MM'), ")
                .append(groupByColumn)
                .append(" ORDER BY USAGE_MONTH, TOTAL_USAGE_COST DESC");

        log.info("Generated query: {}", query.toString());
        log.info("Binding parameters: {}", params);

        try (PreparedStatement stmt = snowflakeConnection.prepareStatement(query.toString())) {
            // Bind the parameters to the query
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
            log.error("Snowflake dynamic data fetch failed", e);
            throw new RuntimeException("Snowflake query failed: " + e.getMessage(), e);
        }

        log.info("Number of rows fetched: {}", results.size());
        return results;
    }
}
