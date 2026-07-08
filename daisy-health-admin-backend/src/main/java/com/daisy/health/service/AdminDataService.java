package com.daisy.health.service;

import com.daisy.health.common.PageResult;

import java.util.List;
import java.util.Map;

public interface AdminDataService {
    Map<String, Object> login(Map<String, Object> payload);

    Map<String, Object> profile();

    Map<String, Object> updateProfile(Map<String, Object> payload);

    Map<String, Object> dashboard();

    List<Map<String, Object>> appointments();

    PageResult<Map<String, Object>> users(String keyword);

    Map<String, Object> createUser(Map<String, Object> payload);

    Map<String, Object> updateUser(Long id, Map<String, Object> payload);

    Map<String, Object> deleteUser(Long id);

    Map<String, Object> user(Long id);

    PageResult<Map<String, Object>> tags();

    Map<String, Object> createTag(Map<String, Object> payload);

    Map<String, Object> updateTag(Long id, Map<String, Object> payload);

    Map<String, Object> deleteTag(Long id);

    Map<String, Object> updateUserTags(Long userId, Map<String, Object> payload);

    PageResult<Map<String, Object>> resource(String name);

    Map<String, Object> createResource(String name, Map<String, Object> payload);

    Map<String, Object> updateResource(String name, Long id, Map<String, Object> payload);

    Map<String, Object> deleteResource(String name, Long id);

    Map<String, Object> resourceDetail(String name, Long id);

    Map<String, Object> saveResourceDetail(String name, Long id, Map<String, Object> payload);

    Map<String, Object> analyticsOverview();

    Map<String, Object> accepted(String action);
}
