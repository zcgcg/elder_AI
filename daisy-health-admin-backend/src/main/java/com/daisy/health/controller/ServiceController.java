package com.daisy.health.controller;

import com.daisy.health.common.ApiResponse;
import com.daisy.health.common.PageResult;
import com.daisy.health.service.AdminDataService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class ServiceController {
    private final AdminDataService dataService;

    public ServiceController(AdminDataService dataService) {
        this.dataService = dataService;
    }

    @GetMapping("/personnel")
    public ApiResponse<PageResult<Map<String, Object>>> personnel() {
        return ApiResponse.success(dataService.resource("personnel"));
    }

    @PostMapping("/personnel")
    public ApiResponse<Map<String, Object>> createPersonnel(@RequestBody Map<String, Object> payload) {
        return ApiResponse.success(dataService.createResource("personnel", payload));
    }

    @PutMapping("/personnel/{id}")
    public ApiResponse<Map<String, Object>> updatePersonnel(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        return ApiResponse.success(dataService.updateResource("personnel", id, payload));
    }

    @DeleteMapping("/personnel/{id}")
    public ApiResponse<Map<String, Object>> deletePersonnel(@PathVariable Long id) {
        return ApiResponse.success(dataService.deleteResource("personnel", id));
    }

    @GetMapping("/audits")
    public ApiResponse<PageResult<Map<String, Object>>> audits() {
        return ApiResponse.success(dataService.resource("audits"));
    }

    @PutMapping("/audits/{id}")
    public ApiResponse<Map<String, Object>> updateAudit(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        return ApiResponse.success(dataService.updateResource("audits", id, payload));
    }

    @PostMapping("/audits/{id}/approve")
    public ApiResponse<Map<String, Object>> approve(@PathVariable Long id) {
        return ApiResponse.success(dataService.accepted("approveAudit:" + id));
    }

    @PostMapping("/audits/{id}/reject")
    public ApiResponse<Map<String, Object>> reject(@PathVariable Long id) {
        return ApiResponse.success(dataService.accepted("rejectAudit:" + id));
    }

    @GetMapping("/work-orders")
    public ApiResponse<PageResult<Map<String, Object>>> workOrders() {
        return ApiResponse.success(dataService.resource("workOrders"));
    }

    @GetMapping("/work-orders/{id}")
    public ApiResponse<Map<String, Object>> workOrder(@PathVariable Long id) {
        return ApiResponse.success(dataService.accepted("workOrder:" + id));
    }

    @PostMapping("/work-orders")
    public ApiResponse<Map<String, Object>> createWorkOrder(@RequestBody Map<String, Object> payload) {
        return ApiResponse.success(dataService.createResource("workOrders", payload));
    }

    @PutMapping({"/work-orders/{id}/reassign", "/work-orders/{id}/cancel", "/work-orders/{id}/start", "/work-orders/{id}/complete"})
    public ApiResponse<Map<String, Object>> updateWorkOrder(@PathVariable Long id) {
        return ApiResponse.success(dataService.accepted("updateWorkOrder:" + id));
    }

    @PutMapping("/work-orders/{id}")
    public ApiResponse<Map<String, Object>> editWorkOrder(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        return ApiResponse.success(dataService.updateResource("workOrders", id, payload));
    }

    @DeleteMapping("/work-orders/{id}")
    public ApiResponse<Map<String, Object>> deleteWorkOrder(@PathVariable Long id) {
        return ApiResponse.success(dataService.deleteResource("workOrders", id));
    }
}

