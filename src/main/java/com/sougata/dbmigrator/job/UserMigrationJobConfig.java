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
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;


@Configuration
@EnableBatchProcessing
public class UserMigrationJobConfig {

    @Bean
    public JdbcCursorItemReader<LegacyUser> legacyUserReader(@Qualifier("legacyDataSource") DataSource dataSource) {
        return new JdbcCursorItemReaderBuilder<LegacyUser>()
                .name("legacyUserReader")
                .dataSource(dataSource)
                .sql("SELECT id, username, email, full_name, created_at FROM users")
                .rowMapper(new BeanPropertyRowMapper<>(LegacyUser.class))
                .build();
    }

    @Bean
    public UserProcessor userProcessor() {
        return new UserProcessor();
    }

    @Bean
    public JpaItemWriter<NewUser> newUserWriter(@Qualifier("newEntityManagerFactory") jakarta.persistence.EntityManagerFactory entityManagerFactory) {
        return new JpaItemWriterBuilder<NewUser>()
                .entityManagerFactory(entityManagerFactory)
                .build();
    }

    @Bean(name = "migrateUserStep")
    public Step migrateUserStep(JobRepository jobRepository,
                                @Qualifier("newTransactionManager") PlatformTransactionManager transactionManager,
                                JdbcCursorItemReader<LegacyUser> legacyUserReader,
                                UserProcessor userProcessor,
                                JpaItemWriter<NewUser> newUserWriter) {
        return new StepBuilder("migrateUserStep", jobRepository)
                .<LegacyUser, NewUser>chunk(1000, transactionManager)
                .reader(legacyUserReader)
                .processor(userProcessor)
                .writer(newUserWriter)
                .build();
    }

    @Bean(name = "userMigrationJob")
    public Job userMigrationJob(JobRepository jobRepository, Step migrateUserStep) {
        return new JobBuilder("userMigrationJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(migrateUserStep)
                .build();
    }
}