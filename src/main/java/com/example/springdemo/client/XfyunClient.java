package com.example.springdemo.client;

import com.example.springdemo.config.XfyunProperties;
import com.example.springdemo.dto.XfyunRequest;
import com.example.springdemo.dto.XfyunResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * 讯飞星火HTTP客户端
 */
@Component
@Slf4j
public class XfyunClient {

    private final XfyunProperties properties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 构造函数注入
    public XfyunClient(@Qualifier("xfyunProperties") XfyunProperties properties) {
        this.properties = properties;
    }

    /**
     * 调用讯飞星火API（非流式）
     */
    public XfyunResponse chat(String userMessage) {
        try {
            // 1. 构建请求体（添加appId）
            XfyunRequest request = buildRequest(userMessage);

            // 2. 生成鉴权参数
            AuthParams authParams = generateAuthParams();

            // 3. 创建WebClient（基础URL不含参数）
            WebClient webClient = WebClient.builder()
                    .baseUrl("https://spark-api-open.xf-yun.com")
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .build();

            log.info("发送请求到讯飞星火: {}", objectMapper.writeValueAsString(request));

            // 4. 使用uri()方法构建带参数的URL（添加appId参数）
            String responseJson = webClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v1/chat/completions")
                            .queryParam("authorization", authParams.authorization)
                            .queryParam("date", authParams.date)
                            .queryParam("host", authParams.host)
                            .queryParam("appid", properties.getAppId()) // 关键：添加appId参数
                            .build())
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.info("收到响应: {}", responseJson);

            // 5. 解析响应
            return objectMapper.readValue(responseJson, XfyunResponse.class);

        } catch (Exception e) {
            log.error("调用讯飞星火API失败", e);
            throw new RuntimeException("AI调用失败: " + e.getMessage(), e);
        }
    }

    /**
     * 构建请求体（添加appId，修复消息列表）
     */
    private XfyunRequest buildRequest(String userMessage) {
        List<XfyunRequest.Message> messages = new ArrayList<>();

        // system消息用于设置AI角色和行为
        XfyunRequest.Message systemMessage = XfyunRequest.Message.builder()
                .role("system")
                .content("你是一个有帮助的AI助手，请用中文回答问题。")
                .build();
        messages.add(systemMessage);

        // user消息是用户输入
        XfyunRequest.Message userMessageObj = XfyunRequest.Message.builder()
                .role("user")
                .content(userMessage)
                .build();
        messages.add(userMessageObj);

        return XfyunRequest.builder()
                .appId(properties.getAppId()) // 关键：添加appId到请求体
                .model(properties.getModelVersion())
                .messages(messages)
                .stream(false)
                .build();
    }

    /**
     * 生成鉴权参数（核心修复：请求方法、编码、时区）
     */
    private AuthParams generateAuthParams() {
        try {
            // 1. 获取UTC时区的当前时间（RFC1123格式，讯飞要求）
            ZonedDateTime utcTime = ZonedDateTime.now(java.time.ZoneOffset.UTC);
            String date = DateTimeFormatter.RFC_1123_DATE_TIME.format(utcTime);

            // 2. 原始签名字符串（关键：请求方法改为POST，和实际请求一致）
            String signatureOrigin = String.format("host: %s\ndate: %s\nPOST /v1/chat/completions HTTP/1.1",
                    "spark-api-open.xf-yun.com", date);

            log.debug("原始签名字符串: {}", signatureOrigin);

            // 3. 使用HMAC-SHA256签名
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec spec = new SecretKeySpec(
                    properties.getApiSecret().getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"
            );
            mac.init(spec);
            byte[] signatureBytes = mac.doFinal(signatureOrigin.getBytes(StandardCharsets.UTF_8));
            String signature = Base64.getEncoder().encodeToString(signatureBytes);

            // 4. 构建Authorization（讯飞规范格式）
            String authorization = String.format(
                    "api_key=\"%s\", algorithm=\"hmac-sha256\", headers=\"host date request-line\", signature=\"%s\"",
                    properties.getApiKey(), signature
            );

            log.debug("Authorization原始值: {}", authorization);


            // 讯飞要求：先Base64编码，然后在URL参数中自动编码
            String authorizationBase64 = Base64.getEncoder().encodeToString(authorization.getBytes(StandardCharsets.UTF_8));

            log.debug("Authorization Base64: {}", authorizationBase64);

            return new AuthParams(authorizationBase64, date, "spark-api-open.xf-yun.com");

        } catch (Exception e) {
            log.error("生成鉴权参数失败", e);
            throw new RuntimeException("生成鉴权参数失败", e);
        }
    }

    /**
     * 鉴权参数内部类（保持不变）
     */
    private static class AuthParams {
        final String authorization;
        final String date;
        final String host;

        AuthParams(String authorization, String date, String host) {
            this.authorization = authorization;
            this.date = date;
            this.host = host;
        }
    }
}