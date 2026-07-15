package com.daisy.health.ai;

import com.daisy.health.common.AuthenticatedUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PersistentAiChatServiceTest {
    private AiChatRepository repository;
    private AiModelClient modelClient;
    private AiContextProvider contextProvider;
    private CurrentAccountProvider accountProvider;
    private PersistentAiChatService service;

    @BeforeEach
    void setUp() {
        repository = mock(AiChatRepository.class);
        modelClient = mock(AiModelClient.class);
        contextProvider = mock(AiContextProvider.class);
        accountProvider = mock(CurrentAccountProvider.class);
        when(accountProvider.requireElderly()).thenReturn(new AuthenticatedUser(88L, "elderly", "13800010001"));
        service = new PersistentAiChatService(repository, modelClient, contextProvider, accountProvider);
    }

    @Test
    void sendsOnlyTheMostRecentTwentyMessagesAndSavesACompleteExchange() {
        AiChatMessage previous = message(7L, "assistant", "上一条回复");
        when(repository.recentForContext(88L, 20)).thenReturn(Collections.singletonList(previous));
        when(contextProvider.systemPrompt()).thenReturn("系统提示词");
        when(contextProvider.contextFor(88L)).thenReturn("安全白名单上下文");
        when(modelClient.complete(eq("系统提示词"), eq("安全白名单上下文"),
                eq(Collections.singletonList(previous)), eq("我的心率怎么样？"))).thenReturn("近期心率记录较平稳。");
        AiChatExchange saved = new AiChatExchange(message(8L, "user", "我的心率怎么样？"), message(9L, "assistant", "近期心率记录较平稳。"));
        when(repository.saveExchange(88L, "我的心率怎么样？", "近期心率记录较平稳。")).thenReturn(saved);

        AiChatExchange result = service.send("  我的心率怎么样？  ");

        assertEquals(9L, result.getAssistantMessage().getId());
        verify(repository).deleteExpired();
        verify(repository).saveExchange(88L, "我的心率怎么样？", "近期心率记录较平稳。");
    }

    @Test
    void providerFailureNeverWritesAnIncompleteConversation() {
        when(repository.recentForContext(anyLong(), eq(20))).thenReturn(Collections.emptyList());
        when(contextProvider.systemPrompt()).thenReturn("系统提示词");
        when(contextProvider.contextFor(88L)).thenReturn("安全白名单上下文");
        when(modelClient.complete(eq("系统提示词"), eq("安全白名单上下文"),
                eq(Collections.emptyList()), eq("问题"))).thenThrow(new AiServiceUnavailableException());

        assertThrows(AiServiceUnavailableException.class, () -> service.send("问题"));

        verify(repository, never()).saveExchange(anyLong(), eq("问题"), org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void rejectsBlankAndOverlongQuestionsBeforeCallingTheProvider() {
        assertThrows(IllegalArgumentException.class, () -> service.send("   "));
        assertThrows(IllegalArgumentException.class, () -> service.send(repeat("问", 1001)));
        verify(modelClient, never()).complete(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyList(), org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void listsOnlyRepositoryMessagesForTheCurrentAccount() {
        when(repository.recent(88L)).thenReturn(Arrays.asList(message(1L, "user", "你好"), message(2L, "assistant", "您好")));

        assertEquals(2, service.messages().size());
        verify(repository).recent(88L);
    }

    private AiChatMessage message(long id, String role, String content) {
        return new AiChatMessage(id, role, content, LocalDateTime.of(2026, 7, 15, 10, 0));
    }

    private String repeat(String text, int count) {
        StringBuilder builder = new StringBuilder();
        for (int index = 0; index < count; index++) builder.append(text);
        return builder.toString();
    }
}
