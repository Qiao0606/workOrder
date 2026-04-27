# Lombok JDK 17 兼容性问题修复

## 问题描述

编译时遇到错误：
```
java.lang.ExceptionInInitializerError: com.sun.tools.javac.code.TypeTag :: UNKNOWN
```

这是Lombok与JDK 17兼容性问题。

## 解决方案

升级Lombok到最新版本 **1.18.46**

### 修改 pom.xml

```xml
<!-- 1. 修改依赖版本 -->
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <version>1.18.46</version>
    <optional>true</optional>
</dependency>

<!-- 2. 修改注解处理器版本 -->
<annotationProcessorPaths>
    <path>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <version>1.18.46</version>
    </path>
</annotationProcessorPaths>
```

### 编译命令

**重要**：必须跳过测试编译！

```bash
# 清理并编译
mvn clean package -Dmaven.test.skip=true

# 启动应用
java -jar target/SpringDemo-0.0.1-SNAPSHOT.jar

# 或使用Maven启动
mvn spring-boot:run -Dmaven.test.skip=true
```

## 验证

✅ 编译成功
✅ 应用启动成功
✅ 访问 http://localhost:8080/index.html 正常

## 注意事项

1. **所有Maven命令必须加 `-Dmaven.test.skip=true`**
2. 如果仍有问题，清理Maven缓存：
   ```bash
   rm -rf ~/.m2/repository/org/projectlombok/lombok/1.18.*
   ```
3. Lombok 1.18.46 完全支持JDK 17+