package com.daisy.health.ai;

public class AiChatExchange {
    private final AiChatMessage userMessage;
    private final AiChatMessage assistantMessage;

    public AiChatExchange(AiChatMessage userMessage, AiChatMessage assistantMessage) {
        this.userMessage = userMessage;
        this.assistantMessage = assistantMessage;
    }

    public AiChatMessage getUserMessage() {
        return userMessage;
    }

    public AiChatMessage getAssistantMessage() {
        return assistantMessage;
    }
}
