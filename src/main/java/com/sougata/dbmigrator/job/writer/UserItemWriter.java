package com.sougata.dbmigrator.job.writer;

import com.sougata.dbmigrator.model.NewUser;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserItemWriter {

    private final EntityManagerFactory entityManagerFactory; // PostgreSQL target

    @Bean
    @StepScope
    public JpaItemWriter<NewUser> newUserWriter() {
        return new JpaItemWriterBuilder<NewUser>()
                .entityManagerFactory(entityManagerFactory)
                .build();
    }
}
