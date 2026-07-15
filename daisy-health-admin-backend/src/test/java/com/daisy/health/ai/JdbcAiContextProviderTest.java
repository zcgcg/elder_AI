package com.daisy.health.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JdbcAiContextProviderTest {
    @Test
    void contextContainsUsefulFactsButDropsIdentifiersAndUnexpectedColumns() {
        StubJdbcTemplate jdbc = new StubJdbcTemplate();
        JdbcAiContextProvider provider = new JdbcAiContextProvider(jdbc, new ObjectMapper());

        String context = provider.contextFor(88L);

        assertTrue(context.contains("高血压"));
        assertTrue(context.contains("心率"));
        assertTrue(context.contains("助浴护理"));
        assertFalse(context.contains("310101195203120021"));
        assertFalse(context.contains("13800010001"));
        assertFalse(context.contains("浦东新区私密地址"));
        assertFalse(context.contains("secret-key"));
        assertFalse(context.contains("\"accountId\""));
        assertTrue(jdbc.queries.stream().noneMatch(sql -> sql.contains("id_card") || sql.contains("emergency_contact") || sql.contains("emergency_phone")));
    }

    private static Map<String, Object> row(Object... values) {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        for (int index = 0; index < values.length; index += 2) result.put(String.valueOf(values[index]), values[index + 1]);
        return result;
    }

    private static class StubJdbcTemplate extends JdbcTemplate {
        private final List<String> queries = new ArrayList<String>();

        @Override
        public List<Map<String, Object>> queryForList(String sql, Object... args) {
            queries.add(sql.toLowerCase());
            if (sql.contains("from elderly_profile")) {
                return Collections.singletonList(row(
                        "age", 74, "gender", "女", "chronicDisease", "高血压", "height", 158,
                        "phone", "13800010001", "idCard", "310101195203120021", "address", "浦东新区私密地址",
                        "apiKey", "secret-key", "accountId", 88
                ));
            }
            if (sql.contains("from health_data")) {
                return Collections.singletonList(row("dataType", "心率", "value", "76", "unit", "bpm", "phone", "13800010001"));
            }
            return Collections.emptyList();
        }

        @Override
        public List<Map<String, Object>> queryForList(String sql) {
            queries.add(sql.toLowerCase());
            if (sql.contains("from product")) {
                return Collections.singletonList(row("name", "助浴护理", "category", "生活照护", "price", 199, "apiKey", "secret-key"));
            }
            return Collections.emptyList();
        }
    }
}
