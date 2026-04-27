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
                // 构建工单信息Map
                Map<String, Object> orderInfo = new HashMap<>();
                orderInfo.put("orderId", workOrder.getOrderId());
                orderInfo.put("orderCode", workOrder.getOrderCode());
                orderInfo.put("categoryId", workOrder.getCategoryId());
                orderInfo.put("acceptTime", workOrder.getAcceptTime());
                orderInfo.put("acceptUser", workOrder.getAcceptUser());
                orderInfo.put("userPhone", workOrder.getUserPhone());
                orderInfo.put("userRegion", workOrder.getUserRegion());
                orderInfo.put("userAddress", workOrder.getUserAddress());
                orderInfo.put("orderContent", workOrder.getOrderContent());
                orderInfo.put("status", workOrder.getStatus());
                orderInfo.put("createTime", workOrder.getCreateTime());
                orderInfo.put("updateTime", workOrder.getUpdateTime());
                
                // 构建返回数据
                Map<String, Object> data = new HashMap<>();
                data.put("orderInfo", orderInfo);
                
                response.put("status", "success");
                response.put("code", 200);
                response.put("message", "查询成功");
                response.put("data", data);
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