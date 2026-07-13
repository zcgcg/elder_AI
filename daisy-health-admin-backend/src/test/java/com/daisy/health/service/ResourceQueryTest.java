package com.daisy.health.service;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ResourceQueryTest {
    @Test
    void filtersRowsByKeywordStatusDateAndTagTogether() {
        ResourceQuery query = new ResourceQuery();
        query.setKeyword("王秀兰");
        query.setStatus("已完成");
        query.setStartDate("2026-07-10");
        query.setEndDate("2026-07-13");
        query.setTag("重点关怀");

        List<Map<String, Object>> result = query.filter(Arrays.asList(
                row("id", 1, "userName", "王秀兰", "status", "已完成", "createdAt", "2026-07-12 09:00", "tags", Arrays.asList("重点关怀")),
                row("id", 2, "userName", "王秀兰", "status", "待服务", "createdAt", "2026-07-12 09:00", "tags", Arrays.asList("重点关怀")),
                row("id", 3, "userName", "王秀兰", "status", "已完成", "createdAt", "2026-07-01 09:00", "tags", Arrays.asList("重点关怀"))
        ));

        assertEquals(1, result.size());
        assertEquals(1, result.get(0).get("id"));
    }

    @Test
    void rejectsAnEndDateBeforeTheStartDate() {
        ResourceQuery query = new ResourceQuery();
        query.setStartDate("2026-07-13");
        query.setEndDate("2026-07-12");

        assertThrows(IllegalArgumentException.class, () -> query.filter(Arrays.asList(row("id", 1))));
    }

    private Map<String, Object> row(Object... values) {
        Map<String, Object> row = new LinkedHashMap<String, Object>();
        for (int i = 0; i < values.length; i += 2) row.put(String.valueOf(values[i]), values[i + 1]);
        return row;
    }
}
