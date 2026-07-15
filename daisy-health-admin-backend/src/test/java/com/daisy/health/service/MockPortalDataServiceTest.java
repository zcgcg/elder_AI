package com.daisy.health.service;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MockPortalDataServiceTest {
    @Test
    void enrolledActivityAppearsAsJoinedOnTheNextRead() {
        MockPortalDataService service = new MockPortalDataService();

        service.enrollElderlyActivity(1L);
        Map<String, Object> activity = service.elderlyActivities().get(0);

        assertEquals(true, activity.get("joined"));
        assertEquals("已报名", activity.get("enrollmentStatus"));
    }

    @Test
    void unknownActivityCannotChangeAnotherActivityEnrollment() {
        MockPortalDataService service = new MockPortalDataService();

        assertThrows(IllegalArgumentException.class, () -> service.enrollElderlyActivity(999L));
        assertEquals(false, service.elderlyActivities().get(0).get("joined"));
    }

    @Test
    void mockElderlyWorkOrdersRejectPersonnelFromAnotherProductCategory() {
        MockPortalDataService service = new MockPortalDataService();

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () -> service.createElderlyWorkOrder(
                record("productId", 1L, "personnelId", 2L)
        ));

        assertEquals("所选服务人员的服务类型与商品服务分类不匹配", error.getMessage());
    }

    private Map<String, Object> record(Object... values) {
        Map<String, Object> result = new java.util.LinkedHashMap<String, Object>();
        for (int i = 0; i < values.length; i += 2) result.put(String.valueOf(values[i]), values[i + 1]);
        return result;
    }
}
