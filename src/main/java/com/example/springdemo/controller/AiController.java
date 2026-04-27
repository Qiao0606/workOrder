package com.example.springdemo.controller;

import com.example.springdemo.service.AiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * AI接口控制器
 * 提供AI对话相关的REST API
 */
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Slf4j
public class AiController {

    private final AiService aiService;

    /**
     * 普通对话接口
     * POST /api/ai/chat
     * 请求体: { "message": "你好" }
     */
    @PostMapping("/chat")
    public Map<String, Object> chat(@RequestBody Map<String, String> request) {
        String message = request.get("message");
        if (message == null || message.trim().isEmpty()) {
            // 替换JDK9+的Map.of()为JDK8的HashMap
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("code", 400);
            errorResult.put("message", "请求消息不能为空");
            errorResult.put("data", null);
            return errorResult;
        }

        String response = aiService.chat(message);
        // 构建返回结果（嵌套Map也用HashMap）
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "success");

        Map<String, String> dataMap = new HashMap<>();
        dataMap.put("reply", response);
        result.put("data", dataMap);
        return result;
    }

    /**
     * 流式对话接口（SSE）
     * GET /api/ai/chat/stream?message=你好
     * 返回: text/event-stream
     */
    @GetMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Stream<String> chatStream(@RequestParam String message) {
        if (message == null || message.trim().isEmpty()) {
            // JDK8中Stream.of()是支持的（JDK8新增），此处无需修改
            return Stream.of("data: {\"error\": \"消息不能为空\"}\n\n");
        }
        return aiService.chatStream(message)
                .map(chunk -> "data: " + chunk + "\n\n");
    }

    /**
     * GET方式对话（简单测试用）
     * GET /api/ai/chat?message=你好
     */
    @GetMapping("/chat")
    public Map<String, Object> chatGet(@RequestParam String message) {
        // 构建request Map（替换Map.of("message", message)）
        Map<String, String> requestMap = new HashMap<>();
        requestMap.put("message", message);
        return chat(requestMap);
    }

    /**
     * 健康检查
     * GET /api/ai/health
     */
    @GetMapping("/health")
    public Map<String, Object> health() {
        // 构建健康检查返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "AI服务运行正常");

        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("status", "UP");
        dataMap.put("timestamp", System.currentTimeMillis());
        result.put("data", dataMap);
        return result;
    }
}