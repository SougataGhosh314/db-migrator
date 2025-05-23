package com.sougata.dbmigrator;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableBatchProcessing
@SpringBootApplication
public class DbMigratorApplication {

    public static void main(String[] args) {
        SpringApplication.run(DbMigratorApplication.class, args);
    }

}
