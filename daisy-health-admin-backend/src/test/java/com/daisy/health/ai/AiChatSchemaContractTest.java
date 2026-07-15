package com.daisy.health.ai;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertTrue;

class AiChatSchemaContractTest {
    @Test
    void schemaStoresAccountScopedMessagesWithAChronologicalIndex() throws Exception {
        ClassPathResource resource = new ClassPathResource("schema.sql");
        String sql = new String(Files.readAllBytes(resource.getFile().toPath()), StandardCharsets.UTF_8).toLowerCase();

        assertTrue(sql.contains("create table if not exists ai_chat_message"));
        assertTrue(sql.contains("account_id bigint not null"));
        assertTrue(sql.contains("role varchar(16) not null"));
        assertTrue(sql.contains("content text not null"));
        assertTrue(sql.contains("idx_ai_chat_account_time (account_id, created_at, id)"));
    }
}
