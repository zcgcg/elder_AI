package com.daisy.health.controller;

import com.daisy.health.common.ApiResponse;
import com.daisy.health.service.PortalDataService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/service-app")
public class ServiceAppController {
    private final PortalDataService portalDataService;

    public ServiceAppController(PortalDataService portalDataService) {
        this.portalDataService = portalDataService;
    }

    @GetMapping("/profile")
    public ApiResponse<Map<String, Object>> profile() {
        return ApiResponse.success(portalDataService.serviceProfile());
    }

    @GetMapping("/work-orders")
    public ApiResponse<List<Map<String, Object>>> workOrders() {
        return ApiResponse.success(portalDataService.serviceWorkOrders());
    }

    @GetMapping("/work-orders/{id}")
    public ApiResponse<Map<String, Object>> workOrder(@PathVariable Long id) {
        return ApiResponse.success(portalDataService.serviceWorkOrder(id));
    }

    @PutMapping("/work-orders/{id}/status")
    public ApiResponse<Map<String, Object>> updateStatus(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        return ApiResponse.success(portalDataService.updateServiceWorkOrderStatus(id, payload));
    }
}
