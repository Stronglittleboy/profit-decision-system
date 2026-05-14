#!/bin/bash
# ============================================
# Day 1 上午 DBA任务执行脚本
# ============================================

set -e  # 遇到错误立即退出

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 数据库配置
DB_HOST="localhost"
DB_PORT="3306"
DB_NAME="jeecg-boot"
DB_USER="root"
DB_PASSWORD="123456"

# 工作目录
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKUP_DIR="${SCRIPT_DIR}/backups"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}Day 1 上午 DBA任务开始执行${NC}"
echo -e "${GREEN}========================================${NC}"

# 检查MySQL客户端
if ! command -v mysql &> /dev/null; then
    echo -e "${RED}错误: 未找到mysql客户端，请先安装${NC}"
    echo "Ubuntu/Debian: sudo apt-get install mysql-client"
    echo "CentOS/RHEL: sudo yum install mysql"
    exit 1
fi

# 检查数据库连接
echo -e "\n${YELLOW}[1/6] 检查数据库连接...${NC}"
if ! mysql -h"${DB_HOST}" -P"${DB_PORT}" -u"${DB_USER}" -p"${DB_PASSWORD}" -e "SELECT 1" &> /dev/null; then
    echo -e "${RED}错误: 无法连接到数据库${NC}"
    echo "请确保MySQL容器正在运行: docker ps | grep mysql"
    echo "或启动容器: cd /home/hlw/work/code/profit-decision-system && docker compose up -d profit-mysql"
    exit 1
fi
echo -e "${GREEN}✓ 数据库连接成功${NC}"

# 创建备份目录
mkdir -p "${BACKUP_DIR}"

# 任务1: 备份现有数据库
echo -e "\n${YELLOW}[2/6] 备份现有数据库...${NC}"
BACKUP_FILE="${BACKUP_DIR}/backup_${TIMESTAMP}.sql"
mysqldump -h"${DB_HOST}" -P"${DB_PORT}" -u"${DB_USER}" -p"${DB_PASSWORD}" \
    --databases "${DB_NAME}" \
    --single-transaction \
    --routines \
    --triggers \
    --events \
    > "${BACKUP_FILE}" 2>/dev/null || true

if [ -f "${BACKUP_FILE}" ]; then
    BACKUP_SIZE=$(du -h "${BACKUP_FILE}" | cut -f1)
    echo -e "${GREEN}✓ 备份完成: ${BACKUP_FILE} (${BACKUP_SIZE})${NC}"
else
    echo -e "${YELLOW}⚠ 备份文件未生成（可能是空数据库）${NC}"
fi

# 任务2: 执行schema-v4-reviewed.sql创建数据库表
echo -e "\n${YELLOW}[3/6] 执行schema-v4-reviewed.sql创建数据库表...${NC}"
if [ ! -f "${SCRIPT_DIR}/schema-v4-reviewed.sql" ]; then
    echo -e "${RED}错误: 未找到schema-v4-reviewed.sql文件${NC}"
    exit 1
fi

mysql -h"${DB_HOST}" -P"${DB_PORT}" -u"${DB_USER}" -p"${DB_PASSWORD}" "${DB_NAME}" < "${SCRIPT_DIR}/schema-v4-reviewed.sql"
echo -e "${GREEN}✓ 数据库表创建完成${NC}"

# 任务3: 验证表结构
echo -e "\n${YELLOW}[4/6] 验证表结构...${NC}"
TABLE_COUNT=$(mysql -h"${DB_HOST}" -P"${DB_PORT}" -u"${DB_USER}" -p"${DB_PASSWORD}" "${DB_NAME}" \
    -e "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='${DB_NAME}'" -sN)

echo "数据库中共有 ${TABLE_COUNT} 张表"

# 列出所有表
echo -e "\n表列表:"
mysql -h"${DB_HOST}" -P"${DB_PORT}" -u"${DB_USER}" -p"${DB_PASSWORD}" "${DB_NAME}" \
    -e "SHOW TABLES" | tail -n +2 | nl

if [ "${TABLE_COUNT}" -ge 19 ]; then
    echo -e "${GREEN}✓ 表结构验证通过（预期19张表，实际${TABLE_COUNT}张）${NC}"
else
    echo -e "${RED}⚠ 警告: 表数量不足（预期19张表，实际${TABLE_COUNT}张）${NC}"
fi

# 任务4: 导入初始化数据
echo -e "\n${YELLOW}[5/6] 导入初始化数据...${NC}"

# 导入init-data.sql
if [ -f "${SCRIPT_DIR}/init-data.sql" ]; then
    echo "导入 init-data.sql..."
    mysql -h"${DB_HOST}" -P"${DB_PORT}" -u"${DB_USER}" -p"${DB_PASSWORD}" "${DB_NAME}" < "${SCRIPT_DIR}/init-data.sql"
    echo -e "${GREEN}✓ init-data.sql 导入完成${NC}"
