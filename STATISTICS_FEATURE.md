# 统计信息功能说明

## 功能概述

前端页面的"📊 查看统计"按钮现在已完整实现，点击后会实时从后端获取系统统计信息并显示。

---

## 实现的功能

### 前端实现

**文件**: `src/main/resources/static/js/chat-api.js`

#### 1. 修改按钮行为
```javascript
function fillQuickQuestion(type) {
    // 类型3是统计功能，直接调用
    if (type === 3) {
        loadStatistics();
        return;
    }
    // ...
}
```

#### 2. 新增统计信息获取函数
```javascript
async function loadStatistics() {
    disableInput();
    addMessageToUI('user', '📊 查看系统统计信息');
    const lid = addLoadingState();

    try {
        const res = await fetch('/api/classify/stats/data');
        const result = await res.json();
        removeLoadingState(lid);

        if (result.code === 200 && result.data) {
            const stats = result.data;
            const classifiedRate = stats.totalOrders > 0 
                ? ((stats.classifiedOrders / stats.totalOrders) * 100).toFixed(1) 
                : 0;
            
            const reply = `📊 系统统计信息

📈 工单统计
━━━━━━━━━━━━━━━
📦 总工单数：${stats.totalOrders}
✅ 已分类工单：${stats.classifiedOrders}
⏳ 未分类工单：${stats.unclassifiedOrders}
📊 分类率：${classifiedRate}%

🏷️ 分类统计
━━━━━━━━━━━━━━━
📋 分类总数：${stats.totalCategories}

📝 规则统计
━━━━━━━━━━━━━━━
🔢 关键词规则数：${stats.totalKeywords}`;
            
            addMessageToUI('ai', reply);
            saveCurrentMessage("ai", reply);
        }
        // ...
    }
}
```

---

### 后端接口

**接口**: `GET /api/classify/stats/data`

**Controller**: `ClassificationController.java:32-36`
```java
@GetMapping("/stats/data")
public Map<String, Object> getStats() {
    log.info("get system stats");
    return buildStatsResponse();
}
```

**实现逻辑**: `ClassificationController.java:141-171`
```java
private Map<String, Object> buildStatsResponse() {
    Map<String, Object> result = new HashMap<>();
    try {
        Map<String, Object> data = new HashMap<>();

        int totalOrders = workOrderMapper.countTotal();
        data.put("totalOrders", totalOrders);

        int classifiedOrders = workOrderMapper.countClassified();
        data.put("classifiedOrders", classifiedOrders);

        data.put("unclassifiedOrders", totalOrders - classifiedOrders);

        int totalCategories = categoryMapper.countAll();
        data.put("totalCategories", totalCategories);

        int totalKeywords = ruleKeywordMapper.countTotal();
        data.put("totalKeywords", totalKeywords);

        result.put("code", 200);
        result.put("message", "success");
        result.put("data", data);

    } catch (Exception e) {
        log.error("failed to get stats info", e);
        result.put("code", 500);
        result.put("message", "failed to get stats info: " + e.getMessage());
        result.put("data", null);
    }
    return result;
}
```

---

## 显示效果

点击"📊 查看统计"按钮后，会显示：

```
📊 系统统计信息

📈 工单统计
━━━━━━━━━━━━━━━
📦 总工单数：150
✅ 已分类工单：120
⏳ 未分类工单：30
📊 分类率：80.0%

🏷️ 分类统计
━━━━━━━━━━━━━━━
📋 分类总数：15

📝 规则统计
━━━━━━━━━━━━━━━
🔢 关键词规则数：45
```

---

## 返回数据结构

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "totalOrders": 150,
    "classifiedOrders": 120,
    "unclassifiedOrders": 30,
    "totalCategories": 15,
    "totalKeywords": 45
  }
}
```

---

## 数据来源

| 统计项 | 数据表 | SQL |
|-------|--------|-----|
| 总工单数 | biz_work_order | `SELECT COUNT(*) FROM biz_work_order` |
| 已分类工单数 | biz_work_order | `SELECT COUNT(*) FROM biz_work_order WHERE category_id > 0` |
| 分类总数 | sys_category | `SELECT COUNT(*) FROM sys_category` |
| 关键词规则数 | rule_keyword | `SELECT COUNT(*) FROM rule_keyword` |

---

## 使用方式

### 方式1：点击按钮
在聊天页面点击快捷按钮"📊 查看统计"

### 方式2：直接调用API
```bash
curl http://localhost:8080/api/classify/stats/data
```

---

## 错误处理

- **网络异常**: 显示"❌ 网络异常，无法获取统计信息"
- **服务端错误**: 显示"❌ 获取统计信息失败: {错误信息}"
- **空数据**: 显示实际统计结果（可能为0）

---

## 更新日志

**v1.0.0 (2026-04-27)**
- ✅ 实现"查看统计"按钮功能
- ✅ 集成后端统计接口
- ✅ 美化统计信息显示
- ✅ 添加错误处理
- ✅ 添加加载动画