package com.sougata.dbmigrator.controller;

import com.sougata.dbmigrator.util.LegacyDbSeeder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class MigrationController {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    @Qualifier("userMigrationJob")
    private Job userMigrationJob; // Inject the Job bean

    @Autowired
    LegacyDbSeeder legacyDbSeeder;

    @PostMapping("/seed-legacy_db")
    public ResponseEntity<Object> seedLegacyDB(@RequestParam int totalUsers) {
        try {
            legacyDbSeeder.run(totalUsers);
            return ResponseEntity.ok("Users created in legacy DB");
        } catch (Exception e) {
            log.error("Error occured seeding users: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("There was an issue seeding users.");
        }
    }

    @PostMapping("/migrate-users")
    public ResponseEntity<String> migrateUsers() {
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis()) // Add a unique parameter
                    .toJobParameters();
            jobLauncher.run(userMigrationJob, jobParameters);
            return ResponseEntity.ok("User migration job submitted successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error submitting user migration job: " + e.getMessage());
        }
    }
}
