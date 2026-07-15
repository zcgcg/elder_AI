package com.daisy.health.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class ServiceCategory {
    public static final String HOUSEKEEPING = "家政护理";
    public static final String REHABILITATION = "康复理疗";
    public static final String HOME_EXAM = "上门体检";
    public static final String OTHER = "其他";
    public static final List<String> ALL = Collections.unmodifiableList(Arrays.asList(
            HOUSEKEEPING, REHABILITATION, HOME_EXAM, OTHER
    ));

    private ServiceCategory() {
    }

    public static String requireValid(Object value) {
        String category = value == null ? "" : String.valueOf(value).trim();
        if (!ALL.contains(category)) {
            throw new IllegalArgumentException("服务分类只能是家政护理、康复理疗、上门体检或其他");
        }
        return category;
    }
}
