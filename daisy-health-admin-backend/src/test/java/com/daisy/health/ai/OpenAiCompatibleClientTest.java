package com.daisy.health.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class OpenAiCompatibleClientTest {
    private RestTemplate restTemplate;
    private MockRestServiceServer server;
    private OpenAiCompatibleClient client;

    @BeforeEach
    void setUp() {
        AiProperties properties = new AiProperties();
        properties.setApiUrl("https://ai.example.test/v1/chat/completions");
        properties.setApiKey("secret-key-never-returned");
        properties.setModel("test-model");
        properties.setMaxResponseChars(4000);
        restTemplate = new RestTemplate();
        server = MockRestServiceServer.bindTo(restTemplate).build();
        client = new OpenAiCompatibleClient(restTemplate, new ObjectMapper(), properties);
    }

    @Test
    void sendsTheStandardBearerAuthenticatedChatCompletionRequest() {
        server.expect(once(), requestTo("https://ai.example.test/v1/chat/completions"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "Bearer secret-key-never-returned"))
                .andExpect(jsonPath("$.model").value("test-model"))
                .andExpect(jsonPath("$.messages[0].role").value("system"))
                .andExpect(jsonPath("$.messages[0].content").value("系统提示词"))
                .andExpect(jsonPath("$.messages[3].content").value("现在的问题"))
                .andRespond(withSuccess("{\"choices\":[{\"message\":{\"content\":\"这是安全回复。\"}}]}", MediaType.APPLICATION_JSON));

        String answer = client.complete(
                "系统提示词",
                "白名单上下文",
                Collections.singletonList(new AiChatMessage(1L, "assistant", "历史回复", LocalDateTime.now())),
                "现在的问题"
        );

        assertEquals("这是安全回复。", answer);
        server.verify();
    }

    @Test
    void upstreamErrorsAreAlwaysConvertedToThePublicSafeError() {
        server.expect(requestTo("https://ai.example.test/v1/chat/completions"))
                .andRespond(withServerError().body("provider secret diagnostics"));

        AiServiceUnavailableException error = assertThrows(AiServiceUnavailableException.class,
                () -> client.complete("系统", "上下文", Collections.emptyList(), "问题"));

        assertEquals(AiServiceUnavailableException.PUBLIC_MESSAGE, error.getMessage());
    }

    @Test
    void redactsSensitiveDataFromCurrentAndHistoricalUserMessages() {
        server.expect(requestTo("https://ai.example.test/v1/chat/completions"))
                .andExpect(content().string(not(containsString("13812345678"))))
                .andExpect(content().string(not(containsString("11010519491231002X"))))
                .andExpect(content().string(not(containsString("eyJhbGciOiJIUzI1NiJ9.payload.signature"))))
                .andExpect(content().string(not(containsString("北京市朝阳区幸福路 8 号"))))
                .andExpect(content().string(containsString("[REDACTED]")))
                .andRespond(withSuccess("{\"choices\":[{\"message\":{\"content\":\"safe\"}}]}", MediaType.APPLICATION_JSON));

        client.complete(
                "system",
                "safe context",
                Collections.singletonList(new AiChatMessage(1L, "user", "我的手机号是 13812345678", LocalDateTime.now())),
                "身份证 11010519491231002X，住址：北京市朝阳区幸福路 8 号，JWT eyJhbGciOiJIUzI1NiJ9.payload.signature"
        );

        server.verify();
    }
}
