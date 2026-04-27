package com.example.springdemo.service.impl;

import com.example.springdemo.dto.ClassificationResult;
import com.example.springdemo.mapper.BizWorkOrderMapper;
import com.example.springdemo.mapper.RuleKeywordMapper;
import com.example.springdemo.mapper.SysCategoryMapper;
import com.example.springdemo.model.BizWorkOrder;
import com.example.springdemo.model.RuleKeyword;
import com.example.springdemo.model.SysCategory;
import com.example.springdemo.service.AiService;
import com.example.springdemo.service.ClassificationService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClassificationServiceImpl implements ClassificationService {

    private final BizWorkOrderMapper workOrderMapper;
    private final SysCategoryMapper categoryMapper;
    private final RuleKeywordMapper ruleKeywordMapper;
    private final AiService aiService;
    private final ObjectMapper objectMapper;

    @Value("${classification.confidence-threshold:4.0}")
    private Float confidenceThreshold;

    private Map<Integer, SysCategory> categoryCache;
    private List<RuleKeyword> ruleCache;

    @PostConstruct
    public void init() {
        try {
            reloadRules();
        } catch (Exception e) {
            log.warn("Failed to load classification rules on startup, will retry later: {}", e.getMessage());
            // 初始化空缓存，避免空指针
            categoryCache = new ConcurrentHashMap<>();
            ruleCache = new ArrayList<>();
        }
    }

    @Override
    public void reloadRules() {
        log.info("Loading classification rules...");
        try {
            List<SysCategory> categories = categoryMapper.selectAll();
            categoryCache = categories.stream()
                    .collect(Collectors.toMap(SysCategory::getCategoryId, c -> c));

            ruleCache = ruleKeywordMapper.selectAllActive();

            log.info("Loaded {} categories and {} rules", categories.size(), ruleCache.size());
        } catch (Exception e) {
            log.error("Failed to load classification rules", e);
            throw new RuntimeException("Failed to load classification rules", e);
        }
    }

    @Override
    public ClassificationResult classify(Long orderId) {
        BizWorkOrder workOrder = workOrderMapper.selectById(orderId);
        if (workOrder == null) {
            throw new IllegalArgumentException("工单不存在: " + orderId);
        }
        return classifyInternal(workOrder);
    }

    @Override
    public ClassificationResult classify(String orderContent) {
        BizWorkOrder workOrder = new BizWorkOrder();
        workOrder.setOrderContent(orderContent);
        return classifyInternal(workOrder);
    }

    private ClassificationResult classifyInternal(BizWorkOrder workOrder) {
        String content = workOrder.getOrderContent();
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("工单内容不能为空");
        }

        log.info("开始分类工单 {}: {}", workOrder.getOrderId(), content);

        ClassificationResult result = new ClassificationResult();
        result.setOrderId(workOrder.getOrderId());

        // Step 1: 关键词规则匹配
        ClassificationResult ruleResult = matchByRules(content);
        log.debug("规则匹配结果: {}", ruleResult);

        if (ruleResult.getConfidence() >= confidenceThreshold) {
            // 规则匹配置信度高，直接返回
            log.info("规则匹配置信度 {} >= 阈值 {}, 直接返回规则结果",
                    ruleResult.getConfidence(), confidenceThreshold);
            result = ruleResult;
            result.setMatchType("RULE");
        } else {
            // Step 2: AI智能分类
            log.info("规则匹配置信度 {} < 阈值 {}, 调用AI分类",
                    ruleResult.getConfidence(), confidenceThreshold);
            ClassificationResult aiResult = classifyByAI(content);
            log.debug("AI分类结果: {}", aiResult);

            // Step 3: 混合策略
            if (ruleResult.getCategoryId() != null &&
                ruleResult.getCategoryId().equals(aiResult.getCategoryId())) {
                // 规则和AI结果一致，提高置信度
                result = ruleResult;
                result.setConfidence(Math.max(ruleResult.getConfidence(), aiResult.getConfidence()));
                result.setAiReasoning(aiResult.getAiReasoning());
                result.setMatchType("HYBRID");
                result.setMatchEvidence("规则+AI一致: " + ruleResult.getMatchEvidence());
            } else {
                // 规则和AI结果不一致，优先采用AI结果
                result = aiResult;
                result.setMatchType("AI");
                result.setMatchEvidence("AI优先(规则不匹配): " + ruleResult.getMatchEvidence());
            }
        }

        // 更新工单表
        if (workOrder.getOrderId() != null) {
            try {
                workOrderMapper.updateCategory(result);
                log.info("更新工单 {} 分类: categoryId={}, categoryName={}",
                        workOrder.getOrderId(), result.getCategoryId(), result.getCategoryName());
            } catch (Exception e) {
                log.error("更新工单分类失败: " + workOrder.getOrderId(), e);
            }
        }

        log.info("工单分类完成: {}", result);
        return result;
    }

    private ClassificationResult matchByRules(String content) {
        ClassificationResult result = new ClassificationResult();
        result.setConfidence(0f);

        Map<Integer, Float> categoryScores = new HashMap<>();
        Map<Integer, List<String>> categoryEvidence = new HashMap<>();

        String lowerContent = content.toLowerCase();

        for (RuleKeyword rule : ruleCache) {
            boolean matched = false;
            List<String> matchedKeywords = new ArrayList<>();

            // 匹配主关键词
            if (containsKeyword(lowerContent, rule.getKeyword())) {
                matched = true;
                matchedKeywords.add(rule.getKeyword());
            }

            // 匹配同义词
            if (rule.getSynonym() != null && !rule.getSynonym().isEmpty()) {
                String[] synonyms = rule.getSynonym().split(",");
                for (String synonym : synonyms) {
                    if (containsKeyword(lowerContent, synonym.trim())) {
                        matched = true;
                        matchedKeywords.add(synonym.trim());
                    }
                }
            }

            if (matched) {
                Integer categoryId = rule.getCategoryId();
                Float currentScore = categoryScores.getOrDefault(categoryId, 0f);
                categoryScores.put(categoryId, currentScore + rule.getWeight());

                List<String> evidence = categoryEvidence.getOrDefault(categoryId, new ArrayList<>());
                evidence.addAll(matchedKeywords);
                categoryEvidence.put(categoryId, evidence);
            }
        }

        // 找出得分最高的分类
        if (!categoryScores.isEmpty()) {
            Map.Entry<Integer, Float> bestMatch = categoryScores.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .orElse(null);

            if (bestMatch != null) {
                Integer categoryId = bestMatch.getKey();
                SysCategory category = categoryCache.get(categoryId);

                if (category != null) {
                    result.setCategoryId(categoryId);
                    result.setCategoryName(category.getCategoryName());
                    result.setCategoryCode(category.getCategoryCode());
                    result.setConfidence(bestMatch.getValue());
                    result.setMatchEvidence("关键词匹配: " + String.join(", ", categoryEvidence.get(categoryId)) +
                                           " (总权重" + bestMatch.getValue() + ")");
                }
            }
        }

        return result;
    }

    private boolean containsKeyword(String content, String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            return false;
        }
        return content.contains(keyword.toLowerCase());
    }

    private ClassificationResult classifyByAI(String content) {
        ClassificationResult result = new ClassificationResult();

        try {
            // 构建Prompt
            StringBuilder prompt = new StringBuilder();
            prompt.append("你是一个热线工单分类专家。请根据工单内容，从以下分类中选择最合适的一个：\n\n");
            prompt.append("分类列表：\n");

            for (SysCategory category : categoryCache.values()) {
                prompt.append(String.format("%d. %s(%s): %s\n",
                        category.getCategoryId(),
                        category.getCategoryName(),
                        category.getCategoryCode(),
                        category.getRemark() != null ? category.getRemark() : ""));
            }

            prompt.append("\n请返回JSON格式：\n");
            prompt.append("{\n");
            prompt.append("  \"categoryId\": 分类ID,\n");
            prompt.append("  \"categoryName\": \"分类名称\",\n");
            prompt.append("  \"reasoning\": \"分类理由\"\n");
            prompt.append("}\n");
            prompt.append("\n工单内容：").append(content).append("\n");
            prompt.append("\n请给出分类结果：");

            log.debug("AI Prompt: {}", prompt);

            // 调用AI
            long startTime = System.currentTimeMillis();
            String aiResponse = aiService.chat(prompt.toString());
            long duration = System.currentTimeMillis() - startTime;

            log.info("AI分类完成，耗时 {}ms: {}", duration, aiResponse);

            // 解析AI响应
            result = parseAIResponse(aiResponse);
            result.setAiReasoning("AI推理: " + result.getAiReasoning());

        } catch (Exception e) {
            log.error("AI分类失败", e);
            result.setConfidence(0f);
            result.setAiReasoning("AI分类失败: " + e.getMessage());
        }

        return result;
    }

    private ClassificationResult parseAIResponse(String aiResponse) {
        ClassificationResult result = new ClassificationResult();
        result.setConfidence(3.5f); // AI默认置信度

        try {
            // 提取JSON部分
            String jsonStr = extractJson(aiResponse);
            JsonNode rootNode = objectMapper.readTree(jsonStr);

            if (rootNode.has("categoryId")) {
                result.setCategoryId(rootNode.get("categoryId").asInt());
                SysCategory category = categoryCache.get(result.getCategoryId());
                if (category != null) {
                    result.setCategoryName(category.getCategoryName());
                    result.setCategoryCode(category.getCategoryCode());
                }
            }

            if (rootNode.has("categoryName")) {
                result.setCategoryName(rootNode.get("categoryName").asText());
            }

            if (rootNode.has("reasoning")) {
                result.setAiReasoning(rootNode.get("reasoning").asText());
            }

        } catch (Exception e) {
            log.error("解析AI响应失败: {}", aiResponse, e);
            result.setAiReasoning("解析失败: " + e.getMessage());
        }

        return result;
    }

    private String extractJson(String text) {
        // 提取JSON内容（支持```json ```格式或纯JSON）
        int start = text.indexOf("{");
        int end = text.lastIndexOf("}") + 1;

        if (start >= 0 && end > start) {
            return text.substring(start, end);
        }

        return text;
    }

    @Override
    public List<ClassificationResult> batchClassify(List<Long> orderIds) {
        if (orderIds == null || orderIds.isEmpty()) {
            return Collections.emptyList();
        }

        log.info("批量分类 {} 个工单", orderIds.size());
        List<ClassificationResult> results = new ArrayList<>();

        for (Long orderId : orderIds) {
            try {
                ClassificationResult result = classify(orderId);
                results.add(result);
            } catch (Exception e) {
                log.error("批量分类失败，工单ID: " + orderId, e);
                ClassificationResult errorResult = new ClassificationResult();
                errorResult.setOrderId(orderId);
                errorResult.setMatchEvidence("分类失败: " + e.getMessage());
                results.add(errorResult);
            }
        }

        log.info("批量分类完成，成功 {} 个，失败 {} 个",
                results.stream().filter(r -> r.getCategoryId() != null).count(),
                results.stream().filter(r -> r.getCategoryId() == null).count());

        return results;
    }

    @Override
    public ClassificationResult classifyPreview(String orderContent) {
        BizWorkOrder workOrder = new BizWorkOrder();
        workOrder.setOrderContent(orderContent);
        return classifyInternalPreview(workOrder);
    }

    @Override
    public BizWorkOrder confirmClassification(Long orderId, Integer categoryId) {
        log.info("确认分类：工单 {} -> 分类 {}", orderId, categoryId);

        // 参数校验
        if (orderId == null) {
            throw new IllegalArgumentException("工单ID不能为空");
        }
        if (categoryId == null) {
            throw new IllegalArgumentException("分类ID不能为空");
        }

        // 查询工单
        BizWorkOrder workOrder = workOrderMapper.selectById(orderId);
        if (workOrder == null) {
            throw new IllegalArgumentException("工单不存在: " + orderId);
        }

        // categoryId = 0 表示跳过/不分类
        if (categoryId == 0) {
            log.info("工单 {} 已标记为跳过分类", orderId);
            workOrder.setStatus(2);
            workOrderMapper.updateById(workOrder);
            return workOrder;
        }
        // 查询分类
        SysCategory category = categoryMapper.selectById(categoryId);
        if (category == null) {
            throw new IllegalArgumentException("分类不存在: " + categoryId);
        }

        // 更新工单分类
        workOrder.setCategoryId(categoryId);
        workOrder.setAiCandidateCategory(category.getCategoryName());
        workOrder.setStatus(1); // 已处理
        workOrderMapper.updateById(workOrder);

        log.info("工单 {} 分类已确认为: {}", orderId, category.getCategoryName());
        return workOrder;
    }

    /**
     * 仅分类不保存（预览模式）
     */
    private ClassificationResult classifyInternalPreview(BizWorkOrder workOrder) {
        String content = workOrder.getOrderContent();
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("工单内容不能为空");
        }

        log.info("预览分类: {}", content);

        ClassificationResult result = new ClassificationResult();
        result.setOrderId(workOrder.getOrderId());

        // Step 1: 关键词规则匹配
        ClassificationResult ruleResult = matchByRules(content);

        if (ruleResult.getConfidence() >= confidenceThreshold) {
            result = ruleResult;
            result.setMatchType("RULE");
        } else {
            // Step 2: AI智能分类
            ClassificationResult aiResult = classifyByAI(content);

            // Step 3: 混合策略
            if (ruleResult.getCategoryId() != null &&
                ruleResult.getCategoryId().equals(aiResult.getCategoryId())) {
                result = ruleResult;
                result.setConfidence(Math.max(ruleResult.getConfidence(), aiResult.getConfidence()));
                result.setAiReasoning(aiResult.getAiReasoning());
                result.setMatchType("HYBRID");
                result.setMatchEvidence("规则+AI一致: " + ruleResult.getMatchEvidence());
            } else {
                result = aiResult;
                result.setMatchType("AI");
                result.setMatchEvidence("AI优先(规则不匹配): " + ruleResult.getMatchEvidence());
            }
        }

        // 注意：预览模式不更新数据库
        log.info("预览分类完成: {}", result);
        return result;
    }
}
