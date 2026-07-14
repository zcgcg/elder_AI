package com.daisy.health.service;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Profile("mock")
public class MockPortalDataService implements PortalDataService {
    private final Set<Long> joinedActivityIds = new HashSet<Long>();
    private final List<Map<String, Object>> messages = new ArrayList<Map<String, Object>>();

    @Override
    public Map<String, Object> elderlyProfile() {
        return record("realName", "演示用户", "phone", "13800010001", "userId", 10001);
    }

    @Override
    public Map<String, Object> updateElderlyProfile(Map<String, Object> payload) {
        Map<String, Object> result = elderlyProfile();
        if (payload != null) result.putAll(payload);
        return result;
    }

    @Override
    public Map<String, Object> updateElderlyAvatar(Map<String, Object> payload) {
        return record("realName", "演示用户", "phone", "13800010001", "userId", 10001,
                "avatarUrl", payload == null ? "" : payload.get("avatarUrl"));
    }

    @Override
    public List<Map<String, Object>> elderlyHealthData() {
        return list(record("dataType", "heart_rate", "value", "73", "unit", "bpm", "recordDate", "2026-07-09"));
    }

    @Override
    public List<Map<String, Object>> elderlyMedications() {
        return list(record("drugName", "硝苯地平控释片", "period", "早餐", "takeTime", "08:00", "dosage", "1片"));
    }

    @Override
    public List<Map<String, Object>> elderlyDevices() {
        return list(record("id", 1L, "deviceName", "智能手环", "deviceType", "band", "deviceCode", "DEMO-001", "status", "绑定"));
    }

    @Override
    public Map<String, Object> updateElderlyDevice(Long id, Map<String, Object> payload) {
        Map<String, Object> result = record("id", id);
        if (payload != null) result.putAll(payload);
        return result;
    }

    @Override
    public List<Map<String, Object>> elderlyReports() {
        return new ArrayList<Map<String, Object>>();
    }

    @Override
    public List<Map<String, Object>> elderlyOrders() {
        return list(record("orderNo", "OD20260706002", "productName", "助浴护理", "status", "pending_service"));
    }

    @Override
    public List<Map<String, Object>> elderlyReviews() {
        return list(record("orderId", 1L, "orderNo", "OD20260706001", "productName", "助浴护理",
                "personnelName", "张敏", "completeTime", "2026-07-09 10:00", "reviewed", false));
    }

    @Override
    public Map<String, Object> createElderlyReview(Map<String, Object> payload) {
        return record("orderId", payload.get("orderId"), "rating", payload.get("rating"),
                "content", payload.get("content"), "reviewed", true, "reviewedAt", "2026-07-14 10:00");
    }

    @Override
    public List<Map<String, Object>> elderlyCoupons() {
        return new ArrayList<Map<String, Object>>();
    }

    @Override
    public Map<String, Object> elderlyPoints() {
        return record("points", 1280, "level", "银卡", "records", new ArrayList<Map<String, Object>>());
    }

    @Override
    public List<Map<String, Object>> elderlyCatalogItems() {
        return list(record("id", 1, "name", "助浴护理", "itemType", "服务", "category", "家政护理", "price", 199));
    }

    @Override
    public List<Map<String, Object>> elderlyPersonnel() {
        return list(record("id", 1L, "name", "张敏", "serviceType", "家政护理", "area", "浦东新区", "rating", 4.8));
    }

    @Override
    public List<Map<String, Object>> elderlyWorkOrders() {
        return new ArrayList<Map<String, Object>>();
    }

    @Override
    public Map<String, Object> createElderlyWorkOrder(Map<String, Object> payload) {
        return record("id", 2, "orderNo", "WO20260710001", "productId", payload.get("productId"), "personnelId", payload.get("personnelId"), "personnelName", "张敏", "serviceItem", "助浴护理", "amount", 199, "status", "待服务", "dispatchTime", "2026-07-10 10:00");
    }

    @Override
    public Map<String, Object> cancelElderlyWorkOrder(Long workOrderId, Map<String, Object> payload) {
        return record("id", workOrderId, "status", "已取消", "cancelReason", payload == null ? "用户取消" : payload.get("reason"));
    }

    @Override
    public Map<String, Object> rescheduleElderlyWorkOrder(Long workOrderId, Map<String, Object> payload) {
        return record("id", workOrderId, "status", "待服务", "serviceTime", payload.get("serviceTime"));
    }

    @Override
    public List<Map<String, Object>> elderlyActivities() {
        boolean joined = joinedActivityIds.contains(1L);
        return list(record(
                "id", 1L,
                "title", "社区健康义诊",
                "coverUrl", "",
                "location", "社区活动中心",
                "startTime", "2026-07-20 09:00",
                "endTime", "2026-07-20 11:30",
                "quota", 50,
                "enrolled", joined ? 19 : 18,
                "status", "已发布",
                "content", "提供血压、血糖检测及健康咨询服务。",
                "joined", joined,
                "canJoin", !joined,
                "enrollmentStatus", joined ? "已报名" : null,
                "enrollTime", joined ? "2026-07-13 10:00" : null
        ));
    }

    @Override
    public Map<String, Object> enrollElderlyActivity(Long activityId) {
        if (!Long.valueOf(1L).equals(activityId)) throw new IllegalArgumentException("活动不存在或已结束");
        joinedActivityIds.add(activityId);
        return record("activityId", activityId, "joined", true, "status", "已报名");
    }

    @Override
    public Map<String, Object> cancelElderlyActivity(Long activityId) {
        joinedActivityIds.remove(activityId);
        return record("activityId", activityId, "joined", false, "status", "已取消");
    }

    @Override
    public List<Map<String, Object>> elderlyMessages() {
        return new ArrayList<Map<String, Object>>(messages);
    }

    @Override
    public Map<String, Object> createElderlyMessage(Map<String, Object> payload) {
        Map<String, Object> message = record("id", messages.size() + 1L, "content", payload.get("content"), "status", "待处理", "createdAt", "2026-07-13 10:00");
        messages.add(0, message);
        return message;
    }

    @Override
    public List<Map<String, Object>> elderlyHealthArticles() {
        return list(record("id", 1L, "title", "夏季健康饮食", "summary", "清淡饮食与科学补水", "author", "健康中心"));
    }

    @Override
    public List<Map<String, Object>> elderlyHealthVideos() {
        return list(record("id", 1L, "title", "居家防跌倒", "lecturer", "李医生", "duration", 600));
    }

    @Override
    public Map<String, Object> serviceProfile() {
        return record("realName", "演示服务人员", "phone", "13900020001", "personnelId", 1);
    }

    @Override
    public List<Map<String, Object>> serviceWorkOrders() {
        return list(record("id", 1, "orderNo", "WO20260706001", "serviceItem", "助浴护理", "status", "pending"));
    }

    @Override
    public Map<String, Object> serviceWorkOrder(Long id) {
        return record("id", id, "orderNo", "WO20260706001", "serviceItem", "助浴护理", "status", "pending");
    }

    @Override
    public Map<String, Object> updateServiceWorkOrderStatus(Long id, Map<String, Object> payload) {
        return record("id", id, "status", payload == null ? "pending" : payload.get("status"));
    }

    private Map<String, Object> record(Object... values) {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        for (int i = 0; i < values.length; i += 2) {
            map.put(String.valueOf(values[i]), values[i + 1]);
        }
        return map;
    }

    private List<Map<String, Object>> list(Map<String, Object> row) {
        List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
        rows.add(row);
        return rows;
    }
}
