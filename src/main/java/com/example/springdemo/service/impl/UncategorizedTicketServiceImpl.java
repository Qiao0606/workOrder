package com.example.springdemo.service.impl;

import com.example.springdemo.mapper.BizWorkOrderMapper;
import com.example.springdemo.model.BizWorkOrder;
import com.example.springdemo.service.TicketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("ticketService.listUncategorized")
@RequiredArgsConstructor
@Slf4j
public class UncategorizedTicketServiceImpl implements TicketService {

    private final BizWorkOrderMapper workOrderMapper;

    @Override
    public Map<String, Object> process(Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            log.info("查询未分类工单列表");
            
            // 查询未分类工单
            List<BizWorkOrder> unclassifiedOrders = workOrderMapper.selectUnclassified();
            
            if (unclassifiedOrders.isEmpty()) {
                response.put("status", "success");
                response.put("code", 200);
                response.put("message", "暂无未分类工单");
                response.put("data", unclassifiedOrders);
            } else {
                // 构建返回数据
                Map<String, Object> data = new HashMap<>();
                data.put("total", unclassifiedOrders.size());
                data.put("orders", unclassifiedOrders);
                
                response.put("status", "success");
                response.put("code", 200);
                response.put("message", "查询成功");
                response.put("data", data);
                
                log.info("查询到 {} 条未分类工单", unclassifiedOrders.size());
            }
            
        } catch (Exception e) {
            log.error("查询未分类工单失败", e);
            response.put("status", "error");
            response.put("code", 500);
            response.put("message", "查询失败: " + e.getMessage());
            response.put("data", null);
        }

        return response;
    }
}