# MySQL 数据库初始化指南

## 快速开始

### 方式一：使用自动化脚本（推荐）

```bash
# 进入项目目录
cd /Users/sky/Downloads/ZIP/SpringDemo/database

# 执行初始化脚本
./init-db.sh -p yourpassword

# 或者指定完整参数
./init-db.sh -h localhost -P 3306 -u root -p yourpassword -d hotline
```

### 方式二：手动执行SQL

```bash
# 登录MySQL
mysql -u root -p

# 执行SQL脚本
source /Users/sky/Downloads/ZIP/SpringDemo/database/init-mysql.sql

# 或者直接执行
mysql -u root -p < /Users/sky/Downloads/ZIP/SpringDemo/database/init-mysql.sql
```

---

## 脚本参数说明

### init-db.sh 参数

| 参数 | 简写 | 说明 | 默认值 |
|------|------|------|--------|
| --host | -h | MySQL主机地址 | localhost |
| --port | -P | MySQL端口 | 3306 |
| --user | -u | MySQL用户名 | root |
| --password | -p | MySQL密码（必填） | - |
| --database | -d | 数据库名 | hotline |
| --help | - | 显示帮助信息 | - |

---

## 数据库结构

### 数据库名
- **hotline** (热线工单智能分类系统)

### 数据表

| 表名 | 说明 | 初始记录数 |
|------|------|-----------|
| sys_category | 分类表 | 10 |
| rule_keyword | 关键词规则表 | 20 |
| biz_work_order | 工单表 | 10 |
| ht_work_order | 历史工单表 | 0 |
| base_department | 部门表 | 5 |

### 示例数据

#### 分类数据
- 城市管理 (CSGL)
- 社会环境 (SHHJ)
- 社会保障 (SBJ)
- 机构经济 (JGJJ)
- 教育 (JYJ)
- 公司管理 (GSGL)
- 交通管理 (JTGL)
- 医疗卫生 (YWJY)
- 林业 (LYJ)
- 房价管理 (FJGL)

#### 关键词规则数据
- 城市管理类：道路、路灯、垃圾、下水道、井盖
- 社会环境类：噪音、污染、扬尘、油烟
- 社会保障类：社保、公积金、医保
- 机构经济类：税务、工商
- 教育类：学校、学区、补课
- 交通管理类：交通、信号灯、违章、停车

#### 工单数据
- 10个示例工单（未分类状态）
- 包含道路、垃圾、社保、学校、交通等各类问题

---

## 验证安装

### 1. 检查数据库

```bash
mysql -u root -p -e "USE hotline; SHOW TABLES;"
```

### 2. 检查表记录

```bash
mysql -u root -p -e "USE hotline; 
SELECT 'sys_category' AS table_name, COUNT(*) AS count FROM sys_category
UNION ALL
SELECT 'rule_keyword', COUNT(*) FROM rule_keyword
UNION ALL
SELECT 'biz_work_order', COUNT(*) FROM biz_work_order
UNION ALL
SELECT 'base_department', COUNT(*) FROM base_department;"
```

### 3. 查看分类数据

```bash
mysql -u root -p -e "USE hotline; SELECT * FROM sys_category;"
```

---

## 应用配置

### 更新 application.properties

初始化完成后，更新项目配置文件：

```properties
# MySQL 数据源配置
spring.datasource.url=jdbc:mysql://localhost:3306/hotline?useUnicode=true&characterEncoding=utf8mb4&useSSL=false&serverTimezone=Asia/Shanghai
spring.datasource.username=root
spring.datasource.password=yourpassword
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# MyBatis 配置
mybatis.mapper-locations=classpath:mapper/*.xml
mybatis.type-aliases-package=com.example.springdemo.model
```

---

## 常见问题

### 1. 连接被拒绝

**错误**: `Access denied for user 'root'@'localhost'`

**解决**: 检查用户名和密码是否正确

### 2. 数据库已存在

**错误**: `Database 'hotline' already exists`

**解决**: 脚本使用了 `CREATE DATABASE IF NOT EXISTS`，不会报错，会继续执行

### 3. 表已存在

**错误**: `Table 'sys_category' already exists`

**解决**: 脚本使用了 `DROP TABLE IF EXISTS`，会先删除再创建

### 4. 字符集问题

**错误**: 中文乱码

**解决**: 确保使用 utf8mb4 字符集，脚本已默认设置

---

## 重置数据库

如果需要重新初始化数据库：

```bash
# 方式一：使用脚本
./init-db.sh -p yourpassword

# 方式二：手动执行
mysql -u root -p -e "DROP DATABASE IF EXISTS hotline;"
mysql -u root -p < /Users/sky/Downloads/ZIP/SpringDemo/database/init-mysql.sql
```

---

## 备份数据库

```bash
# 备份数据库
mysqldump -u root -p hotline > hotline_backup_$(date +%Y%m%d).sql

# 恢复数据库
mysql -u root -p hotline < hotline_backup_20260427.sql
```

---

## 文件说明

| 文件 | 说明 |
|------|------|
| init-mysql.sql | MySQL数据库初始化SQL脚本 |
| init-db.sh | 自动化初始化Shell脚本 |
| README.md | 本说明文档 |

---

## 技术支持

如有问题，请检查：
1. MySQL服务是否启动
2. 用户名密码是否正确
3. SQL文件路径是否正确
4. 字符集设置是否正确