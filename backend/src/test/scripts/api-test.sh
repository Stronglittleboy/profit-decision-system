#!/usr/bin/env bash
# ============================================================
# 接口集成测试脚本
# 使用方式: bash api-test.sh [BASE_URL]
# 默认: http://localhost:8080
# 前提: 后端服务已启动，数据库已初始化
# ============================================================

set -euo pipefail

BASE="${1:-http://localhost:8080}"
PASS=0
FAIL=0
TOKEN=""

green()  { printf "\033[32m✓ %s\033[0m\n" "$1"; }
red()    { printf "\033[31m✗ %s\033[0m\n" "$1"; }

assert_status() {
  local desc="$1" expected="$2" actual="$3"
  if [ "$expected" = "$actual" ]; then
    green "$desc (HTTP $actual)"
    PASS=$((PASS + 1))
  else
    red "$desc — expected $expected, got $actual"
    FAIL=$((FAIL + 1))
  fi
}

# ─── 1. 登录 ───
echo ""
echo "=== 登录 ==="

STATUS=$(curl -s -o /tmp/login_resp.json -w '%{http_code}' \
  -X POST "$BASE/api/auth/login" \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"123456"}')
assert_status "POST /api/auth/login" "200" "$STATUS"

TOKEN=$(python3 -c "import json; print(json.load(open('/tmp/login_resp.json'))['data']['token'])" 2>/dev/null || echo "")
if [ -z "$TOKEN" ]; then
  red "无法提取 token，后续测试将跳过鉴权"
fi

AUTH="Authorization: Bearer $TOKEN"

# ─── 2. 健康检查 ───
echo ""
echo "=== 健康检查 ==="

STATUS=$(curl -s -o /dev/null -w '%{http_code}' "$BASE/api/health")
assert_status "GET /api/health" "200" "$STATUS"

# ─── 3. 仪表盘 ───
echo ""
echo "=== 仪表盘 ==="

STATUS=$(curl -s -o /dev/null -w '%{http_code}' -H "$AUTH" "$BASE/api/dashboard/summary")
assert_status "GET /api/dashboard/summary" "200" "$STATUS"

# ─── 4. 会计科目 CRUD ───
echo ""
echo "=== 会计科目 ==="

# 查询树
STATUS=$(curl -s -o /dev/null -w '%{http_code}' -H "$AUTH" "$BASE/api/account-subject/tree")
assert_status "GET /api/account-subject/tree" "200" "$STATUS"

# 搜索
STATUS=$(curl -s -o /dev/null -w '%{http_code}' -H "$AUTH" "$BASE/api/account-subject/tree?keyword=库存")
assert_status "GET /api/account-subject/tree?keyword=库存" "200" "$STATUS"

# 新增
STATUS=$(curl -s -o /tmp/subject_create.json -w '%{http_code}' \
  -X POST "$BASE/api/account-subject" \
  -H "$AUTH" -H 'Content-Type: application/json' \
  -d '{"code":"9901","name":"测试科目","type":"asset","debitCredit":"debit","sort":99}')
assert_status "POST /api/account-subject (新增)" "200" "$STATUS"

SUBJECT_ID=$(python3 -c "import json; print(json.load(open('/tmp/subject_create.json'))['data']['id'])" 2>/dev/null || echo "")

# 查看详情
if [ -n "$SUBJECT_ID" ]; then
  STATUS=$(curl -s -o /dev/null -w '%{http_code}' -H "$AUTH" "$BASE/api/account-subject/$SUBJECT_ID")
  assert_status "GET /api/account-subject/$SUBJECT_ID (详情)" "200" "$STATUS"

  # 编辑
  STATUS=$(curl -s -o /dev/null -w '%{http_code}' \
    -X PUT "$BASE/api/account-subject/$SUBJECT_ID" \
    -H "$AUTH" -H 'Content-Type: application/json' \
    -d '{"code":"9901","name":"测试科目改名","type":"asset","debitCredit":"debit","sort":99}')
  assert_status "PUT /api/account-subject/$SUBJECT_ID (编辑)" "200" "$STATUS"

  # 启停
  STATUS=$(curl -s -o /dev/null -w '%{http_code}' \
    -X PATCH "$BASE/api/account-subject/$SUBJECT_ID/status" \
    -H "$AUTH" -H 'Content-Type: application/json' \
    -d '{"enabled":false}')
  assert_status "PATCH /api/account-subject/$SUBJECT_ID/status (停用)" "200" "$STATUS"

  # 删除
  STATUS=$(curl -s -o /dev/null -w '%{http_code}' \
    -X DELETE "$BASE/api/account-subject/$SUBJECT_ID" \
    -H "$AUTH")
  assert_status "DELETE /api/account-subject/$SUBJECT_ID (删除)" "200" "$STATUS"
