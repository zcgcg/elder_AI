package com.daisy.health.ai;

public class AiServiceUnavailableException extends RuntimeException {
    public static final String PUBLIC_MESSAGE = "AI 服务暂时不可用，请稍后重试或给管理员留言";

    public AiServiceUnavailableException() {
        super(PUBLIC_MESSAGE);
    }

    public AiServiceUnavailableException(Throwable cause) {
        super(PUBLIC_MESSAGE, cause);
    }
}
