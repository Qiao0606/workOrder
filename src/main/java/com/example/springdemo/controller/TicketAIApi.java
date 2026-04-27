package com.example.springdemo.controller;

import com.example.springdemo.dto.ClassificationResult;
import com.example.springdemo.mapper.BizWorkOrderMapper;
import com.example.springdemo.model.BizWorkOrder;
import com.example.springdemo.service.ClassificationService;
import com.example.springdemo.service.TicketClassifier;
import com.example.springdemo.service.TicketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/ai/ticket")
@RequiredArgsConstructor
@Slf4j
public class TicketAIApi {
    @Autowired
    private ApplicationContext context;

    private final ClassificationService classificationService;
    private final BizWorkOrderMapper workOrderMapper;

    @PostMapping
    public Map<String, Object> handleTicketRequest(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();

        // 获取用户输入
        String content = request.get("content");
        log.info("收到AI工单请求: content={}", content);

        // 意图分类逻辑
        String intent = classifyIntent(content);
        log.info("识别意图: {}", intent);

        // 根据意图执行不同操作
        if (intent.equals("knowledge_base_response")) {
            response.put("answer", "知识库回答");
        } else if (intent.equals("default_response")) {
            response.put("answer", "您好，我是工单智能助手。请描述您的问题或输入工单指令：");
        } else if (intent.equals("classify_by_order_id")) {
            // 新增：根据工单号分类
            response = handleClassifyByOrderId(content);
        } else if (intent.equals("confirm_classification")) {
            // 新增：确认分类
            response = handleConfirmClassification(content);
        } else {
            try {
                Object service = context.getBean(intent);
                if (service instanceof TicketClassifier) {
                    response.put("data", ((TicketClassifier) service).classify(request));
                } else if (service instanceof TicketService) {
                    // 从content中提取工单号
                    Long orderId = extractOrderId(content);
                    
                    if (orderId == null) {
                        response.put("status", "error");
                        response.put("code", 400);
                        response.put("message", "无法识别工单号，请输入正确的工单号，例如：'查询 10' 或 '工单 10'");
                    } else {
                        // 构建包含orderId的请求
                        Map<String, String> serviceRequest = new HashMap<>();
                        serviceRequest.put("orderId", String.valueOf(orderId));
                        serviceRequest.put("content", content);
                        
                        // TicketService.process() 返回的是完整的response，直接使用
                        response = ((TicketService) service).process(serviceRequest);
                    }
                }
            } catch (Exception e) {
                log.error("获取服务失败: intent={}, error={}", intent, e.getMessage());
                response.put("status", "error");
                response.put("code", 500);
                response.put("message", "服务暂时不可用，请稍后重试");
            }
        }

        return response;
    }

    /**
     * 处理工单号分类请求
     * 格式: "分类 12345" 或 "12345 分类" 或 "工单12345"
     */
    private Map<String, Object> handleClassifyByOrderId(String content) {
        Map<String, Object> response = new HashMap<>();

        try {
            // 提取工单号
            Long orderId = extractOrderId(content);
            if (orderId == null) {
                response.put("status", "error");
                response.put("code", 400);
                response.put("message", "无法识别工单号，请输入正确的工单号，例如：'分类 12345' 或 '工单12345'");
                return response;
            }

            // 查询工单
            BizWorkOrder workOrder = workOrderMapper.selectById(orderId);
            if (workOrder == null) {
                response.put("status", "error");
                response.put("code", 404);
                response.put("message", "工单不存在: " + orderId);
                return response;
            }

            // 检查是否已分类
            if (workOrder.getCategoryId() != null && workOrder.getCategoryId() > 0) {
                response.put("status", "warning");
                response.put("code", 200);
                response.put("message", String.format("工单 %d 已分类为: %s", orderId, workOrder.getCategoryId()));
                response.put("data", buildOrderInfo(workOrder));
                return response;
            }

            // 调用分类服务（预览模式，不保存）
            ClassificationResult result = classificationService.classify(orderId);

            // 构建响应
            Map<String, Object> data = new HashMap<>();
            data.put("orderInfo", buildOrderInfo(workOrder));
            data.put("classification", buildClassificationInfo(result));
            data.put("needConfirm", true);
            data.put("confirmHint", String.format(
                "请确认是否将工单 %d 分类为 '%s'（置信度: %.2f%%）？输入 '确认 %d %d' 完成分类，或输入 '跳过 %d' 跳过分类",
                orderId, result.getCategoryName(), result.getConfidence() * 100, orderId, result.getCategoryId(), orderId
            ));

            response.put("status", "success");
            response.put("code", 200);
            response.put("message", "分类预览完成，等待确认");
            response.put("data", data);

            log.info("工单分类预览: orderId={}, categoryId={}, categoryName={}, confidence={}",
                orderId, result.getCategoryId(), result.getCategoryName(), result.getConfidence());

        } catch (Exception e) {
            log.error("工单分类失败: content={}", content, e);
            response.put("status", "error");
            response.put("code", 500);
            response.put("message", "分类失败: " + e.getMessage());
        }

        return response;
    }

