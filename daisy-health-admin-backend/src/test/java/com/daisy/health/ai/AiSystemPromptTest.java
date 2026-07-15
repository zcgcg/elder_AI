package com.daisy.health.ai;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertTrue;

class AiSystemPromptTest {
    @Test
    void promptDefinesProjectKnowledgeMedicalSafetyPrivacyAndEscalation() throws Exception {
        ClassPathResource resource = new ClassPathResource("prompts/elderly-ai-customer-service.md");
        String prompt = new String(Files.readAllBytes(resource.getFile().toPath()), StandardCharsets.UTF_8);

        assertTrue(prompt.contains("黛西健康智慧养老系统智能客服"));
        assertTrue(prompt.contains("不得诊断"));
        assertTrue(prompt.contains("不得开药、停药、调整剂量"));
        assertTrue(prompt.contains("胸痛"));
        assertTrue(prompt.contains("呼叫 120"));
        assertTrue(prompt.contains("API Key"));
        assertTrue(prompt.contains("给管理员留言"));
        assertTrue(prompt.contains("用户资料、健康数据、用药、设备、报告"));
    }
}
