package com.daisy.health.common;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
@Profile("mysql")
public class PermissionService {
    private static final List<String> AUTHENTICATED_ONLY = Arrays.asList(
            "auth", "uploads"
    );

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public PermissionService(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    public boolean canAccess(AuthenticatedUser user, String method, String uri) {
        String module = moduleFor(uri);
        if (module == null || AUTHENTICATED_ONLY.contains(module)) {
            return true;
        }
        if (user == null || !"staff".equals(user.getRoleType())) {
            return false;
        }
        return hasPermission(loadPermissions(user.getAccountId()), module, actionFor(method));
    }

    public Map<String, List<String>> loadPermissions(Long accountId) {
        List<String> rows = jdbcTemplate.queryForList(
                "select r.permissions from admin_profile p join role r on r.id = p.role_id " +
                        "where p.account_id = ? limit 1",
                String.class,
                accountId
        );
        if (rows.isEmpty() || rows.get(0) == null || rows.get(0).trim().length() == 0) {
            return Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(rows.get(0), new TypeReference<Map<String, List<String>>>() {});
        } catch (Exception ex) {
            throw new SecurityException("Invalid permission configuration");
        }
    }

    private boolean hasPermission(Map<String, List<String>> permissions, String module, String action) {
        if (allowed(permissions.get("*"), "*")) {
            return true;
        }
        return allowed(permissions.get(module), action);
    }

    private boolean allowed(List<String> actions, String action) {
        return actions != null && (actions.contains("*") || actions.contains(action));
    }

    private String actionFor(String method) {
        if ("GET".equalsIgnoreCase(method)) {
            return "view";
        }
        if ("DELETE".equalsIgnoreCase(method)) {
            return "delete";
        }
        return "edit";
    }

    private String moduleFor(String uri) {
        String path = uri == null ? "" : uri;
        if (path.startsWith("/api/v1/")) {
            path = path.substring("/api/v1/".length());
        }
        String segment = path.contains("/") ? path.substring(0, path.indexOf('/')) : path;
        if (segment.length() == 0) {
            return null;
        }
        if ("auth".equals(segment) || "uploads".equals(segment)) return segment;
        if ("dashboard".equals(segment) || "appointments".equals(segment)) return "dashboard";
        if ("analytics".equals(segment)) return "analytics";
        if ("users".equals(segment) || "tags".equals(segment) || "messages".equals(segment)
                || "health-settings".equals(segment) || "devices".equals(segment) || "reports".equals(segment)
                || "coupons".equals(segment) || "user-points".equals(segment) || "points-records".equals(segment)
                || "member-levels".equals(segment) || "points-rules".equals(segment)
                || "medications".equals(segment) || "health-data".equals(segment)
                || "assessment-results".equals(segment)) return "users";
        if ("personnel".equals(segment) || "audits".equals(segment) || "work-orders".equals(segment)) return "service";
        if ("products".equals(segment) || "product-categories".equals(segment) || "service-items".equals(segment)) return "products";
        if ("orders".equals(segment) || "after-sales".equals(segment) || "reviews".equals(segment)) return "trade";
        if ("staffs".equals(segment) || "roles".equals(segment) || "logs".equals(segment) || "agreements".equals(segment)) return "system";
        return "operations";
    }
}
