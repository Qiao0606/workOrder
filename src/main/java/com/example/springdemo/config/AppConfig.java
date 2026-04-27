package com.example.springdemo.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 配置类
 * 启用配置属性绑定
 */
@Configuration
@EnableConfigurationProperties(XfyunProperties.class)
public class AppConfig {
}
