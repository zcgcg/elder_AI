package com.daisy.health.controller;

import com.daisy.health.common.ApiResponse;
import com.daisy.health.common.PageResult;
import com.daisy.health.service.AdminDataService;
import com.daisy.health.service.ResourceQuery;
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
public class UserController {
    private final AdminDataService dataService;

    public UserController(AdminDataService dataService) {
        this.dataService = dataService;
    }

    @GetMapping("/users")
    public ApiResponse<PageResult<Map<String, Object>>> users(ResourceQuery query) {
        return ApiResponse.success(dataService.users(query));
    }

    @PostMapping("/users")
    public ApiResponse<Map<String, Object>> createUser(@RequestBody Map<String, Object> payload) {
        return ApiResponse.success(dataService.createUser(payload));
    }

    @GetMapping("/users/{id}")
    public ApiResponse<Map<String, Object>> user(@PathVariable Long id) {
        return ApiResponse.success(dataService.user(id));
    }

    @PutMapping("/users/{id}")
    public ApiResponse<Map<String, Object>> updateUser(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        return ApiResponse.success(dataService.updateUser(id, payload));
    }

    @DeleteMapping("/users/{id}")
    public ApiResponse<Map<String, Object>> deleteUser(@PathVariable Long id) {
        return ApiResponse.success(dataService.deleteUser(id));
    }

    @PutMapping("/users/{id}/tags")
    public ApiResponse<Map<String, Object>> updateUserTags(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        return ApiResponse.success(dataService.updateUserTags(id, payload));
    }

    @GetMapping("/users/{id}/health-data")
    public ApiResponse<Object> healthData(@PathVariable Long id) {
        return ApiResponse.success(dataService.user(id).get("healthData"));
    }

    @GetMapping("/users/{id}/medications")
    public ApiResponse<Object> medications(@PathVariable Long id) {
        return ApiResponse.success(dataService.user(id).get("medications"));
    }

    @PostMapping("/users/{id}/medications")
    public ApiResponse<Map<String, Object>> createMedication(@PathVariable Long id) {
        return ApiResponse.success(dataService.accepted("createMedication:" + id));
    }

    @GetMapping("/tags")
    public ApiResponse<PageResult<Map<String, Object>>> tags() {
        return ApiResponse.success(dataService.tags());
    }

    @PostMapping("/tags")
    public ApiResponse<Map<String, Object>> createTag(@RequestBody Map<String, Object> payload) {
        return ApiResponse.success(dataService.createTag(payload));
    }

    @PutMapping("/tags/{id}")
    public ApiResponse<Map<String, Object>> updateTag(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        return ApiResponse.success(dataService.updateTag(id, payload));
    }

    @DeleteMapping("/tags/{id}")
    public ApiResponse<Map<String, Object>> deleteTag(@PathVariable Long id) {
        return ApiResponse.success(dataService.deleteTag(id));
    }

    @GetMapping("/messages")
    public ApiResponse<PageResult<Map<String, Object>>> userResources() {
        return ApiResponse.success(dataService.resource("posts", new ResourceQuery()));
    }

    @PostMapping("/messages")
    public ApiResponse<Map<String, Object>> createUserResource(@RequestBody Map<String, Object> payload) {
        return ApiResponse.success(dataService.createResource("posts", payload));
    }

    @PutMapping("/messages/{id}")
    public ApiResponse<Map<String, Object>> updateUserResource(@PathVariable Long id) {
        return ApiResponse.success(dataService.updateResource("posts", id, new java.util.LinkedHashMap<String, Object>()));
    }

    @DeleteMapping("/messages/{id}")
    public ApiResponse<Map<String, Object>> deleteUserResource(@PathVariable Long id) {
        return ApiResponse.success(dataService.accepted("deleteUserResource:" + id));
    }
}

