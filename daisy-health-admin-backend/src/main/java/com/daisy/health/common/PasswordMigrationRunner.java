package com.daisy.health.common;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@Profile("mysql")
public class PasswordMigrationRunner implements ApplicationRunner {
    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;

    public PasswordMigrationRunner(JdbcTemplate jdbcTemplate, PasswordEncoder passwordEncoder) {
        this.jdbcTemplate = jdbcTemplate;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        migrate("staff");
        migrate("account");
    }

    private void migrate(String tableName) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "select id, password_hash from `" + tableName + "` where password_hash not like '$2a$%' " +
                        "and password_hash not like '$2b$%' and password_hash not like '$2y$%'"
        );
        for (Map<String, Object> row : rows) {
            Object raw = row.get("password_hash");
            if (raw == null || String.valueOf(raw).trim().length() == 0) {
                continue;
            }
            jdbcTemplate.update(
                    "update `" + tableName + "` set password_hash = ? where id = ?",
                    passwordEncoder.encode(String.valueOf(raw)),
                    row.get("id")
            );
        }
    }
}
