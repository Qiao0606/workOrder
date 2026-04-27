# 快速命令参考

## ⚠️ 重要提示

**所有Maven命令都必须加上** `-Dmaven.test.skip=true` **参数！**

因为测试代码编译时会有Lombok兼容性问题，必须跳过测试编译。

---

## 🚀 常用命令

### 编译打包

```bash
# ✅ 正确：跳过测试编译
mvn clean package -Dmaven.test.skip=true

# ❌ 错误：会编译测试代码，导致失败
mvn clean package
```

### 启动应用

```bash
# 方式1：使用启动脚本（推荐）
./start.sh

# 方式2：运行JAR包
java -jar target/SpringDemo-0.0.1-SNAPSHOT.jar

# 方式3：Maven启动
mvn spring-boot:run -Dmaven.test.skip=true
```

### 仅编译

```bash
# ✅ 正确
mvn clean compile -Dmaven.test.skip=true

# ❌ 错误
mvn clean compile
```

---

## 📋 完整测试流程

### 1. 编译打包

```bash
mvn clean package -Dmaven.test.skip=true
```

**预期输出**：
```
[INFO] BUILD SUCCESS
[INFO] Total time: 1.942 s
```

### 2. 启动应用

```bash
./start.sh
```

或

```bash
java -jar target/SpringDemo-0.0.1-SNAPSHOT.jar
```

**预期输出**：
```
Started SpringDemoApplication in X.XXX seconds
```

### 3. 清除浏览器缓存

**重要**：前端代码更新后必须清除缓存！

- **强制刷新**：`Ctrl + Shift + R` (Windows) 或 `Cmd + Shift + R` (Mac)
- **无痕模式**：`Ctrl + Shift + N` (Windows) 或 `Cmd + Shift + N` (Mac)

### 4. 访问应用

```
http://localhost:8080/index.html
```

### 5. 测试功能

```
输入：分类 1

预期输出：
🏷️ 分类预览结果

━━━━━━━━━━━━━━━
📋 工单号：1
📝 工单内容：...
━━━━━━━━━━━━━━━

✅ 推荐分类：城市管理(CSGL)
📊 分类ID：1
📈 置信度：92.0%

[✓ 确认分类]  [✗ 跳过分类]
```

---

## 🔧 故障排查

### 问题1：编译失败 - Lombok错误

**错误信息**：
```
Fatal error compiling: java.lang.ExceptionInInitializerError
com.sun.tools.javac.code.TypeTag :: UNKNOWN
```

**原因**：编译了测试代码

**解决**：使用 `-Dmaven.test.skip=true`
```bash
mvn clean package -Dmaven.test.skip=true
```

### 问题2：端口被占用

**错误信息**：
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

### 问题3：前端没有更新

**原因**：浏览器缓存

**解决**：
1. 强制刷新：`Ctrl + Shift + R`
2. 清除浏览器缓存
3. 使用无痕模式

### 问题4：数据库连接失败

**错误信息**：
```
Communications link failure
```

**解决**：
```bash
# 启动MySQL
brew services start mysql

# 或使用H2内存数据库
mvn spring-boot:run -Dspring-boot.run.profiles=h2 -Dmaven.test.skip=true
```

---

## 📝 命令速查表

| 命令 | 说明 |
|------|------|
| `mvn clean package -Dmaven.test.skip=true` | 清理并打包（跳过测试） |
| `mvn clean compile -Dmaven.test.skip=true` | 仅编译（跳过测试） |
| `mvn spring-boot:run -Dmaven.test.skip=true` | Maven启动（跳过测试） |
| `java -jar target/SpringDemo-0.0.1-SNAPSHOT.jar` | 运行JAR包 |
| `./start.sh` | 使用脚本启动 |
| `lsof -i:8080` | 查看端口占用 |
| `kill -9 <PID>` | 终止进程 |

---

## 💡 最佳实践

### 1. 使用启动脚本

```bash
# 一键编译和启动
./start.sh
```

启动脚本会自动：
- 检查是否已编译
- 如果没有，自动编译
- 启动应用

### 2. 开发时使用Maven

```bash
# 开发模式（支持热部署）
mvn spring-boot:run -Dmaven.test.skip=true
```

### 3. 生产环境使用JAR包

```bash
# 后台运行
nohup java -jar target/SpringDemo-0.0.1-SNAPSHOT.jar > app.log 2>&1 &

# 查看日志
tail -f app.log
```

---

## 🎯 下一步

1. ✅ 编译：`mvn clean package -Dmaven.test.skip=true`
2. ✅ 启动：`./start.sh`
3. ✅ 清除缓存：`Ctrl + Shift + R`
4. ✅ 访问：`http://localhost:8080/index.html`
5. ✅ 测试：输入"分类 1"

---

**最后更新**：2026-04-27