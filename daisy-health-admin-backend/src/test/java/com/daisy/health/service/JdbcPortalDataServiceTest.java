package com.daisy.health.service;

import com.daisy.health.common.AuthenticatedUser;
import com.daisy.health.common.JwtAuthFilter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JdbcPortalDataServiceTest {
    private JdbcTemplate jdbcTemplate;
    private JdbcPortalDataService service;

    @BeforeEach
    void setUp() {
        jdbcTemplate = mock(JdbcTemplate.class);
        service = new JdbcPortalDataService(jdbcTemplate);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(JwtAuthFilter.USER_ATTRIBUTE, new AuthenticatedUser(99L, "elderly", "13800010001"));
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        when(jdbcTemplate.queryForList(startsWith("select legacy_user_id"), eq(99L)))
                .thenReturn(Collections.singletonList(record("legacy_user_id", 7L)));
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void enrollingReservesCapacityAndCreatesAnEnrollmentForCurrentUser() {
        when(jdbcTemplate.queryForList(contains("from activity where id"), eq(21L)))
                .thenReturn(Collections.singletonList(record("id", 21L, "title", "社区健康义诊")));
        when(jdbcTemplate.queryForList(contains("from activity_enroll where"), eq(21L), eq(7L)))
                .thenReturn(Collections.<Map<String, Object>>emptyList());
        when(jdbcTemplate.update(startsWith("update activity set"), eq(21L))).thenReturn(1);

        Map<String, Object> result = service.enrollElderlyActivity(21L);

        assertEquals(true, result.get("joined"));
        verify(jdbcTemplate).update(startsWith("update activity set"), eq(21L), eq(21L));
        verify(jdbcTemplate).update(startsWith("insert into activity_enroll"), eq(21L), eq(7L));
    }

    @Test
    void repeatedEnrollmentIsIdempotentEvenWhenNoCapacityRemains() {
        when(jdbcTemplate.queryForList(contains("from activity where id"), eq(21L)))
                .thenReturn(Collections.singletonList(record("id", 21L, "title", "社区健康义诊")));
        when(jdbcTemplate.queryForList(contains("from activity_enroll where"), eq(21L), eq(7L)))
                .thenReturn(Collections.singletonList(record("id", 55L, "status", "enrolled")));

        Map<String, Object> result = service.enrollElderlyActivity(21L);

        assertEquals("已报名", result.get("status"));
        verify(jdbcTemplate, never()).update(startsWith("update activity set"), eq(21L), eq(21L));
    }

    @Test
    void enrollmentCapacityUsesActualActiveEnrollmentRows() {
        when(jdbcTemplate.queryForList(contains("from activity where id"), eq(21L)))
                .thenReturn(Collections.singletonList(record("id", 21L, "title", "社区健康义诊", "quota", 1L)));
        when(jdbcTemplate.queryForList(contains("from activity_enroll where"), eq(21L), eq(7L)))
                .thenReturn(Collections.<Map<String, Object>>emptyList());
        when(jdbcTemplate.queryForList(startsWith("select count(*) as enrolled"), eq(21L)))
                .thenReturn(Collections.singletonList(record("enrolled", 1L)));

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> service.enrollElderlyActivity(21L)
        );

        assertEquals("活动名额已满", error.getMessage());
        verify(jdbcTemplate, never()).update(startsWith("insert into activity_enroll"), eq(21L), eq(7L));
    }

    @Test
    void enrollmentUsesReadCommittedAfterWaitingForTheActivityLock() throws Exception {
        Transactional transaction = JdbcPortalDataService.class
                .getMethod("enrollElderlyActivity", Long.class)
                .getAnnotation(Transactional.class);

        assertEquals(Isolation.READ_COMMITTED, transaction.isolation());
    }

    @Test
    void activityListUsesLatestEnrollmentAndKeepsJoinedHistoryVisible() {
        service.elderlyActivities();

        assertTrue(org.mockito.Mockito.mockingDetails(jdbcTemplate).getInvocations().stream()
                .map(invocation -> String.valueOf(invocation.getRawArguments()[0]))
                .anyMatch(sql -> sql.contains("left join activity_enroll ae")
                        && sql.contains("max(latest.id)")
                        && sql.contains("or ae.status in ('enrolled', 'attended')")));
    }

    @Test
    void activityListCountsActiveEnrollmentRowsInsteadOfTrustingCachedCounter() {
        service.elderlyActivities();

        assertTrue(org.mockito.Mockito.mockingDetails(jdbcTemplate).getInvocations().stream()
                .map(invocation -> String.valueOf(invocation.getRawArguments()[0]))
                .anyMatch(sql -> sql.contains("count(*) as enrolled")
                        && sql.contains("max(id) as latestId")
                        && sql.contains("status in ('enrolled', 'attended')")
                        && sql.contains("coalesce(ec.enrolled, 0) as enrolled")));
    }

    @Test
    void creatingWorkOrderWithoutPersonnelFailsBeforeWritingOrderRows() {
        when(jdbcTemplate.queryForList(startsWith("select id, name, category, price"), eq(7L)))
                .thenReturn(Collections.singletonList(record("id", 7L, "name", "助浴护理", "category", "家政护理", "price", 199)));
        Map<String, Object> payload = record("productId", 7L);

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> service.createElderlyWorkOrder(payload)
        );

        assertEquals("请选择服务人员", error.getMessage());
        assertTrue(org.mockito.Mockito.mockingDetails(jdbcTemplate).getInvocations().stream()
                .map(invocation -> String.valueOf(invocation.getRawArguments()[0]))
                .noneMatch(sql -> sql.startsWith("insert into service_order")));
    }

    private Map<String, Object> record(Object... values) {
        Map<String, Object> row = new LinkedHashMap<String, Object>();
        for (int i = 0; i < values.length; i += 2) row.put(String.valueOf(values[i]), values[i + 1]);
        return row;
    }
}
