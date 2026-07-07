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
public class ProductController {
    private final AdminDataService dataService;

    public ProductController(AdminDataService dataService) {
        this.dataService = dataService;
    }

    @GetMapping("/products")
    public ApiResponse<PageResult<Map<String, Object>>> products() {
        return ApiResponse.success(dataService.resource("products"));
    }

    @PostMapping("/products")
    public ApiResponse<Map<String, Object>> createProduct(@RequestBody Map<String, Object> payload) {
        return ApiResponse.success(dataService.createResource("products", payload));
    }

    @PutMapping("/products/{id}")
    public ApiResponse<Map<String, Object>> updateProduct(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        return ApiResponse.success(dataService.updateResource("products", id, payload));
    }

    @DeleteMapping("/products/{id}")
    public ApiResponse<Map<String, Object>> deleteProduct(@PathVariable Long id) {
        return ApiResponse.success(dataService.deleteResource("products", id));
    }
}