fi

# ─── 5. 往来方 CRUD ───
echo ""
echo "=== 往来方 ==="

# 列表
STATUS=$(curl -s -o /dev/null -w '%{http_code}' -H "$AUTH" "$BASE/api/counterparty")
assert_status "GET /api/counterparty (列表)" "200" "$STATUS"

# 搜索 + 类型筛选
STATUS=$(curl -s -o /dev/null -w '%{http_code}' -H "$AUTH" "$BASE/api/counterparty?keyword=客户&type=customer")
assert_status "GET /api/counterparty?keyword=客户&type=customer" "200" "$STATUS"

# 新增
STATUS=$(curl -s -o /tmp/cp_create.json -w '%{http_code}' \
  -X POST "$BASE/api/counterparty" \
  -H "$AUTH" -H 'Content-Type: application/json' \
  -d '{"name":"测试往来方","type":"customer","contact":"测试人","phone":"13900000000"}')
assert_status "POST /api/counterparty (新增)" "200" "$STATUS"

CP_ID=$(python3 -c "import json; print(json.load(open('/tmp/cp_create.json'))['data']['id'])" 2>/dev/null || echo "")

if [ -n "$CP_ID" ]; then
  # 详情
  STATUS=$(curl -s -o /dev/null -w '%{http_code}' -H "$AUTH" "$BASE/api/counterparty/$CP_ID")
  assert_status "GET /api/counterparty/$CP_ID (详情)" "200" "$STATUS"

  # 编辑
  STATUS=$(curl -s -o /dev/null -w '%{http_code}' \
    -X PUT "$BASE/api/counterparty/$CP_ID" \
    -H "$AUTH" -H 'Content-Type: application/json' \
    -d '{"name":"测试往来方改名","type":"supplier","contact":"新联系人","phone":"13900000001"}')
  assert_status "PUT /api/counterparty/$CP_ID (编辑)" "200" "$STATUS"

  # 启停
  STATUS=$(curl -s -o /dev/null -w '%{http_code}' \
    -X PATCH "$BASE/api/counterparty/$CP_ID/status" \
    -H "$AUTH" -H 'Content-Type: application/json' \
    -d '{"enabled":false}')
  assert_status "PATCH /api/counterparty/$CP_ID/status (停用)" "200" "$STATUS"

  # 删除
  STATUS=$(curl -s -o /dev/null -w '%{http_code}' \
    -X DELETE "$BASE/api/counterparty/$CP_ID" \
    -H "$AUTH")
  assert_status "DELETE /api/counterparty/$CP_ID (删除)" "200" "$STATUS"
fi

# ─── 6. 参数校验（应返回 400） ───
echo ""
echo "=== 参数校验 ==="

STATUS=$(curl -s -o /dev/null -w '%{http_code}' \
  -X POST "$BASE/api/account-subject" \
  -H "$AUTH" -H 'Content-Type: application/json' \
  -d '{"code":"","name":"","type":"","debitCredit":"","sort":0}')
assert_status "POST /api/account-subject (空参数→400)" "400" "$STATUS"

STATUS=$(curl -s -o /dev/null -w '%{http_code}' \
  -X POST "$BASE/api/counterparty" \
  -H "$AUTH" -H 'Content-Type: application/json' \
  -d '{"name":"","type":""}')
assert_status "POST /api/counterparty (空参数→400)" "400" "$STATUS"

# ─── 汇总 ───
echo ""
echo "==============================="
echo "  通过: $PASS   失败: $FAIL"
echo "==============================="

if [ "$FAIL" -gt 0 ]; then
  exit 1
fi
