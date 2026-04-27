package com.example.springdemo.service;

import java.util.Map;

/**
 * 工单分类器接口
 * 用于处理工单分类相关操作
 */
public interface TicketClassifier {

    /**
     * 对工单进行分类
     * @param request 请求参数（包含工单信息）
     * @return 分类结果
     */
    Map<String, Object> classify(Map<String, String> request);
}