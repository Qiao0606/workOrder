# 分类预览和确认功能修复说明

## 问题描述

用户输入"分类 10"后，系统只返回了分类结果，没有弹出确认框让用户确认是否保存到数据库。

## 问题原因

前端 `sendMessage()` 函数没有调用后端的 `/ai/ticket` 接口，而是使用了旧的分类逻辑，导致：
1. 无法识别工单号分类请求
2. 无法处理 `needConfirm` 标志
3. 无法显示确认按钮

## 修复内容

### 1. 修改前端消息发送逻辑

**文件**: `src/main/resources/static/js/chat-api.js`

**修改前**:
```javascript
async function sendMessage() {
    const actionType = detectActionType(message);
    let response;
    
    if (actionType === 'query') {
        response = await queryWorkOrder(orderId);
    } else if (actionType === 'classify') {
        response = await previewClassify(cleanContent);
    } else {
        response = await callGeneralApi(message);
    }
    // ...
}
```

**修改后**:
```javascript
async function sendMessage() {
    // 调用AI接口处理消息
    const response = await fetch('/ai/ticket', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ content: message })
    });
    
    const result = await response.json();
    
    // 检查是否需要确认
    if (result.data.needConfirm) {
        displayClassificationPreview(result.data);
    }
    // ...
}
```

### 2. 新增分类预览显示函数

```javascript
function displayClassificationPreview(data) {
    const orderInfo = data.orderInfo;
    const classification = data.classification;
    
    // 显示分类预览信息
    let reply = `🏷️ 分类预览结果\n\n`;
    reply += `📋 工单号：${orderInfo.orderId}\n`;
    reply += `📝 工单内容：${orderInfo.orderContent}\n`;
    reply += `✅ 推荐分类：${classification.categoryName}\n`;
    reply += `📈 置信度：${(classification.confidence * 100).toFixed(1)}%\n`;
    
    addMessageToUI('ai', reply);
    
    // 添加确认和取消按钮
    const buttonsHtml = `
        <button onclick="confirmOrderClassification(${orderId}, ${categoryId})">✓ 确认分类</button>
        <button onclick="skipClassification(${orderId})">✗ 跳过分类</button>
    `;
    messageList.lastElementChild.innerHTML += buttonsHtml;
}
```

### 3. 新增确认分类函数

```javascript
async function confirmOrderClassification(orderId, categoryId) {
    // 发送确认请求
    const response = await fetch('/ai/ticket', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ content: `确认 ${orderId} ${categoryId}` })
    });
    
    // 更新数据库
    // 显示成功消息
}
```

### 4. 新增跳过分类函数

```javascript
async function skipClassification(orderId) {
    // 发送跳过请求
    const response = await fetch('/ai/ticket', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ content: `跳过 ${orderId}` })
    });
    
    // 显示跳过消息
}
```

---

## 完整流程

### 步骤1：用户输入"分类 10"

**前端发送**:
```javascript
POST /ai/ticket
{
  "content": "分类 10"
}
```

### 步骤2：后端识别并处理

**后端处理**:
1. 识别为工单号分类请求
2. 查询工单信息
3. 调用分类服务（预览模式，不保存）
4. 返回预览结果

**后端返回**:
```json
{
  "status": "success",
  "code": 200,
  "message": "分类预览完成，等待确认",
  "data": {
    "needConfirm": true,
    "orderInfo": {
      "orderId": 10,
      "orderContent": "市民反映XX道路路灯不亮..."
    },
    "classification": {
      "categoryId": 1,
      "categoryName": "城市管理",
      "confidence": 0.92,
      "matchType": "RULE",
      "matchEvidence": "关键词匹配: 道路、路灯"
    },
    "confirmHint": "请确认是否将工单 10 分类为 '城市管理'（置信度: 92.00%）？输入 '确认 10 1' 完成分类"
  }
}
```

### 步骤3：前端显示预览和按钮

