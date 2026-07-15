package com.daisy.health.ai;

public interface AiContextProvider {
    String systemPrompt();

    String contextFor(long accountId);
}
