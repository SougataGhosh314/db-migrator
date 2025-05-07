package com.sougata.dbmigrator.job.processor;

import com.sougata.dbmigrator.model.LegacyUser;
import com.sougata.dbmigrator.model.NewUser;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class UserItemProcessor implements ItemProcessor<LegacyUser, NewUser> {

    @Override
    public NewUser process(LegacyUser legacyUser) {
        NewUser newUser = new NewUser();
        newUser.setName(legacyUser.getFullName() != null ? legacyUser.getFullName() : legacyUser.getUsername());
        newUser.setEmail(legacyUser.getEmail());
        newUser.setRegisteredOn(legacyUser.getCreatedAt() != null ? legacyUser.getCreatedAt() : LocalDateTime.now());
        return newUser;
    }
}
