package com.daisy.health.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.jdbc.core.JdbcTemplate;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@Profile("mysql")
public class JdbcAiContextProvider implements AiContextProvider {
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;
    private final String prompt;

    public JdbcAiContextProvider(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
        this.prompt = loadPrompt();
    }

    @Override
    public String systemPrompt() {
        return prompt;
    }

    @Override
    public String contextFor(long accountId) {
        try {
            Map<String, Object> context = new LinkedHashMap<String, Object>();
            context.put("当前用户健康概况", safeFirst(profile(accountId),
                    "age", "gender", "height", "weight", "chronicDisease", "sleepQuality",
                    "smokingFreq", "drinkingFreq", "exerciseFreq", "dietPreference"));
            context.put("最近30天健康测量", safeRows(healthData(accountId),
                    "dataType", "value", "unit", "recordDate", "recordTime", "source"));
            context.put("当前用药记录", safeRows(medications(accountId),
                    "period", "drugName", "frequency", "takeTime", "dosage", "reminderEnabled", "source"));
            context.put("最近健康报告", safeRows(reports(accountId),
                    "title", "reportType", "reportDate", "summary", "doctorName"));
            context.put("当前商品服务", safeRows(products(), "name", "category", "price"));
            context.put("近期可报名活动", safeRows(activities(),
                    "title", "location", "startTime", "endTime", "quota", "enrolled", "content"));
            context.put("最新健康资讯", safeRows(articles(), "title", "summary", "category", "tags"));
            context.put("最新健康讲堂", safeRows(videos(), "title", "description", "lecturer", "category", "duration"));
            return objectMapper.writeValueAsString(context);
        } catch (Exception ex) {
            throw new AiServiceUnavailableException(ex);
        }
    }

    private List<Map<String, Object>> profile(long accountId) {
        return jdbcTemplate.queryForList(
                "select timestampdiff(year, u.birthday, curdate()) as age, " +
                        "case u.gender when 1 then '男' when 2 then '女' else '未知' end as gender, " +
                        "u.height, u.weight, u.chronic_disease as chronicDisease, u.sleep_quality as sleepQuality, " +
                        "u.smoking_freq as smokingFreq, u.drinking_freq as drinkingFreq, " +
                        "u.exercise_freq as exerciseFreq, u.diet_preference as dietPreference " +
                        "from elderly_profile p join `user` u on u.id = p.legacy_user_id where p.account_id = ? limit 1",
                accountId
        );
    }

    private List<Map<String, Object>> healthData(long accountId) {
        return jdbcTemplate.queryForList(
                "select case h.data_type when 'heart_rate' then '心率' when 'weight' then '体重' " +
                        "when 'blood_pressure' then '血压' when 'blood_glucose' then '血糖' else h.data_type end as dataType, " +
                        "h.value, h.unit, date_format(h.record_date, '%Y-%m-%d') as recordDate, " +
                        "date_format(h.record_time, '%H:%i') as recordTime, h.source " +
                        "from health_data h join elderly_profile p on p.legacy_user_id = h.user_id " +
                        "where p.account_id = ? and h.record_date >= date_sub(curdate(), interval 30 day) " +
                        "order by h.record_date desc, h.record_time desc, h.id desc limit 30",
                accountId
        );
    }

    private List<Map<String, Object>> medications(long accountId) {
        return jdbcTemplate.queryForList(
                "select m.period, m.drug_name as drugName, m.frequency, date_format(m.take_time, '%H:%i') as takeTime, " +
                        "m.dosage, m.reminder_enabled = 1 as reminderEnabled, m.source " +
                        "from medication_record m join elderly_profile p on p.legacy_user_id = m.user_id " +
                        "where p.account_id = ? order by m.take_time, m.id limit 30",
                accountId
        );
    }

    private List<Map<String, Object>> reports(long accountId) {
        return jdbcTemplate.queryForList(
                "select r.title, r.report_type as reportType, date_format(r.report_date, '%Y-%m-%d') as reportDate, " +
                        "r.summary, r.doctor_name as doctorName from report r " +
                        "join elderly_profile p on p.legacy_user_id = r.user_id where p.account_id = ? " +
                        "order by r.report_date desc, r.id desc limit 5",
                accountId
        );
    }

    private List<Map<String, Object>> products() {
        return jdbcTemplate.queryForList(
                "select name, category, price from product where status = 1 order by id limit 50"
        );
    }

    private List<Map<String, Object>> activities() {
        return jdbcTemplate.queryForList(
                "select title, location, date_format(start_time, '%Y-%m-%d %H:%i') as startTime, " +
                        "date_format(end_time, '%Y-%m-%d %H:%i') as endTime, quota, enrolled, content " +
                        "from activity where status = 'published' and end_time >= now() order by start_time limit 30"
        );
    }

    private List<Map<String, Object>> articles() {
        return jdbcTemplate.queryForList(
                "select title, summary, category, tags from article where status = 1 order by created_at desc, id desc limit 20"
        );
    }

    private List<Map<String, Object>> videos() {
        return jdbcTemplate.queryForList(
                "select title, description, lecturer, category, duration from video where status = 1 " +
                        "order by created_at desc, id desc limit 20"
        );
    }

    private Map<String, Object> safeFirst(List<Map<String, Object>> rows, String... keys) {
        if (rows == null || rows.isEmpty()) return new LinkedHashMap<String, Object>();
        return safeRow(rows.get(0), Arrays.asList(keys));
    }

    private List<Map<String, Object>> safeRows(List<Map<String, Object>> rows, String... keys) {
        List<Map<String, Object>> safe = new ArrayList<Map<String, Object>>();
        if (rows == null) return safe;
        List<String> allowed = Arrays.asList(keys);
        for (Map<String, Object> row : rows) safe.add(safeRow(row, allowed));
        return safe;
    }

    private Map<String, Object> safeRow(Map<String, Object> row, List<String> keys) {
        Map<String, Object> safe = new LinkedHashMap<String, Object>();
        for (String key : keys) {
            if (row.containsKey(key)) safe.put(key, row.get(key));
        }
        return safe;
    }

    private String loadPrompt() {
        try {
            return StreamUtils.copyToString(
                    new ClassPathResource("prompts/elderly-ai-customer-service.md").getInputStream(),
                    StandardCharsets.UTF_8
            );
        } catch (Exception ex) {
            throw new IllegalStateException("AI 系统提示词加载失败", ex);
        }
    }
}
