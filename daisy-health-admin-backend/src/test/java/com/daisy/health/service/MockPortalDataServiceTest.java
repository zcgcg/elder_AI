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
}
