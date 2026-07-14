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
        assertTrue(sql.contains("insert ignore into work_order"));
        assertTrue(sql.contains("where order_no in ("), "board refresh must not rewrite migrated order links");
        assertTrue(sql.contains("set w.product_id = o.product_id"));
    }

    @Test
    void startupRepairsAndEnforcesOneOrderPerWorkOrder() throws Exception {
        ClassPathResource resource = new ClassPathResource("data.sql");
        String sql = new String(Files.readAllBytes(resource.getFile().toPath()), StandardCharsets.UTF_8)
                .toLowerCase();

        assertTrue(sql.contains("tmp_extra_work_orders"), "duplicate work orders must receive cloned transaction orders");
        assertTrue(sql.contains("migration_order_no"), "cloned transaction orders must use a stable migration key");
        assertTrue(sql.contains("wom-"), "transaction orders without work orders must receive a migrated work order");
        assertTrue(sql.contains("sha2("), "migration identifiers must avoid collisions with historical numbers");
        assertTrue(sql.contains("uk_work_order_order_id"), "the one-to-one relation must be enforced by a unique index");
    }
}
