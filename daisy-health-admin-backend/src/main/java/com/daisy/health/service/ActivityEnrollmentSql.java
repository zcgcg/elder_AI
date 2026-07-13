package com.daisy.health.service;

import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

final class ActivityEnrollmentSql {
    static final String LATEST_ACTIVE_COUNTS =
            "(select enrollment.activity_id, count(*) as enrolled from activity_enroll enrollment " +
                    "join (select activity_id, user_id, max(id) as latestId from activity_enroll group by activity_id, user_id) latest " +
                    "on latest.latestId = enrollment.id where enrollment.status in ('enrolled', 'attended') group by enrollment.activity_id) ec";

    static final String ACTIVE_COUNT_FOR_ACTIVITY =
            "select count(*) as enrolled from activity_enroll enrollment " +
                    "join (select activity_id, user_id, max(id) as latestId from activity_enroll where activity_id = ? group by activity_id, user_id) latest " +
                    "on latest.latestId = enrollment.id where enrollment.status in ('enrolled', 'attended')";

    static void ensureCapacity(JdbcTemplate jdbcTemplate, Long activityId, long quota) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                ACTIVE_COUNT_FOR_ACTIVITY,
                activityId
        );
        long enrolled = rows.isEmpty() || !(rows.get(0).get("enrolled") instanceof Number)
                ? 0L : ((Number) rows.get(0).get("enrolled")).longValue();
        if (enrolled >= quota) throw new IllegalArgumentException("活动名额已满");
    }

    static void lockAndEnsureCapacity(JdbcTemplate jdbcTemplate, Long activityId) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "select quota from activity where id = ? for update",
                activityId
        );
        if (rows.isEmpty()) throw new IllegalArgumentException("活动不存在");
        Object value = rows.get(0).get("quota");
        long quota = value instanceof Number ? ((Number) value).longValue() : 0L;
        ensureCapacity(jdbcTemplate, activityId, quota);
    }

    static void syncCount(JdbcTemplate jdbcTemplate, Long activityId) {
        if (activityId == null) return;
        jdbcTemplate.update(
                "update activity set enrolled = (" + ACTIVE_COUNT_FOR_ACTIVITY + ") where id = ?",
                activityId,
                activityId
        );
    }

    private ActivityEnrollmentSql() {
    }
}
