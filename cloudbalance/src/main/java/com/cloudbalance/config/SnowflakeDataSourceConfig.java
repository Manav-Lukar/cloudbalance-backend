package com.cloudbalance.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Configuration
@Slf4j
public class SnowflakeDataSourceConfig {

    @Value("${snowflake.url}")
    private String url;

    @Value("${snowflake.username}")
    private String username;

    @Value("${snowflake.password}")
    private String password;

//    @Value("${snowflake.driver-class-name}")
//    private String driverClassName;

//    @Bean(name = "snowflakeDataSource")
//    public DataSource snowflakeDataSource() {
//        DriverManagerDataSource dataSource = new DriverManagerDataSource();
//        dataSource.setUrl(url);
//        dataSource.setUsername(username);
//        dataSource.setPassword(password);
////        dataSource.setDriverClassName(driverClassName);
//        return dataSource;
//    }

    //
//    @Bean(name = "snowflakeJdbcTemplate")
//    public JdbcTemplate snowflakeJdbcTemplate(@Qualifier("snowflakeDataSource") DataSource snowflakeDataSource) {
//        return new JdbcTemplate(snowflakeDataSource);
//    }
    @Bean
    public Connection snowflakeConnection() throws SQLException {
        log.info("Making bean for snowflake");
//        String url = String.format(datasource);
        return DriverManager.getConnection(url,
                username, password);
    }

}
