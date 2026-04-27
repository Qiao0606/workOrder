# 分类ID解析问题修复说明

## 问题描述

用户输入"分类 10"后，系统返回的分类预览中 `categoryId=0`，导致：
1. 点击"确认分类"时传递的是 `categoryId=0`
2. 数据库更新为 `category_id=0`（表示跳过分类）
3. 工单仍然显示在"未分类工单"列表中

## 问题原因

从日志可以看出：

```
AI分类完成，耗时 4966ms: ```json
{
  "categoryId": "GSGL",  // ❌ AI返回的是字符串类型的分类代码
  "categoryName": "公司管理"
}
```

但代码使用 `asInt()` 解析：

```java
result.setCategoryId(rootNode.get("categoryId").asInt());
// 字符串"GSGL"转换为整数变成0
```

**根本原因**：AI返回的 `categoryId` 是**分类代码**（字符串），而不是分类ID（数字）。

---

## 解决方案

修改 `ClassificationServiceImpl.parseAIResponse()` 方法，支持两种格式：

### 修复后的代码

```java
private ClassificationResult parseAIResponse(String aiResponse) {
    ClassificationResult result = new ClassificationResult();
    result.setConfidence(3.5f);

    try {
        String jsonStr = extractJson(aiResponse);
        JsonNode rootNode = objectMapper.readTree(jsonStr);

        if (rootNode.has("categoryId")) {
            JsonNode categoryIdNode = rootNode.get("categoryId");
            
            // ✅ 检查categoryId是数字还是字符串
            if (categoryIdNode.isNumber()) {
                // 数字类型：直接使用
                result.setCategoryId(categoryIdNode.asInt());
                SysCategory category = categoryCache.get(result.getCategoryId());
                if (category != null) {
                    result.setCategoryName(category.getCategoryName());
                    result.setCategoryCode(category.getCategoryCode());
                }
            } else if (categoryIdNode.isTextual()) {
                // ✅ 字符串类型：根据categoryCode查找对应的categoryId
                String categoryCode = categoryIdNode.asText();
                SysCategory category = findCategoryByCode(categoryCode);
                if (category != null) {
                    result.setCategoryId(category.getCategoryId());
                    result.setCategoryName(category.getCategoryName());
                    result.setCategoryCode(category.getCategoryCode());
                }
            }
        }
        
        // ... 其他解析逻辑 ...
    } catch (Exception e) {
        log.error("解析AI响应失败: {}", aiResponse, e);
    }

    return result;
}

/**
 * 根据分类代码查找分类
 */
private SysCategory findCategoryByCode(String categoryCode) {
    for (SysCategory category : categoryCache.values()) {
        if (categoryCode.equalsIgnoreCase(category.getCategoryCode())) {
            return category;
        }
    }
    return null;
}
```

---

## 分类代码与ID映射关系

| 分类代码 | 分类ID | 分类名称 |
|---------|--------|---------|
| CSGL | 1 | 城市管理 |
| SHHJ | 2 | 社会环境 |
| SBJ | 3 | 社会保障 |
| JGJJ | 4 | 机构经济 |
| JYJ | 5 | 教育 |
| GSGL | 6 | 公司管理 |
| JTGL | 7 | 交通管理 |
| YWJY | 8 | 医疗卫生 |
| LYJ | 9 | 林业 |
| FJGL | 10 | 房价管理 |

---

## 验证方法

### 测试步骤

1. 输入"分类 10"或任意工单号
2. 查看分类预览结果，确认 `categoryId` 不再是 0
3. 点击"确认分类"按钮
4. 检查数据库：
   ```sql
   SELECT order_id, category_id FROM biz_work_order WHERE order_id = 10;
   ```
   应该看到 `category_id = 6`（公司管理）

5. 查看未分类工单列表，确认工单10不再显示

---

## 预期结果

修复后，分类流程应该正常工作：

1. ✅ AI返回 `"categoryId": "GSGL"`
2. ✅ 代码查找 `GSGL` 对应的分类ID `6`
3. ✅ 分类预览显示 `categoryId=6, categoryName=公司管理`
4. ✅ 前端确认时传递 `categoryId=6`
5. ✅ 数据库更新 `category_id=6`
6. ✅ 工单从"未分类列表"中消失