#!/usr/bin/env bash
# ============================================================
# 全模块接口集成测试
# 使用方式: bash api-test.sh [BASE_URL]
# 默认: http://localhost:8080
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

jq_field() { python3 -c "import json,sys; print(json.load(open('$1'))$2)" 2>/dev/null || echo ""; }

TS=$(date +%s)

# ─── 1. 登录 ───
echo ""
echo "=== 1. 登录 ==="
STATUS=$(curl -s -o /tmp/login_resp.json -w '%{http_code}' \
  -X POST "$BASE/api/auth/login" \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"123456"}')
assert_status "POST /api/auth/login" "200" "$STATUS"
TOKEN=$(jq_field /tmp/login_resp.json "['data']['token']")
AUTH="Authorization: Bearer $TOKEN"

# ─── 2. 健康 + 仪表盘 ───
echo ""
echo "=== 2. 健康 + 仪表盘 ==="
STATUS=$(curl -s -o /dev/null -w '%{http_code}' "$BASE/api/health")
assert_status "GET /api/health" "200" "$STATUS"
STATUS=$(curl -s -o /dev/null -w '%{http_code}' -H "$AUTH" "$BASE/api/dashboard/summary")
assert_status "GET /api/dashboard/summary" "200" "$STATUS"

# ─── 3. 会计科目 ───
echo ""
echo "=== 3. 会计科目 ==="
STATUS=$(curl -s -o /dev/null -w '%{http_code}' -H "$AUTH" "$BASE/api/account-subject/tree")
assert_status "GET /api/account-subject/tree" "200" "$STATUS"

STATUS=$(curl -s -o /tmp/subj.json -w '%{http_code}' \
  -X POST "$BASE/api/account-subject" \
  -H "$AUTH" -H 'Content-Type: application/json' \
  -d "{\"code\":\"T${TS: -4}\",\"name\":\"测试科目$TS\",\"type\":\"asset\",\"debitCredit\":\"debit\",\"sort\":99}")
assert_status "POST /api/account-subject" "200" "$STATUS"
SID=$(jq_field /tmp/subj.json "['data']['id']")

if [ -n "$SID" ]; then
  STATUS=$(curl -s -o /dev/null -w '%{http_code}' -H "$AUTH" "$BASE/api/account-subject/$SID")
  assert_status "GET /api/account-subject/$SID" "200" "$STATUS"
fi

# ─── 4. 往来方 ───
echo ""
echo "=== 4. 往来方 ==="
STATUS=$(curl -s -o /dev/null -w '%{http_code}' -H "$AUTH" "$BASE/api/counterparty")
assert_status "GET /api/counterparty" "200" "$STATUS"

STATUS=$(curl -s -o /tmp/cp.json -w '%{http_code}' \
  -X POST "$BASE/api/counterparty" \
  -H "$AUTH" -H 'Content-Type: application/json' \
  -d "{\"name\":\"测试客户$TS\",\"type\":\"customer\",\"contact\":\"测试\",\"phone\":\"13900000000\"}")
assert_status "POST /api/counterparty" "200" "$STATUS"
CPID=$(jq_field /tmp/cp.json "['data']['id']")

# ─── 5. 收支事实 + 分摊 ───
echo ""
echo "=== 5. 收支事实 + 跨期分摊 ==="

STATUS=$(curl -s -o /dev/null -w '%{http_code}' -H "$AUTH" "$BASE/api/fact-event")
assert_status "GET /api/fact-event (列表)" "200" "$STATUS"

