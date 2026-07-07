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

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class OperationController {
    private final AdminDataService dataService;

    public OperationController(AdminDataService dataService) {
        this.dataService = dataService;
    }

    @GetMapping({"/posts", "/activities", "/articles", "/recipes", "/diseases", "/institutions", "/videos", "/comments", "/foods", "/assessments"})
    public ApiResponse<PageResult<Map<String, Object>>> list(HttpServletRequest request) {
        String resource = request.getRequestURI().substring(request.getRequestURI().lastIndexOf('/') + 1);
        return ApiResponse.success(dataService.resource(resource));
    }

    @PostMapping({"/posts", "/activities", "/articles", "/recipes", "/diseases", "/institutions", "/videos", "/comments", "/foods", "/assessments"})
    public ApiResponse<Map<String, Object>> create(HttpServletRequest request, @RequestBody Map<String, Object> payload) {
        String resource = request.getRequestURI().substring(request.getRequestURI().lastIndexOf('/') + 1);
        return ApiResponse.success(dataService.createResource(resource, payload));
    }

    @PutMapping({"/activities/{id}", "/articles/{id}", "/comments/{id}/hide"})
    public ApiResponse<Map<String, Object>> update(HttpServletRequest request, @PathVariable Long id, @RequestBody(required = false) Map<String, Object> payload) {
        String uri = request.getRequestURI();
        String resource = uri.substring("/api/v1/".length(), uri.indexOf("/", "/api/v1/".length()));
        return ApiResponse.success(dataService.updateResource(resource, id, payload == null ? new java.util.LinkedHashMap<String, Object>() : payload));
    }

    @PutMapping({"/posts/{id}", "/recipes/{id}", "/diseases/{id}", "/institutions/{id}", "/videos/{id}", "/comments/{id}", "/foods/{id}", "/assessments/{id}"})
    public ApiResponse<Map<String, Object>> updateAny(HttpServletRequest request, @PathVariable Long id, @RequestBody Map<String, Object> payload) {
        String uri = request.getRequestURI();
        String resource = uri.substring("/api/v1/".length(), uri.indexOf("/", "/api/v1/".length()));
        return ApiResponse.success(dataService.updateResource(resource, id, payload));
    }

    @DeleteMapping({"/posts/{id}", "/activities/{id}", "/articles/{id}", "/recipes/{id}", "/diseases/{id}", "/institutions/{id}", "/videos/{id}", "/comments/{id}", "/foods/{id}", "/assessments/{id}"})
    public ApiResponse<Map<String, Object>> deletePost(HttpServletRequest request, @PathVariable Long id) {
        String uri = request.getRequestURI();
        String resource = uri.substring("/api/v1/".length(), uri.indexOf("/", "/api/v1/".length()));
        return ApiResponse.success(dataService.deleteResource(resource, id));
    }
}

