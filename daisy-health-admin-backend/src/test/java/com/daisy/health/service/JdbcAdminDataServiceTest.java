package com.daisy.health.service;

import com.daisy.health.common.JwtService;
import com.daisy.health.common.PermissionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
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

    private Map<String, Object> record(Object... values) {
        Map<String, Object> row = new LinkedHashMap<String, Object>();
        for (int i = 0; i < values.length; i += 2) row.put(String.valueOf(values[i]), values[i + 1]);
        return row;
    }
}
