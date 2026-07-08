package com.daisy.health.controller;

import com.daisy.health.common.ApiResponse;
import com.daisy.health.service.AdminDataService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/resource-details")
public class ResourceDetailController {
    private final AdminDataService dataService;

    public ResourceDetailController(AdminDataService dataService) {
        this.dataService = dataService;
    }

    @GetMapping("/{resource}/{id}")
    public ApiResponse<Map<String, Object>> detail(@PathVariable String resource, @PathVariable Long id) {
        return ApiResponse.success(dataService.resourceDetail(resource, id));
    }

    @PutMapping("/{resource}/{id}")
    public ApiResponse<Map<String, Object>> save(@PathVariable String resource, @PathVariable Long id, @RequestBody Map<String, Object> payload) {
        return ApiResponse.success(dataService.saveResourceDetail(resource, id, payload));
    }
}
