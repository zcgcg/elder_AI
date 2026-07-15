package com.daisy.health.ai;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public class AiChatMessage {
    private final Long id;
    private final String role;
    private final String content;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime createdAt;

    public AiChatMessage(Long id, String role, String content, LocalDateTime createdAt) {
        this.id = id;
        this.role = role;
        this.content = content;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getRole() {
        return role;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
