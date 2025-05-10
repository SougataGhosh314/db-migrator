package com.sougata.dbmigrator.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = "com.sougata.dbmigrator.model",
        entityManagerFactoryRef = "newEntityManagerFactory",
        transactionManagerRef = "newTransactionManager"
)
public class NewDatabaseConfig {

    @Primary
    @Bean(name = "newDataSource")
    public DataSource newDataSource() {
        return DataSourceBuilder.create()
                .url("jdbc:postgresql://localhost:5432/newdb") // Replace with your PostgreSQL URL
                .username("postgres") // Replace with your PostgreSQL username
                .password("admin") // Replace with your PostgreSQL password
                .driverClassName("org.postgresql.Driver")
                .build();
    }

    @Primary
    @Bean(name = "newEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean newEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("newDataSource") DataSource dataSource
    ) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", "update"); // Or "create-drop", "validate" as needed
        return builder
                .dataSource(dataSource)
                .packages("com.sougata.dbmigrator.model")
                .persistenceUnit("newdb")
                .properties(properties)
                .build();
    }

    @Primary
    @Bean(name = {"newTransactionManager", "transactionManager"})
    public PlatformTransactionManager newTransactionManager(
            @Qualifier("newEntityManagerFactory") jakarta.persistence.EntityManagerFactory entityManagerFactory
    ) {
        return new JpaTransactionManager(entityManagerFactory);
    }

    @Bean(name = "dataSource")
    @ConfigurationProperties("spring.datasource")
    public DataSource dataSource() {
        return DataSourceBuilder.create().build();
    }

}