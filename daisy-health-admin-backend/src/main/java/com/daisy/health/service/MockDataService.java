package com.daisy.health.service;

import com.daisy.health.common.JwtService;
import com.daisy.health.common.PageResult;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.time.LocalDate;

@Service
@Profile("mock")
public class MockDataService implements AdminDataService {
    private final JwtService jwtService;

    public MockDataService(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    public Map<String, Object> login(Map<String, Object> payload) {
        String phone = stringValue(payload.get("phone"));
        String password = stringValue(payload.get("password"));
        if (phone.length() == 0 || password.length() == 0) {
            throw new IllegalArgumentException("账号或密码不能为空");
        }
        return record(
                "token", jwtService.createToken(1L, "staff", phone),
                "user", record("id", 1, "name", "系统管理员", "phone", phone, "role", "超级管理员")
        );
    }

    public Map<String, Object> profile() {
        return record("id", 1, "staffNo", "S0001", "name", "系统管理员", "phone", "13800000000", "avatarUrl", "", "role", "超级管理员", "remark", "初始化管理员");
    }

    public Map<String, Object> updateProfile(Map<String, Object> payload) {
        return record(
                "id", 1,
                "staffNo", payload.containsKey("staffNo") ? payload.get("staffNo") : "S0001",
                "name", payload.containsKey("name") ? payload.get("name") : "系统管理员",
                "phone", payload.containsKey("phone") ? payload.get("phone") : "13800000000",
                "avatarUrl", payload.containsKey("avatarUrl") ? payload.get("avatarUrl") : "",
                "role", payload.containsKey("role") ? payload.get("role") : "超级管理员",
                "remark", payload.get("remark")
        );
    }

    public Map<String, Object> dashboard() {
        return record(
                "metrics", list(
                        record("label", "新增用户", "value", 128, "delta", "+12.4%", "tone", "success"),
                        record("label", "新增工单", "value", 76, "delta", "+8.7%", "tone", "warning"),
                        record("label", "新增订单", "value", 214, "delta", "+16.1%", "tone", "success"),
                        record("label", "新增动态", "value", 43, "delta", "-3.2%", "tone", "danger")
                ),
                "tagDistribution", list(
                        record("name", "高血压", "value", 168),
                        record("name", "独居老人", "value", 132),
                        record("name", "康复护理", "value", 98),
                        record("name", "糖尿病", "value", 86),
                        record("name", "重点关怀", "value", 64)
                ),
                "serviceShare", list(
                        record("name", "家政护理", "value", 42),
                        record("name", "康复理疗", "value", 34),
                        record("name", "上门体检", "value", 24)
                ),
                "trend", list(
                        record("day", "周一", "users", 18, "orders", 32),
                        record("day", "周二", "users", 24, "orders", 40),
                        record("day", "周三", "users", 36, "orders", 46),
                        record("day", "周四", "users", 42, "orders", 51),
                        record("day", "周五", "users", 48, "orders", 62),
                        record("day", "周六", "users", 56, "orders", 71),
                        record("day", "周日", "users", 64, "orders", 86)
                )
        );
    }

    public List<Map<String, Object>> appointments(ResourceQuery query) {
        LocalDate start = query.startDateOr(LocalDate.now());
        LocalDate end = query.endDateOr(start.plusDays(6));
        if (end.isBefore(start) || end.isAfter(start.plusDays(6))) {
            throw new IllegalArgumentException("预约看板最多查询连续 7 天");
        }
        List<Map<String, Object>> rows = list(
                record("id", 1, "serviceDate", start.toString(), "productId", 4, "hour", 9, "serviceName", "助浴护理", "timeRange", "09:00-10:30", "userName", "王秀兰", "status", "待服务"),
                record("id", 2, "serviceDate", start.plusDays(1).toString(), "productId", 2, "hour", 10, "serviceName", "肩颈康复", "timeRange", "10:00-11:00", "userName", "陈建国", "status", "服务中")
        );
        return query.filter(rows);
    }

    public PageResult<Map<String, Object>> users(ResourceQuery query) {
        List<Map<String, Object>> rows = list(
                userSummary(10001, "兰姨", "王秀兰", "138****0001", "2026-06-18", "高血压", "重点关怀"),
                userSummary(10002, "建国叔", "陈建国", "138****0002", "2026-06-21", "康复护理"),
                userSummary(10003, "桂英", "赵桂英", "138****0003", "2026-06-28", "独居老人")
        );
        List<Map<String, Object>> filtered = query.filter(rows);
        return new PageResult<Map<String, Object>>(filtered.size(), filtered);
    }

    public Map<String, Object> createUser(Map<String, Object> payload) {
        return record("accepted", true, "id", 99999, "action", "createUser");
    }

    public Map<String, Object> updateUser(Long id, Map<String, Object> payload) {
        return record("accepted", true, "id", id, "action", "updateUser");
    }

    public Map<String, Object> deleteUser(Long id) {
        return record("accepted", true, "id", id, "action", "deleteUser");
    }

    public Map<String, Object> user(Long id) {
        Map<String, Object> row = userSummary(id, "兰姨", "王秀兰", "138****0001", "2026-06-18", "高血压", "重点关怀");
        row.put("gender", "女");
        row.put("birthday", "1952-03-12");
        row.put("address", "上海市浦东新区");
        row.put("height", 158);
        row.put("weight", 58.4);
        row.put("bloodType", "A");
        row.put("chronicDisease", "高血压");
        row.put("sleepQuality", "良好");
        row.put("exerciseFreq", "每周3次");
        row.put("lastBuyTime", "2026-07-02");
        row.put("lastLoginTime", "2026-07-06 08:30");
        row.put("medications", medications());
        row.put("healthData", healthData());
        return row;
    }

    public PageResult<Map<String, Object>> tags() {
        List<Map<String, Object>> rows = list(
                record("id", 1, "name", "潜在客户", "type", "manual", "color", "gray", "userCount", 0, "status", "启用"),
                record("id", 2, "name", "重点客户", "type", "manual", "color", "green", "userCount", 1, "status", "启用"),
                record("id", 3, "name", "普通客户", "type", "manual", "color", "blue", "userCount", 1, "status", "启用"),
                record("id", 4, "name", "多次消费客户", "type", "manual", "color", "purple", "userCount", 1, "status", "启用"),
                record("id", 5, "name", "高血压", "type", "manual", "color", "green", "userCount", 1, "status", "启用"),
                record("id", 6, "name", "高血糖", "type", "manual", "color", "orange", "userCount", 1, "status", "启用"),
                record("id", 7, "name", "高血脂", "type", "manual", "color", "purple", "userCount", 1, "status", "启用"),
                record("id", 8, "name", "慢性病", "type", "manual", "color", "red", "userCount", 1, "status", "启用")
        );
        return new PageResult<Map<String, Object>>(rows.size(), rows);
    }

    public Map<String, Object> createTag(Map<String, Object> payload) {
        return record("accepted", true, "id", 99999, "resource", "tags");
    }

    public Map<String, Object> updateTag(Long id, Map<String, Object> payload) {
        return record("accepted", true, "id", id, "resource", "tags");
    }

    public Map<String, Object> deleteTag(Long id) {
        return record("accepted", true, "id", id, "resource", "tags");
    }

    public Map<String, Object> updateUserTags(Long userId, Map<String, Object> payload) {
        return record("accepted", true, "id", userId, "resource", "userTags");
    }

    public PageResult<Map<String, Object>> resource(String name, ResourceQuery query) {
        List<Map<String, Object>> rows = resources().get(name);
        if (rows == null) {
            rows = resources().get("posts");
        }
        List<Map<String, Object>> filtered = query.filter(rows);
        return new PageResult<Map<String, Object>>(filtered.size(), filtered);
    }

    @Override
    public PageResult<Map<String, Object>> workOrders(Long personnelId, Long customerId, ResourceQuery query) {
        List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
        for (Map<String, Object> row : resources().get("workOrders")) {
            if (personnelId != null && !String.valueOf(personnelId).equals(String.valueOf(row.get("personnelId")))) continue;
            if (customerId != null && !String.valueOf(customerId).equals(String.valueOf(row.get("customerId")))) continue;
            rows.add(row);
        }
        List<Map<String, Object>> filtered = query.filter(rows);
        return new PageResult<Map<String, Object>>(filtered.size(), filtered);
    }

    public Map<String, Object> createResource(String name, Map<String, Object> payload) {
        return record("accepted", true, "id", 99999, "resource", name);
    }

    public Map<String, Object> updateResource(String name, Long id, Map<String, Object> payload) {
        return record("accepted", true, "id", id, "resource", name, "action", "update");
    }

    public Map<String, Object> deleteResource(String name, Long id) {
        return record("accepted", true, "id", id, "resource", name, "action", "delete");
    }

    public Map<String, Object> analyticsOverview(ResourceQuery query) {
        return record(
                "metrics", list(
                        record("label", "新增用户", "value", 12, "delta", "所选时段"),
                        record("label", "成交金额", "value", "¥8640", "delta", "所选时段"),
                        record("label", "完成工单", "value", 9, "delta", "所选时段"),
                        record("label", "好评率", "value", "96.8%", "delta", "所选时段")
                ),
                "ageDistribution", list(record("name", "60-69岁", "value", 4), record("name", "70-79岁", "value", 5), record("name", "80岁以上", "value", 3)),
                "tradeDistribution", list(record("name", "家政护理", "value", 6), record("name", "康复理疗", "value", 4)),
                "serviceTrend", list(record("day", "07-12", "orders", 3, "amount", 1260), record("day", "07-13", "orders", 5, "amount", 2100))
        );
    }

    public Map<String, Object> accepted(String action) {
        return record("accepted", true, "action", action, "operator", "系统管理员");
    }

    private List<Map<String, Object>> medications() {
        return list(
                record("period", "早餐", "drugName", "硝苯地平控释片", "frequency", "每天", "takeTime", "08:00", "dosage", "1片", "reminderEnabled", true),
                record("period", "晚餐", "drugName", "阿司匹林", "frequency", "每天", "takeTime", "19:00", "dosage", "1片", "reminderEnabled", true)
        );
    }

    private List<Map<String, Object>> healthData() {
        return list(
                record("id", 1, "recordDate", "2026-07-01", "dataType", "weight", "value", 58.8, "unit", "kg", "source", "智能体脂秤"),
                record("id", 2, "recordDate", "2026-07-01", "dataType", "heart_rate", "value", 76, "unit", "bpm", "source", "智能手环"),
                record("id", 3, "recordDate", "2026-07-02", "dataType", "weight", "value", 58.6, "unit", "kg", "source", "智能体脂秤"),
                record("id", 4, "recordDate", "2026-07-02", "dataType", "heart_rate", "value", 74, "unit", "bpm", "source", "智能手环"),
                record("id", 5, "recordDate", "2026-07-03", "dataType", "weight", "value", 58.5, "unit", "kg", "source", "智能体脂秤"),
                record("id", 6, "recordDate", "2026-07-03", "dataType", "heart_rate", "value", 78, "unit", "bpm", "source", "智能手环")
        );
    }

    private Map<String, List<Map<String, Object>>> resources() {
        Map<String, List<Map<String, Object>>> data = new LinkedHashMap<String, List<Map<String, Object>>>();
        data.put("personnel", list(
                record("id", 1, "name", "张敏", "phone", "13900020001", "serviceType", "家政护理", "area", "浦东新区", "auditStatus", "已通过", "status", "启用", "updatedAt", "2026-07-05 10:20"),
                record("id", 2, "name", "李华", "phone", "13900020002", "serviceType", "康复理疗", "area", "徐汇区", "auditStatus", "已通过", "status", "启用", "updatedAt", "2026-07-04 15:30")
        ));
        data.put("audits", list(record("id", 1, "name", "周丽", "serviceType", "上门体检", "auditStatus", "待审核", "phone", "138****8123", "updatedAt", "2026-07-06 09:10")));
        data.put("workOrders", list(
                record("id", 1, "orderNo", "WO20260706001", "serviceItem", "助浴护理", "personnelId", 1, "personnelName", "张敏", "customerId", 10001, "customer", "王秀兰", "status", "待服务", "updatedAt", "2026-07-06 11:00"),
                record("id", 2, "orderNo", "WO20260706002", "serviceItem", "肩颈康复", "personnelId", 2, "personnelName", "李华", "customerId", 10002, "customer", "陈建国", "status", "服务中", "updatedAt", "2026-07-06 12:00")
        ));
        data.put("products", list(
                record("id", 1, "name", "2小时日常清洁", "category", "家政护理", "price", 129, "status", "上架", "updatedAt", "2026-07-03 17:00"),
                record("id", 2, "name", "脑中风康复训练", "category", "康复理疗", "price", 299, "status", "上架", "updatedAt", "2026-07-01 14:30")
        ));
        data.put("orders", list(record("id", 1, "orderNo", "OD20260706001", "productName", "上门基础体检", "buyer", "赵桂英", "amount", 399, "status", "待接单")));
        data.put("afterSales", list(record("id", 1, "orderNo", "OD20260702008", "applicant", "刘爱华", "reason", "服务时间变更", "status", "处理中")));
        data.put("reviews", list(record("id", 1, "productName", "助餐服务", "user", "沈美玲", "rating", 5, "status", "已显示")));
        data.put("posts", list(record("id", 1, "title", "社区晨练打卡", "publisher", "王秀兰", "likes", 89, "status", "已发布")));
        data.put("activities", list(record("id", 1, "title", "慢病管理讲座", "location", "浦东社区中心", "quota", 60, "status", "报名中")));
        data.put("articles", list(record("id", 1, "title", "夏季老人补水指南", "author", "运营中心", "status", "已发布")));
        data.put("staffs", list(record("id", 1, "staffNo", "S0001", "name", "系统管理员", "role", "超级管理员", "status", "启用")));
        data.put("roles", list(record("id", 1, "name", "超级管理员", "description", "全部模块与全部操作", "status", "启用")));
        data.put("logs", list(record("id", 1, "operator", "系统管理员", "actionType", "登录", "target", "后台系统", "createdAt", "2026-07-06 09:00")));
        return data;
    }

    private Map<String, Object> userSummary(Object id, String nickname, String realName, String phone, String createdAt, String... tags) {
        return record("id", id, "nickname", nickname, "realName", realName, "phone", phone, "tags", Arrays.asList(tags), "createdAt", createdAt);
    }

    private Map<String, Object> record(Object... values) {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        for (int i = 0; i < values.length; i += 2) {
            map.put(String.valueOf(values[i]), values[i + 1]);
        }
        return map;
    }

    private List<Map<String, Object>> list(Map<String, Object>... rows) {
        return new ArrayList<Map<String, Object>>(Arrays.asList(rows));
    }

    private String stringValue(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}
