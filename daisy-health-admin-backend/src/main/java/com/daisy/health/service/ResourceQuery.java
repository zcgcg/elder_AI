package com.daisy.health.service;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ResourceQuery {
    private String keyword;
    private String status;
    private String startDate;
    private String endDate;
    private String tag;

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public LocalDate startDateOr(LocalDate fallback) {
        return parseDate(startDate, fallback, "开始日期格式应为 YYYY-MM-DD");
    }

    public LocalDate endDateOr(LocalDate fallback) {
        return parseDate(endDate, fallback, "结束日期格式应为 YYYY-MM-DD");
    }

    public List<Map<String, Object>> filter(List<Map<String, Object>> rows) {
        LocalDate start = blank(startDate) ? null : startDateOr(null);
        LocalDate end = blank(endDate) ? null : endDateOr(null);
        if (start != null && end != null && end.isBefore(start)) {
            throw new IllegalArgumentException("结束日期不能早于开始日期");
        }
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        for (Map<String, Object> row : rows) {
            if (!matchesKeyword(row) || !matchesStatus(row) || !matchesTag(row) || !matchesDate(row, start, end)) {
                continue;
            }
            result.add(row);
        }
        return result;
    }

    private boolean matchesKeyword(Map<String, Object> row) {
        if (blank(keyword)) return true;
        String expected = keyword.trim().toLowerCase(Locale.ROOT);
        for (Map.Entry<String, Object> entry : row.entrySet()) {
            if (entry.getValue() != null && String.valueOf(entry.getValue()).toLowerCase(Locale.ROOT).contains(expected)) {
                return true;
            }
        }
        return false;
    }

    private boolean matchesStatus(Map<String, Object> row) {
        if (blank(status)) return true;
        return status.trim().equals(String.valueOf(row.get("status")));
    }

    private boolean matchesTag(Map<String, Object> row) {
        if (blank(tag)) return true;
        Object tags = row.get("tags");
        if (tags instanceof Iterable) {
            for (Object value : (Iterable<?>) tags) {
                if (tag.trim().equals(String.valueOf(value))) return true;
            }
            return false;
        }
        return tags != null && String.valueOf(tags).contains(tag.trim());
    }

    private boolean matchesDate(Map<String, Object> row, LocalDate start, LocalDate end) {
        if (start == null && end == null) return true;
        LocalDate actual = rowDate(row);
        if (actual == null) return false;
        return (start == null || !actual.isBefore(start)) && (end == null || !actual.isAfter(end));
    }

    private LocalDate rowDate(Map<String, Object> row) {
        String[] keys = {"filterDate", "serviceDate", "updatedAt", "createdAt", "recordDate", "reportDate", "expireDate", "startTime", "enrollTime"};
        for (String key : keys) {
            Object value = row.get(key);
            if (value == null) continue;
            String text = String.valueOf(value).trim();
            if (text.length() < 10) continue;
            try {
                return LocalDate.parse(text.substring(0, 10));
            } catch (DateTimeParseException ignored) {
            }
        }
        return null;
    }

    private LocalDate parseDate(String value, LocalDate fallback, String message) {
        if (blank(value)) return fallback;
        try {
            return LocalDate.parse(value.trim());
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException(message);
        }
    }

    private boolean blank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
