package com.daisy.health.service;

final class OrderStatusMapping {
    private OrderStatusMapping() {
    }

    static String fromWorkOrder(String workOrderStatus) {
        if ("completed".equals(workOrderStatus)) {
            return "completed";
        }
        if ("cancelled".equals(workOrderStatus)) {
            return "closed";
        }
        return "pending_service";
    }
}
