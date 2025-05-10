package com.sougata.dbmigrator.job.processor;

import com.sougata.dbmigrator.model.LegacyUser;
import com.sougata.dbmigrator.model.NewUser;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
public class UserProcessor implements ItemProcessor<LegacyUser, NewUser> {

    @Override
    public NewUser process(LegacyUser legacyUser) throws Exception {
        System.out.println("Migrating: " + legacyUser);
        NewUser newUser = new NewUser();
        newUser.setName(legacyUser.getFullName());
        newUser.setEmail(legacyUser.getEmail());
        newUser.setRegisteredOn(legacyUser.getCreatedAt());
        return newUser;
    }
}