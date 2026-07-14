package com.daisy.health;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DataSqlPersistenceTest {
    @Test
    void startupSeedDoesNotOverwriteSavedAdminAvatar() throws Exception {
        InputStream input = getClass().getClassLoader().getResourceAsStream("data.sql");
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int read;
        while ((read = input.read(buffer)) != -1) output.write(buffer, 0, read);
        String sql = new String(output.toByteArray(), StandardCharsets.UTF_8).toLowerCase();

        assertFalse(sql.contains("update staff set avatar_url = 'https://api.dicebear.com"));
        assertTrue(sql.contains("avatar_url = coalesce(avatar_url"));
    }

    @Test
    void startupSeedKeepsInitialPasswordButDoesNotOverwriteChangedPasswords() throws Exception {
        InputStream input = getClass().getClassLoader().getResourceAsStream("data.sql");
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int read;
        while ((read = input.read(buffer)) != -1) output.write(buffer, 0, read);
        String sql = new String(output.toByteArray(), StandardCharsets.UTF_8).toLowerCase();

        assertTrue(sql.contains("select id, phone, '753951', 'elderly'"));
        assertTrue(sql.contains("select 200000 + id, phone, '753951', 'service'"));
        assertFalse(sql.contains("'admin123'"));
        assertFalse(sql.contains("password_hash = values(password_hash)"));
        assertFalse(sql.contains("set phone = '13402832834',\n    password_hash"));
        assertFalse(sql.contains("set phone = '13402832834',\r\n    password_hash"));
    }
}