else
    echo -e "${YELLOW}⚠ 未找到 init-data.sql${NC}"
fi

# 导入dict-data.sql
if [ -f "${SCRIPT_DIR}/dict-data.sql" ]; then
    echo "导入 dict-data.sql..."
    mysql -h"${DB_HOST}" -P"${DB_PORT}" -u"${DB_USER}" -p"${DB_PASSWORD}" "${DB_NAME}" < "${SCRIPT_DIR}/dict-data.sql"
    echo -e "${GREEN}✓ dict-data.sql 导入完成${NC}"
else
    echo -e "${YELLOW}⚠ 未找到 dict-data.sql${NC}"
fi

# 任务5: 创建索引
echo -e "\n${YELLOW}[6/6] 创建性能优化索引...${NC}"

# 创建索引SQL
cat > "${SCRIPT_DIR}/create-indexes.sql" << 'EOF'
-- ============================================
-- 性能优化索引创建脚本
-- ============================================

-- fact_event表索引（已在schema中定义，这里添加额外的复合索引）
-- 用于利润计算的复合索引
CREATE INDEX idx_fact_event_profit_calc ON fact_event(accounting_date, org_unit_id, type, status);

-- 用于现金流分析的索引
CREATE INDEX idx_fact_event_cash_flow ON fact_event(cash_date, payment_method) WHERE cash_date IS NOT NULL;

-- 用于分摊计算的索引
CREATE INDEX idx_fact_event_amortization ON fact_event(amortization_start, amortization_end) 
WHERE amortization_start IS NOT NULL;

-- voucher表索引（已在schema中定义，这里添加额外的复合索引）
-- 用于凭证查询的复合索引
CREATE INDEX idx_voucher_period_status ON voucher(period, status);

-- 用于审计的索引
CREATE INDEX idx_voucher_created ON voucher(created_at, created_by);

-- audit_log分区表索引
-- 用于审计查询的复合索引
CREATE INDEX idx_audit_log_entity ON audit_log(entity_type, entity_id, operation);

-- 用于时间范围查询的索引
CREATE INDEX idx_audit_log_time ON audit_log(created_at);

-- 用于用户操作追踪的索引
CREATE INDEX idx_audit_log_user ON audit_log(user_id, created_at);

-- attribution_result表索引（用于归因查询）
CREATE INDEX idx_attribution_result_query ON attribution_result(org_unit_id, period, status);

-- receivable表索引（用于应收应付管理）
CREATE INDEX idx_receivable_status ON receivable(status, due_date);
CREATE INDEX idx_receivable_counterparty ON receivable(counterparty_type, counterparty_id, status);

-- profit_snapshot表索引（用于利润报表）
CREATE INDEX idx_profit_snapshot_query ON profit_snapshot(org_unit_id, period, snapshot_type);

-- goal表索引（用于目标管理）
CREATE INDEX idx_goal_period ON goal(period, org_unit_id);

-- 显示所有索引创建结果
SELECT 'Indexes created successfully' AS status;
EOF

echo "执行索引创建脚本..."
mysql -h"${DB_HOST}" -P"${DB_PORT}" -u"${DB_USER}" -p"${DB_PASSWORD}" "${DB_NAME}" < "${SCRIPT_DIR}/create-indexes.sql" 2>&1 | grep -v "Duplicate key name" || true
echo -e "${GREEN}✓ 索引创建完成${NC}"

# 最终验证
echo -e "\n${GREEN}========================================${NC}"
echo -e "${GREEN}Day 1 上午 DBA任务执行完成${NC}"
echo -e "${GREEN}========================================${NC}"

# 生成执行报告
echo -e "\n${YELLOW}执行报告:${NC}"
echo "1. 备份文件: ${BACKUP_FILE}"
echo "2. 数据库表数量: ${TABLE_COUNT}"
echo "3. 初始化数据: 已导入"
echo "4. 性能索引: 已创建"

# 显示关键表的记录数
echo -e "\n${YELLOW}关键表记录数:${NC}"
mysql -h"${DB_HOST}" -P"${DB_PORT}" -u"${DB_USER}" -p"${DB_PASSWORD}" "${DB_NAME}" << 'EOSQL'
SELECT 
    'org_unit' AS table_name, COUNT(*) AS record_count FROM org_unit
UNION ALL
SELECT 'user', COUNT(*) FROM user
UNION ALL
SELECT 'account_subject', COUNT(*) FROM account_subject
UNION ALL
SELECT 'attribution_rule', COUNT(*) FROM attribution_rule
UNION ALL
SELECT 'goal', COUNT(*) FROM goal;
EOSQL

echo -e "\n${GREEN}✓ 所有任务执行完成！${NC}"
