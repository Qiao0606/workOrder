package com.example.springdemo.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 讯飞星火配置类
 * 从application.properties中读取配置
 */
@Data
@Component // 单例Bean，默认名称为xfyunProperties（类名小驼峰）
@ConfigurationProperties(prefix = "xfyun")
// 无需额外修改，该注解组合只会生成1个Bean
public class XfyunProperties {

    /**
     * APPID：从讯飞开放平台获取
     */
    private String appId;

    /**
     * APIKey：从讯飞开放平台获取
     */
    private String apiKey;

    /**
     * APISecret：从讯飞开放平台获取
     */
    private String apiSecret;

    /**
     * 模型版本：generalv3.5(默认), generalv3, generalv4等
     */
    private String modelVersion = "generalv3.5";

    /**
     * API请求地址
     */
    private String apiUrl = "https://spark-api-open.xf-yun.com/v1/chat/completions";
}