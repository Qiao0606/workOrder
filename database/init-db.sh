#!/bin/bash

# ============================================
# MySQL 数据库快速初始化脚本
# 数据库名: hotline
# 用途: 快速创建数据库和表结构
# ============================================

# 颜色定义
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 配置变量
DB_HOST="localhost"
DB_PORT="3306"
DB_USER="root"
DB_NAME="hotline"
SQL_FILE="$(dirname "$0")/init-mysql.sql"

# 显示帮助信息
show_help() {
    echo -e "${YELLOW}使用方法:${NC}"
    echo "  $0 [选项]"
    echo ""
    echo -e "${YELLOW}选项:${NC}"
    echo "  -h, --host      MySQL主机地址 (默认: localhost)"
    echo "  -P, --port      MySQL端口 (默认: 3306)"
    echo "  -u, --user      MySQL用户名 (默认: root)"
    echo "  -p, --password  MySQL密码 (必填)"
    echo "  -d, --database  数据库名 (默认: hotline)"
    echo "  --help          显示帮助信息"
    echo ""
    echo -e "${YELLOW}示例:${NC}"
    echo "  $0 -p yourpassword"
    echo "  $0 -h localhost -P 3306 -u root -p yourpassword"
    echo ""
}

# 参数解析
while [[ $# -gt 0 ]]; do
    case $1 in
        -h|--host)
            DB_HOST="$2"
            shift 2
            ;;
        -P|--port)
            DB_PORT="$2"
            shift 2
            ;;
        -u|--user)
            DB_USER="$2"
            shift 2
            ;;
        -p|--password)
            DB_PASS="$2"
            shift 2
            ;;
        -d|--database)
            DB_NAME="$2"
            shift 2
            ;;
        --help)
            show_help
            exit 0
            ;;
        *)
            echo -e "${RED}未知选项: $1${NC}"
            show_help
            exit 1
            ;;
    esac
done

# 检查密码
if [ -z "$DB_PASS" ]; then
    echo -e "${RED}错误: MySQL密码不能为空${NC}"
    echo ""
    show_help
    exit 1
fi

# 检查SQL文件
if [ ! -f "$SQL_FILE" ]; then
    echo -e "${RED}错误: SQL文件不存在: $SQL_FILE${NC}"
    exit 1
fi

# 检查MySQL客户端
if ! command -v mysql &> /dev/null; then
    echo -e "${RED}错误: 未找到MySQL客户端，请先安装MySQL${NC}"
    exit 1
fi

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}  MySQL 数据库初始化脚本${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""
echo -e "${YELLOW}配置信息:${NC}"
echo "  MySQL主机: $DB_HOST:$DB_PORT"
echo "  用户名: $DB_USER"
echo "  数据库名: $DB_NAME"
echo "  SQL文件: $SQL_FILE"
echo ""

# 测试连接
echo -e "${YELLOW}[1/3] 测试MySQL连接...${NC}"
if mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASS" -e "SELECT 1;" &> /dev/null; then
    echo -e "${GREEN}✓ MySQL连接成功${NC}"
else
    echo -e "${RED}✗ MySQL连接失败，请检查配置${NC}"
    exit 1
fi

# 执行SQL脚本
echo -e "${YELLOW}[2/3] 执行SQL脚本...${NC}"
if mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASS" < "$SQL_FILE"; then
    echo -e "${GREEN}✓ SQL脚本执行成功${NC}"
else
    echo -e "${RED}✗ SQL脚本执行失败${NC}"
    exit 1
fi

# 验证数据
echo -e "${YELLOW}[3/3] 验证数据...${NC}"
echo ""

# 查询表记录数
TABLES=$(mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASS" -D"$DB_NAME" -N -e "SHOW TABLES;")

echo -e "${GREEN}数据库表:${NC}"
for table in $TABLES; do
    count=$(mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASS" -D"$DB_NAME" -N -e "SELECT COUNT(*) FROM $table;")
    printf "  %-20s %s 条记录\n" "$table" "$count"
done

echo ""
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}  数据库初始化完成！${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""
echo -e "${YELLOW}下一步:${NC}"
echo "  1. 更新 application.properties 中的数据库连接信息"
echo "  2. 启动应用: mvn spring-boot:run"
echo ""