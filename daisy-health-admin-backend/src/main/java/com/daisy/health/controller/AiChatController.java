package com.daisy.health.controller;

import com.daisy.health.ai.AiChatExchange;
import com.daisy.health.ai.AiChatMessage;
import com.daisy.health.ai.AiChatService;
import com.daisy.health.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/elderly/ai-chat")
public class AiChatController {
    private final AiChatService chatService;

    public AiChatController(AiChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/messages")
    public ApiResponse<List<AiChatMessage>> messages() {
        return ApiResponse.success(chatService.messages());
    }

    @PostMapping("/messages")
    public ApiResponse<AiChatExchange> send(@RequestBody Map<String, Object> payload) {
        Object content = payload == null ? null : payload.get("content");
        return ApiResponse.success(chatService.send(content == null ? "" : String.valueOf(content)));
    }
}
