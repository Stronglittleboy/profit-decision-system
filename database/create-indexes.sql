-- ============================================
-- 性能优化索引创建脚本
-- Day 1 上午任务 - 索引优化
-- ============================================

-- 1. fact_event表索引优化
-- ============================================

-- 用于利润计算的复合索引（高频查询）
CREATE INDEX idx_fact_event_profit_calc 
ON fact_event(accounting_date, org_unit_id, type, status);

-- 用于现金流分析的索引
CREATE INDEX idx_fact_event_cash_flow 
ON fact_event(cash_date, payment_method);

-- 用于分摊计算的索引
CREATE INDEX idx_fact_event_amortization 
ON fact_event(amortization_start, amortization_end);

-- 用于业务日期查询的索引
CREATE INDEX idx_fact_event_business_date 
ON fact_event(business_date, org_unit_id);

-- 用于发票管理的索引
CREATE INDEX idx_fact_event_invoice 
ON fact_event(invoice_no, invoice_date);

-- 2. voucher表索引优化
-- ============================================

-- 用于凭证查询的复合索引
CREATE INDEX idx_voucher_period_status 
ON voucher(period, status);

-- 用于审计的索引
CREATE INDEX idx_voucher_created 
ON voucher(created_at, created_by);

-- 用于凭证日期查询
CREATE INDEX idx_voucher_date 
ON voucher(voucher_date, period);

-- 3. audit_log表索引优化
-- ============================================

-- 用于审计查询的复合索引
CREATE INDEX idx_audit_log_entity 
ON audit_log(entity_type, entity_id, operation);

-- 用于时间范围查询的索引
CREATE INDEX idx_audit_log_time 
ON audit_log(created_at);

-- 用于用户操作追踪的索引
CREATE INDEX idx_audit_log_user 
ON audit_log(user_id, created_at);

-- 用于IP追踪的索引
CREATE INDEX idx_audit_log_ip 
ON audit_log(ip_address, created_at);

-- 4. attribution_result表索引优化
-- ============================================

-- 用于归因查询的复合索引
CREATE INDEX idx_attribution_result_query 
ON attribution_result(org_unit_id, period, status);

-- 用于事件关联查询
CREATE INDEX idx_attribution_result_event 
ON attribution_result(fact_event_id, status);

-- 用于规则追踪
CREATE INDEX idx_attribution_result_rule 
ON attribution_result(rule_id, created_at);

-- 5. receivable表索引优化
-- ============================================

-- 用于应收应付状态管理
CREATE INDEX idx_receivable_status 
ON receivable(status, due_date);

-- 用于对手方查询
CREATE INDEX idx_receivable_counterparty 
ON receivable(counterparty_type, counterparty_id, status);

-- 用于账龄分析
CREATE INDEX idx_receivable_aging 
ON receivable(due_date, status);

-- 用于组织单元查询
CREATE INDEX idx_receivable_org 
ON receivable(org_unit_id, status);

-- 6. profit_snapshot表索引优化
-- ============================================

-- 用于利润报表查询
CREATE INDEX idx_profit_snapshot_query 
ON profit_snapshot(org_unit_id, period, snapshot_type);

-- 用于时间序列分析
CREATE INDEX idx_profit_snapshot_time 
ON profit_snapshot(period, snapshot_type);

-- 用于快照版本管理
CREATE INDEX idx_profit_snapshot_version 
ON profit_snapshot(version, created_at);

-- 7. goal表索引优化
-- ============================================

-- 用于目标管理查询
CREATE INDEX idx_goal_period 
ON goal(period, org_unit_id);

-- 用于目标状态查询
CREATE INDEX idx_goal_status 
ON goal(status, period);

-- 8. account_subject表索引优化
-- ============================================

-- 用于科目树查询（已在schema中有parent_id索引）
-- 添加科目类型查询索引
CREATE INDEX idx_account_subject_type_enabled 
ON account_subject(type, enabled);

-- 用于科目级别查询
CREATE INDEX idx_account_subject_level 
ON account_subject(level, enabled);

-- 9. counterparty表索引优化
-- ============================================

-- 用于对手方类型查询
CREATE INDEX idx_counterparty_type_status 
ON counterparty(type, status);

-- 用于对手方名称搜索（全文索引）
CREATE FULLTEXT INDEX idx_counterparty_name_fulltext 
ON counterparty(name);

-- 用于信用管理
CREATE INDEX idx_counterparty_credit 
ON counterparty(credit_limit, status);

-- 10. org_unit表索引优化
-- ============================================

-- 用于组织树查询（已在schema中有parent_id索引）
-- 添加组织类型查询索引
CREATE INDEX idx_org_unit_type_enabled 
ON org_unit(type, enabled);

-- 用于组织层级查询
CREATE INDEX idx_org_unit_level 
ON org_unit(level, enabled);

-- 11. attribution_rule表索引优化
-- ============================================

-- 用于规则优先级查询
CREATE INDEX idx_attribution_rule_priority 
ON attribution_rule(priority, enabled);

-- 用于规则类型查询
CREATE INDEX idx_attribution_rule_type 
ON attribution_rule(rule_type, enabled);

-- 12. period_closing表索引优化
-- ============================================

-- 用于期间结账查询
CREATE INDEX idx_period_closing_status 
ON period_closing(status, period);

-- 用于组织期间查询
CREATE INDEX idx_period_closing_org 
ON period_closing(org_unit_id, period);

-- 13. voucher_entry表索引优化
-- ============================================

-- 用于凭证分录查询
CREATE INDEX idx_voucher_entry_voucher 
ON voucher_entry(voucher_id, entry_no);

-- 用于科目查询
CREATE INDEX idx_voucher_entry_subject 
ON voucher_entry(account_subject_id, debit_credit);

-- 用于辅助核算查询
CREATE INDEX idx_voucher_entry_auxiliary 
ON voucher_entry(counterparty_id, project_id);

-- 14. cash_flow表索引优化
-- ============================================

-- 用于现金流查询
CREATE INDEX idx_cash_flow_date 
ON cash_flow(flow_date, org_unit_id);

-- 用于现金流类型查询
CREATE INDEX idx_cash_flow_type 
ON cash_flow(flow_type, status);

-- 用于对手方现金流查询
CREATE INDEX idx_cash_flow_counterparty 
ON cash_flow(counterparty_type, counterparty_id);

-- 15. attachment表索引优化
-- ============================================

-- 用于附件关联查询
CREATE INDEX idx_attachment_entity 
ON attachment(entity_type, entity_id);

-- 用于附件类型查询
CREATE INDEX idx_attachment_type 
ON attachment(file_type, status);

-- 用于上传者查询
CREATE INDEX idx_attachment_uploader 
ON attachment(uploaded_by, created_at);

-- ============================================
-- 索引创建完成验证
-- ============================================

-- 显示fact_event表的所有索引
SELECT 
    'fact_event' AS table_name,
    COUNT(*) AS index_count
FROM information_schema.statistics 
WHERE table_schema = DATABASE() 
  AND table_name = 'fact_event'
  AND index_name != 'PRIMARY';

-- 显示所有表的索引统计
SELECT 
    table_name,
    COUNT(DISTINCT index_name) AS index_count
FROM information_schema.statistics 
WHERE table_schema = DATABASE()
  AND index_name != 'PRIMARY'
GROUP BY table_name
ORDER BY table_name;

SELECT 'All indexes created successfully!' AS status;
