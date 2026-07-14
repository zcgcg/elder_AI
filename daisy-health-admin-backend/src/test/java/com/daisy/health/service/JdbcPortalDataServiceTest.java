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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.any;
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
    void elderlyWorkOrderChangesKeepTheOwnershipLockInsideAReadCommittedTransaction() throws Exception {
        Transactional cancelTransaction = JdbcPortalDataService.class
                .getMethod("cancelElderlyWorkOrder", Long.class, Map.class)
                .getAnnotation(Transactional.class);
        Transactional rescheduleTransaction = JdbcPortalDataService.class
                .getMethod("rescheduleElderlyWorkOrder", Long.class, Map.class)
                .getAnnotation(Transactional.class);

        assertEquals(Isolation.READ_COMMITTED, cancelTransaction.isolation());
        assertEquals(Isolation.READ_COMMITTED, rescheduleTransaction.isolation());
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

    @Test
    void completedOwnedServiceCanBeReviewedOnce() {
        when(jdbcTemplate.queryForList(startsWith("select o.id, o.product_id as productId"), eq(31L), eq(7L)))
                .thenReturn(Collections.singletonList(record("id", 31L, "productId", 9L, "productName", "助浴护理")));
        when(jdbcTemplate.queryForList(startsWith("select id from review"), eq(31L), eq(7L)))
                .thenReturn(Collections.<Map<String, Object>>emptyList());
        when(jdbcTemplate.queryForList(startsWith("select o.id as orderId"), eq(31L), eq(7L)))
                .thenReturn(Collections.singletonList(record(
                        "orderId", 31L, "productName", "助浴护理", "rating", 5,
                        "content", "服务很细心", "reviewed", true
                )));

        Map<String, Object> result = service.createElderlyReview(
                record("orderId", 31L, "rating", 5, "content", " 服务很细心 ")
        );

        assertEquals(true, result.get("reviewed"));
        assertTrue(org.mockito.Mockito.mockingDetails(jdbcTemplate).getInvocations().stream()
                .map(invocation -> String.valueOf(invocation.getRawArguments()[0]))
                .anyMatch(sql -> sql.startsWith("select o.id, o.product_id as productId") && sql.contains("for update")));
        verify(jdbcTemplate).update(
                startsWith("insert into review"), eq(31L), eq(9L), eq(7L), eq(5), eq("服务很细心")
        );
    }

    @Test
    void serviceCannotBeReviewedUntilItsWorkOrderIsCompleted() {
        when(jdbcTemplate.queryForList(startsWith("select o.id, o.product_id as productId"), eq(31L), eq(7L)))
                .thenReturn(Collections.<Map<String, Object>>emptyList());

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> service.createElderlyReview(record("orderId", 31L, "rating", 5))
        );

        assertEquals("只能评价本人已完成的服务", error.getMessage());
        verify(jdbcTemplate, never()).update(startsWith("insert into review"), any(), any(), any(), any(), any());
    }

    @Test
    void duplicateServiceReviewIsRejected() {
        when(jdbcTemplate.queryForList(startsWith("select o.id, o.product_id as productId"), eq(31L), eq(7L)))
                .thenReturn(Collections.singletonList(record("id", 31L, "productId", 9L)));
        when(jdbcTemplate.queryForList(startsWith("select id from review"), eq(31L), eq(7L)))
                .thenReturn(Collections.singletonList(record("id", 77L)));

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> service.createElderlyReview(record("orderId", 31L, "rating", 4))
        );

        assertEquals("该服务已经评价，不能重复提交", error.getMessage());
    }

    @Test
    void cancellingEnrollmentMarksLatestEnrollmentAndResyncsCount() {
        when(jdbcTemplate.queryForList(contains("from activity where id"), eq(21L)))
                .thenReturn(Collections.singletonList(record("id", 21L, "title", "社区健康义诊")));
        when(jdbcTemplate.queryForList(contains("from activity_enroll where activity_id"), eq(21L), eq(7L)))
                .thenReturn(Collections.singletonList(record("id", 55L, "status", "enrolled")));

        Map<String, Object> result = service.cancelElderlyActivity(21L);

        assertEquals(false, result.get("joined"));
        verify(jdbcTemplate).update(startsWith("update activity_enroll set status = 'cancelled'"), eq(55L), eq(7L));
        verify(jdbcTemplate).update(startsWith("update activity set"), eq(21L), eq(21L));
    }

    @Test
    void completedWorkOrderCannotBeCancelledByElderlyUser() {
        when(jdbcTemplate.queryForList(startsWith("select id, order_id as orderId"), eq(12L), eq(7L)))
                .thenReturn(Collections.singletonList(record("id", 12L, "orderId", 31L, "status", "completed")));

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> service.cancelElderlyWorkOrder(12L, record("reason", "时间冲突"))
        );

        assertEquals("只有待服务工单可以取消", error.getMessage());
        verify(jdbcTemplate, never()).update(startsWith("update work_order set status = 'cancelled'"), any(), any(), any());
    }

    @Test
    void pendingWorkOrderCanBeRescheduledToFutureTime() {
        when(jdbcTemplate.queryForList(startsWith("select id, order_id as orderId"), eq(13L), eq(7L)))
                .thenReturn(Collections.singletonList(record("id", 13L, "orderId", 32L, "status", "pending")));
        when(jdbcTemplate.queryForList(contains("where w.id = ? and w.customer_id = ? limit 1"), eq(13L), eq(7L)))
                .thenReturn(Collections.singletonList(record("id", 13L, "status", "pending")));
        String future = LocalDateTime.now().plusDays(2).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        Map<String, Object> result = service.rescheduleElderlyWorkOrder(13L, record("serviceTime", future));

        assertEquals("待服务", result.get("status"));
        verify(jdbcTemplate).update(startsWith("update work_order set service_time"), eq(future), eq(13L), eq(7L));
    }

    @Test
    void blankMessageIsRejectedBeforeInsert() {
        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> service.createElderlyMessage(record("content", "  "))
        );

        assertEquals("请输入留言内容", error.getMessage());
        verify(jdbcTemplate, never()).update(startsWith("insert into elderly_message"), any(), any());
    }

    private Map<String, Object> record(Object... values) {
        Map<String, Object> row = new LinkedHashMap<String, Object>();
        for (int i = 0; i < values.length; i += 2) row.put(String.valueOf(values[i]), values[i + 1]);
        return row;
    }
}
