# 数据库表结构对比分析报告

## 对比时间
2026-04-27

## 对比范围
- 项目代码中的数据库表结构
- 用户提供的实际数据库表结构

---

## 对比结果总结

### ✅ 完全匹配的表

| 表名 | Model类 | Mapper XML | 状态 |
|------|---------|------------|------|
| biz_work_order | BizWorkOrder.java | BizWorkOrderMapper.xml | ✅ 匹配 |
| sys_category | SysCategory.java | SysCategoryMapper.xml | ✅ 匹配 |
| rule_keyword | RuleKeyword.java | RuleKeywordMapper.xml | ✅ 匹配 |

---

### ⚠️ 项目未使用的表

| 表名 | 说明 | 影响 |
|------|------|------|
| base_department | 部门表 | ❌ 项目代码中无引用 |
| ht_work_order | 历史工单表 | ❌ 项目代码中无引用 |

---

## 详细对比

### 1. biz_work_order（工单表）

#### 实际数据库结构
```sql
CREATE TABLE `biz_work_order` (
  `order_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `order_code` VARCHAR(32) NOT NULL,
  `category_id` INT UNSIGNED NOT NULL,
  `accept_time` DATETIME DEFAULT NULL,
  `accept_user` VARCHAR(32) DEFAULT '',
  `user_phone` VARCHAR(20) DEFAULT '',
  `user_region` VARCHAR(64) DEFAULT '',
  `user_address` VARCHAR(200) DEFAULT '',
  `order_content` TEXT,
  `ai_candidate_category` VARCHAR(100) DEFAULT '',
  `ai_match_evidence` TEXT,
  `status` TINYINT NOT NULL DEFAULT 1,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`order_id`),
  UNIQUE KEY `uk_order_code` (`order_code`),
  KEY `idx_accept_time` (`accept_time`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_status` (`status`),
  KEY `idx_user_region` (`user_region`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

#### 项目代码中的定义
**Model类**: `BizWorkOrder.java`
```java
@Data
public class BizWorkOrder {
    private Long orderId;              // ✅ order_id
    private String orderCode;          // ✅ order_code
    private Integer categoryId;        // ✅ category_id
    private LocalDateTime acceptTime;  // ✅ accept_time
    private String acceptUser;         // ✅ accept_user
    private String userPhone;          // ✅ user_phone
    private String userRegion;         // ✅ user_region
    private String userAddress;        // ✅ user_address
    private String orderContent;       // ✅ order_content
    private String aiCandidateCategory;// ✅ ai_candidate_category
    private String aiMatchEvidence;    // ✅ ai_match_evidence
    private Integer status;            // ✅ status
    private LocalDateTime createTime;  // ✅ create_time
    private LocalDateTime updateTime;  // ✅ update_time
}
```

**Mapper XML**: `BizWorkOrderMapper.xml`
- ✅ 所有字段映射正确
- ✅ SQL语句中的字段名全部正确
- ✅ 索引使用正确

#### 字段对比

| 数据库字段 | Model属性 | 类型匹配 | 状态 |
|-----------|----------|---------|------|
| order_id | orderId | BIGINT ↔ Long | ✅ |
| order_code | orderCode | VARCHAR(32) ↔ String | ✅ |
| category_id | categoryId | INT ↔ Integer | ✅ |
| accept_time | acceptTime | DATETIME ↔ LocalDateTime | ✅ |
| accept_user | acceptUser | VARCHAR(32) ↔ String | ✅ |
| user_phone | userPhone | VARCHAR(20) ↔ String | ✅ |
| user_region | userRegion | VARCHAR(64) ↔ String | ✅ |
| user_address | userAddress | VARCHAR(200) ↔ String | ✅ |
| order_content | orderContent | TEXT ↔ String | ✅ |
| ai_candidate_category | aiCandidateCategory | VARCHAR(100) ↔ String | ✅ |
| ai_match_evidence | aiMatchEvidence | TEXT ↔ String | ✅ |
| status | status | TINYINT ↔ Integer | ✅ |
| create_time | createTime | DATETIME ↔ LocalDateTime | ✅ |
| update_time | updateTime | DATETIME ↔ LocalDateTime | ✅ |

**结论**: ✅ **完全匹配**

---

### 2. sys_category（分类表）

#### 实际数据库结构
```sql
CREATE TABLE `sys_category` (
  `category_id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `parent_id` INT UNSIGNED NOT NULL DEFAULT 0,
  `category_name` VARCHAR(50) NOT NULL,
  `category_code` VARCHAR(20) NOT NULL,
  `sort` TINYINT NOT NULL DEFAULT 10,
  `status` TINYINT NOT NULL DEFAULT 1,
  `remark` VARCHAR(200) DEFAULT '',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`category_id`),
  UNIQUE KEY `uk_category_code` (`category_code`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

#### 项目代码中的定义
**Model类**: `SysCategory.java`
```java
@Data
public class SysCategory {
    private Integer categoryId;        // ✅ category_id
    private Integer parentId;          // ✅ parent_id
    private String categoryName;       // ✅ category_name
    private String categoryCode;       // ✅ category_code
    private Integer sort;              // ✅ sort
    private Integer status;            // ✅ status
    private String remark;             // ✅ remark
    private LocalDateTime createTime;  // ✅ create_time
    private LocalDateTime updateTime;  // ✅ update_time
}
```

#### 字段对比

| 数据库字段 | Model属性 | 类型匹配 | 状态 |
|-----------|----------|---------|------|
| category_id | categoryId | INT ↔ Integer | ✅ |
| parent_id | parentId | INT ↔ Integer | ✅ |
| category_name | categoryName | VARCHAR(50) ↔ String | ✅ |
| category_code | categoryCode | VARCHAR(20) ↔ String | ✅ |
| sort | sort | TINYINT ↔ Integer | ✅ |
| status | status | TINYINT ↔ Integer | ✅ |
| remark | remark | VARCHAR(200) ↔ String | ✅ |
| create_time | createTime | DATETIME ↔ LocalDateTime | ✅ |
| update_time | updateTime | DATETIME ↔ LocalDateTime | ✅ |

**结论**: ✅ **完全匹配**

---

### 3. rule_keyword（关键词规则表）

#### 实际数据库结构
```sql
CREATE TABLE `rule_keyword` (
  `rule_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `category_id` INT UNSIGNED NOT NULL,
  `keyword` VARCHAR(64) NOT NULL,
  `synonym` VARCHAR(500) DEFAULT '',
  `match_type` TINYINT NOT NULL DEFAULT 1,
  `weight` FLOAT NOT NULL DEFAULT 1,
  `hit_count` INT UNSIGNED NOT NULL DEFAULT 0,
  `status` TINYINT NOT NULL DEFAULT 1,
  `effective_time` DATETIME DEFAULT NULL,
  `expire_time` DATETIME DEFAULT NULL,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`rule_id`),
  KEY `idx_category_status` (`category_id`, `status`),
  KEY `idx_effective_time` (`effective_time`),
  KEY `idx_keyword` (`keyword`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

#### 项目代码中的定义
**Model类**: `RuleKeyword.java`
```java
@Data
public class RuleKeyword {
    private Long ruleId;               // ✅ rule_id
    private Integer categoryId;        // ✅ category_id
    private String keyword;            // ✅ keyword
    private String synonym;            // ✅ synonym
    private Integer matchType;         // ✅ match_type
    private Float weight;              // ✅ weight
    private Integer hitCount;          // ✅ hit_count
    private Integer status;            // ✅ status
    private LocalDateTime effectiveTime;// ✅ effective_time
    private LocalDateTime expireTime;   // ✅ expire_time
    private LocalDateTime createTime;   // ✅ create_time
    private LocalDateTime updateTime;   // ✅ update_time
}
```

#### 字段对比

| 数据库字段 | Model属性 | 类型匹配 | 状态 |
|-----------|----------|---------|------|
| rule_id | ruleId | BIGINT ↔ Long | ✅ |
| category_id | categoryId | INT ↔ Integer | ✅ |
| keyword | keyword | VARCHAR(64) ↔ String | ✅ |
| synonym | synonym | VARCHAR(500) ↔ String | ✅ |
| match_type | matchType | TINYINT ↔ Integer | ✅ |
| weight | weight | FLOAT ↔ Float | ✅ |
| hit_count | hitCount | INT ↔ Integer | ✅ |
| status | status | TINYINT ↔ Integer | ✅ |
| effective_time | effectiveTime | DATETIME ↔ LocalDateTime | ✅ |
| expire_time | expireTime | DATETIME ↔ LocalDateTime | ✅ |
| create_time | createTime | DATETIME ↔ LocalDateTime | ✅ |
| update_time | updateTime | DATETIME ↔ LocalDateTime | ✅ |

**结论**: ✅ **完全匹配**

---

### 4. base_department（部门表）

#### 实际数据库结构
```sql
CREATE TABLE `base_department` (
  `dept_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `dept_name` VARCHAR(64) NOT NULL,
  `dept_code` VARCHAR(20) NOT NULL,
  `parent_dept_id` INT UNSIGNED NOT NULL DEFAULT 0,
  `responsibility` VARCHAR(500) DEFAULT '',
  `contact_user` VARCHAR(32) DEFAULT '',
  `contact_phone` VARCHAR(20) DEFAULT '',
  `status` TINYINT NOT NULL DEFAULT 1,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`dept_id`),
  UNIQUE KEY `uk_dept_code` (`dept_code`),
  KEY `idx_dept_name` (`dept_name`),
  KEY `idx_parent_dept_id` (`parent_dept_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

#### 项目代码中的定义
- ❌ 无对应Model类
- ❌ 无对应Mapper
- ❌ 代码中无任何引用

**结论**: ❌ **项目未使用此表**

---

### 5. ht_work_order（历史工单表）

#### 实际数据库结构
```sql
CREATE TABLE `ht_work_order` (
  `order_id` BIGINT UNSIGNED NOT NULL,
  `order_code` VARCHAR(32) NOT NULL,
  `category_id` INT UNSIGNED NOT NULL,
  `accept_time` DATETIME DEFAULT NULL,
  `accept_user` VARCHAR(32) DEFAULT '',
  `user_phone` VARCHAR(20) DEFAULT '',
  `user_address` VARCHAR(64) DEFAULT '',
  `order_content` TEXT,
  `ai_candidate_category` VARCHAR(100) DEFAULT '',
  `ai_match_evidence` TEXT,
  `status` TINYINT NOT NULL DEFAULT 1,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`order_id`),
  UNIQUE KEY `uk_order_code` (`order_code`),
  KEY `idx_accept_time` (`accept_time`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

#### 项目代码中的定义
- ❌ 无对应Model类
- ❌ 无对应Mapper
- ❌ 代码中无任何引用

**结论**: ❌ **项目未使用此表**

---

## SQL语句检查

### BizWorkOrderMapper.xml

#### 查询语句检查
```xml
<select id="selectById">
    SELECT order_id, order_code, category_id, accept_time, accept_user,
           user_phone, user_region, user_address, order_content,
           ai_candidate_category, ai_match_evidence, status,
           create_time, update_time
    FROM biz_work_order
    WHERE order_id = #{orderId}
</select>
```
**检查结果**: ✅ 所有字段名与数据库一致

#### 更新语句检查
```xml
<update id="updateCategory">
    UPDATE biz_work_order
    SET category_id = #{categoryId},
        ai_candidate_category = #{categoryName},
        ai_match_evidence = #{matchEvidence},
        update_time = NOW()
    WHERE order_id = #{orderId}
</update>
```
**检查结果**: ✅ 所有字段名与数据库一致

---

### SysCategoryMapper.xml

#### 查询语句检查
```xml
<select id="selectAll">
    SELECT category_id, parent_id, category_name, category_code,
           sort, status, remark, create_time, update_time
    FROM sys_category
    WHERE status = 1
    ORDER BY sort ASC, category_id ASC
</select>
```
**检查结果**: ✅ 所有字段名与数据库一致

---

### RuleKeywordMapper.xml

#### 查询语句检查
```xml
<select id="selectAllActive">
    SELECT rule_id, category_id, keyword, synonym, match_type,
           weight, hit_count, status, effective_time, expire_time,
           create_time, update_time
    FROM rule_keyword
    WHERE status = 1
      AND (effective_time IS NULL OR effective_time <= NOW())
      AND (expire_time IS NULL OR expire_time >= NOW())
    ORDER BY category_id ASC, weight DESC
</select>
```
**检查结果**: ✅ 所有字段名与数据库一致

---

## 发现的问题

### ✅ 无字段引用错误

经过全面检查，项目代码中的所有字段引用都与实际数据库结构完全匹配，没有发现任何错误。

### ⚠️ 未使用的表

项目中有两个表在实际数据库中存在，但项目代码中未使用：

1. **base_department** - 部门表
   - 用途：可能用于工单派发、部门管理
   - 建议：如果需要部门功能，可以后续添加

2. **ht_work_order** - 历史工单表
   - 用途：可能用于存储已归档的历史工单
   - 建议：如果需要工单归档功能，可以后续添加

---

## 索引对比

### biz_work_order

| 索引名 | 数据库 | 项目代码 | 状态 |
|--------|--------|---------|------|
| PRIMARY | order_id | ✅ 使用 | ✅ |
| uk_order_code | order_code | ✅ 使用 | ✅ |
| idx_accept_time | accept_time | ✅ 使用 | ✅ |
| idx_category_id | category_id | ✅ 使用 | ✅ |
| idx_status | status | ✅ 使用 | ✅ |
| idx_user_region | user_region | ❌ 未使用 | ⚠️ |

**建议**: `idx_user_region` 索引已创建但项目代码中未使用此字段进行查询，建议保留以备将来使用。

---

## 数据类型对比

### TINYINT ↔ Integer 映射

数据库中的 `TINYINT` 类型在 Java Model 中映射为 `Integer`，这是常见做法：
- ✅ 完全兼容
- ✅ 无数据精度损失
- ✅ 无转换问题

### FLOAT ↔ Float 映射

数据库中的 `FLOAT` 类型在 Java Model 中映射为 `Float`：
- ✅ 完全匹配
- ✅ 无精度损失

---

## 项目初始化脚本对比

### 项目中的 init.sql
- ✅ 创建数据库和表结构
- ✅ 插入示例数据
- ⚠️ 缺少部分索引（如 idx_user_region）

### 建议

更新 `database/init.sql` 以包含所有索引：
```sql
-- 在 biz_work_order 表中添加缺失的索引
ALTER TABLE biz_work_order ADD INDEX idx_user_region (user_region);
```

---

## 总结

### ✅ 匹配情况

1. **Model实体类**: 所有字段与数据库完全匹配
2. **Mapper XML**: 所有SQL语句字段名正确
3. **数据类型**: 类型映射正确，无兼容性问题
4. **字段引用**: 无错误引用

### ⚠️ 注意事项

1. **base_department**: 项目未使用，数据库中存在
2. **ht_work_order**: 项目未使用，数据库中存在
3. **idx_user_region**: 索引存在但项目未使用

### 💡 建议

1. 如果需要部门管理功能，可以后续添加 `BaseDepartment` 实体类和Mapper
2. 如果需要历史工单归档功能，可以后续添加 `HtWorkOrder` 实体类和Mapper
3. 更新 `database/init.sql` 以包含所有索引定义

---

**检查人**: AI Code Assistant  
**检查日期**: 2026-04-27  
**检查结果**: ✅ 无字段引用错误