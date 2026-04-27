package com.example.springdemo.service;

import com.example.springdemo.dto.ClassificationResult;
import com.example.springdemo.model.BizWorkOrder;

import java.util.List;

public interface ClassificationService {
    ClassificationResult classify(Long orderId);

    ClassificationResult classify(String orderContent);

    List<ClassificationResult> batchClassify(List<Long> orderIds);

    void reloadRules();

    /**
     * 仅分类不保存（预览模式）
     * @param orderContent 工单内容
     * @return 分类结果
     */
    ClassificationResult classifyPreview(String orderContent);

    /**
     * 确认分类并保存到数据库
     * @param orderId 工单ID
     * @param categoryId 分类ID
     * @return 更新后的工单信息
     */
    BizWorkOrder confirmClassification(Long orderId, Integer categoryId);
}
