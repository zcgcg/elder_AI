package com.daisy.health.common;

import com.daisy.health.ai.AiServiceUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(IllegalArgumentException.class)
    public ApiResponse<Void> handleBusiness(IllegalArgumentException ex) {
        return ApiResponse.error(1005, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<Void> handleValidation(MethodArgumentNotValidException ex) {
        return ApiResponse.error(1001, "参数错误");
    }

    @ExceptionHandler(SecurityException.class)
    public ApiResponse<Void> handleSecurity(SecurityException ex) {
        return ApiResponse.error(1003, ex.getMessage());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ApiResponse<Void> handleUploadTooLarge(MaxUploadSizeExceededException ex) {
        return ApiResponse.error(1005, "上传文件超过系统限制（最大 20MB）");
    }

    @ExceptionHandler(AiServiceUnavailableException.class)
    public ApiResponse<Void> handleAiUnavailable(AiServiceUnavailableException ex) {
        return ApiResponse.error(5001, AiServiceUnavailableException.PUBLIC_MESSAGE);
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleException(Exception ex) {
        LOGGER.error("未处理的系统异常", ex);
        return ApiResponse.error(5000, "系统处理失败，请稍后重试");
    }
}