    /**
     * 处理分类确认请求
     * 格式: "确认 12345 5" 或 "确认工单12345分类为5"
     */
    private Map<String, Object> handleConfirmClassification(String content) {
        Map<String, Object> response = new HashMap<>();

        try {
            // 提取工单号和分类ID
            Long orderId = extractOrderId(content);
            Integer categoryId = extractCategoryId(content);

            if (orderId == null) {
                response.put("status", "error");
                response.put("code", 400);
                response.put("message", "无法识别工单号，请输入正确的格式，例如：'确认 12345 5'");
                return response;
            }

            if (categoryId == null) {
                // 检查是否是跳过操作
                if (content.contains("跳过") || content.contains("忽略")) {
                    categoryId = 0; // 0 表示跳过分类
                } else {
                    response.put("status", "error");
                    response.put("code", 400);
                    response.put("message", "无法识别分类ID，请输入正确的格式，例如：'确认 12345 5'");
                    return response;
                }
            }

            // 调用确认服务
            BizWorkOrder workOrder = classificationService.confirmClassification(orderId, categoryId);

            String message = (categoryId == 0)
                ? String.format("工单 %d 已跳过分类", orderId)
                : String.format("工单 %d 已成功分类为: %s", orderId, workOrder.getCategoryId());

            response.put("status", "success");
            response.put("code", 200);
            response.put("message", message);
            response.put("data", buildOrderInfo(workOrder));

            log.info("工单分类确认: orderId={}, categoryId={}", orderId, categoryId);

        } catch (IllegalArgumentException e) {
            log.warn("分类确认参数错误: {}", e.getMessage());
            response.put("status", "error");
            response.put("code", 400);
            response.put("message", e.getMessage());
        } catch (Exception e) {
            log.error("分类确认失败: content={}", content, e);
            response.put("status", "error");
            response.put("code", 500);
            response.put("message", "确认失败: " + e.getMessage());
        }

        return response;
    }

    /**
     * 意图分类：根据用户输入内容识别意图
     * @param content 用户输入内容
     * @return 意图标识（对应Spring Bean名称）
     */
    private String classifyIntent(String content) {
        if (content == null || content.trim().isEmpty()) {
            return "default_response";
        }

        String lowerContent = content.toLowerCase().trim();

        // 确认分类（优先级最高）
        if (lowerContent.contains("确认") || lowerContent.matches(".*确认.*\\d+.*\\d+.*")) {
            return "confirm_classification";
        }

        // 跳过分类
        if (lowerContent.contains("跳过") || lowerContent.contains("忽略")) {
            return "confirm_classification";
        }

        // 工单号分类（检测数字 - 优先级高于工单内容分类）
        // 匹配格式：分类 123、123 分类、工单123、仅数字
        if (containsOrderId(lowerContent)) {
            // 明确的分类关键词
            if (lowerContent.contains("分类") || lowerContent.contains("识别") ||
                lowerContent.contains("归类")) {
                return "classify_by_order_id";
            }
            
            // 明确的查询关键词 - 查询工单详情
            if (lowerContent.contains("查询") || lowerContent.contains("详情") ||
                lowerContent.contains("查看")) {
                return "ticketService.getTicketDetail";
            }
            
            // 仅数字 - 默认为查询工单详情
            if (lowerContent.matches("^\\d+$")) {
                return "ticketService.getTicketDetail";
            }
            
            // 包含"工单"但不包含分类关键词 - 查询详情
            if (lowerContent.contains("工单") && !lowerContent.contains("分类")) {
                return "ticketService.getTicketDetail";
            }
        }

        // 工单分类相关（对工单内容进行分类）
        if (lowerContent.contains("分类") || lowerContent.contains("识别") ||
            lowerContent.contains("归类") || lowerContent.contains("自动分类")) {
            return "ticketClassifier.classify";
        }

        // 工单详情查询
        if (lowerContent.contains("详情") || lowerContent.contains("查询工单") ||
            lowerContent.contains("工单信息") || lowerContent.contains("查看工单")) {
            return "ticketService.getTicketDetail";
        }

        // 统计信息
        if (lowerContent.contains("统计") || lowerContent.contains("报表") ||
            lowerContent.contains("数据统计") || lowerContent.contains("分析")) {
            return "ticketService.getStatistics";
        }

        // 未分类工单列表
        if (lowerContent.contains("未分类") || lowerContent.contains("待处理") ||
            lowerContent.contains("待分类") || lowerContent.contains("未归类")) {
            return "ticketService.listUncategorized";
        }

        // 默认响应
        return "default_response";
    }

    /**
     * 检查内容是否包含工单号
     */
    private boolean containsOrderId(String content) {
        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher(content);
        return matcher.find();
    }

    /**
     * 从内容中提取工单号
     */
    private Long extractOrderId(String content) {
        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            try {
                return Long.valueOf(matcher.group());
            } catch (NumberFormatException e) {
                log.warn("工单号格式错误: {}", matcher.group());
                return null;
            }
        }
        return null;
    }

    /**
     * 从内容中提取分类ID（第二个数字）
     */
    private Integer extractCategoryId(String content) {
        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher(content);

        int count = 0;
        while (matcher.find()) {
            count++;
            if (count == 2) { // 第二个数字是分类ID
                try {
                    return Integer.valueOf(matcher.group());
                } catch (NumberFormatException e) {
                    log.warn("分类ID格式错误: {}", matcher.group());
                    return null;
                }
            }
        }
        return null;
    }

    /**
     * 构建工单信息
     */
    private Map<String, Object> buildOrderInfo(BizWorkOrder order) {
        Map<String, Object> info = new HashMap<>();
        info.put("orderId", order.getOrderId());
        info.put("orderContent", order.getOrderContent());
        info.put("categoryId", order.getCategoryId());
        info.put("createTime", order.getCreateTime());
        info.put("acceptTime", order.getAcceptTime());
        return info;
    }

    /**
     * 构建分类信息
     */
    private Map<String, Object> buildClassificationInfo(ClassificationResult result) {
        Map<String, Object> info = new HashMap<>();
        info.put("categoryId", result.getCategoryId());
        info.put("categoryName", result.getCategoryName());
        info.put("categoryCode", result.getCategoryCode());
        info.put("confidence", result.getConfidence());
        info.put("matchType", result.getMatchType());
        info.put("matchEvidence", result.getMatchEvidence());
        info.put("aiReasoning", result.getAiReasoning());
        return info;
    }
}