# 意图识别优先级修复说明

## 问题描述

用户输入"分类 10"时，系统返回的是：
```
🏷️ 分类：房价管理(FJGL)
置信度：3.50
```

而不是预期的分类预览和确认按钮。

## 问题原因

后端意图识别逻辑的优先级问题：

**错误的优先级**：
1. 确认分类
2. 跳过分类
3. ❌ 工单内容分类（先匹配）
4. 工单号分类（后匹配，被前面的规则拦截）

**输入"分类 10"的匹配过程**：
1. 包含"分类"关键词
2. ✅ 匹配到 `ticketClassifier.classify`（工单内容分类）
3. ❌ 不会执行工单号分类逻辑

**结果**：调用了 `TicketClassifierImpl.classify()`，对字符串"分类 10"进行内容分类，返回分类结果。

---

## 修复方案

### 调整意图识别优先级

将工单号分类的检测逻辑**提前**，优先级高于工单内容分类：

**修复后的优先级**：
1. 确认分类
2. 跳过分类
3. ✅ 工单号分类（优先检测）
4. 工单内容分类

### 修复代码

**文件**: `TicketAIApi.java`

```java
private String classifyIntent(String content) {
    if (content == null || content.trim().isEmpty()) {
        return "default_response";
    }

    String lowerContent = content.toLowerCase().trim();

    // 1. 确认分类（优先级最高）
    if (lowerContent.contains("确认") || lowerContent.matches(".*确认.*\\d+.*\\d+.*")) {
        return "confirm_classification";
    }

    // 2. 跳过分类
    if (lowerContent.contains("跳过") || lowerContent.contains("忽略")) {
        return "confirm_classification";
    }

    // 3. 工单号分类（优先级高于工单内容分类）
    // 匹配格式：分类 123、123 分类、工单123、查询123、仅数字
    if (containsOrderId(lowerContent)) {
        // 如果包含"分类"、"工单"、"查询"等关键词，或者只有数字
        if (lowerContent.contains("分类") || lowerContent.contains("识别") ||
            lowerContent.contains("归类") || lowerContent.contains("工单") ||
            lowerContent.contains("查询") || lowerContent.matches("^\\d+$")) {
            return "classify_by_order_id";  // ✅ 返回工单号分类
        }
    }

    // 4. 工单内容分类
    if (lowerContent.contains("分类") || lowerContent.contains("识别") ||
        lowerContent.contains("归类") || lowerContent.contains("自动分类")) {
        return "ticketClassifier.classify";
    }

    // ... 其他意图
}
```

---

## 匹配规则说明

### 工单号分类触发条件

需要**同时满足**以下条件：
1. 内容包含数字（`containsOrderId()` 返回 true）
2. 内容包含以下关键词之一：
   - "分类"
   - "识别"
   - "归类"
   - "工单"
   - "查询"
   - 或者内容只包含数字（`^\d+$`）

### 支持的输入格式

| 输入格式 | 示例 | 匹配结果 |
|---------|------|---------|
| 分类+数字 | "分类 10" | ✅ 工单号分类 |
| 数字+分类 | "10 分类" | ✅ 工单号分类 |
| 工单+数字 | "工单 10" | ✅ 工单号分类 |
| 查询+数字 | "查询 10" | ✅ 工单号分类 |
| 仅数字 | "10" | ✅ 工单号分类 |
| 分类内容 | "道路破损需要维修" | ✅ 工单内容分类 |

---

## 修复前后对比

### 修复前

**输入**: "分类 10"

**匹配过程**:
1. 检测到"分类"关键词
2. ✅ 匹配到 `ticketClassifier.classify`
3. 调用 `TicketClassifierImpl.classify("分类 10")`
4. 对字符串"分类 10"进行AI分类

**返回**:
```json
{
  "code": 200,
  "data": {
    "categoryId": 10,
    "categoryName": "房价管理(FJGL)",
    "confidence": 0.035,
    "matchType": "AI"
  }
}
```

