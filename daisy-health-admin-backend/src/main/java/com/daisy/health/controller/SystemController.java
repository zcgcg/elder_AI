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
public class SystemController {
    private final AdminDataService dataService;

    public SystemController(AdminDataService dataService) {
        this.dataService = dataService;
    }

    @GetMapping("/staffs")
    public ApiResponse<PageResult<Map<String, Object>>> staffs() {
        return ApiResponse.success(dataService.resource("staffs"));
    }

    @PostMapping("/staffs")
    public ApiResponse<Map<String, Object>> createStaff(@RequestBody Map<String, Object> payload) {
        return ApiResponse.success(dataService.createResource("staffs", payload));
    }

    @PutMapping("/staffs/{id}")
    public ApiResponse<Map<String, Object>> updateStaff(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        return ApiResponse.success(dataService.updateResource("staffs", id, payload));
    }

    @DeleteMapping("/staffs/{id}")
    public ApiResponse<Map<String, Object>> deleteStaff(@PathVariable Long id) {
        return ApiResponse.success(dataService.deleteResource("staffs", id));
    }

    @GetMapping("/roles")
    public ApiResponse<PageResult<Map<String, Object>>> roles() {
        return ApiResponse.success(dataService.resource("roles"));
    }

    @PostMapping("/roles")
    public ApiResponse<Map<String, Object>> createRole(@RequestBody Map<String, Object> payload) {
        return ApiResponse.success(dataService.createResource("roles", payload));
    }

    @PutMapping("/roles/{id}")
    public ApiResponse<Map<String, Object>> updateRole(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        return ApiResponse.success(dataService.updateResource("roles", id, payload));
    }

    @DeleteMapping("/roles/{id}")
    public ApiResponse<Map<String, Object>> deleteRole(@PathVariable Long id) {
        return ApiResponse.success(dataService.deleteResource("roles", id));
    }

    @GetMapping("/logs")
    public ApiResponse<PageResult<Map<String, Object>>> logs() {
        return ApiResponse.success(dataService.resource("logs"));
    }

    @GetMapping("/agreements")
    public ApiResponse<PageResult<Map<String, Object>>> agreements() {
        return ApiResponse.success(dataService.resource("agreements"));
    }

    @PostMapping("/agreements")
    public ApiResponse<Map<String, Object>> createAgreement(@RequestBody Map<String, Object> payload) {
        return ApiResponse.success(dataService.createResource("agreements", payload));
    }

    @PutMapping("/agreements/{id}")
    public ApiResponse<Map<String, Object>> updateAgreement(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        return ApiResponse.success(dataService.updateResource("agreements", id, payload));
    }

    @DeleteMapping("/agreements/{id}")
    public ApiResponse<Map<String, Object>> deleteAgreement(@PathVariable Long id) {
        return ApiResponse.success(dataService.deleteResource("agreements", id));
    }
}

