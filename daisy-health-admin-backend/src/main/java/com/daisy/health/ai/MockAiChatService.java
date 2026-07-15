package com.daisy.health.ai;

import com.daisy.health.common.AuthenticatedUser;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
@Profile("mock")
public class MockAiChatService implements AiChatService {
    private final CurrentAccountProvider accountProvider;
    private final AtomicLong ids = new AtomicLong(1L);
    private final Map<Long, List<AiChatMessage>> conversations = new ConcurrentHashMap<Long, List<AiChatMessage>>();

    public MockAiChatService(CurrentAccountProvider accountProvider) {
        this.accountProvider = accountProvider;
    }

    @Override
    public List<AiChatMessage> messages() {
        AuthenticatedUser user = accountProvider.requireElderly();
        List<AiChatMessage> messages = conversations.get(user.getAccountId());
        if (messages == null) return Collections.emptyList();
        List<AiChatMessage> copy = new ArrayList<AiChatMessage>();
        LocalDateTime cutoff = LocalDateTime.now().minusDays(7);
        synchronized (messages) {
            for (AiChatMessage message : messages) {
                if (!message.getCreatedAt().isBefore(cutoff)) copy.add(message);
            }
        }
        copy.sort(Comparator.comparing(AiChatMessage::getCreatedAt).thenComparing(AiChatMessage::getId));
        return copy;
    }

    @Override
    public AiChatExchange send(String content) {
        String question = AiChatQuestion.requireValid(content);
        AuthenticatedUser user = accountProvider.requireElderly();
        LocalDateTime now = LocalDateTime.now();
        AiChatMessage userMessage = new AiChatMessage(ids.getAndIncrement(), "user", question, now);
        AiChatMessage assistantMessage = new AiChatMessage(
                ids.getAndIncrement(),
                "assistant",
                "当前是无数据库演示模式。我可以介绍黛西健康的用户资料、健康记录、服务工单、活动和管理员留言等功能；真实 AI 与一周记录需要使用 MySQL 模式并配置 AI 环境变量。",
                now
        );
        List<AiChatMessage> messages = conversations.computeIfAbsent(
                user.getAccountId(), key -> Collections.synchronizedList(new ArrayList<AiChatMessage>())
        );
        messages.add(userMessage);
        messages.add(assistantMessage);
        return new AiChatExchange(userMessage, assistantMessage);
    }
}
