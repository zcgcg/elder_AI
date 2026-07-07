package com.daisy.health.controller;

import com.daisy.health.common.ApiResponse;
import com.daisy.health.service.AdminDataService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AdminDataService dataService;

    public AuthController(AdminDataService dataService) {
        this.dataService = dataService;
    }

    @PostMapping("/login")
    public ApiResponse<Map<String, Object>> login(@RequestBody Map<String, Object> payload) {
        return ApiResponse.success(dataService.login(payload));
    }

    @PostMapping("/logout")
    public ApiResponse<Map<String, Object>> logout() {
        return ApiResponse.success(dataService.accepted("logout"));
    }

    @GetMapping("/profile")
    public ApiResponse<Map<String, Object>> profile() {
        return ApiResponse.success(dataService.profile());
    }

    @PutMapping("/profile")
    public ApiResponse<Map<String, Object>> updateProfile(@RequestBody Map<String, Object> payload) {
        return ApiResponse.success(dataService.updateProfile(payload));
    }

    @PutMapping("/password")
    public ApiResponse<Map<String, Object>> updatePassword() {
        return ApiResponse.success(dataService.accepted("updatePassword"));
    }
}

