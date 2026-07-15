package com.daisy.health.ai;

import com.daisy.health.common.AuthenticatedUser;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Profile("mysql")
public class PersistentAiChatService implements AiChatService {
    private static final int CONTEXT_MESSAGE_LIMIT = 20;

    private final AiChatRepository repository;
    private final AiModelClient modelClient;
    private final AiContextProvider contextProvider;
    private final CurrentAccountProvider accountProvider;

    public PersistentAiChatService(AiChatRepository repository, AiModelClient modelClient,
                                   AiContextProvider contextProvider, CurrentAccountProvider accountProvider) {
        this.repository = repository;
        this.modelClient = modelClient;
        this.contextProvider = contextProvider;
        this.accountProvider = accountProvider;
    }

    @Override
    public List<AiChatMessage> messages() {
        AuthenticatedUser user = accountProvider.requireElderly();
        repository.deleteExpired();
        return repository.recent(user.getAccountId());
    }

    @Override
    public AiChatExchange send(String content) {
        String question = AiChatQuestion.requireValid(content);

        AuthenticatedUser user = accountProvider.requireElderly();
        repository.deleteExpired();
        List<AiChatMessage> history = repository.recentForContext(user.getAccountId(), CONTEXT_MESSAGE_LIMIT);
        String answer = modelClient.complete(
                contextProvider.systemPrompt(),
                contextProvider.contextFor(user.getAccountId()),
                history,
                question
        );
        return repository.saveExchange(user.getAccountId(), question, answer);
    }
}
