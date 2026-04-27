package com.example.springdemo.aspect;

import com.example.springdemo.model.BizWorkOrder;
import com.example.springdemo.service.ClassificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class ClassificationAspect {

    private final ClassificationService classificationService;

    @Pointcut("execution(* com.example.springdemo.service.BizWorkOrderService.save(..)) && args(workOrder)")
    public void workOrderSavePointcut(BizWorkOrder workOrder) {}

    @Pointcut("execution(* com.example.springdemo.service.BizWorkOrderService.update(..)) && args(workOrder)")
    public void workOrderUpdatePointcut(BizWorkOrder workOrder) {}

    @AfterReturning(pointcut = "workOrderSavePointcut(workOrder)", returning = "result")
    public void afterWorkOrderSave(BizWorkOrder workOrder, Object result) {
        if (result instanceof Integer && (Integer) result > 0) {
            log.info("工单保存成功，触发自动分类: {}", workOrder.getOrderCode());
            triggerClassificationAsync(workOrder);
        }
    }

    @AfterReturning(pointcut = "workOrderUpdatePointcut(workOrder)", returning = "result")
    public void afterWorkOrderUpdate(BizWorkOrder workOrder, Object result) {
        if (result instanceof Integer && (Integer) result > 0) {
            log.info("工单更新成功，触发自动分类: {}", workOrder.getOrderId());
            triggerClassificationAsync(workOrder);
        }
    }

    @Async("classificationExecutor")
    public void triggerClassificationAsync(BizWorkOrder workOrder) {
        try {
            // 等待1秒，确保数据库事务提交
            Thread.sleep(1000);

            if (workOrder.getOrderId() != null) {
                log.info("异步分类工单: {}", workOrder.getOrderId());
                classificationService.classify(workOrder.getOrderId());
            } else if (workOrder.getOrderContent() != null) {
                log.info("异步分类工单内容");
                classificationService.classify(workOrder.getOrderContent());
            }
        } catch (Exception e) {
            log.error("异步分类失败", e);
        }
    }
}
