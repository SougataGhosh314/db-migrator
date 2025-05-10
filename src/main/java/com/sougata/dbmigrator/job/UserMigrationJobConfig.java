package com.sougata.dbmigrator.job;

import com.sougata.dbmigrator.job.processor.UserProcessor;
import com.sougata.dbmigrator.model.LegacyUser;
import com.sougata.dbmigrator.model.NewUser;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.*;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.item.database.support.MySqlPagingQueryProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.sql.Timestamp;
import java.util.Map;


@Configuration
@EnableBatchProcessing
public class UserMigrationJobConfig {

//    @Bean
//    public JdbcCursorItemReader<LegacyUser> legacyUserReader(@Qualifier("legacyDataSource") DataSource dataSource) {
//        return new JdbcCursorItemReaderBuilder<LegacyUser>()
//                .name("legacyUserReader")
//                .dataSource(dataSource)
//                .sql("SELECT id, username, email, full_name, created_at FROM users")
//                .rowMapper(new BeanPropertyRowMapper<>(LegacyUser.class))
//                .build();
//    }

    @Bean
    public JdbcPagingItemReader<LegacyUser> legacyUserReader(@Qualifier("legacyDataSource") DataSource dataSource) {
        MySqlPagingQueryProvider queryProvider = new MySqlPagingQueryProvider();
        queryProvider.setSelectClause("id, username, email, full_name, created_at");
        queryProvider.setFromClause("from users");
        queryProvider.setSortKeys(Map.of("id", Order.ASCENDING)); // required for paging

        return new JdbcPagingItemReaderBuilder<LegacyUser>()
                .name("legacyUserReader")
                .dataSource(dataSource)
                .pageSize(1000) // matches chunk size ideally
                .queryProvider(queryProvider)
                .rowMapper(new BeanPropertyRowMapper<>(LegacyUser.class))
                .build();
    }

    @Bean
    public UserProcessor userProcessor() {
        return new UserProcessor();
    }

//    @Bean
//    public JpaItemWriter<NewUser> newUserWriter(@Qualifier("newEntityManagerFactory") jakarta.persistence.EntityManagerFactory entityManagerFactory) {
//        return new JpaItemWriterBuilder<NewUser>()
//                .entityManagerFactory(entityManagerFactory)
//                .build();
//    }

    @Bean
    public JdbcBatchItemWriter<NewUser> newUserWriter(@Qualifier("newDataSource") DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<NewUser>()
                .dataSource(dataSource)
                .sql("INSERT INTO users (name, email, registered_on) VALUES (?, ?, ?)")
                .itemPreparedStatementSetter((user, ps) -> {
                    ps.setString(1, user.getName());
                    ps.setString(2, user.getEmail());
                    ps.setTimestamp(3, Timestamp.valueOf(user.getRegisteredOn()));
                })
                .build();
    }


    @Bean(name = "migrateUserStep")
    public Step migrateUserStep(JobRepository jobRepository,
                                @Qualifier("newTransactionManager") PlatformTransactionManager transactionManager,
                                JdbcPagingItemReader<LegacyUser> legacyUserReader,
                                UserProcessor userProcessor,
                                JdbcBatchItemWriter<NewUser> newUserWriter) {
        return new StepBuilder("migrateUserStep", jobRepository)
                .<LegacyUser, NewUser>chunk(1000, transactionManager)
                .reader(legacyUserReader)
                .processor(userProcessor)
                .writer(newUserWriter)
//                .taskExecutor(new SimpleAsyncTaskExecutor())
                .taskExecutor(migrationTaskExecutor())
                .throttleLimit(4) // Or 6 to match your core count
                .build();
    }

    @Bean(name = "userMigrationJob")
    public Job userMigrationJob(JobRepository jobRepository, Step migrateUserStep) {
        return new JobBuilder("userMigrationJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(migrateUserStep)
                .build();
    }

    @Bean
    public TaskExecutor migrationTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4); // or match your CPU cores
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(10); // small queue keeps backpressure
        executor.setThreadNamePrefix("migration-");
        executor.initialize();
        return executor;
    }

}