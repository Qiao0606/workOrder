# Java 8兼容性编译指南

## 最终解决方案

经过多次测试，找到了稳定的Java 8兼容配置：

### 关键配置

#### 1. Java版本
```xml
<properties>
    <java.version>8</java.version>
    <maven.compiler.source>8</maven.compiler.source>
    <maven.compiler.target>8</maven.compiler.target>
</properties>
```

#### 2. Lombok版本
```xml
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <version>1.18.32</version>
    <optional>true</optional>
</dependency>
```

#### 3. Maven编译器插件版本（关键！）
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <version>3.11.0</version>  <!-- 关键：使用3.11.0而不是3.8.1 -->
    <configuration>
        <source>8</source>
        <target>8</target>
        <encoding>UTF-8</encoding>
        <annotationProcessorPaths>
            <path>
                <groupId>org.projectlombok</groupId>
                <artifactId>lombok</artifactId>
                <version>1.18.32</version>
            </path>
        </annotationProcessorPaths>
    </configuration>
</plugin>
```

---

## 为什么会出现Lombok兼容性问题？

### 问题根源

```
java.lang.ExceptionInInitializerError: com.sun.tools.javac.code.TypeTag :: UNKNOWN
```

**原因**：
- Lombok 1.18.20-1.18.30 使用了已废弃的 `sun.misc.Unsafe` API
- Java 17 对这些API进行了更严格的限制
- Maven编译器插件 3.8.1 与Lombok的注解处理器配合有问题

### 解决方案

**升级Maven编译器插件到 3.11.0**：
- ✅ 更好的Java 17+支持
- ✅ 改进的注解处理器兼容性
- ✅ 修复了与Lombok的集成问题

---

## 编译验证

### 成功输出
```
[INFO] BUILD SUCCESS
[INFO] Total time:  4.306 s
```

### 字节码版本
```
major version: 52  ✅ Java 8
```

### JAR文件
```
-rw-r--r--  1 sky  staff   31M  SpringDemo-0.0.1-SNAPSHOT.jar
```

---

## 常见问题

### Q1: 为什么使用Java 17编译器生成Java 8字节码？

**A**: 这是一种常见的做法，称为"交叉编译"：
- 开发环境使用最新的LTS版本（Java 17）
- 生产环境可以使用Java 8+
- 向后兼容，降低部署要求

### Q2: 生成的JAR能在Java 8上运行吗？

**A**: 可以！字节码版本为52（Java 8），可以在Java 8及以上版本运行。

### Q3: 如果还遇到Lombok错误怎么办？

**A**: 尝试以下步骤：
```bash
# 1. 清理Maven缓存
rm -rf ~/.m2/repository/org/projectlombok

# 2. 清理项目构建
mvn clean

# 3. 重新编译
mvn package -Dmaven.test.skip=true
```

---

## 快速编译命令

```bash
# 使用编译脚本
./build-java8.sh

# 或手动编译
mvn clean package -Dmaven.test.skip=true

# 运行应用
java -jar target/SpringDemo-0.0.1-SNAPSHOT.jar
```

---

## 配置文件对比

### ❌ 错误配置（会导致Lombok错误）
```xml
<plugin>
    <artifactId>maven-compiler-plugin</artifactId>
    <version>3.8.1</version>  <!-- 旧版本 -->
</plugin>
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <version>1.18.20</version>  <!-- 不兼容版本 -->
</dependency>
```

### ✅ 正确配置
```xml
<plugin>
    <artifactId>maven-compiler-plugin</artifactId>
    <version>3.11.0</version>  <!-- 新版本 -->
</plugin>
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <version>1.18.32</version>  <!-- 兼容版本 -->
</dependency>
```

---

## 总结

✅ **Maven编译器插件 3.11.0** + **Lombok 1.18.32** = 完美兼容
✅ 使用Java 17编译器生成Java 8字节码
✅ 可在Java 8+环境运行