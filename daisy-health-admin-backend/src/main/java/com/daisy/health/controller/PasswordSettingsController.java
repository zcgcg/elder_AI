package com.daisy.health.controller;

import com.daisy.health.common.ApiResponse;
import com.daisy.health.service.AdminDataService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/settings")
public class PasswordSettingsController {
    private final AdminDataService dataService;

    public PasswordSettingsController(AdminDataService dataService) {
        this.dataService = dataService;
    }

    @PutMapping("/users/{id}/password/reset")
    public ApiResponse<Map<String, Object>> resetUserPassword(@PathVariable Long id) {
        return ApiResponse.success(dataService.resetUserPassword(id));
    }

    @PutMapping("/personnel/{id}/password/reset")
    public ApiResponse<Map<String, Object>> resetPersonnelPassword(@PathVariable Long id) {
        return ApiResponse.success(dataService.resetPersonnelPassword(id));
    }
}