if [ -n "$SID" ] && [ -n "$CPID" ]; then
  # 普通收入
  STATUS=$(curl -s -o /tmp/fe1.json -w '%{http_code}' \
    -X POST "$BASE/api/fact-event" \
    -H "$AUTH" -H 'Content-Type: application/json' \
    -d "{\"type\":\"income\",\"amount\":50000,\"businessDate\":\"2026-05-14\",\"subjectId\":$SID,\"counterpartyId\":$CPID}")
  assert_status "POST /api/fact-event (普通收入)" "200" "$STATUS"
  FE1=$(jq_field /tmp/fe1.json "['data']['id']")

  # 固定成本 + 分摊
  STATUS=$(curl -s -o /tmp/fe2.json -w '%{http_code}' \
    -X POST "$BASE/api/fact-event" \
    -H "$AUTH" -H 'Content-Type: application/json' \
    -d "{\"type\":\"cost\",\"amount\":12000,\"businessDate\":\"2026-01-01\",\"subjectId\":$SID,\"counterpartyId\":$CPID,\"costCategory\":\"fixed\",\"amortizeStart\":\"2026-01-01\",\"amortizeEnd\":\"2026-12-01\"}")
  assert_status "POST /api/fact-event (固定成本+分摊)" "200" "$STATUS"
  FE2=$(jq_field /tmp/fe2.json "['data']['id']")

  if [ -n "$FE2" ]; then
    STATUS=$(curl -s -o /tmp/amort.json -w '%{http_code}' -H "$AUTH" "$BASE/api/fact-event/$FE2/amortization")
    assert_status "GET /api/fact-event/$FE2/amortization (分摊明细)" "200" "$STATUS"
    AMORT_COUNT=$(python3 -c "import json; print(len(json.load(open('/tmp/amort.json'))['data']))" 2>/dev/null || echo "0")
    if [ "$AMORT_COUNT" = "12" ]; then
      green "分摊明细 12 条 ✓"
      PASS=$((PASS + 1))
    else
      red "分摊明细预期 12 条，实际 $AMORT_COUNT"
      FAIL=$((FAIL + 1))
    fi
  fi

  # 冲正
  if [ -n "$FE1" ]; then
    STATUS=$(curl -s -o /dev/null -w '%{http_code}' -X POST -H "$AUTH" "$BASE/api/fact-event/$FE1/reverse")
    assert_status "POST /api/fact-event/$FE1/reverse (冲正)" "200" "$STATUS"
  fi
fi

# ─── 6. 项目 ───
echo ""
echo "=== 6. 项目 ==="

STATUS=$(curl -s -o /dev/null -w '%{http_code}' -H "$AUTH" "$BASE/api/project")
assert_status "GET /api/project" "200" "$STATUS"

STATUS=$(curl -s -o /tmp/proj.json -w '%{http_code}' \
  -X POST "$BASE/api/project" \
  -H "$AUTH" -H 'Content-Type: application/json' \
  -d "{\"code\":\"PRJ-$TS\",\"name\":\"测试项目$TS\",\"startDate\":\"2026-01-01\",\"endDate\":\"2026-12-31\",\"budget\":100000}")
assert_status "POST /api/project" "200" "$STATUS"
PID=$(jq_field /tmp/proj.json "['data']['id']")

if [ -n "$PID" ]; then
  STATUS=$(curl -s -o /dev/null -w '%{http_code}' -H "$AUTH" "$BASE/api/project/$PID")
  assert_status "GET /api/project/$PID" "200" "$STATUS"
  STATUS=$(curl -s -o /dev/null -w '%{http_code}' -X POST \
    -H "$AUTH" -H 'Content-Type: application/json' \
    "$BASE/api/project/$PID/transition" -d '{"action":"start"}')
  assert_status "POST /api/project/$PID/transition (启动)" "200" "$STATUS"
fi

# ─── 7. 合同 ───
echo ""
echo "=== 7. 合同 ==="

STATUS=$(curl -s -o /dev/null -w '%{http_code}' -H "$AUTH" "$BASE/api/contract")
assert_status "GET /api/contract" "200" "$STATUS"

STATUS=$(curl -s -o /tmp/ct.json -w '%{http_code}' \
  -X POST "$BASE/api/contract" \
  -H "$AUTH" -H 'Content-Type: application/json' \
  -d "{\"code\":\"CT-$TS\",\"name\":\"测试合同$TS\",\"type\":\"sales\",\"counterpartyId\":$CPID,\"amount\":80000,\"signDate\":\"2026-05-14\",\"startDate\":\"2026-05-14\",\"endDate\":\"2026-12-31\"}")
assert_status "POST /api/contract" "200" "$STATUS"
CTID=$(jq_field /tmp/ct.json "['data']['id']")

if [ -n "$CTID" ]; then
  STATUS=$(curl -s -o /dev/null -w '%{http_code}' -H "$AUTH" "$BASE/api/contract/$CTID")
  assert_status "GET /api/contract/$CTID" "200" "$STATUS"
  STATUS=$(curl -s -o /dev/null -w '%{http_code}' -X POST \
    -H "$AUTH" -H 'Content-Type: application/json' \
    "$BASE/api/contract/$CTID/transition" -d '{"action":"activate"}')
  assert_status "POST /api/contract/$CTID/transition (生效)" "200" "$STATUS"
fi

