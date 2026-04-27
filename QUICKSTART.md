# 快速启动指南

## 方式一：使用H2内存数据库（推荐，无需安装MySQL）

### 1. 启动应用
```bash
./start-h2.sh
```

或者手动启动：
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=h2
```

### 2. 访问H2数据库控制台
浏览器访问：http://localhost:8080/h2-console

连接信息：
- **JDBC URL**: `jdbc:h2:mem:hotline`
- **用户名**: `sa`
- **密码**: (留空)

### 3. 测试接口
```bash
# 获取统计数据
curl http://localhost:8080/api/classify/stats/data

# 获取未分类工单
curl http://localhost:8080/api/classify/unclassified

# 预览分类
curl -X POST http://localhost:8080/api/classify/preview \
  -H "Content-Type: application/json" \
  -d '{"orderContent": "市民反映XX道路路灯不亮，影响夜间出行安全"}'
```

---

## 方式二：使用MySQL数据库（需要安装MySQL）

### 1. 安装并配置MySQL
```bash
# macOS
brew install mysql
brew services start mysql

# 运行初始化脚本
./start-mysql.sh
```

### 2. 修改数据库配置
编辑 `src/main/resources/application.properties`，修改密码：
```properties
spring.datasource.password=你的MySQL密码
```

### 3. 启动应用
```bash
mvn spring-boot:run
```

---

## 常见问题

### Q: 启动时报数据库连接错误？
A: 使用方式一（H2内存数据库），无需安装MySQL：
```bash
./start-h2.sh
```

### Q: 如何查看数据库数据？
A: 访问 http://localhost:8080/h2-console 查看H2数据库内容

### Q: 接口返回错误？
A: 检查日志，确保应用启动成功且数据库已初始化

---

## 项目结构

```
SpringDemo/
├── start-h2.sh              # H2启动脚本
├── start-mysql.sh           # MySQL初始化脚本
├── database/
│   └── init.sql             # MySQL初始化SQL
├── src/main/resources/
│   ├── application.properties        # MySQL配置
│   ├── application-h2.properties     # H2配置
│   └── schema.sql                    # H2数据库初始化
└── README.md
```