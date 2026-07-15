package com.daisy.health.ai;

final class AiChatQuestion {
    private static final int MAX_LENGTH = 1000;

    private AiChatQuestion() {
    }

    static String requireValid(String content) {
        String question = content == null ? "" : content.trim();
        if (question.isEmpty()) throw new IllegalArgumentException("请输入问题");
        if (question.length() > MAX_LENGTH) throw new IllegalArgumentException("问题不能超过 1000 字");
        return question;
    }
}
