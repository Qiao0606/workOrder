package com.example.springdemo.service.impl;

import com.example.springdemo.dto.ClassificationResult;
import com.example.springdemo.service.ClassificationService;
import com.example.springdemo.service.TicketClassifier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service("ticketClassifier.classify")
@RequiredArgsConstructor
@Slf4j
public class TicketClassifierImpl implements TicketClassifier {

    private final ClassificationService classificationService;

    @Override
    public Map<String, Object> classify(Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 获取工单内容
            String content = request.get("content");
            if (content == null || content.trim().isEmpty()) {
                response.put("status", "error");
                response.put("code", 400);
                response.put("message", "工单内容不能为空");
                return response;
            }

            log.info("开始分类工单: content={}", content.substring(0, Math.min(50, content.length())));

            // 调用分类服务
            ClassificationResult result = classificationService.classify(content.trim());

            if (result == null) {
                response.put("status", "error");
                response.put("code", 500);
                response.put("message", "分类失败：返回结果为空");
                return response;
            }

            // 构建响应
            Map<String, Object> data = new HashMap<>();
            data.put("categoryId", result.getCategoryId());
            data.put("categoryName", result.getCategoryName());
            data.put("categoryCode", result.getCategoryCode());
            data.put("confidence", result.getConfidence());
            data.put("matchType", result.getMatchType());
            data.put("matchEvidence", result.getMatchEvidence());
            data.put("aiReasoning", result.getAiReasoning());

            response.put("status", "success");
            response.put("code", 200);
            response.put("message", "分类成功");
            response.put("data", data);

            log.info("工单分类完成: categoryId={}, categoryName={}, matchType={}", 
                result.getCategoryId(), result.getCategoryName(), result.getMatchType());

        } catch (IllegalArgumentException e) {
            log.warn("分类参数错误: {}", e.getMessage());
            response.put("status", "error");
            response.put("code", 400);
            response.put("message", "参数错误: " + e.getMessage());
        } catch (Exception e) {
            log.error("工单分类失败", e);
            response.put("status", "error");
            response.put("code", 500);
            response.put("message", "分类失败: " + e.getMessage());
        }

        return response;
    }
}