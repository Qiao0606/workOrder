package com.example.springdemo.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理器
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 处理所有异常
     */
    @ExceptionHandler(Exception.class)
    public Map<String, Object> handleException(Exception e) {
        log.error("发生错误: {}", e.getMessage(), e);
        // 替换JDK9+的Map.of()为JDK8的HashMap
        Map<String, Object> result = new HashMap<>();
        result.put("code", 500);
        result.put("message", "服务器内部错误: " + e.getMessage());
        result.put("data", null);
        return result;
    }

    /**
     * 处理运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    public Map<String, Object> handleRuntimeException(RuntimeException e) {
        log.error("运行时错误: {}", e.getMessage(), e);
        // 替换JDK9+的Map.of()为JDK8的HashMap
        Map<String, Object> result = new HashMap<>();
        result.put("code", 500);
        result.put("message", e.getMessage());
        result.put("data", null);
        return result;
    }
}