package com.daisy.health.ai;

import java.util.List;

public interface AiChatService {
    List<AiChatMessage> messages();

    AiChatExchange send(String content);
}
