package com.daisy.health.controller;

import com.daisy.health.ai.AiChatExchange;
import com.daisy.health.ai.AiChatMessage;
import com.daisy.health.ai.AiChatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class AiChatControllerTest {
    private AiChatService chatService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        chatService = mock(AiChatService.class);
        mockMvc = standaloneSetup(new AiChatController(chatService)).build();
    }

    @Test
    void elderlyUserCanReadTheLastWeekConversationInChronologicalOrder() throws Exception {
        when(chatService.messages()).thenReturn(Arrays.asList(
                message(1L, "user", "康复项目适合多大年龄？", "2026-07-15T10:00:00"),
                message(2L, "assistant", "需要结合具体服务说明确认。", "2026-07-15T10:00:01")
        ));

        mockMvc.perform(get("/api/v1/elderly/ai-chat/messages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].role").value("user"))
                .andExpect(jsonPath("$.data[1].role").value("assistant"));
    }

    @Test
    void elderlyUserSendsOneQuestionAndReceivesTheSavedExchange() throws Exception {
        AiChatMessage user = message(3L, "user", "我最近的心率怎么样？", "2026-07-15T10:01:00");
        AiChatMessage assistant = message(4L, "assistant", "我可以帮您总结记录，但不能替代医生诊断。", "2026-07-15T10:01:01");
        when(chatService.send(eq("我最近的心率怎么样？"))).thenReturn(new AiChatExchange(user, assistant));

        mockMvc.perform(post("/api/v1/elderly/ai-chat/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"我最近的心率怎么样？\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userMessage.content").value("我最近的心率怎么样？"))
                .andExpect(jsonPath("$.data.assistantMessage.role").value("assistant"));

        verify(chatService).send("我最近的心率怎么样？");
    }

    private AiChatMessage message(long id, String role, String content, String createdAt) {
        return new AiChatMessage(id, role, content, LocalDateTime.parse(createdAt));
    }
}
