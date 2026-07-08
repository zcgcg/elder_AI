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
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class PhaseResourceController {
    private final AdminDataService dataService;

    public PhaseResourceController(AdminDataService dataService) {
        this.dataService = dataService;
    }

    @GetMapping({
            "/health-settings", "/devices", "/reports", "/coupons", "/user-points", "/points-records",
            "/member-levels", "/points-rules", "/product-categories", "/service-items", "/banners",
            "/activity-enrolls", "/topics", "/assessment-results", "/medications", "/health-data"
    })
    public ApiResponse<PageResult<Map<String, Object>>> list(HttpServletRequest request) {
        return ApiResponse.success(dataService.resource(resourceName(request)));
    }

    @PostMapping({
            "/health-settings", "/devices", "/reports", "/coupons", "/user-points", "/points-records",
            "/member-levels", "/points-rules", "/product-categories", "/service-items", "/banners",
            "/activity-enrolls", "/topics", "/assessment-results", "/medications", "/health-data"
    })
    public ApiResponse<Map<String, Object>> create(HttpServletRequest request, @RequestBody Map<String, Object> payload) {
        return ApiResponse.success(dataService.createResource(resourceName(request), payload));
    }

    @PutMapping({
            "/health-settings/{id}", "/devices/{id}", "/reports/{id}", "/coupons/{id}", "/user-points/{id}", "/points-records/{id}",
            "/member-levels/{id}", "/points-rules/{id}", "/product-categories/{id}", "/service-items/{id}", "/banners/{id}",
            "/activity-enrolls/{id}", "/topics/{id}", "/assessment-results/{id}", "/medications/{id}", "/health-data/{id}"
    })
    public ApiResponse<Map<String, Object>> update(HttpServletRequest request, @PathVariable Long id, @RequestBody(required = false) Map<String, Object> payload) {
        return ApiResponse.success(dataService.updateResource(resourceName(request), id, payload == null ? new LinkedHashMap<String, Object>() : payload));
    }

    @DeleteMapping({
            "/health-settings/{id}", "/devices/{id}", "/reports/{id}", "/coupons/{id}", "/user-points/{id}", "/points-records/{id}",
            "/member-levels/{id}", "/points-rules/{id}", "/product-categories/{id}", "/service-items/{id}", "/banners/{id}",
            "/activity-enrolls/{id}", "/topics/{id}", "/assessment-results/{id}", "/medications/{id}", "/health-data/{id}"
    })
    public ApiResponse<Map<String, Object>> delete(HttpServletRequest request, @PathVariable Long id) {
        return ApiResponse.success(dataService.deleteResource(resourceName(request), id));
    }

    private String resourceName(HttpServletRequest request) {
        String path = request.getRequestURI().substring("/api/v1/".length());
        String segment = path.contains("/") ? path.substring(0, path.indexOf('/')) : path;
        if ("health-settings".equals(segment)) return "healthSettings";
        if ("user-points".equals(segment)) return "userPoints";
        if ("points-records".equals(segment)) return "pointsRecords";
        if ("member-levels".equals(segment)) return "memberLevels";
        if ("points-rules".equals(segment)) return "pointsRules";
        if ("product-categories".equals(segment)) return "productCategories";
        if ("service-items".equals(segment)) return "serviceItems";
        if ("activity-enrolls".equals(segment)) return "activityEnrolls";
        if ("assessment-results".equals(segment)) return "assessmentResults";
        if ("health-data".equals(segment)) return "healthData";
        return segment;
    }
}
