# SpringDemo - 智能工单分类系统

## 项目简介

这是一个基于 Spring Boot 的智能工单分类系统，结合了关键词规则匹配和AI智能分类两种方式来对工单进行自动分类。

## 技术栈

- Spring Boot 2.7.18
- MyBatis 2.3.0
- MySQL 8.0
- 讯飞星火大模型API
- Lombok 1.18.30

## 环境要求

- JDK 17+
- Maven 3.6+
- MySQL 8.0+

## 数据库配置

### 1. 安装并启动MySQL

**macOS:**
```bash
brew install mysql
brew services start mysql
```

**Linux:**
```bash
sudo apt-get install mysql-server
sudo systemctl start mysql
```

**Windows:**
下载并安装 MySQL Installer: https://dev.mysql.com/downloads/installer/

### 2. 初始化数据库

```bash
# 登录MySQL
mysql -u root -p

# 执行初始化脚本
source database/init.sql
```

或者直接在MySQL命令行中执行：
```sql
CREATE DATABASE IF NOT EXISTS hotline DEFAULT CHARACTER SET utf8mb4;
```

### 3. 修改数据库配置

编辑 `src/main/resources/application.properties`：

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/hotline?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false
spring.datasource.username=root
spring.datasource.password=你的密码
```

## 快速启动

### 1. 编译项目
```bash
mvn clean package -DskipTests
```

### 2. 运行项目
```bash
java -jar target/SpringDemo-0.0.1-SNAPSHOT.jar
```

或者使用Maven运行：
```bash
mvn spring-boot:run
```

### 3. 访问接口

项目启动后，访问：http://localhost:8080

## 主要接口

### 分类接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/classify/preview` | POST | 预览工单分类（不保存） |
| `/api/classify/confirm` | POST | 确认分类结果并保存 |
| `/api/classify/classify/{id}` | GET | 对指定工单进行分类 |
| `/api/classify/batch` | POST | 批量分类工单 |
| `/api/classify/unclassified` | GET | 获取未分类工单列表 |
| `/api/classify/categories` | GET | 获取所有分类列表 |
| `/api/classify/stats/data` | GET | 获取统计数据 |
| `/api/classify/reload` | POST | 重新加载分类规则 |

### 示例请求

**预览分类:**
```bash
curl -X POST http://localhost:8080/api/classify/preview \
  -H "Content-Type: application/json" \
  -d '{"orderContent": "市民反映XX道路路灯不亮，影响夜间出行安全"}'
```

**确认分类:**
```bash
curl -X POST http://localhost:8080/api/classify/confirm \
  -H "Content-Type: application/json" \
  -d '{"orderId": 1, "categoryId": 1}'
```

## 数据库表结构

- **sys_category** - 工单分类表
- **rule_keyword** - 关键词规则表
- **biz_work_order** - 工单表

## 无数据库启动

如果暂时没有MySQL数据库，项目会以降级模式启动，数据库相关功能将不可用。启动后会输出警告日志，但不影响项目启动。

## 常见问题

### 1. 数据库连接失败

错误：`Communications link failure`

解决：
- 确认MySQL服务已启动
- 检查数据库连接配置是否正确
- 确认数据库 `hotline` 已创建

### 2. Lombok编译错误

错误：`Can't initialize javac processor`

解决：
- 使用JDK 17编译
- Maven命令：`mvn clean compile -DskipTests`

## 项目结构

```
SpringDemo/
├── database/
│   └── init.sql              # 数据库初始化脚本
├── src/main/java/
│   └── com/example/springdemo/
│       ├── aspect/           # AOP切面
│       ├── client/           # 外部API客户端
│       ├── config/           # 配置类
│       ├── controller/       # 控制器
│       ├── dto/              # 数据传输对象
│       ├── exception/        # 异常处理
│       ├── mapper/           # MyBatis Mapper
│       ├── model/            # 实体类
│       ├── service/          # 服务层
│       └── SpringDemoApplication.java
└── src/main/resources/
    ├── mapper/               # MyBatis XML
    └── application.properties # 配置文件
```