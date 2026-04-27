package com.example.springdemo.service;

/**
 * AI服务接口
 */
public interface AiService {

    /**
     * 发送消息给AI并获取回复
     * @param message 用户消息
     * @return AI回复内容
     */
    String chat(String message);

    /**
     * 流式对话（用于逐步返回AI响应）
     * @param message 用户消息
     * @return AI回复流（逐字返回）
     */
    java.util.stream.Stream<String> chatStream(String message);
}
