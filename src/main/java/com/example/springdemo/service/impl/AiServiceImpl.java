package com.example.springdemo.service.impl;

import com.example.springdemo.client.XfyunClient;
import com.example.springdemo.dto.XfyunResponse;
import com.example.springdemo.service.AiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.stream.Stream;

/**
 * AI服务实现类
 * 使用讯飞星火大模型
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AiServiceImpl implements AiService {

    private final XfyunClient xfyunClient;

    @Override
    public String chat(String message) {
        log.info("收到用户消息: {}", message);

        // 调用讯飞星火API
        XfyunResponse response = xfyunClient.chat(message);

        // 提取AI回复内容
        String reply = null;
        if (response.getChoices() != null && !response.getChoices().isEmpty()) {
            reply = response.getChoices().get(0).getMessage().getContent();
        }

        // 如果API调用失败，返回错误信息
        if (reply == null || reply.isEmpty()) {
            reply = "抱歉，AI没有返回有效回答。错误码: " + response.getCode() +
                    ", 消息: " + response.getMessage();
        }

        log.info("AI回复: {}", reply);
        log.info("Token使用: 提示词={}, 完成={}, 总计={}",
                response.getUsage() != null ? response.getUsage().getPromptTokens() : 0,
                response.getUsage() != null ? response.getUsage().getCompletionTokens() : 0,
                response.getUsage() != null ? response.getUsage().getTotalTokens() : 0);

        return reply;
    }

    @Override
    public Stream<String> chatStream(String message) {
        log.info("收到流式请求，消息: {}", message);

        // 先获取完整回复，再模拟流式输出
        String reply = chat(message);

        // 将回复拆分为单个字符流（模拟流式输出）
        return reply.chars()
                .mapToObj(c -> String.valueOf((char) c));
    }
}
