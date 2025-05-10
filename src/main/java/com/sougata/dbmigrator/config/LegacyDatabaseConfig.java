package com.sougata.dbmigrator.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class LegacyDatabaseConfig {

    @Bean(name = "legacyDataSource")
    public DataSource legacyDataSource() {
        return DataSourceBuilder.create()
                .url("jdbc:mysql://localhost:3306/legacydb") // Replace with your MySQL URL
                .username("legacy_user") // Replace with your MySQL username
                .password("legacy_pass") // Replace with your MySQL password
                .driverClassName("com.mysql.cj.jdbc.Driver")
                .build();
    }

    @Bean(name = "legacyJdbcTemplate")
    public JdbcTemplate legacyJdbcTemplate(@Qualifier("legacyDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}