**前端显示**:
```
🏷️ 分类：房价管理(FJGL)
置信度：3.50
```

### 修复后

**输入**: "分类 10"

**匹配过程**:
1. 检测到数字 "10"
2. 检测到"分类"关键词
3. ✅ 匹配到 `classify_by_order_id`
4. 调用 `handleClassifyByOrderId("分类 10")`
5. 提取工单号：10
6. 查询工单信息
7. 调用分类服务（预览模式）
8. 返回预览结果

**返回**:
```json
{
  "code": 200,
  "data": {
    "needConfirm": true,
    "orderInfo": {
      "orderId": 10,
      "orderContent": "市民反映XX道路路灯不亮..."
    },
    "classification": {
      "categoryId": 1,
      "categoryName": "城市管理(CSGL)",
      "confidence": 0.92,
      "matchEvidence": "关键词匹配: 道路、路灯"
    },
    "confirmHint": "请确认是否将工单 10 分类为 '城市管理'？"
  }
}
```

**前端显示**:
```
🏷️ 分类预览结果

━━━━━━━━━━━━━━━
📋 工单号：10
📝 工单内容：市民反映XX道路路灯不亮...
━━━━━━━━━━━━━━━

✅ 推荐分类：城市管理(CSGL)
📊 分类ID：1
📈 置信度：92.0%
🔍 匹配类型：RULE
💡 匹配证据：关键词匹配: 道路、路灯

[✓ 确认分类]  [✗ 跳过分类]
```

---

## 测试验证

### 测试用例

| 输入 | 预期意图 | 预期行为 |
|------|---------|---------|
| "分类 10" | classify_by_order_id | 分类预览 |
| "10 分类" | classify_by_order_id | 分类预览 |
| "工单 10" | classify_by_order_id | 分类预览 |
| "10" | classify_by_order_id | 分类预览 |
| "确认 10 1" | confirm_classification | 确认分类 |
| "跳过 10" | confirm_classification | 跳过分类 |
| "道路破损需要维修" | ticketClassifier.classify | 内容分类 |
| "查询工单详情" | ticketService.getTicketDetail | 查询详情 |

### 验证步骤

1. 重新编译项目
```bash
mvn clean compile -DskipTests
```

2. 重启应用
```bash
mvn spring-boot:run
```

3. 清除浏览器缓存
   - Chrome: Ctrl+Shift+Delete 或 Cmd+Shift+Delete
   - 或者使用无痕模式

4. 访问前端
```
http://localhost:8080/index.html
```

5. 测试输入
   - 输入："分类 10"
   - 预期：显示分类预览和确认按钮

---

## 注意事项

### 1. 浏览器缓存

前端代码已更新，但浏览器可能缓存了旧的JS文件。解决方法：

**方法1：强制刷新**
- Windows: Ctrl+F5
- Mac: Cmd+Shift+R

**方法2：清除缓存**
- Chrome: 设置 → 隐私和安全 → 清除浏览数据

**方法3：无痕模式**
- Chrome: Ctrl+Shift+N (Windows) / Cmd+Shift+N (Mac)

### 2. 应用重启

后端代码修改后需要重启应用：
```bash
# 停止应用（Ctrl+C）
# 重新编译
mvn clean package -DskipTests

# 启动应用
java -jar target/SpringDemo-0.0.1-SNAPSHOT.jar
```

### 3. 验证修改生效

查看日志中的意图识别结果：
```
2026-04-27 14:05:00 INFO  TicketAIApi - 收到AI工单请求: content=分类 10
2026-04-27 14:05:00 INFO  TicketAIApi - 识别意图: classify_by_order_id
```

---

## 编译验证

✅ 编译成功
```
[INFO] BUILD SUCCESS
[INFO] Total time: 1.600 s
```

---

**修复时间**: 2026-04-27  
**修复文件**: `TicketAIApi.java`  
**修复内容**: 调整意图识别优先级，工单号分类优先于工单内容分类