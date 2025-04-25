package com.cloudbalance.service;

import com.cloudbalance.entity.GroupBy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
////                "SHOW WAREHOUSES",
////                "USE WAREHOUSE COMPUTE_WH",
//                "SHOW TABLES",
                "SELECT * FROM COST_EXPLORER LIMIT 605"
        };

        try (Statement stmt = snowflakeConnection.createStatement()) {
            for (int i = 0; i < queries.length; i++) {
                String query = queries[i];
                log.info("Executing: {}", query);
                boolean hasResultSet = stmt.execute(query);

                // Only process SELECT result (last query)
                if (i == queries.length - 1 && hasResultSet) {
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
        String query = """
        SELECT * 
        FROM COST_EXPLORER LIMIT 15
        
    """;

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
        String query = "SELECT LINKEDACCOUNTID FROM COST_EXPLORER limit 50";

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
        String query = "SELECT LINEITEM_USAGEAMOUNT " +
                "FROM AWS.COST.COST_EXPLORER " +
                "WHERE LINKEDACCOUNTID = ? " +
                "LIMIT 100000";

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



}
