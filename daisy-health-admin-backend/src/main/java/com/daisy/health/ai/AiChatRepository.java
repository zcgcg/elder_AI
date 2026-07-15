package com.daisy.health.ai;

import java.util.List;

public interface AiChatRepository {
    List<AiChatMessage> recent(long accountId);

    List<AiChatMessage> recentForContext(long accountId, int limit);

    AiChatExchange saveExchange(long accountId, String userContent, String assistantContent);

    int deleteExpired();
}
