package com.daisy.health.service;

import java.util.List;
import java.util.Map;

public interface PortalDataService {
    Map<String, Object> elderlyProfile();

    Map<String, Object> updateElderlyProfile(Map<String, Object> payload);

    Map<String, Object> updateElderlyAvatar(Map<String, Object> payload);

    List<Map<String, Object>> elderlyHealthData();

    List<Map<String, Object>> elderlyMedications();

    List<Map<String, Object>> elderlyDevices();

    Map<String, Object> updateElderlyDevice(Long id, Map<String, Object> payload);

    List<Map<String, Object>> elderlyReports();

    List<Map<String, Object>> elderlyOrders();

    List<Map<String, Object>> elderlyReviews();

    Map<String, Object> createElderlyReview(Map<String, Object> payload);

    List<Map<String, Object>> elderlyCoupons();

    Map<String, Object> elderlyPoints();

    List<Map<String, Object>> elderlyCatalogItems();

    List<Map<String, Object>> elderlyPersonnel();

    List<Map<String, Object>> elderlyWorkOrders();

    Map<String, Object> createElderlyWorkOrder(Map<String, Object> payload);

    Map<String, Object> cancelElderlyWorkOrder(Long workOrderId, Map<String, Object> payload);

    Map<String, Object> rescheduleElderlyWorkOrder(Long workOrderId, Map<String, Object> payload);

    List<Map<String, Object>> elderlyActivities();

    Map<String, Object> enrollElderlyActivity(Long activityId);

    Map<String, Object> cancelElderlyActivity(Long activityId);

    List<Map<String, Object>> elderlyMessages();

    Map<String, Object> createElderlyMessage(Map<String, Object> payload);

    List<Map<String, Object>> elderlyHealthArticles();

    List<Map<String, Object>> elderlyHealthVideos();

    Map<String, Object> serviceProfile();

    List<Map<String, Object>> serviceWorkOrders();

    Map<String, Object> serviceWorkOrder(Long id);

    Map<String, Object> updateServiceWorkOrderStatus(Long id, Map<String, Object> payload);
}
