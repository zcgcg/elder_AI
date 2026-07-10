package com.daisy.health.service;

import java.util.List;
import java.util.Map;

public interface PortalDataService {
    Map<String, Object> elderlyProfile();

    List<Map<String, Object>> elderlyHealthData();

    List<Map<String, Object>> elderlyMedications();

    List<Map<String, Object>> elderlyDevices();

    List<Map<String, Object>> elderlyReports();

    List<Map<String, Object>> elderlyOrders();

    List<Map<String, Object>> elderlyCoupons();

    Map<String, Object> elderlyPoints();

    List<Map<String, Object>> elderlyCatalogItems();

    List<Map<String, Object>> elderlyWorkOrders();

    Map<String, Object> createElderlyWorkOrder(Map<String, Object> payload);

    Map<String, Object> serviceProfile();

    List<Map<String, Object>> serviceWorkOrders();

    Map<String, Object> serviceWorkOrder(Long id);

    Map<String, Object> updateServiceWorkOrderStatus(Long id, Map<String, Object> payload);
}
