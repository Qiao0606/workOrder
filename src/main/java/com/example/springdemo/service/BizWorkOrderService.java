package com.example.springdemo.service;

import com.example.springdemo.model.BizWorkOrder;

public interface BizWorkOrderService {
    int save(BizWorkOrder workOrder);

    int update(BizWorkOrder workOrder);
}
