package com.example.springdemo.service;

import java.util.Map;

/**
 * 工单服务接口
 * 用于处理不同类型的工单相关操作
 */
public interface TicketService {

    /**
     * 处理工单请求
     * @param request 请求参数
     * @return 处理结果
     */
    Map<String, Object> process(Map<String, String> request);
}