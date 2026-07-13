package com.daisy.health.controller;

import com.daisy.health.common.ApiResponse;
import com.daisy.health.service.PortalDataService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/elderly")
public class ElderlyPortalController {
    private final PortalDataService portalDataService;

    public ElderlyPortalController(PortalDataService portalDataService) {
        this.portalDataService = portalDataService;
    }

    @GetMapping("/profile")
    public ApiResponse<Map<String, Object>> profile() {
        return ApiResponse.success(portalDataService.elderlyProfile());
    }

    @PutMapping("/profile")
    public ApiResponse<Map<String, Object>> updateProfile(@RequestBody Map<String, Object> payload) {
        return ApiResponse.success(portalDataService.updateElderlyProfile(payload));
    }

    @PutMapping("/profile/avatar")
    public ApiResponse<Map<String, Object>> updateAvatar(@RequestBody Map<String, Object> payload) {
        return ApiResponse.success(portalDataService.updateElderlyAvatar(payload));
    }

    @GetMapping("/health-data")
    public ApiResponse<List<Map<String, Object>>> healthData() {
        return ApiResponse.success(portalDataService.elderlyHealthData());
    }

    @GetMapping("/medications")
    public ApiResponse<List<Map<String, Object>>> medications() {
        return ApiResponse.success(portalDataService.elderlyMedications());
    }

    @GetMapping("/devices")
    public ApiResponse<List<Map<String, Object>>> devices() {
        return ApiResponse.success(portalDataService.elderlyDevices());
    }

    @PutMapping("/devices/{id}")
    public ApiResponse<Map<String, Object>> updateDevice(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        return ApiResponse.success(portalDataService.updateElderlyDevice(id, payload));
    }

    @GetMapping("/reports")
    public ApiResponse<List<Map<String, Object>>> reports() {
        return ApiResponse.success(portalDataService.elderlyReports());
    }

    @GetMapping("/orders")
    public ApiResponse<List<Map<String, Object>>> orders() {
        return ApiResponse.success(portalDataService.elderlyOrders());
    }

    @GetMapping("/coupons")
    public ApiResponse<List<Map<String, Object>>> coupons() {
        return ApiResponse.success(portalDataService.elderlyCoupons());
    }

    @GetMapping("/points")
    public ApiResponse<Map<String, Object>> points() {
        return ApiResponse.success(portalDataService.elderlyPoints());
    }

    @GetMapping("/catalog-items")
    public ApiResponse<List<Map<String, Object>>> catalogItems() {
        return ApiResponse.success(portalDataService.elderlyCatalogItems());
    }

    @GetMapping("/personnel")
    public ApiResponse<List<Map<String, Object>>> personnel() {
        return ApiResponse.success(portalDataService.elderlyPersonnel());
    }

    @GetMapping("/work-orders")
    public ApiResponse<List<Map<String, Object>>> workOrders() {
        return ApiResponse.success(portalDataService.elderlyWorkOrders());
    }

    @PostMapping("/work-orders")
    public ApiResponse<Map<String, Object>> createWorkOrder(@RequestBody Map<String, Object> payload) {
        return ApiResponse.success(portalDataService.createElderlyWorkOrder(payload));
    }

    @GetMapping("/activities")
    public ApiResponse<List<Map<String, Object>>> activities() {
        return ApiResponse.success(portalDataService.elderlyActivities());
    }

    @PostMapping("/activities/{id}/enroll")
    public ApiResponse<Map<String, Object>> enrollActivity(@PathVariable Long id) {
        return ApiResponse.success(portalDataService.enrollElderlyActivity(id));
    }

    @GetMapping("/health-articles")
    public ApiResponse<List<Map<String, Object>>> healthArticles() {
        return ApiResponse.success(portalDataService.elderlyHealthArticles());
    }

    @GetMapping("/health-videos")
    public ApiResponse<List<Map<String, Object>>> healthVideos() {
        return ApiResponse.success(portalDataService.elderlyHealthVideos());
    }
}
