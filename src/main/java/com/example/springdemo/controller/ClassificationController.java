package com.example.springdemo.controller;

import com.example.springdemo.dto.ClassificationResult;
import com.example.springdemo.mapper.BizWorkOrderMapper;
import com.example.springdemo.mapper.RuleKeywordMapper;
import com.example.springdemo.mapper.SysCategoryMapper;
import com.example.springdemo.model.BizWorkOrder;
import com.example.springdemo.model.SysCategory;
import com.example.springdemo.service.ClassificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/classify")
@RequiredArgsConstructor
@Slf4j
public class ClassificationController {

    private final ClassificationService classificationService;
    private final BizWorkOrderMapper workOrderMapper;
    private final SysCategoryMapper categoryMapper;
    private final RuleKeywordMapper ruleKeywordMapper;

    // ========== 核心解决：使用明确的路径定义避免冲突 ==========

    // 静态路径 - 明确指定所有非参数化路径
    @GetMapping("/stats/data")
    public Map<String, Object> getStats() {
        log.info("get system stats");
        return buildStatsResponse();
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "classification service is running");

        Map<String, Object> data = new HashMap<>();
        data.put("status", "UP");
        data.put("timestamp", System.currentTimeMillis());
        result.put("data", data);
        return result;
    }

    @GetMapping("/categories")
    public Map<String, Object> getAllCategories() {
        log.info("get all categories");
        return buildCategoriesResponse();
    }

    @GetMapping("/unclassified")
    public Map<String, Object> getUnclassifiedOrders() {
        log.info("get unclassified orders");
        return buildUnclassifiedResponse();
    }

    // 参数化路径 - 明确指定路径并添加显式类型转换
    @GetMapping("/order-detail/{id}")
    public Map<String, Object> getOrderDetail(@PathVariable("id") Long id) {
        log.info("query order detail: {}", id);
        return buildOrderDetailResponse(id);
    }

    @GetMapping("/classify/{id:\\d+}")
    public Map<String, Object> getClassificationResult(@PathVariable("id") Long id) {
        log.info("query classification result for order: {}", id);
        return buildClassificationResponse(id);
    }

    @PostMapping("/batch")
    public Map<String, Object> batchClassify(@RequestBody Map<String, Object> request) {
        log.info("received batch classification request");
        return buildBatchResponse(request);
    }

    @PostMapping("/reload")
    public Map<String, Object> reloadRules() {
        log.info("manually trigger rule reloading");
        return buildReloadResponse();
    }

    @PostMapping("/preview")
    public Map<String, Object> classifyPreview(@RequestBody Map<String, Object> request) {
        log.info("received preview classification request: {}", request);
        return buildPreviewResponse(request);
    }

    @PostMapping("/confirm")
    public Map<String, Object> confirmClassification(@RequestBody Map<String, Object> request) {
        log.info("received confirm classification request: {}", request);
        return buildConfirmResponse(request);
    }

    // 新增：直接获取未分类工单列表（适合AI处理）
    @GetMapping("/ai/unclassified")
    public Map<String, Object> getUnclassifiedOrdersForAI() {
        log.info("get unclassified orders for AI processing");

        Map<String, Object> result = new HashMap<>();
        try {
            List<BizWorkOrder> unclassifiedOrders = workOrderMapper.selectUnclassified();

            if (unclassifiedOrders.isEmpty()) {
                result.put("code", 200);
                result.put("message", "success");
                result.put("data", "No unclassified orders found.");
                return result;
            }

            // 构建适合AI处理的未分类工单列表
            StringBuilder contentBuilder = new StringBuilder();
            contentBuilder.append("📋 **未分类工单列表**\n\n");
            contentBuilder.append("共有 ").append(unclassifiedOrders.size()).append(" 个未分类工单：\n\n");

            for (BizWorkOrder order : unclassifiedOrders) {
                contentBuilder.append("工单号: ").append(order.getOrderId()).append("\n");
                contentBuilder.append("内容: ").append(order.getOrderContent()).append("\n");
                contentBuilder.append("---\n");
            }

            result.put("code", 200);
            result.put("message", "success");
            result.put("data", contentBuilder.toString());

        } catch (Exception e) {
            log.error("failed to get unclassified orders for AI processing", e);
            result.put("code", 500);
            result.put("message", "failed to get unclassified orders: " + e.getMessage());
            result.put("data", null);
        }
        return result;
    }

    // 私有辅助方法
    private Map<String, Object> buildStatsResponse() {
        Map<String, Object> result = new HashMap<>();
        try {
            Map<String, Object> data = new HashMap<>();

            int totalOrders = workOrderMapper.countTotal();
            data.put("totalOrders", totalOrders);

            int classifiedOrders = workOrderMapper.countClassified();
            data.put("classifiedOrders", classifiedOrders);

            data.put("unclassifiedOrders", totalOrders - classifiedOrders);

            int totalCategories = categoryMapper.countAll();
            data.put("totalCategories", totalCategories);

            int totalKeywords = ruleKeywordMapper.countTotal();
            data.put("totalKeywords", totalKeywords);

            result.put("code", 200);
            result.put("message", "success");
            result.put("data", data);

        } catch (Exception e) {
            log.error("failed to get stats info", e);
            result.put("code", 500);
            result.put("message", "failed to get stats info: " + e.getMessage());
            result.put("data", null);
        }
        return result;
    }

    private Map<String, Object> buildCategoriesResponse() {
        Map<String, Object> result = new HashMap<>();
        try {
            List<SysCategory> categories = categoryMapper.selectAll();
            result.put("code", 200);
            result.put("message", "success");
            result.put("data", categories);
        } catch (Exception e) {
            log.error("failed to get categories list", e);
            result.put("code", 500);
            result.put("message", "failed to get categories list: " + e.getMessage());
            result.put("data", null);
        }
        return result;
    }

    private Map<String, Object> buildUnclassifiedResponse() {
        Map<String, Object> result = new HashMap<>();
        try {
            List<BizWorkOrder> unclassifiedOrders = workOrderMapper.selectUnclassified();
            result.put("code", 200);
            result.put("message", "success");
            result.put("data", unclassifiedOrders);
        } catch (Exception e) {
            log.error("failed to get unclassified orders", e);
            result.put("code", 500);
            result.put("message", "failed to get unclassified orders: " + e.getMessage());
            result.put("data", null);
        }
        return result;
    }

    private Map<String, Object> buildOrderDetailResponse(Long id) {
        Map<String, Object> result = new HashMap<>();
        try {
            BizWorkOrder workOrder = workOrderMapper.selectById(id);

            if (workOrder == null) {
                result.put("code", 404);
                result.put("message", "order not found: " + id);
                result.put("data", null);
                return result;
            }

            result.put("code", 200);
            result.put("message", "success");
            result.put("data", workOrder);

        } catch (Exception e) {
            log.error("failed to query order detail: " + id, e);
            result.put("code", 500);
            result.put("message", "query failed: " + e.getMessage());
            result.put("data", null);
        }
        return result;
    }

    private Map<String, Object> buildClassificationResponse(Long id) {
        Map<String, Object> result = new HashMap<>();
        try {
            ClassificationResult classificationResult = classificationService.classify(id);

            result.put("code", 200);
            result.put("message", "success");
            result.put("data", classificationResult);

        } catch (Exception e) {
            log.error("failed to query classification result for order: " + id, e);
            result.put("code", 500);
            result.put("message", "query failed: " + e.getMessage());
            result.put("data", null);
        }
        return result;
    }

    private Map<String, Object> buildBatchResponse(Map<String, Object> request) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (!request.containsKey("orderIds")) {
                result.put("code", 400);
                result.put("message", "request parameter error, need orderIds array");
                result.put("data", null);
                return result;
            }

            @SuppressWarnings("unchecked")
            List<Long> orderIds = (List<Long>) request.get("orderIds");

            if (orderIds == null || orderIds.isEmpty()) {
                result.put("code", 400);
                result.put("message", "orderIds cannot be empty");
                result.put("data", null);
                return result;
            }

            List<ClassificationResult> results = classificationService.batchClassify(orderIds);

            result.put("code", 200);
            result.put("message", "success");
            result.put("data", results);

        } catch (Exception e) {
            log.error("batch classification failed", e);
            result.put("code", 500);
            result.put("message", "batch classification failed: " + e.getMessage());
            result.put("data", null);
        }
        return result;
    }

    private Map<String, Object> buildReloadResponse() {
        Map<String, Object> result = new HashMap<>();
        try {
            classificationService.reloadRules();

            result.put("code", 200);
            result.put("message", "success");
            result.put("data", "rule reloading succeeded");

        } catch (Exception e) {
            log.error("rule reloading failed", e);
            result.put("code", 500);
            result.put("message", "rule reloading failed: " + e.getMessage());
            result.put("data", null);
        }
        return result;
    }

    private Map<String, Object> buildPreviewResponse(Map<String, Object> request) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (!request.containsKey("orderContent")) {
                result.put("code", 400);
                result.put("message", "request parameter error, need orderContent");
                result.put("data", null);
                return result;
            }

            String orderContent = request.get("orderContent").toString();
            ClassificationResult classificationResult = classificationService.classifyPreview(orderContent);

            result.put("code", 200);
            result.put("message", "success");
            result.put("data", classificationResult);

        } catch (Exception e) {
            log.error("preview classification failed", e);
            result.put("code", 500);
            result.put("message", "preview classification failed: " + e.getMessage());
            result.put("data", null);
        }
        return result;
    }

    private Map<String, Object> buildConfirmResponse(Map<String, Object> request) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (!request.containsKey("orderId") || !request.containsKey("categoryId")) {
                result.put("code", 400);
                result.put("message", "request parameter error, need orderId and categoryId");
                result.put("data", null);
                return result;
            }

            Object orderIdObj = request.get("orderId");
            Object categoryIdObj = request.get("categoryId");

            if (orderIdObj == null) {
                result.put("code", 400);
                result.put("message", "orderId cannot be empty");
                result.put("data", null);
                return result;
            }

            Long orderId;
            try {
                if (orderIdObj instanceof Number) {
                    orderId = ((Number) orderIdObj).longValue();
                } else {
                    orderId = Long.valueOf(orderIdObj.toString());
                }
            } catch (NumberFormatException e) {
                result.put("code", 400);
                result.put("message", "orderId format error");
                result.put("data", null);
                return result;
            }

            if (categoryIdObj == null || "".equals(categoryIdObj.toString().trim())) {
                result.put("code", 400);
                result.put("message", "categoryId parameter error");
                result.put("data", null);
                return result;
            }

            Integer categoryId;
            try {
                if (categoryIdObj instanceof Number) {
                    categoryId = ((Number) categoryIdObj).intValue();
                } else {
                    categoryId = Integer.valueOf(categoryIdObj.toString());
                }
            } catch (NumberFormatException e) {
                result.put("code", 400);
                result.put("message", "categoryId format error");
                result.put("data", null);
                return result;
            }

            BizWorkOrder workOrder = classificationService.confirmClassification(orderId, categoryId);

            String message = (categoryId == 0) ? "skipped classification" : "classification confirmed";
            result.put("code", 200);
            result.put("message", message);
            result.put("data", workOrder);

        } catch (IllegalArgumentException e) {
            log.error("confirm classification parameter error", e);
            result.put("code", 400);
            result.put("message", e.getMessage());
            result.put("data", null);
        } catch (Exception e) {
            log.error("confirm classification failed", e);
            result.put("code", 500);
            result.put("message", "confirm classification failed: " + e.getMessage());
            result.put("data", null);
        }
        return result;
    }
}