**显示内容**:
```
🏷️ 分类预览结果

━━━━━━━━━━━━━━━
📋 工单号：10
📝 工单内容：市民反映XX道路路灯不亮...
━━━━━━━━━━━━━━━

✅ 推荐分类：城市管理
📊 分类ID：1
📈 置信度：92.0%
🔍 匹配类型：RULE
💡 匹配证据：关键词匹配: 道路、路灯

━━━━━━━━━━━━━━━
💡 请确认是否将工单 10 分类为 '城市管理'（置信度: 92.00%）？

[✓ 确认分类]  [✗ 跳过分类]
```

### 步骤4A：用户点击"确认分类"

**前端发送**:
```javascript
POST /ai/ticket
{
  "content": "确认 10 1"
}
```

**后端处理**:
1. 调用 `classificationService.confirmClassification(10, 1)`
2. 更新数据库 `biz_work_order` 表
3. 返回成功消息

**后端返回**:
```json
{
  "status": "success",
  "code": 200,
  "message": "工单 10 已成功分类为: 1",
  "data": {
    "orderId": 10,
    "categoryId": 1,
    "orderContent": "市民反映XX道路路灯不亮..."
  }
}
```

**前端显示**:
```
✅ 分类确认成功

工单号：10
已分类为：分类ID 1
```

### 步骤4B：用户点击"跳过分类"

**前端发送**:
```javascript
POST /ai/ticket
{
  "content": "跳过 10"
}
```

**后端处理**:
1. 调用 `classificationService.confirmClassification(10, 0)`
2. 更新数据库，将 `category_id` 设为 0（跳过状态）
3. 返回成功消息

**前端显示**:
```
⏭️ 已跳过分类

工单号：10
```

---

## 后端接口说明

### `/ai/ticket` 接口

**支持的输入格式**：

| 输入格式 | 示例 | 说明 |
|---------|------|------|
| 分类+工单号 | "分类 10" | 分类预览 |
| 工单号+分类 | "10 分类" | 分类预览 |
| 仅工单号 | "10" | 分类预览 |
| 确认分类 | "确认 10 1" | 保存到数据库 |
| 跳过分类 | "跳过 10" | 标记为跳过 |

**响应格式**：

**分类预览**:
```json
{
  "needConfirm": true,
  "orderInfo": { /* 工单信息 */ },
  "classification": { /* 分类结果 */ },
  "confirmHint": "提示信息"
}
```

**确认分类**:
```json
{
  "status": "success",
  "code": 200,
  "message": "工单 10 已成功分类为: 1",
  "data": { /* 更新后的工单信息 */ }
}
```

---

## 数据库更新

确认分类后，会更新 `biz_work_order` 表：

```sql
UPDATE biz_work_order
SET category_id = ?,
    ai_candidate_category = ?,
    ai_match_evidence = ?,
    update_time = NOW()
WHERE order_id = ?
```

**字段说明**：
- `category_id`: 分类ID（确认后设置）
- `ai_candidate_category`: 分类名称
- `ai_match_evidence`: 匹配证据
- `update_time`: 更新时间

---

## 测试步骤

1. 启动应用
```bash
mvn spring-boot:run
```

2. 访问前端
```
http://localhost:8080/index.html
```

3. 测试流程
   - 输入："分类 10"
   - 查看分类预览结果
   - 点击"确认分类"或"跳过分类"
   - 验证数据库是否更新

4. 验证数据库
```sql
SELECT * FROM biz_work_order WHERE order_id = 10;
```

---

## 修复验证

✅ 前端正确调用 `/ai/ticket` 接口
✅ 后端返回 `needConfirm: true`
✅ 前端显示分类预览和确认按钮
✅ 点击确认按钮后更新数据库
✅ 点击跳过按钮后标记为跳过
✅ 编译通过，无错误

---

**修复时间**: 2026-04-27  
**修复文件**: `src/main/resources/static/js/chat-api.js`