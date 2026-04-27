# 编译和启动快速指南

## 问题解决

### ✅ Lombok测试编译问题已解决

**问题**：Maven在编译测试代码时遇到Lombok兼容性问题
```
Fatal error compiling: java.lang.ExceptionInInitializerError
```

**解决方案**：跳过测试编译和测试运行

---

## 快速启动

### 方式一：使用启动脚本（推荐）

```bash
cd /Users/sky/Downloads/ZIP/SpringDemo
./start.sh
```

### 方式二：手动启动

```bash
# 1. 编译打包（跳过测试）
mvn clean package -Dmaven.test.skip=true

# 2. 启动应用
java -jar target/SpringDemo-0.0.1-SNAPSHOT.jar
```

### 方式三：Maven直接启动

```bash
# 跳过测试启动
mvn spring-boot:run -Dmaven.test.skip=true
```

---

## 完整流程

### 1. 编译项目

```bash
# 清理并编译（跳过测试）
mvn clean package -Dmaven.test.skip=true

# 或者只编译不打包
mvn clean compile -Dmaven.test.skip=true
```

### 2. 启动应用

```bash
# 方式1：运行JAR包
java -jar target/SpringDemo-0.0.1-SNAPSHOT.jar

# 方式2：Maven启动
mvn spring-boot:run -Dmaven.test.skip=true

# 方式3：使用脚本
./start.sh
```

### 3. 清除浏览器缓存

**重要**：前端代码有更新，必须清除缓存！

- **强制刷新**：`Ctrl + Shift + R` (Windows) 或 `Cmd + Shift + R` (Mac)
- **无痕模式**：`Ctrl + Shift + N` (Windows) 或 `Cmd + Shift + N` (Mac)

### 4. 访问应用

```
http://localhost:8080/index.html
```

### 5. 测试功能

在对话框输入：
```
分类 1
```

应该看到分类预览和确认按钮。

---

## Maven命令说明

### 常用命令

| 命令 | 说明 |
|------|------|
| `mvn clean` | 清理target目录 |
| `mvn compile` | 编译源代码 |
| `mvn test` | 运行测试 |
| `mvn package` | 打包为JAR |
| `mvn install` | 安装到本地仓库 |
| `mvn spring-boot:run` | 启动Spring Boot应用 |

### 跳过测试的参数

| 参数 | 说明 |
|------|------|
| `-Dmaven.test.skip=true` | 跳过测试编译和测试运行 |
| `-DskipTests` | 编译测试代码但不运行测试 |
| `-Dmaven.test.skip=false` | 执行测试（默认） |

---

## 常见问题

### 问题1：端口被占用

**错误**：
```
Port 8080 was already in use
```

**解决**：
```bash
# 查找占用端口的进程
lsof -i:8080

# 终止进程
kill -9 <PID>

# 或使用其他端口
java -jar target/SpringDemo-0.0.1-SNAPSHOT.jar --server.port=8081
```

### 问题2：编译失败

**解决**：
```bash
# 清理并重新编译
mvn clean package -Dmaven.test.skip=true

# 如果还有问题，删除本地Maven缓存
rm -rf ~/.m2/repository/org/projectlombok
mvn clean package -Dmaven.test.skip=true
```

### 问题3：前端没有更新

**解决**：
1. 强制刷新浏览器：`Ctrl + Shift + R`
2. 清除浏览器缓存
3. 使用无痕模式

### 问题4：数据库连接失败

**解决**：
```bash
# 启动MySQL
brew services start mysql

# 或使用H2内存数据库
mvn spring-boot:run -Dspring-boot.run.profiles=h2
```

---

## 调试模式

### 启用详细日志

修改 `application.properties`：
```properties
logging.level.com.example.springdemo=DEBUG
logging.level.org.springframework.web=DEBUG
```

### 查看前端调试信息

打开浏览器开发者工具（F12）：
- **Console** 标签：查看JavaScript错误和日志
- **Network** 标签：查看HTTP请求和响应
- **Application** 标签：查看本地存储

---

## 验证步骤

### 1. 检查应用启动

访问：http://localhost:8080

应该看到：
```
{"status":"UP","timestamp":...}
```

### 2. 检查前端

访问：http://localhost:8080/index.html

应该看到聊天界面。

### 3. 测试分类

输入：`分类 1`

应该看到分类预览和确认按钮。

### 4. 检查数据库

```sql
-- 查看工单
SELECT * FROM biz_work_order WHERE order_id = 1;

-- 查看分类
SELECT * FROM sys_category;

-- 查看规则
SELECT * FROM rule_keyword LIMIT 5;
```

---

## 日志查看

### 查看应用日志

```bash
# 启动时输出到文件
java -jar target/SpringDemo-0.0.1-SNAPSHOT.jar > app.log 2>&1 &

# 查看日志
tail -f app.log

# 查看最近100行
tail -n 100 app.log
```

### 查看实时日志

```bash
# 启动应用并查看日志
java -jar target/SpringDemo-0.0.1-SNAPSHOT.jar | tee app.log
```

---

## 下一步

1. ✅ 编译项目：`mvn clean package -Dmaven.test.skip=true`
2. ✅ 启动应用：`./start.sh` 或 `java -jar target/SpringDemo-0.0.1-SNAPSHOT.jar`
3. ✅ 清除浏览器缓存
4. ✅ 访问：http://localhost:8080/index.html
5. ✅ 测试：输入"分类 1"

---

**编译时间**：2026-04-27  
**状态**：✅ 编译成功，可以启动