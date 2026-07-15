package com.daisy.health.service;

final class ServiceDurationPolicy {
    static final long DEFAULT_MINUTES = 60L;

    private ServiceDurationPolicy() {
    }

    static long minutes(Object value) {
        try {
            long duration = value instanceof Number
                    ? ((Number) value).longValue()
                    : Long.parseLong(value == null ? "" : String.valueOf(value).trim());
            return duration > 0 ? duration : DEFAULT_MINUTES;
        } catch (RuntimeException ignored) {
            return DEFAULT_MINUTES;
        }
    }
}
