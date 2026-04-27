#!/bin/bash

echo "======================================"
echo "  启动热线工单智能分类系统"
echo "======================================"
echo ""

# 检查是否已编译
if [ ! -f "target/SpringDemo-0.0.1-SNAPSHOT.jar" ]; then
    echo "正在编译项目..."
    mvn clean package -Dmaven.test.skip=true
    if [ $? -ne 0 ]; then
        echo "❌ 编译失败"
        exit 1
    fi
fi

echo "✅ 编译成功"
echo ""
echo "正在启动应用..."
echo ""

# 启动应用
java -jar target/SpringDemo-0.0.1-SNAPSHOT.jar