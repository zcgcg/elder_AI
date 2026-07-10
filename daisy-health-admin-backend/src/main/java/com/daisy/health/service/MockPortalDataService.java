package com.daisy.health.service;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Profile("mock")
public class MockPortalDataService implements PortalDataService {
    @Override
    public Map<String, Object> elderlyProfile() {
        return record("realName", "演示用户", "phone", "13800010001", "userId", 10001);
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
        return list(record("deviceName", "智能手环", "status", "绑定"));
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
    public List<Map<String, Object>> elderlyWorkOrders() {
        return new ArrayList<Map<String, Object>>();
    }

    @Override
    public Map<String, Object> createElderlyWorkOrder(Map<String, Object> payload) {
        return record("id", 2, "orderNo", "WO20260710001", "productId", payload.get("productId"), "serviceItem", "助浴护理", "amount", 199, "status", "待服务", "dispatchTime", "2026-07-10 10:00");
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
