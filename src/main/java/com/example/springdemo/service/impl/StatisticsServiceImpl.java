package com.example.springdemo.service.impl;

import com.example.springdemo.mapper.BizWorkOrderMapper;
import com.example.springdemo.mapper.RuleKeywordMapper;
import com.example.springdemo.mapper.SysCategoryMapper;
import com.example.springdemo.service.TicketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service("ticketService.getStatistics")
@RequiredArgsConstructor
@Slf4j
public class StatisticsServiceImpl implements TicketService {

    private final BizWorkOrderMapper workOrderMapper;
    private final SysCategoryMapper categoryMapper;
    private final RuleKeywordMapper ruleKeywordMapper;

    @Override
    public Map<String, Object> process(Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            log.info("查询统计数据");
            
            // 查询工单统计
            int totalOrders = workOrderMapper.countTotal();
            int classifiedOrders = workOrderMapper.countClassified();
            int unclassifiedOrders = totalOrders - classifiedOrders;
            
            // 查询分类和规则统计
            int totalCategories = categoryMapper.countAll();
            int totalKeywords = ruleKeywordMapper.countTotal();
            int activeKeywords = ruleKeywordMapper.countAllActive();
            
            // 构建统计数据
            Map<String, Object> data = new HashMap<>();
            
            // 工单统计
            Map<String, Object> orderStats = new HashMap<>();
            orderStats.put("total", totalOrders);
            orderStats.put("classified", classifiedOrders);
            orderStats.put("unclassified", unclassifiedOrders);
            orderStats.put("classificationRate", totalOrders > 0 ? 
                String.format("%.2f%%", (classifiedOrders * 100.0 / totalOrders)) : "0.00%");
            data.put("orders", orderStats);
            
            // 分类统计
            Map<String, Object> categoryStats = new HashMap<>();
            categoryStats.put("total", totalCategories);
            data.put("categories", categoryStats);
            
            // 规则统计
            Map<String, Object> ruleStats = new HashMap<>();
            ruleStats.put("total", totalKeywords);
            ruleStats.put("active", activeKeywords);
            ruleStats.put("inactive", totalKeywords - activeKeywords);
            data.put("rules", ruleStats);
            
            response.put("status", "success");
            response.put("code", 200);
            response.put("message", "查询成功");
            response.put("data", data);
            
            log.info("统计查询成功: 工单总数={}, 已分类={}, 未分类={}", 
                totalOrders, classifiedOrders, unclassifiedOrders);
            
        } catch (Exception e) {
            log.error("查询统计数据失败", e);
            response.put("status", "error");
            response.put("code", 500);
            response.put("message", "查询失败: " + e.getMessage());
            response.put("data", null);
        }

        return response;
    }
}