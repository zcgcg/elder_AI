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
public class TradeController {
    private final AdminDataService dataService;

    public TradeController(AdminDataService dataService) {
        this.dataService = dataService;
    }

    @GetMapping("/orders")
    public ApiResponse<PageResult<Map<String, Object>>> orders(ResourceQuery query) {
        return ApiResponse.success(dataService.resource("orders", query));
    }

    @GetMapping("/orders/{id}")
    public ApiResponse<Map<String, Object>> order(@PathVariable Long id) {
        return ApiResponse.success(dataService.accepted("order:" + id));
    }

    @PostMapping("/orders")
    public ApiResponse<Map<String, Object>> createOrder(@RequestBody Map<String, Object> payload) {
        return ApiResponse.success(dataService.createResource("orders", payload));
    }

    @PutMapping({"/orders/{id}/close", "/orders/{id}/dispatch"})
    public ApiResponse<Map<String, Object>> updateOrder(@PathVariable Long id) {
        return ApiResponse.success(dataService.accepted("updateOrder:" + id));
    }

    @PutMapping("/orders/{id}")
    public ApiResponse<Map<String, Object>> editOrder(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        return ApiResponse.success(dataService.updateResource("orders", id, payload));
    }

    @DeleteMapping("/orders/{id}")
    public ApiResponse<Map<String, Object>> deleteOrder(@PathVariable Long id) {
        return ApiResponse.success(dataService.deleteResource("orders", id));
    }

    @GetMapping("/after-sales")
    public ApiResponse<PageResult<Map<String, Object>>> afterSales(ResourceQuery query) {
        return ApiResponse.success(dataService.resource("afterSales", query));
    }

    @PostMapping("/after-sales")
    public ApiResponse<Map<String, Object>> createAfterSale(@RequestBody Map<String, Object> payload) {
        return ApiResponse.success(dataService.createResource("afterSales", payload));
    }

    @PutMapping({"/after-sales/{id}/approve", "/after-sales/{id}/reject"})
    public ApiResponse<Map<String, Object>> updateAfterSale(@PathVariable Long id) {
        return ApiResponse.success(dataService.accepted("updateAfterSale:" + id));
    }

    @PutMapping("/after-sales/{id}")
    public ApiResponse<Map<String, Object>> editAfterSale(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        return ApiResponse.success(dataService.updateResource("afterSales", id, payload));
    }

    @DeleteMapping("/after-sales/{id}")
    public ApiResponse<Map<String, Object>> deleteAfterSale(@PathVariable Long id) {
        return ApiResponse.success(dataService.deleteResource("afterSales", id));
    }

    @GetMapping("/reviews")
    public ApiResponse<PageResult<Map<String, Object>>> reviews(ResourceQuery query) {
        return ApiResponse.success(dataService.resource("reviews", query));
    }

    @PostMapping("/reviews")
    public ApiResponse<Map<String, Object>> createReview(@RequestBody Map<String, Object> payload) {
        return ApiResponse.success(dataService.createResource("reviews", payload));
    }

    @PutMapping({"/reviews/{id}/reply", "/reviews/{id}/hide"})
    public ApiResponse<Map<String, Object>> updateReview(@PathVariable Long id) {
        return ApiResponse.success(dataService.accepted("updateReview:" + id));
    }

    @PutMapping("/reviews/{id}")
    public ApiResponse<Map<String, Object>> editReview(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        return ApiResponse.success(dataService.updateResource("reviews", id, payload));
    }

    @DeleteMapping("/reviews/{id}")
    public ApiResponse<Map<String, Object>> deleteReview(@PathVariable Long id) {
        return ApiResponse.success(dataService.deleteResource("reviews", id));
    }
}

