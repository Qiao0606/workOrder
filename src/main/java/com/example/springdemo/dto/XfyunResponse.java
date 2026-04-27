package com.example.springdemo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * 讯飞星火响应DTO
 */
@Data
public class XfyunResponse {

    /**
     * 响应码
     */
    private String code;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 会话ID
     */
    private String sid;

    /**
     * 选择列表（AI回复内容）
     */
    private List<Choice> choices;

    /**
     * 用量信息
     */
    private Usage usage;

    @Data
    public static class Choice {

        /**
         * 消息对象
         */
        private Message message;

        /**
         * 结束原因
         */
        private String finishReason;

        /**
         * 选择索引
         */
        private Integer index;
    }

    @Data
    public static class Message {

        /**
         * 角色
         */
        private String role;

        /**
         * 内容
         */
        private String content;
    }

    @Data
    public static class Usage {

        /**
         * 提示词token数
         */
        @JsonProperty("prompt_tokens")
        private Integer promptTokens;

        /**
         * 完成token数
         */
        @JsonProperty("completion_tokens")
        private Integer completionTokens;

        /**
         * 总token数
         */
        @JsonProperty("total_tokens")
        private Integer totalTokens;
    }
}
