#!/bin/bash

# SpringDemo项目编译脚本（Java 8兼容）
# 使用Java 17编译器生成Java 8字节码
# Lombok版本: 1.18.46（已验证兼容）

echo "=========================================="
echo "SpringDemo 项目编译脚本"
echo "目标: Java 8 兼容"
echo "Lombok: 1.18.46"
echo "=========================================="
echo ""

# 清理旧的构建文件
echo "清理旧的构建文件..."
mvn clean

# 编译项目（跳过测试）
echo ""
echo "开始编译项目..."
mvn package -Dmaven.test.skip=true

# 检查编译结果
if [ $? -eq 0 ]; then
    echo ""
    echo "=========================================="
    echo "✅ 编译成功！"
    echo "=========================================="
    echo ""
    echo "JAR文件位置: target/SpringDemo-0.0.1-SNAPSHOT.jar"
    echo ""
    
    # 验证字节码版本
    echo "验证字节码版本..."
    cd target/classes
    VERSION=$(javap -v com/example/springdemo/SpringDemoApplication.class 2>/dev/null | grep "major version" | awk '{print $3}')
    cd ../..
    
    if [ "$VERSION" = "52" ]; then
        echo "✅ 字节码版本: $VERSION (Java 8)"
    else
        echo "⚠️  字节码版本: $VERSION (期望: 52)"
    fi
    
    echo ""
    echo "运行方式:"
    echo "  java -jar target/SpringDemo-0.0.1-SNAPSHOT.jar"
    echo ""
else
    echo ""
    echo "=========================================="
    echo "❌ 编译失败！"
    echo "=========================================="
    echo ""
    echo "请检查错误信息并修复问题"
    echo ""
    echo "如果遇到Lombok错误，请确保使用以下版本："
    echo "  - Lombok: 1.18.46"
    echo "  - Maven编译器插件: 3.11.0"
    exit 1
fi