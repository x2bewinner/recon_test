#!/bin/bash

# 測試執行腳本
# 用法: ./run-tests.sh [選項]
# 選項:
#   -a, --all        執行所有測試（預設）
#   -s, --service    只執行 Service 層測試
#   -c, --controller 只執行 Controller 層測試
#   -h, --help       顯示幫助訊息

set -e

# 顏色定義
GREEN='\033[0;32m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 顯示幫助訊息
show_help() {
    echo "測試執行腳本"
    echo ""
    echo "用法: ./run-tests.sh [選項]"
    echo ""
    echo "選項:"
    echo "  -a, --all        執行所有測試（預設）"
    echo "  -s, --service    只執行 Service 層測試"
    echo "  -c, --controller 只執行 Controller 層測試"
    echo "  -h, --help       顯示幫助訊息"
    echo ""
}

# 執行所有測試
run_all_tests() {
    echo -e "${BLUE}執行所有測試...${NC}"
    mvn clean test
}

# 執行 Service 層測試
run_service_tests() {
    echo -e "${BLUE}執行 Service 層測試...${NC}"
    mvn clean test -Dtest=AuditRegisterServiceTest
}

# 執行 Controller 層測試
run_controller_tests() {
    echo -e "${BLUE}執行 Controller 層測試...${NC}"
    mvn clean test -Dtest=AuditRegisterControllerTest
}

# 解析參數
case "${1:-}" in
    -h|--help)
        show_help
        exit 0
        ;;
    -s|--service)
        run_service_tests
        ;;
    -c|--controller)
        run_controller_tests
        ;;
    -a|--all|"")
        run_all_tests
        ;;
    *)
        echo "未知選項: $1"
        show_help
        exit 1
        ;;
esac

echo -e "${GREEN}測試完成！${NC}"

