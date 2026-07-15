package com.daisy.health.service;

import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

public final class ServiceAssignmentPolicy {
    private ServiceAssignmentPolicy() {
    }

    public static long requireAssignable(JdbcTemplate jdbcTemplate, Long personnelId, Object productCategory) {
        if (personnelId == null || personnelId <= 0) {
            throw new IllegalArgumentException("请选择服务人员");
        }
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "select id, service_type as serviceType from service_personnel " +
                        "where id = ? and status = 1 and audit_status = '已通过' limit 1",
                personnelId
        );
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("所选服务人员不存在或不可接单");
        }
        String category = ServiceCategory.requireValid(productCategory);
        if (!category.equals(String.valueOf(rows.get(0).get("serviceType")))) {
            throw new IllegalArgumentException("所选服务人员的服务类型与商品服务分类不匹配");
        }
        return personnelId;
    }
}
