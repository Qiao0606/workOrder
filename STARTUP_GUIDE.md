# 应用启动指南

## 问题解决

### ✅ Lombok兼容性问题已修复

**问题**：Lombok 1.18.30 与 JDK 17 不兼容
```
java.lang.ExceptionInInitializerError: com.sun.tools.javac.code.TypeTag :: UNKNOWN
```

**解决方案**：升级到 Lombok 1.18.32
```xml
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <version>1.18.32</version>
</dependency>
```

**验证**：✅ 编译成功

---

## 启动应用

### 方式一：使用Maven（开发环境）

```bash
# 启动应用
mvn spring-boot:run
```

### 方式二：运行JAR包（生产环境）

```bash
# 运行打包好的JAR
java -jar target/SpringDemo-0.0.1-SNAPSHOT.jar
```

### 方式三：后台运行

```bash
# 后台运行
nohup java -jar target/SpringDemo-0.0.1-SNAPSHOT.jar > app.log 2>&1 &

# 查看日志
tail -f app.log

# 停止应用
ps aux | grep SpringDemo
kill -9 <PID>
```

---

## 访问应用

启动成功后，访问以下地址：

### 前端页面
- 主页：http://localhost:8080/index.html
- 分类管理：http://localhost:8080/classification.html

### API接口
- AI工单接口：http://localhost:8080/ai/ticket
- 分类接口：http://localhost:8080/api/classify/...

### H2控制台（如果使用H2）
- URL：http://localhost:8080/h2-console
- JDBC URL：jdbc:h2:mem:hotline
- 用户名：sa
- 密码：（空）

---

## 完整测试流程

### 1. 启动应用

```bash
cd /Users/sky/Downloads/ZIP/SpringDemo
mvn spring-boot:run
```

**预期输出**：
```
Started SpringDemoApplication in X.XXX seconds
```

### 2. 清除浏览器缓存

**重要**：由于前端代码有更新，需要清除浏览器缓存

**方法1：强制刷新**
- Windows: `Ctrl + F5`
- Mac: `Cmd + Shift + R`

**方法2：无痕模式**
- Chrome: `Ctrl + Shift + N` (Windows) / `Cmd + Shift + N` (Mac)

### 3. 访问前端

打开浏览器访问：
```
http://localhost:8080/index.html
```

### 4. 测试分类功能

**步骤1：输入工单号分类**
```
在对话框输入：分类 10
```

**步骤2：查看分类预览**
```
预期输出：
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

**步骤3：点击确认分类**
```
预期输出：
✅ 分类确认成功

工单号：10
已分类为：分类ID 1
```

### 5. 验证数据库

**使用MySQL**：
```sql
SELECT order_id, category_id, ai_candidate_category, ai_match_evidence 
FROM biz_work_order 
WHERE order_id = 10;
```

**使用H2**：
访问 http://localhost:8080/h2-console
```sql
SELECT * FROM biz_work_order WHERE order_id = 10;
```

---

## 常见启动问题

### 问题1：端口被占用

**错误信息**：
```
Web server failed to start. Port 8080 was already in use.
```

**解决方案**：
```bash
# 查找占用端口的进程
lsof -i:8080

# 终止进程
kill -9 <PID>

# 或者使用其他端口
java -jar target/SpringDemo-0.0.1-SNAPSHOT.jar --server.port=8081
```

### 问题2：数据库连接失败

**错误信息**：
```
Communications link failure
```

**解决方案**：
```bash
# 启动MySQL
brew services start mysql

# 或者使用H2内存数据库
mvn spring-boot:run -Dspring-boot.run.profiles=h2
```

### 问题3：前端没有更新

**现象**：前端显示旧版本，没有确认按钮

**解决方案**：
1. 强制刷新浏览器：`Ctrl + Shift + R` / `Cmd + Shift + R`
2. 清除浏览器缓存
3. 使用无痕模式
4. 检查 target/classes/static/js/chat-api.js 是否更新

### 问题4：编译错误

**解决方案**：
```bash
# 清理并重新编译
mvn clean compile -DskipTests

# 如果还有问题，删除本地仓库缓存
rm -rf ~/.m2/repository/org/projectlombok
mvn clean compile -DskipTests
```

---

## 日志级别调整

如果需要查看详细日志，修改 `application.properties`：

```properties
# 日志级别
logging.level.com.example.springdemo=DEBUG
logging.level.org.springframework.web=DEBUG

# 输出到文件
logging.file.name=logs/application.log
logging.file.max-size=10MB
logging.file.max-history=30
```

---

## 性能调优

### JVM参数

```bash
# 设置堆内存
java -Xms512m -Xmx2g -jar target/SpringDemo-0.0.1-SNAPSHOT.jar

# 设置GC参数
java -XX:+UseG1GC -Xmx2g -jar target/SpringDemo-0.0.1-SNAPSHOT.jar
```

### 连接池配置

修改 `application.properties`：

```properties
# HikariCP连接池
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=30000
```

---

## 监控端点

Spring Boot Actuator端点：

```bash
# 健康检查
curl http://localhost:8080/actuator/health

# 应用信息
curl http://localhost:8080/actuator/info

# 环境变量
curl http://localhost:8080/actuator/env
```

---

## 开发模式

### 热部署

```bash
# 使用Spring Boot DevTools
mvn spring-boot:run -Dspring-boot.run.fork=false
```

添加依赖：
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-devtools</artifactId>
    <optional>true</optional>
</dependency>
```

### 远程调试

```bash
# 启用远程调试
java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005 \
     -jar target/SpringDemo-0.0.1-SNAPSHOT.jar
```

然后在IDE中配置远程调试，连接到 `localhost:5005`。

---

## 下一步

1. ✅ 应用已成功编译打包
2. ✅ 启动应用：`mvn spring-boot:run`
3. ✅ 清除浏览器缓存
4. ✅ 测试分类功能：输入"分类 10"

---

**编译时间**：2026-04-27  
**Lombok版本**：1.18.32  
**JDK版本**：17  
**状态**：✅ 编译成功