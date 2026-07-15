package com.daisy.health.ai;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SensitiveDataRedactorTest {
    @Test
    void preservesOrdinaryQuestionsAboutAddressesAndEmergencyContactFeatures() {
        String question = "活动地址在哪里？紧急联系人功能怎么用？";

        assertTrue(SensitiveDataRedactor.redact(question).equals(question));
    }

    @Test
    void redactsNaturalFirstPersonAddressDeclarations() {
        String result = SensitiveDataRedactor.redact("我家在上海市浦东新区世纪大道100号，请帮我看看附近活动");

        assertFalse(result.contains("上海市浦东新区世纪大道100号"));
        assertTrue(result.contains("[REDACTED]"));
    }

    @Test
    void redactsNaturalEmergencyContactDeclarations() {
        String result = SensitiveDataRedactor.redact("我的紧急联系人叫张三，电话是 010-87654321");

        assertFalse(result.contains("张三"));
        assertFalse(result.contains("010-87654321"));
        assertTrue(result.contains("[REDACTED]"));
    }

    @Test
    void removesCredentialsContactDetailsAndAccountIdentifiers() {
        String result = SensitiveDataRedactor.redact(
                "紧急联系人：张三 13912345678；account_id=8848；API key: vendorSecret123；Bearer abcdefghijklmnop"
        );

        assertFalse(result.contains("张三"));
        assertFalse(result.contains("13912345678"));
        assertFalse(result.contains("8848"));
        assertFalse(result.contains("vendorSecret123"));
        assertFalse(result.contains("abcdefghijklmnop"));
        assertTrue(result.contains("[REDACTED]"));
    }
}
