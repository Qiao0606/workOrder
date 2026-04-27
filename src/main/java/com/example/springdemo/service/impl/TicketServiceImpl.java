package com.example.springdemo.service.impl;

import com.example.springdemo.mapper.BizWorkOrderMapper;
import com.example.springdemo.model.BizWorkOrder;
import com.example.springdemo.service.TicketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service("ticketService.getTicketDetail")
@RequiredArgsConstructor
@Slf4j
public class TicketServiceImpl implements TicketService {

    private final BizWorkOrderMapper workOrderMapper;

    @Override
    public Map<String, Object> process(Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 获取工单ID参数
            String orderIdStr = request.get("orderId");
            if (orderIdStr == null || orderIdStr.trim().isEmpty()) {
                response.put("status", "error");
                response.put("code", 400);
                response.put("message", "工单ID不能为空");
                return response;
            }

            // 转换工单ID
            Long orderId;
            try {
                orderId = Long.valueOf(orderIdStr.trim());
            } catch (NumberFormatException e) {
                response.put("status", "error");
                response.put("code", 400);
                response.put("message", "工单ID格式错误");
                return response;
            }

            // 查询工单详情
            log.info("查询工单详情: orderId={}", orderId);
            BizWorkOrder workOrder = workOrderMapper.selectById(orderId);

            if (workOrder == null) {
                response.put("status", "error");
                response.put("code", 404);
                response.put("message", "工单不存在: " + orderId);
                response.put("data", null);
            } else {
                response.put("status", "success");
                response.put("code", 200);
                response.put("message", "查询成功");
                response.put("data", workOrder);
            }

        } catch (Exception e) {
            log.error("查询工单详情失败", e);
            response.put("status", "error");
            response.put("code", 500);
            response.put("message", "查询失败: " + e.getMessage());
            response.put("data", null);
        }

        return response;
    }
}