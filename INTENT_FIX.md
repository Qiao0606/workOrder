# 意图识别问题修复说明

## 问题描述

用户输入"查询 10"时，系统错误地触发了**工单分类**功能，而不是**查询工单详情**。

## 问题原因

在 `TicketAIApi.classifyIntent()` 方法中，意图识别逻辑有误：

### 错误的代码（修复前）

```java
// 工单号分类（检测数字）
if (containsOrderId(lowerContent)) {
    // ❌ 错误：把"查询"也当作分类关键词
    if (lowerContent.contains("分类") || lowerContent.contains("识别") ||
        lowerContent.contains("归类") || lowerContent.contains("工单") ||
        lowerContent.contains("查询") || lowerContent.matches("^\\d+$")) {
        return "classify_by_order_id";  // 所有包含数字和这些关键词的都当作分类
    }
}
```

**问题分析**：
- 用户输入"查询 10"
- 代码检测到数字 `10` 和关键词"查询"
- 错误地返回 `classify_by_order_id`（分类）
- 而应该返回 `ticketService.getTicketDetail`（查询详情）

---

## 解决方案

修改意图识别逻辑，明确区分"查询"和"分类"意图：

### 正确的代码（修复后）

```java
// 工单号分类（检测数字 - 优先级高于工单内容分类）
if (containsOrderId(lowerContent)) {
    // ✅ 明确的分类关键词
    if (lowerContent.contains("分类") || lowerContent.contains("识别") ||
        lowerContent.contains("归类")) {
        return "classify_by_order_id";
    }
    
    // ✅ 明确的查询关键词 - 查询工单详情
    if (lowerContent.contains("查询") || lowerContent.contains("详情") ||
        lowerContent.contains("查看")) {
        return "ticketService.getTicketDetail";
    }
    
    // ✅ 仅数字 - 默认为查询工单详情
    if (lowerContent.matches("^\\d+$")) {
        return "ticketService.getTicketDetail";
    }
    
    // ✅ 包含"工单"但不包含分类关键词 - 查询详情
    if (lowerContent.contains("工单") && !lowerContent.contains("分类")) {
        return "ticketService.getTicketDetail";
    }
}
```

---

## 意图识别规则

修复后的意图识别规则：

| 用户输入 | 识别意图 | 执行操作 |
|---------|---------|---------|
| "分类 10" | `classify_by_order_id` | 对工单10进行分类 |
| "识别 10" | `classify_by_order_id` | 对工单10进行分类 |
| "归类 10" | `classify_by_order_id` | 对工单10进行分类 |
| "查询 10" | `ticketService.getTicketDetail` | 查询工单10详情 |
| "详情 10" | `ticketService.getTicketDetail` | 查询工单10详情 |
| "查看 10" | `ticketService.getTicketDetail` | 查询工单10详情 |
| "10"（仅数字） | `ticketService.getTicketDetail` | 查询工单10详情 |
| "工单 10" | `ticketService.getTicketDetail` | 查询工单10详情 |
| "工单分类 10" | `classify_by_order_id` | 对工单10进行分类 |

---

## 测试验证

### 测试步骤

1. **查询工单**
   - 输入："查询 10"
   - 预期：显示工单10的详细信息

2. **分类工单**
   - 输入："分类 10"
   - 预期：显示分类预览和确认按钮

3. **仅数字**
   - 输入："10"
   - 预期：显示工单10的详细信息（默认查询）

4. **工单详情**
   - 输入："工单 10"
   - 预期：显示工单10的详细信息

---

## 注意事项

1. **关键词优先级**：分类关键词 > 查询关键词 > 默认查询
2. **仅数字处理**：默认当作查询工单详情，避免误触发分类
3. **组合关键词**：如"工单分类 10"，会优先匹配"分类"关键词