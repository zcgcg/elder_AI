package com.daisy.health.ai;

import java.util.List;

public interface AiModelClient {
    String complete(String systemPrompt, String context, List<AiChatMessage> history, String question);
}
