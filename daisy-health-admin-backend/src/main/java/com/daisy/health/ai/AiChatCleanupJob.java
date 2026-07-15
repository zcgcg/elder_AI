package com.daisy.health.ai;

import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Profile("mysql")
public class AiChatCleanupJob {
    private final AiChatRepository repository;

    public AiChatCleanupJob(AiChatRepository repository) {
        this.repository = repository;
    }

    @Scheduled(fixedDelayString = "${daisy.ai.cleanup-delay-ms:300000}")
    public void deleteExpiredMessages() {
        repository.deleteExpired();
    }
}
