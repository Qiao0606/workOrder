# 工单查询返回格式修复说明

## 问题描述

后端成功查询到工单数据，但前端显示"操作失败"。

从日志看：
```
查询工单详情: orderId=10  ✅ 查询成功
<==    Row: 10, WO202401010, 6, ...  ✅ 数据返回成功
```

但前端显示"操作失败"。

## 问题原因

**后端返回的数据格式**与**前端期望的格式**不匹配。

### 后端返回格式（修复前）

```json
{
  "code": 200,
  "status": "success",
  "message": "查询成功",
  "data": {
    "orderId": 10,
    "orderCode": "WO202401010",
    "categoryId": 6,
    ...
  }
}
```

### 前端期望的格式

```javascript
// 前端代码第250-255行
if (result.data.orderInfo) {  // ❌ 期望 data 中有 orderInfo 字段
    const orderInfo = result.data.orderInfo;
    const reply = `📋 工单详情\n\n工单号：${orderInfo.orderId}...`;
}
```

前端期望 `result.data.orderInfo`，但后端直接返回 `result.data` 就是工单对象，导致 `result.data.orderInfo` 为 `undefined`。

---

## 解决方案

修改后端返回格式，将工单对象包裹在 `orderInfo` 字段中：

### 修复后的代码

```java
if (workOrder == null) {
    response.put("status", "error");
    response.put("code", 404);
    response.put("message", "工单不存在: " + orderId);
    response.put("data", null);
} else {
    // ✅ 构建工单信息Map
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
    
    // ✅ 构建返回数据，包裹在orderInfo中
    Map<String, Object> data = new HashMap<>();
    data.put("orderInfo", orderInfo);
    
    response.put("status", "success");
    response.put("code", 200);
    response.put("message", "查询成功");
    response.put("data", data);
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
      "orderId": 10,
      "orderCode": "WO202401010",
      "categoryId": 6,
      "acceptTime": "2026-04-27T13:58:20",
      "acceptUser": "王十二",
      "userPhone": "13800138010",
      "userRegion": "顺义区",
      "userAddress": "MM街道NN路",
      "orderContent": "小区停车位被占用，物业不处理",
      "status": 1,
      "createTime": "2026-04-27T13:58:20",
      "updateTime": "2026-04-27T15:54:53"
    }
  }
}
```

---

## 前端显示效果

修复后，前端会正确显示：

```
📋 工单详情

工单号：10
内容：小区停车位被占用，物业不处理
时间：2026-04-27 13:58:20
```

---

## 验证方法

1. 输入"查询 10"
2. 应显示工单10的详细信息
3. 检查日志应看到查询成功的记录