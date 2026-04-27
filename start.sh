#!/bin/bash

# 停止旧进程
echo "正在停止旧进程..."
PID=$(lsof -ti:8080)
if [ ! -z "$PID" ]; then
    kill -9 $PID
    echo "已停止进程: $PID"
    sleep 2
fi

# 启动应用
echo "正在启动应用..."
mvn spring-boot:run -Dmaven.test.skip=true > app.log 2>&1 &
echo "应用启动中，进程ID: $!"

# 等待启动
echo "等待应用启动..."
sleep 10

# 检查状态
if curl -s http://localhost:8080/index.html > /dev/null; then
    echo "✅ 应用启动成功！"
    echo ""
    echo "访问地址: http://localhost:8080/index.html"
    echo ""
    echo "查看日志: tail -f app.log"
    echo "停止应用: lsof -ti:8080 | xargs kill -9"
else
    echo "❌ 应用启动失败，请查看日志:"
    tail -50 app.log
fi