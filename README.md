# Spring Batch User Migrator

## Overview

This project is a high-performance **DB-to-DB user migration tool** built using **Spring Batch**. It migrates users from a legacy MySQL database to a new PostgreSQL database efficiently and reliably.

It also includes a fast seeder for generating synthetic data in the legacy database to simulate real-world loads.

---

## Features

* **Spring Batch-powered job configuration**
* **Efficient seeding** of synthetic users into MySQL
* **Optimized data migration** using paging, batching, and multithreading
* Supports large-scale migrations (tested on 110,000+ users)
* Easy to extend for custom ETL logic

---

## Data Flow

1. **Seeder**: Populates the legacy MySQL database with test user data using JDBC batch insertions.
2. **Reader**: Uses `JdbcPagingItemReader` to read users page-by-page from MySQL.
3. **Processor**: Converts `LegacyUser` to `NewUser` (maps field names and types).
4. **Writer**: Uses `JdbcBatchItemWriter` to write the records in bulk to PostgreSQL.

---

## Legacy User Schema (MySQL)

```sql
CREATE TABLE users (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(255),
  email VARCHAR(255),
  full_name VARCHAR(255),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## New User Schema (PostgreSQL)

```sql
CREATE TABLE users (
  userid BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
  email VARCHAR(255),
  name VARCHAR(255),
  registered_on TIMESTAMP
);
```

---

## Seeder (LegacyDbSeeder.java)

### Optimizations

* Uses multithreaded row insertions
* Minimal SQL parsing overhead
* Reduces round-trips by inserting in **batches of 1000**
* Avoids object creation per row (reuses batch args list)

### Performance

* Seeding 110K users takes **< 1 second** on modern CPUs

---

## Migration Step (Spring Batch)

### Optimizations

* **JdbcPagingItemReader**:

    * Avoids issues with forward-only cursors in multithreaded readers (unlike JdbcCursorItemReader)
    * Breaks work into pages (here, 1000 rows per page)

* **JdbcBatchItemWriter**:

    * Efficient batch inserts into PostgreSQL
    * Reduces JDBC overhead

* **Async Task Execution**:

    * Uses `MigrationTaskExecutor` (a bounded `ThreadPoolTaskExecutor`) with a throttle limit (e.g. 4 threads)
    * Enables concurrent chunk processing (CPU cores > 1 get used)

* **Chunk size tuning**:

    * Optimal for read/write balance (tested with chunk size = 1000)

### Performance

* Migrating 110K users now takes **\~4.5 seconds** (previously 3+ minutes)

---

## How to Run

### 1. Set up MySQL and PostgreSQL databases

Create the required schemas and user tables as shown above.

### 2. Configure `application.yml` or `application.properties`

Ensure both legacy and new DB credentials are defined correctly.

### 3. Seed the legacy DB (for testing)

```java
@Autowired
LegacyDbSeeder seeder;
seeder.run(110000); // Or any desired number
```

### 4. Run the migration job

You can run the Spring Boot app, and it will execute the `userMigrationJob` configured in `UserMigrationJobConfig.java`.

---

## Potential Improvements

* Partitioned steps for multi-host parallelism
* Retry and skip logic for fault tolerance
* Real-time progress reporting
* Integration with observability tools (e.g., Micrometer, Prometheus)
* Command-line params for total users, chunk size, etc.

---

## Requirements

* Java 17+
* Spring Boot 3.x
* Spring Batch
* MySQL (for legacy data)
* PostgreSQL (for new target)

---

## Author

Built with ❤️ by Sougata Ghosh to demonstrate scalable batch ETL with Spring.

---
