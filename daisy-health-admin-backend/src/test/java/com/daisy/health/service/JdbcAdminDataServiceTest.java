package com.daisy.health.service;

import com.daisy.health.common.AuthenticatedUser;
import com.daisy.health.common.JwtAuthFilter;
import com.daisy.health.common.JwtService;
import com.daisy.health.common.PermissionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JdbcAdminDataServiceTest {
    @Test
    void productAndPersonnelWritesRejectCategoriesOutsideTheFourServiceCategories() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        JdbcAdminDataService service = service(jdbcTemplate);

        IllegalArgumentException productError = assertThrows(IllegalArgumentException.class, () -> service.createResource(
                "products", record("name", "未知服务", "category", "自定义分类")
        ));
        IllegalArgumentException personnelError = assertThrows(IllegalArgumentException.class, () -> service.createResource(
                "personnel", record("name", "测试人员", "serviceType", "自定义分类")
        ));

        assertEquals("服务分类只能是家政护理、康复理疗、上门体检或其他", productError.getMessage());
        assertEquals("服务分类只能是家政护理、康复理疗、上门体检或其他", personnelError.getMessage());
        verify(jdbcTemplate, never()).update(any(org.springframework.jdbc.core.PreparedStatementCreator.class), any(org.springframework.jdbc.support.KeyHolder.class));
    }

    @Test
    void fixedServiceCategoriesCannotBeCreatedEditedOrDeletedThroughCrud() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        JdbcAdminDataService service = service(jdbcTemplate);

        assertEquals("服务分类为系统固定四类，不能新增、修改或删除", assertThrows(
                IllegalArgumentException.class,
                () -> service.createResource("productCategories", record("name", "第五类"))
        ).getMessage());
        assertEquals("服务分类为系统固定四类，不能新增、修改或删除", assertThrows(
                IllegalArgumentException.class,
                () -> service.updateResource("productCategories", 1L, record("name", "改名"))
        ).getMessage());
        assertEquals("服务分类为系统固定四类，不能新增、修改或删除", assertThrows(
                IllegalArgumentException.class,
                () -> service.deleteResource("productCategories", 1L)
        ).getMessage());

        verify(jdbcTemplate, never()).update(startsWith("delete from `product_category`"), any(Object[].class));
    }

    @Test
    void adminCannotAssignAWorkOrderToPersonnelFromAnotherServiceCategory() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.queryForList(startsWith("select id, name, category, price"), eq(7L)))
                .thenReturn(Collections.singletonList(record(
                        "id", 7L, "name", "肩周炎理疗", "category", "康复理疗", "price", 259, "duration", 60
                )));
        when(jdbcTemplate.queryForList(startsWith("select id, service_type as serviceType from service_personnel"), eq(1L)))
                .thenReturn(Collections.singletonList(record("id", 1L, "serviceType", "家政护理")));
        when(jdbcTemplate.queryForList(startsWith("select id from `user`")))
                .thenReturn(Collections.singletonList(record("id", 10001L)));
        JdbcAdminDataService service = service(jdbcTemplate);

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () -> service.createResource(
                "workOrders",
                record("productId", 7L, "customerId", 10001L, "personnelId", 1L, "serviceTime", "2026-07-18 09:00:00")
        ));

        assertEquals("所选服务人员的服务类型与商品服务分类不匹配", error.getMessage());
        verify(jdbcTemplate, never()).update(any(org.springframework.jdbc.core.PreparedStatementCreator.class), any(org.springframework.jdbc.support.KeyHolder.class));
    }

    @Test
    void editingAWorkOrderCannotSwitchToPersonnelFromAnotherServiceCategory() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.queryForList(startsWith("select p.category from work_order"), eq(9L)))
                .thenReturn(Collections.singletonList(record("category", "康复理疗")));
        when(jdbcTemplate.queryForList(startsWith("select id, service_type as serviceType from service_personnel"), eq(1L)))
                .thenReturn(Collections.singletonList(record("id", 1L, "serviceType", "家政护理")));
        JdbcAdminDataService service = service(jdbcTemplate);

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () -> service.updateResource(
                "workOrders", 9L, record("personnelId", 1L)
        ));

        assertEquals("所选服务人员的服务类型与商品服务分类不匹配", error.getMessage());
        verify(jdbcTemplate, never()).update(startsWith("update `work_order`"), any(Object[].class));
    }

    @Test
    void appointmentBoardUsesTheWorkOrderDurationSnapshotInsteadOfCompletionTime() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        JdbcAdminDataService service = service(jdbcTemplate);

        service.appointments(new ResourceQuery());

        assertTrue(org.mockito.Mockito.mockingDetails(jdbcTemplate).getInvocations().stream()
                .map(invocation -> String.valueOf(invocation.getRawArguments()[0]))
                .anyMatch(sql -> sql.contains("coalesce(nullif(w.service_duration, 0), 60) minute")
                        && !sql.contains("timestampdiff(minute, w.service_time, w.complete_time)")));
    }

    @Test
    void adminWorkOrderRequiresAServiceTimeBeforeWritingItsOrderPair() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        JdbcAdminDataService service = service(jdbcTemplate);

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> service.createResource("workOrders", record(
                        "productId", 7L,
                        "customerId", 10001L,
                        "personnelId", 2L
                ))
        );

        assertEquals("请选择服务时间", error.getMessage());
        verify(jdbcTemplate, never()).update(any(org.springframework.jdbc.core.PreparedStatementCreator.class), any(org.springframework.jdbc.support.KeyHolder.class));
    }

    @Test
    void resettingAUserPasswordAlwaysUsesTheFixedDefault() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        when(passwordEncoder.encode("753951")).thenReturn("fixed-default-hash");
        when(jdbcTemplate.update(
                startsWith("update account a join elderly_profile"),
                eq("fixed-default-hash"),
                eq(10001L)
        )).thenReturn(1);
        JdbcAdminDataService service = new JdbcAdminDataService(
                jdbcTemplate,
                passwordEncoder,
                mock(JwtService.class),
                mock(PermissionService.class),
                new ObjectMapper()
        );

        Map<String, Object> result = service.resetUserPassword(10001L);

        assertEquals(true, result.get("reset"));
        verify(passwordEncoder).encode("753951");
    }

    @Test
    void resettingAServicePersonnelPasswordAlwaysUsesTheFixedDefault() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        when(passwordEncoder.encode("753951")).thenReturn("fixed-default-hash");
        when(jdbcTemplate.update(
                startsWith("update account a join service_profile"),
                eq("fixed-default-hash"),
                eq(2L)
        )).thenReturn(1);
        JdbcAdminDataService service = new JdbcAdminDataService(
                jdbcTemplate,
                passwordEncoder,
                mock(JwtService.class),
                mock(PermissionService.class),
                new ObjectMapper()
        );

        Map<String, Object> result = service.resetPersonnelPassword(2L);

        assertEquals(true, result.get("reset"));
        verify(passwordEncoder).encode("753951");
    }

    @Test
    void authenticatedAccountCanChangeItsOwnPassword() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        when(jdbcTemplate.queryForList(startsWith("select password_hash as passwordHash"), eq(1L)))
                .thenReturn(Collections.singletonList(record("passwordHash", "stored-hash")));
        when(passwordEncoder.matches("753951", "stored-hash")).thenReturn(true);
        when(passwordEncoder.encode("654321")).thenReturn("new-hash");
        JdbcAdminDataService service = new JdbcAdminDataService(
                jdbcTemplate,
                passwordEncoder,
                mock(JwtService.class),
                mock(PermissionService.class),
                new ObjectMapper()
        );
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(JwtAuthFilter.USER_ATTRIBUTE, new AuthenticatedUser(1L, "staff", "13402832834"));
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        try {
            Map<String, Object> result = service.updatePassword(record(
                    "currentPassword", "753951",
                    "newPassword", "654321",
                    "confirmPassword", "654321"
            ));

            assertEquals(true, result.get("changed"));
            verify(jdbcTemplate).update("update account set password_hash = ?, updated_at = now() where id = ?", "new-hash", 1L);
            verify(jdbcTemplate).update("update staff set password_hash = ? where id = ?", "new-hash", 1L);
        } finally {
            RequestContextHolder.resetRequestAttributes();
        }
    }

    @Test
    void changingPasswordRejectsAnIncorrectCurrentPassword() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        when(jdbcTemplate.queryForList(startsWith("select password_hash as passwordHash"), eq(10001L)))
                .thenReturn(Collections.singletonList(record("passwordHash", "stored-hash")));
        when(passwordEncoder.matches("wrong-password", "stored-hash")).thenReturn(false);
        JdbcAdminDataService service = new JdbcAdminDataService(
                jdbcTemplate,
                passwordEncoder,
                mock(JwtService.class),
                mock(PermissionService.class),
                new ObjectMapper()
        );
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(JwtAuthFilter.USER_ATTRIBUTE, new AuthenticatedUser(10001L, "elderly", "13800010001"));
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        try {
            IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () -> service.updatePassword(record(
                    "currentPassword", "wrong-password",
                    "newPassword", "654321",
                    "confirmPassword", "654321"
            )));

            assertEquals("当前密码不正确", error.getMessage());
        } finally {
            RequestContextHolder.resetRequestAttributes();
        }
    }

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

        service.resource("activities", new ResourceQuery());

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
    void standaloneTransactionOrderCreationIsRejectedToPreserveOneToOnePairing() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        JdbcAdminDataService service = service(jdbcTemplate);

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> service.createResource("orders", record("productId", 7L, "buyerId", 10001L))
        );

        assertEquals("请通过履约工单创建交易订单", error.getMessage());
        verify(jdbcTemplate, never()).update(startsWith("insert into `service_order`"), any(Object[].class));
    }

    @Test
    void deletingAWorkOrderDeletesItsWholeTransactionPair() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.queryForList(startsWith("select order_id as orderId from work_order"), eq(9L)))
                .thenReturn(Collections.singletonList(record("orderId", 31L)));
        JdbcAdminDataService service = service(jdbcTemplate);

        service.deleteResource("workOrders", 9L);

        verify(jdbcTemplate).update("delete from review where order_id = ?", 31L);
        verify(jdbcTemplate).update("delete from after_sale where order_id = ?", 31L);
        verify(jdbcTemplate).update("delete from work_order where order_id = ?", 31L);
        verify(jdbcTemplate).update("delete from service_order where id = ?", 31L);
    }

    @Test
    void completingAWorkOrderSynchronizesItsTransactionOrder() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.queryForList(startsWith("select order_id as orderId from work_order"), eq(9L)))
                .thenReturn(Collections.singletonList(record("orderId", 31L)));
        JdbcAdminDataService service = service(jdbcTemplate);

        service.updateResource("workOrders", 9L, record("status", "已完成"));

        verify(jdbcTemplate).update("update service_order set status = ? where id = ?", "completed", 31L);
    }

    @Test
    void editingAnAppointmentSynchronizesItsTransactionOrderDetails() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.queryForList(startsWith("select order_id as orderId from work_order"), eq(9L)))
                .thenReturn(Collections.singletonList(record("orderId", 31L)));
        when(jdbcTemplate.queryForList(startsWith("select id, name, category, price"), eq(7L)))
                .thenReturn(Collections.singletonList(record(
                        "id", 7L, "name", "助浴护理", "category", "家政护理", "price", 199, "duration", 90
                )));
        when(jdbcTemplate.queryForList(startsWith("select id from `user`")))
                .thenReturn(Collections.singletonList(record("id", 10001L)));
        when(jdbcTemplate.queryForList(startsWith("select id, service_type as serviceType from service_personnel"), eq(1L)))
                .thenReturn(Collections.singletonList(record("id", 1L, "serviceType", "家政护理")));
        JdbcAdminDataService service = service(jdbcTemplate);

        service.updateResource("appointments", 9L, record(
                "productId", 7L,
                "personnelId", 1L,
                "customerId", 10002L
        ));

        assertTrue(org.mockito.Mockito.mockingDetails(jdbcTemplate).getInvocations().stream()
                .map(invocation -> String.valueOf(invocation.getRawArguments()[0]))
                .anyMatch(sql -> sql.startsWith("update `service_order` set")
                        && sql.contains("`product_id`")
                        && sql.contains("`product_name`")
                        && sql.contains("`amount`")
                        && sql.contains("`buyer_id`")
                        && sql.contains("`service_type`")));
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
