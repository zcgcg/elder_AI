package com.daisy.health.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ServiceDurationPolicyTest {
    @Test
    void missingAndNonPositiveDurationsDefaultToOneHour() {
        assertEquals(60L, ServiceDurationPolicy.minutes(null));
        assertEquals(60L, ServiceDurationPolicy.minutes(0));
        assertEquals(60L, ServiceDurationPolicy.minutes(-15));
        assertEquals(60L, ServiceDurationPolicy.minutes(""));
    }

    @Test
    void positiveDurationIsKeptInMinutes() {
        assertEquals(90L, ServiceDurationPolicy.minutes(90));
        assertEquals(45L, ServiceDurationPolicy.minutes("45"));
    }
}
