package com.example.springdemo.service.impl;

import com.example.springdemo.mapper.BizWorkOrderMapper;
import com.example.springdemo.model.BizWorkOrder;
import com.example.springdemo.service.BizWorkOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class BizWorkOrderServiceImpl implements BizWorkOrderService {

    private final BizWorkOrderMapper workOrderMapper;

    @Override
    public int save(BizWorkOrder workOrder) {
        log.info("保存工单: {}", workOrder.getOrderCode());
        return workOrderMapper.insert(workOrder);
    }

    @Override
    public int update(BizWorkOrder workOrder) {
        log.info("更新工单: {}", workOrder.getOrderId());
        return workOrderMapper.update(workOrder);
    }
}
