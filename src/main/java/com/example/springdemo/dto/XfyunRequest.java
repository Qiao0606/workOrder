package com.example.springdemo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 讯飞星火请求DTO
 */
@Data
@Builder
public class XfyunRequest {

    private String appId;

    /**
     * 模型版本
     */
    private String model;

    /**
     * 消息列表
     */
    private List<Message> messages;

    /**
     * 是否流式输出
     */
    private Boolean stream = false;

    /**
     * 消息对象
     */
    @Data
    @Builder
    public static class Message {

        /**
         * 角色：system(系统提示), user(用户), assistant(AI)
         */
        private String role;

        /**
         * 消息内容
         */
        private String content;
    }
}
