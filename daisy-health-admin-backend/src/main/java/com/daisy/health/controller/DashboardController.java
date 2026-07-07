package com.daisy.health.controller;

import com.daisy.health.common.ApiResponse;
import com.daisy.health.service.AdminDataService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class DashboardController {
    private final AdminDataService dataService;

    public DashboardController(AdminDataService dataService) {
        this.dataService = dataService;
    }

    @GetMapping("/dashboard")
    public ApiResponse<Map<String, Object>> dashboard() {
        return ApiResponse.success(dataService.dashboard());
    }

    @GetMapping("/appointments")
    public ApiResponse<List<Map<String, Object>>> appointments() {
        return ApiResponse.success(dataService.appointments());
    }

    @PostMapping("/appointments")
    public ApiResponse<Map<String, Object>> createAppointment(@RequestBody Map<String, Object> payload) {
        return ApiResponse.success(dataService.createResource("appointments", payload));
    }

    @GetMapping("/analytics/overview")
    public ApiResponse<Map<String, Object>> analyticsOverview() {
        return ApiResponse.success(dataService.analyticsOverview());
    }
}

