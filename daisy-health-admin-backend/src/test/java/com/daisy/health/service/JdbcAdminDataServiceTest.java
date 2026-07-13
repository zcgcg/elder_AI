package com.daisy.health.service;

import com.daisy.health.common.JwtService;
import com.daisy.health.common.PermissionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JdbcAdminDataServiceTest {
    @Test
    void userDetailLoadsIndependentHealthRecordsInsteadOfMergingTypesUnderOneId() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.queryForList(startsWith("select u.id"), eq(10001L)))
                .thenReturn(Collections.singletonList(record("id", 10001L, "realName", "王秀兰")));

        JdbcAdminDataService service = new JdbcAdminDataService(
                jdbcTemplate,
                mock(PasswordEncoder.class),
                mock(JwtService.class),
                mock(PermissionService.class),
                new ObjectMapper()
        );

        service.user(10001L);

        assertTrue(org.mockito.Mockito.mockingDetails(jdbcTemplate).getInvocations().stream()
                .map(invocation -> String.valueOf(invocation.getRawArguments()[0]))
                .anyMatch(sql -> sql.startsWith("select id, data_type as dataType, value, unit")
                        && !sql.contains("group by record_date")));
    }

    @Test
    void userDetailWorkOrdersIncludeAssignedPersonnel() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.queryForList(startsWith("select u.id"), eq(10001L)))
                .thenReturn(Collections.singletonList(record("id", 10001L, "realName", "王秀兰")));

        JdbcAdminDataService service = new JdbcAdminDataService(
                jdbcTemplate,
                mock(PasswordEncoder.class),
                mock(JwtService.class),
                mock(PermissionService.class),
                new ObjectMapper()
        );

        service.user(10001L);

        assertTrue(org.mockito.Mockito.mockingDetails(jdbcTemplate).getInvocations().stream()
                .map(invocation -> String.valueOf(invocation.getRawArguments()[0]))
                .anyMatch(sql -> sql.contains("p.name as personnelName")
                        && sql.contains("left join service_personnel p")));
    }

    @Test
    void adminActivityListIncludesEveryFieldShownInUserActivityDetails() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        JdbcAdminDataService service = new JdbcAdminDataService(
                jdbcTemplate,
                mock(PasswordEncoder.class),
                mock(JwtService.class),
                mock(PermissionService.class),
                new ObjectMapper()
        );

        service.resource("activities");

        assertTrue(org.mockito.Mockito.mockingDetails(jdbcTemplate).getInvocations().stream()
                .map(invocation -> String.valueOf(invocation.getRawArguments()[0]))
                .anyMatch(sql -> sql.contains("cover_url as coverUrl")
                        && sql.contains("startTime")
                        && sql.contains("endTime")
                        && sql.contains("content")
                        && sql.contains("count(*) as enrolled")
                        && sql.contains("max(id) as latestId")
                        && sql.contains("coalesce(ec.enrolled, 0) as enrolled")));
    }

    @Test
    void editingActivityDetailsDoesNotResetEnrollmentCount() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        JdbcAdminDataService service = new JdbcAdminDataService(
                jdbcTemplate,
                mock(PasswordEncoder.class),
                mock(JwtService.class),
                mock(PermissionService.class),
                new ObjectMapper()
        );

        service.updateResource("activities", 21L, record("title", "更新后的活动"));

        assertTrue(org.mockito.Mockito.mockingDetails(jdbcTemplate).getInvocations().stream()
                .map(invocation -> String.valueOf(invocation.getRawArguments()[0]))
                .anyMatch(sql -> sql.startsWith("update `activity` set") && !sql.contains("enrolled")));
    }

    @Test
    void editingAnEnrollmentSynchronizesTheCompatibilityCount() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.queryForList(startsWith("select activity_id as activityId"), eq(88L)))
                .thenReturn(Collections.singletonList(record("activityId", 21L)));
        JdbcAdminDataService service = service(jdbcTemplate);

        service.updateResource("activityEnrolls", 88L, record("status", "已取消"));

        assertTrue(org.mockito.Mockito.mockingDetails(jdbcTemplate).getInvocations().stream()
                .map(invocation -> String.valueOf(invocation.getRawArguments()[0]))
                .anyMatch(sql -> sql.startsWith("update activity set enrolled = (select count(*) as enrolled")));
    }

    @Test
    void deletingAnEnrollmentSynchronizesTheCompatibilityCount() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.queryForList(startsWith("select activity_id as activityId"), eq(88L)))
                .thenReturn(Collections.singletonList(record("activityId", 21L)));
        JdbcAdminDataService service = service(jdbcTemplate);

        service.deleteResource("activityEnrolls", 88L);

        assertTrue(org.mockito.Mockito.mockingDetails(jdbcTemplate).getInvocations().stream()
                .map(invocation -> String.valueOf(invocation.getRawArguments()[0]))
                .anyMatch(sql -> sql.startsWith("update activity set enrolled = (select count(*) as enrolled")));
    }

    @Test
    void reactivatingAnEnrollmentCannotExceedTheRealQuota() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.queryForList(startsWith("select activity_id as activityId"), eq(88L)))
                .thenReturn(Collections.singletonList(record("activityId", 21L, "status", "cancelled")));
        when(jdbcTemplate.queryForList(startsWith("select quota from activity"), eq(21L)))
                .thenReturn(Collections.singletonList(record("quota", 1L)));
        when(jdbcTemplate.queryForList(startsWith("select count(*) as enrolled"), eq(21L)))
                .thenReturn(Collections.singletonList(record("enrolled", 1L)));
        JdbcAdminDataService service = service(jdbcTemplate);

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> service.updateResource("activityEnrolls", 88L, record("status", "已报名"))
        );

        assertEquals("活动名额已满", error.getMessage());
    }

    @Test
    void adminEnrollmentWritesUseReadCommittedAfterWaitingForTheActivityLock() throws Exception {
        Transactional transaction = JdbcAdminDataService.class
                .getMethod("updateResource", String.class, Long.class, Map.class)
                .getAnnotation(Transactional.class);

        assertEquals(Isolation.READ_COMMITTED, transaction.isolation());
    }

    @Test
    void movingAnActiveEnrollmentToAnotherUserCannotBypassTheQuota() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.queryForList(startsWith("select activity_id as activityId"), eq(88L)))
                .thenReturn(Collections.singletonList(record(
                        "activityId", 21L, "userId", 10001L, "status", "enrolled"
                )));
        when(jdbcTemplate.queryForList(startsWith("select id from `user`")))
                .thenReturn(Collections.singletonList(record("id", 10001L)));
        when(jdbcTemplate.queryForList(startsWith("select quota from activity"), eq(21L)))
                .thenReturn(Collections.singletonList(record("quota", 1L)));
        when(jdbcTemplate.queryForList(startsWith("select count(*) as enrolled"), eq(21L)))
                .thenReturn(Collections.singletonList(record("enrolled", 1L)));
        JdbcAdminDataService service = service(jdbcTemplate);

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> service.updateResource("activityEnrolls", 88L, record("userId", 10002L))
        );

        assertEquals("活动名额已满", error.getMessage());
    }

    private JdbcAdminDataService service(JdbcTemplate jdbcTemplate) {
        return new JdbcAdminDataService(
                jdbcTemplate,
                mock(PasswordEncoder.class),
                mock(JwtService.class),
                mock(PermissionService.class),
                new ObjectMapper()
        );
    }

    private Map<String, Object> record(Object... values) {
        Map<String, Object> row = new LinkedHashMap<String, Object>();
        for (int i = 0; i < values.length; i += 2) row.put(String.valueOf(values[i]), values[i + 1]);
        return row;
    }
}
