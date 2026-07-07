package com.daisy.health.controller;

import com.daisy.health.common.ApiResponse;
import com.daisy.health.service.AdminDataService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/analytics")
public class AnalyticsController {
    private final AdminDataService dataService;

    public AnalyticsController(AdminDataService dataService) {
        this.dataService = dataService;
    }

    @GetMapping({"/users/overview", "/users/age", "/users/gender", "/users/social", "/trade/overview", "/trade/products", "/trade/repurchase", "/service/work-orders", "/service/performance", "/reviews"})
    public ApiResponse<Map<String, Object>> analytics() {
        return ApiResponse.success(dataService.analyticsOverview());
    }
}

