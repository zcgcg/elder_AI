package com.daisy.health.service;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SeedDataContractTest {
    @Test
    void documentedPrimaryServiceTestAccountIsRestoredBeforeAccountMirroring() throws Exception {
        ClassPathResource resource = new ClassPathResource("data.sql");
        String sql = new String(Files.readAllBytes(resource.getFile().toPath()), StandardCharsets.UTF_8)
                .replace("\r\n", "\n");
        int restore = sql.indexOf("update service_personnel\nset phone = '13900020001'");
        int mirror = sql.indexOf("select 200000 + id, phone, '753951', 'service'");

        assertTrue(restore >= 0, "primary service test account must be restored");
        assertTrue(restore < mirror, "service test account must be restored before account mirroring");
    }

    @Test
    void appointmentBoardSeedsEveryDayInTheSevenDayWindow() throws Exception {
        ClassPathResource resource = new ClassPathResource("data.sql");
        String sql = new String(Files.readAllBytes(resource.getFile().toPath()), StandardCharsets.UTF_8);

        assertTrue(sql.contains("'WOBOARD-DAY-0'"));
        for (int day = 1; day <= 6; day++) {
            assertTrue(sql.contains("'WOBOARD-DAY-" + day + "'"), "missing appointment seed for day " + day);
            assertTrue(sql.contains("interval " + day + " day"), "missing appointment date for day " + day);
        }
        assertTrue(sql.contains("on duplicate key update"));
        assertTrue(sql.contains("set w.product_id = o.product_id"));
    }
}
