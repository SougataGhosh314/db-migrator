package com.sougata.dbmigrator.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
        System.out.println("Using runLarge() to seed");
        int batchSize = 1000; // safer for multi-row SQL insert
        int numThreads = 4;   // tune based on CPU cores

        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        CountDownLatch latch = new CountDownLatch((totalUsers + batchSize - 1) / batchSize);

        for (int start = 1; start <= totalUsers; start += batchSize) {
            final int batchStart = start;
            final int batchEnd = Math.min(start + batchSize - 1, totalUsers);

            executor.submit(() -> {
                try {
                    insertBatch(batchStart, batchEnd);
                } finally {
                    latch.countDown();
                }
            });
        }

        try {
            latch.await(); // wait for all batches
            executor.shutdown();
            System.out.println("Inserted " + totalUsers + " users into legacydb.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Seeding interrupted", e);
        }
    }

    private void insertBatch(int startId, int endId) {
        StringBuilder sql = new StringBuilder("INSERT INTO users (username, email, full_name, created_at) VALUES ");
        List<Object> args = new ArrayList<>((endId - startId + 1) * 3);

        for (int i = startId; i <= endId; i++) {
            if (i > startId) sql.append(", ");
            sql.append("(?, ?, ?, NOW())");
            String username = "user" + i;
            args.add(username);
            args.add(username + "@example.com");
            args.add("User " + i);
        }

        legacyJdbcTemplate.update(sql.toString(), args.toArray());
    }

}