# ─── 8. 应收账款 + 回款流水 ───
echo ""
echo "=== 8. 应收账款 ==="

STATUS=$(curl -s -o /dev/null -w '%{http_code}' -H "$AUTH" "$BASE/api/receivable")
assert_status "GET /api/receivable" "200" "$STATUS"

STATUS=$(curl -s -o /tmp/recv.json -w '%{http_code}' \
  -X POST "$BASE/api/receivable" \
  -H "$AUTH" -H 'Content-Type: application/json' \
  -d "{\"code\":\"RCV-$TS\",\"counterpartyId\":$CPID,\"amount\":30000,\"dueDate\":\"2026-06-30\"}")
assert_status "POST /api/receivable" "200" "$STATUS"
RID=$(jq_field /tmp/recv.json "['data']['id']")

if [ -n "$RID" ]; then
  # 登记回款
  STATUS=$(curl -s -o /dev/null -w '%{http_code}' \
    -X POST "$BASE/api/receivable/$RID/payment" \
    -H "$AUTH" -H 'Content-Type: application/json' \
    -d '{"amount":10000,"payDate":"2026-05-14","remark":"首期回款"}')
  assert_status "POST /api/receivable/$RID/payment (回款)" "200" "$STATUS"

  # 查看流水
  STATUS=$(curl -s -o /dev/null -w '%{http_code}' -H "$AUTH" "$BASE/api/receivable/$RID/payments")
  assert_status "GET /api/receivable/$RID/payments (流水)" "200" "$STATUS"
fi

# 批量逾期
STATUS=$(curl -s -o /dev/null -w '%{http_code}' -X POST -H "$AUTH" "$BASE/api/receivable/batch-overdue")
assert_status "POST /api/receivable/batch-overdue" "200" "$STATUS"

# ─── 9. 应付账款 + 付款流水 ───
echo ""
echo "=== 9. 应付账款 ==="

STATUS=$(curl -s -o /dev/null -w '%{http_code}' -H "$AUTH" "$BASE/api/payable")
assert_status "GET /api/payable" "200" "$STATUS"

STATUS=$(curl -s -o /tmp/pay.json -w '%{http_code}' \
  -X POST "$BASE/api/payable" \
  -H "$AUTH" -H 'Content-Type: application/json' \
  -d "{\"code\":\"PAY-$TS\",\"counterpartyId\":$CPID,\"amount\":20000,\"dueDate\":\"2026-07-31\"}")
assert_status "POST /api/payable" "200" "$STATUS"
PAID=$(jq_field /tmp/pay.json "['data']['id']")

if [ -n "$PAID" ]; then
  STATUS=$(curl -s -o /dev/null -w '%{http_code}' \
    -X POST "$BASE/api/payable/$PAID/payment" \
    -H "$AUTH" -H 'Content-Type: application/json' \
    -d '{"amount":5000,"payDate":"2026-05-14","remark":"首期付款"}')
  assert_status "POST /api/payable/$PAID/payment (付款)" "200" "$STATUS"

  STATUS=$(curl -s -o /dev/null -w '%{http_code}' -H "$AUTH" "$BASE/api/payable/$PAID/payments")
  assert_status "GET /api/payable/$PAID/payments (流水)" "200" "$STATUS"
fi

STATUS=$(curl -s -o /dev/null -w '%{http_code}' -X POST -H "$AUTH" "$BASE/api/payable/batch-overdue")
assert_status "POST /api/payable/batch-overdue" "200" "$STATUS"

# ─── 10. 参数校验 ───
echo ""
echo "=== 10. 参数校验 ==="
STATUS=$(curl -s -o /dev/null -w '%{http_code}' \
  -X POST "$BASE/api/account-subject" \
  -H "$AUTH" -H 'Content-Type: application/json' \
  -d '{"code":"","name":"","type":"","debitCredit":"","sort":0}')
assert_status "POST /api/account-subject (空参数→400)" "400" "$STATUS"

STATUS=$(curl -s -o /dev/null -w '%{http_code}' \
  -X POST "$BASE/api/fact-event" \
  -H "$AUTH" -H 'Content-Type: application/json' \
  -d '{"type":"","amount":0}')
assert_status "POST /api/fact-event (空参数→400)" "400" "$STATUS"

# ─── 汇总 ───
echo ""
echo "==============================="
echo "  通过: $PASS   失败: $FAIL"
echo "==============================="

if [ "$FAIL" -gt 0 ]; then
  exit 1
fi
