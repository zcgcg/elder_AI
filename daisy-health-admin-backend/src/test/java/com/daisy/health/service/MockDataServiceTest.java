package com.daisy.health.service;

import com.daisy.health.common.JwtService;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MockDataServiceTest {
    @Test
    void mockAdminWorkOrdersRejectPersonnelFromAnotherProductCategory() {
        MockDataService service = new MockDataService(new JwtService(
                "mock-test-jwt-secret-with-at-least-32-bytes", 60000L
        ));

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () -> service.createResource(
                "workOrders", record("productId", 2L, "personnelId", 1L)
        ));

        assertEquals("所选服务人员的服务类型与商品服务分类不匹配", error.getMessage());
    }

    private Map<String, Object> record(Object... values) {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        for (int i = 0; i < values.length; i += 2) result.put(String.valueOf(values[i]), values[i + 1]);
        return result;
    }
}
