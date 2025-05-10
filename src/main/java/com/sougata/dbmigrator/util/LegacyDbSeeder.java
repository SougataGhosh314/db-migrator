package com.sougata.dbmigrator.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class LegacyDbSeeder {

    @Autowired
    @Qualifier("legacyJdbcTemplate")
    private JdbcTemplate legacyJdbcTemplate;

    public void run(int totalUsers) {
        if (totalUsers < 10000)
            runSmall(totalUsers);
        else runLarge(totalUsers);
    }

    public void runSmall(int totalUsers) {
        for (int i = 1; i <= totalUsers; i++) {
            String username = "user" + i;
            String email = "user" + i + "@example.com";
            String fullName = "User " + i;
            legacyJdbcTemplate.update(
                    "INSERT INTO users (username, email, full_name, created_at) VALUES (?, ?, ?, NOW())",
                    username, email, fullName
            );
        }

        System.out.println("Inserted " + totalUsers + " users into legacydb.");
    }

    public void runLarge(int totalUsers) {
        int batchSize = 5000;
        List<Object[]> batchArgs = new ArrayList<>(batchSize);

        for (int i = 1; i <= totalUsers; i++) {
            String username = "User" + i;
            String email = "user" + i + "@example.com";
            String fullName = "User " + i;
            batchArgs.add(new Object[]{username, email, fullName});

            if (i % batchSize == 0 || i == totalUsers) {
                legacyJdbcTemplate.batchUpdate(
                        "INSERT INTO users (username, email, full_name, created_at) VALUES (?, ?, ?, NOW())",
                        batchArgs
                );
                batchArgs.clear(); // Reset for next batch
            }
        }

        System.out.println("Inserted " + totalUsers + " users into legacydb.");
    }


}
