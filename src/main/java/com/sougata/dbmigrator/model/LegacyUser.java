package com.sougata.dbmigrator.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class LegacyUser {
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private LocalDateTime createdAt;
}
