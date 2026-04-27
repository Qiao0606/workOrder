# 双层data嵌套问题修复

## 问题描述

后端查询工单成功，但前端显示"操作失败"。

从API测试返回的数据结构：
```json
{
  "data": {
    "code": 200,
    "data": {
      "orderInfo": {...}
    },
    "message": "查询成功",
    "status": "success"
  }
}
```

**问题**：有两层 `data` 嵌套！

---

## 问题原因

### 代码分析

**TicketAIApi.java 第73行（修复前）**：
```java
response.put("data", ((TicketService) service).process(serviceRequest));
```

**TicketServiceImpl.java 返回值**：
```java
Map<String, Object> response = new HashMap<>();
response.put("code", 200);
response.put("status", "success");
response.put("message", "查询成功");
response.put("data", data);  // 已经包含data字段
return response;
```

### 问题流程

1. `TicketServiceImpl.process()` 返回：
   ```json
   {
     "code": 200,
     "status": "success",
     "message": "查询成功",
     "data": { "orderInfo": {...} }
   }
   ```

2. `TicketAIApi` 又包了一层：
   ```java
   response.put("data", ticketServiceResult);
   ```

3. 最终返回：
   ```json
   {
     "data": {           // ❌ 外层data（TicketAIApi包的）
       "code": 200,
       "status": "success",
       "message": "查询成功",
       "data": {         // ✅ 内层data（TicketServiceImpl的）
         "orderInfo": {...}
       }
     }
   }
   ```

4. 前端判断：
   ```javascript
   if (result.data.orderInfo) {  // ❌ 找不到，因为路径是 result.data.data.orderInfo
   ```

---

## 解决方案

修改 `TicketAIApi.java`，不再包一层 `data`，直接使用 `TicketService` 的返回值：

### 修复后的代码

```java
} else if (service instanceof TicketService) {
    // 从content中提取工单号
    Long orderId = extractOrderId(content);
    
    if (orderId == null) {
        response.put("status", "error");
        response.put("code", 400);
        response.put("message", "无法识别工单号");
    } else {
        Map<String, String> serviceRequest = new HashMap<>();
        serviceRequest.put("orderId", String.valueOf(orderId));
        serviceRequest.put("content", content);
        
        // ✅ 直接使用TicketService的返回值，不再包一层
        response = ((TicketService) service).process(serviceRequest);
    }
}
```

---

## 修复后的数据格式

```json
{
  "code": 200,
  "status": "success",
  "message": "查询成功",
  "data": {
    "orderInfo": {
      "orderId": 8,
      "orderCode": "WO202401008",
      "orderContent": "道路破损严重，有大坑，车辆通行困难",
      "acceptTime": "2026-04-27T13:58:20",
      "acceptUser": "吴十",
      "userPhone": "13800138008",
      "userRegion": "大兴区",
      "userAddress": "II街道JJ路"
    }
  }
}
```

---

## 前端显示效果

修复后，前端会正确显示：

```
📋 工单详情

工单号：8
内容：道路破损严重，有大坑，车辆通行困难
时间：2026-04-27 13:58:20
```