package com.sougata.dbmigrator.job.reader;

import com.sougata.dbmigrator.model.LegacyUser;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
@RequiredArgsConstructor
public class UserItemReader {

    private final DataSource dataSource; // MySQL source

    @Bean
    @StepScope
    public JdbcCursorItemReader<LegacyUser> legacyUserReader(
            @Value("#{jobParameters['sourceTable']}") String sourceTable
    ) {
        return new JdbcCursorItemReaderBuilder<LegacyUser>()
                .name("legacyUserReader")
                .dataSource(dataSource)
                .sql("SELECT id, username, email, full_name, created_at FROM " + sourceTable)
                .rowMapper(new BeanPropertyRowMapper<>(LegacyUser.class))
                .build();
    }
}
