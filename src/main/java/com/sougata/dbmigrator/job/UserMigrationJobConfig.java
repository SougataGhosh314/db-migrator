package com.sougata.dbmigrator.job;

import com.sougata.dbmigrator.model.LegacyUser;
import com.sougata.dbmigrator.model.NewUser;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.skip.AlwaysSkipItemSkipPolicy;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.PlatformTransactionManager;


@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class UserMigrationJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final JobExecutionListener jobCompletionListener;

    @Bean
    public Job migrateUserJob(Step migrateUsersStep) {
        return new JobBuilder("migrateUserJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(jobCompletionListener)
                .start(migrateUsersStep)
                .build();
    }

    @Bean
    public Step migrateUsersStep(
            JdbcCursorItemReader<LegacyUser> legacyUserReader,
            ItemProcessor<LegacyUser, NewUser> userItemProcessor,
            JpaItemWriter<NewUser> newUserWriter,
            @Value("#{jobParameters['chunkSize']}") Integer chunkSize
    ) {
        return new StepBuilder("migrateUsersStep", jobRepository)
                .<LegacyUser, NewUser>chunk(chunkSize, transactionManager)
                .reader(legacyUserReader)
                .processor(userItemProcessor)
                .writer(newUserWriter)
                .faultTolerant()
                .skipPolicy(new AlwaysSkipItemSkipPolicy())
                .listener(stepExecutionListener())
                .build();
    }

    @Bean
    public StepExecutionListener stepExecutionListener() {
        return new StepExecutionListener() {
            @Override
            public void beforeStep(StepExecution stepExecution) {
                // Custom logic
            }

            @Override
            public ExitStatus afterStep(StepExecution stepExecution) {
                return stepExecution.getExitStatus();
            }
        };
    }
}
