# AI工单分类功能使用指南

## 功能概述

本系统实现了通过对话框输入工单号进行智能分类，并支持手动确认分类结果的完整流程。

---

## 核心功能

### 1. 智能意图识别

系统自动识别用户输入的意图，支持以下操作：

| 意图类型 | 触发关键词 | 示例 |
|---------|-----------|------|
| 工单号分类 | 数字+分类/工单 | "分类 12345"、"工单12345"、"12345" |
| 确认分类 | 确认+工单号+分类ID | "确认 12345 5" |
| 跳过分类 | 跳过/忽略+工单号 | "跳过 12345" |
| 工单详情 | 详情、查询工单 | "查询工单详情" |
| 未分类列表 | 未分类、待处理 | "未分类工单" |
| 统计信息 | 统计、报表 | "数据统计" |

---

## 使用流程

### 场景1：工单分类（推荐流程）

**步骤1：输入工单号**
```
用户输入: "分类 12345"
或: "工单12345"
或: "12345 分类"
```

**系统响应:**
```json
{
  "status": "success",
  "code": 200,
  "message": "分类预览完成，等待确认",
  "data": {
    "orderInfo": {
      "orderId": 12345,
      "orderContent": "市民反映道路破损问题...",
      "categoryId": null,
      "createTime": "2024-01-01 10:00:00"
    },
    "classification": {
      "categoryId": 5,
      "categoryName": "市政设施",
      "categoryCode": "CITY_INFRA",
      "confidence": 0.85,
      "matchType": "RULE",
      "matchEvidence": "关键词匹配: 道路、破损",
      "aiReasoning": null
    },
    "needConfirm": true,
    "confirmHint": "请确认是否将工单 12345 分类为 '市政设施'（置信度: 85.00%）？输入 '确认 12345 5' 完成分类，或输入 '跳过 12345' 跳过分类"
  }
}
```

**步骤2：确认分类**
```
用户输入: "确认 12345 5"
```

**系统响应:**
```json
{
  "status": "success",
  "code": 200,
  "message": "工单 12345 已成功分类为: 5",
  "data": {
    "orderId": 12345,
    "orderContent": "市民反映道路破损问题...",
    "categoryId": 5,
    "createTime": "2024-01-01 10:00:00"
  }
}
```

**步骤3（可选）：跳过分类**
```
用户输入: "跳过 12345"
```

**系统响应:**
```json
{
  "status": "success",
  "code": 200,
  "message": "工单 12345 已跳过分类",
  "data": {
    "orderId": 12345,
    "categoryId": 0
  }
}
```

---

### 场景2：直接输入工单号

```
用户输入: "12345"
```

系统会自动识别为工单号，并返回分类预览结果。

---

### 场景3：已分类工单处理

如果工单已经分类，系统会提示：

```json
{
  "status": "warning",
  "code": 200,
  "message": "工单 12345 已分类为: 5",
  "data": {
    "orderId": 12345,
    "categoryId": 5,
    ...
  }
}
```

---

## API接口

### 对话式接口

**请求:**
```http
POST /ai/ticket
Content-Type: application/json

{
  "content": "分类 12345"
}
```

**响应:**
参见上述场景示例

---

### REST API接口

#### 1. 分类预览（不保存）
```http
POST /api/classify/preview
Content-Type: application/json

{
  "orderContent": "市民反映道路破损问题..."
}
```

#### 2. 确认分类（保存到数据库）
```http
POST /api/classify/confirm
Content-Type: application/json

{
  "orderId": 12345,
  "categoryId": 5
}
```

#### 3. 根据工单号分类
```http
GET /api/classify/classify/12345
```

#### 4. 批量分类
```http
POST /api/classify/batch
Content-Type: application/json

{
  "orderIds": [12345, 12346, 12347]
}
```

---

## 关键特性

### ✅ 智能识别
- 自动识别工单号（支持多种格式）
- 自动识别意图（分类、确认、查询等）
- 自动处理已分类工单

### ✅ 预览+确认机制
- 先预览分类结果，不直接保存
- 用户可查看置信度和匹配证据
- 支持手动确认或跳过

### ✅ 多种输入方式
- "分类 12345"
- "工单12345"
- "12345 分类"
- "12345"

### ✅ 完整的错误处理
- 工单不存在
- 工单已分类
- 参数格式错误
- 数据库异常

---

## 分类结果说明

| 字段 | 说明 |
|------|------|
| categoryId | 分类ID |
| categoryName | 分类名称 |
| categoryCode | 分类代码 |
| confidence | 置信度（0-1） |
| matchType | 匹配类型（RULE-规则匹配/AI-AI分类/HYBRID-混合） |
| matchEvidence | 匹配证据（关键词、匹配规则等） |
| aiReasoning | AI推理过程（仅AI分类时） |

---

## 注意事项

1. **工单号识别规则**: 提取内容中的第一个数字作为工单号
2. **分类ID识别规则**: 提取内容中的第二个数字作为分类ID
3. **确认操作**: 格式为 "确认 {工单号} {分类ID}"
4. **跳过操作**: 分类ID设为0，表示跳过分类
5. **已分类工单**: 系统会提示已分类状态，避免重复分类

---

## 测试示例

### 测试1：正常分类流程
```bash
# 1. 输入工单号
curl -X POST http://localhost:8080/ai/ticket \
  -H "Content-Type: application/json" \
  -d '{"content": "分类 12345"}'

# 2. 确认分类
curl -X POST http://localhost:8080/ai/ticket \
  -H "Content-Type: application/json" \
  -d '{"content": "确认 12345 5"}'
```

### 测试2：跳过分类
```bash
curl -X POST http://localhost:8080/ai/ticket \
  -H "Content-Type: application/json" \
  -d '{"content": "跳过 12345"}'
```

### 测试3：查询未分类工单
```bash
curl -X POST http://localhost:8080/ai/ticket \
  -H "Content-Type: application/json" \
  -d '{"content": "未分类工单"}'
```

---

## 更新日志

**v1.0.0 (2026-04-27)**
- ✅ 实现工单号识别和分类功能
- ✅ 实现预览+确认机制
- ✅ 支持多种输入格式
- ✅ 完善的错误处理
- ✅ 支持跳过分类操作