-- 字典数据初始化脚本
-- 用于 jeecg-boot 代码生成器

-- 1. 客户/供应商类型字典
INSERT INTO sys_dict (id, dict_name, dict_code, description, del_flag, create_by, create_time, update_by, update_time, type) 
VALUES ('counterparty_type_dict', '客户供应商类型', 'counterparty_type', '客户供应商类型字典', 0, 'admin', NOW(), 'admin', NOW(), 0);

INSERT INTO sys_dict_item (id, dict_id, item_text, item_value, description, sort_order, status, create_by, create_time, update_by, update_time) VALUES
('counterparty_type_1', 'counterparty_type_dict', '客户', 'customer', '客户', 1, 1, 'admin', NOW(), 'admin', NOW()),
('counterparty_type_2', 'counterparty_type_dict', '供应商', 'supplier', '供应商', 2, 1, 'admin', NOW(), 'admin', NOW()),
('counterparty_type_3', 'counterparty_type_dict', '客户+供应商', 'both', '既是客户又是供应商', 3, 1, 'admin', NOW(), 'admin', NOW());

-- 2. 收支类型字典
INSERT INTO sys_dict (id, dict_name, dict_code, description, del_flag, create_by, create_time, update_by, update_time, type) 
VALUES ('fact_event_type_dict', '收支类型', 'fact_event_type', '收支类型字典', 0, 'admin', NOW(), 'admin', NOW(), 0);

INSERT INTO sys_dict_item (id, dict_id, item_text, item_value, description, sort_order, status, create_by, create_time, update_by, update_time) VALUES
('fact_event_type_1', 'fact_event_type_dict', '收入', 'income', '收入', 1, 1, 'admin', NOW(), 'admin', NOW()),
('fact_event_type_2', 'fact_event_type_dict', '成本', 'cost', '成本', 2, 1, 'admin', NOW(), 'admin', NOW()),
('fact_event_type_3', 'fact_event_type_dict', '行为', 'behavior', '行为记录', 3, 1, 'admin', NOW(), 'admin', NOW());

-- 3. 收支状态字典
INSERT INTO sys_dict (id, dict_name, dict_code, description, del_flag, create_by, create_time, update_by, update_time, type) 
VALUES ('fact_event_status_dict', '收支状态', 'fact_event_status', '收支状态字典', 0, 'admin', NOW(), 'admin', NOW(), 0);

INSERT INTO sys_dict_item (id, dict_id, item_text, item_value, description, sort_order, status, create_by, create_time, update_by, update_time) VALUES
('fact_event_status_1', 'fact_event_status_dict', '有效', 'valid', '有效', 1, 1, 'admin', NOW(), 'admin', NOW()),
('fact_event_status_2', 'fact_event_status_dict', '已冲销', 'reversed', '已冲销', 2, 1, 'admin', NOW(), 'admin', NOW());

-- 4. 应收应付状态字典
INSERT INTO sys_dict (id, dict_name, dict_code, description, del_flag, create_by, create_time, update_by, update_time, type) 
VALUES ('receivable_status_dict', '应收应付状态', 'receivable_status', '应收应付状态字典', 0, 'admin', NOW(), 'admin', NOW(), 0);

INSERT INTO sys_dict_item (id, dict_id, item_text, item_value, description, sort_order, status, create_by, create_time, update_by, update_time) VALUES
('receivable_status_1', 'receivable_status_dict', '未收款', 'unpaid', '未收款', 1, 1, 'admin', NOW(), 'admin', NOW()),
('receivable_status_2', 'receivable_status_dict', '部分收款', 'partial', '部分收款', 2, 1, 'admin', NOW(), 'admin', NOW()),
('receivable_status_3', 'receivable_status_dict', '已收款', 'paid', '已收款', 3, 1, 'admin', NOW(), 'admin', NOW()),
('receivable_status_4', 'receivable_status_dict', '已核销', 'written_off', '已核销', 4, 1, 'admin', NOW(), 'admin', NOW());

-- 5. 期间结账状态字典
INSERT INTO sys_dict (id, dict_name, dict_code, description, del_flag, create_by, create_time, update_by, update_time, type) 
VALUES ('period_status_dict', '期间结账状态', 'period_status', '期间结账状态字典', 0, 'admin', NOW(), 'admin', NOW(), 0);

INSERT INTO sys_dict_item (id, dict_id, item_text, item_value, description, sort_order, status, create_by, create_time, update_by, update_time) VALUES
('period_status_1', 'period_status_dict', '未结账', 'open', '未结账', 1, 1, 'admin', NOW(), 'admin', NOW()),
('period_status_2', 'period_status_dict', '已结账', 'closed', '已结账', 2, 1, 'admin', NOW(), 'admin', NOW()),
('period_status_3', 'period_status_dict', '已反结账', 'reopened', '已反结账', 3, 1, 'admin', NOW(), 'admin', NOW());

-- 6. 会计科目类型字典
INSERT INTO sys_dict (id, dict_name, dict_code, description, del_flag, create_by, create_time, update_by, update_time, type) 
VALUES ('account_type_dict', '会计科目类型', 'account_type', '会计科目类型字典', 0, 'admin', NOW(), 'admin', NOW(), 0);

INSERT INTO sys_dict_item (id, dict_id, item_text, item_value, description, sort_order, status, create_by, create_time, update_by, update_time) VALUES
('account_type_1', 'account_type_dict', '资产', 'asset', '资产类科目', 1, 1, 'admin', NOW(), 'admin', NOW()),
('account_type_2', 'account_type_dict', '负债', 'liability', '负债类科目', 2, 1, 'admin', NOW(), 'admin', NOW()),
('account_type_3', 'account_type_dict', '权益', 'equity', '权益类科目', 3, 1, 'admin', NOW(), 'admin', NOW()),
('account_type_4', 'account_type_dict', '成本', 'cost', '成本类科目', 4, 1, 'admin', NOW(), 'admin', NOW()),
('account_type_5', 'account_type_dict', '损益', 'profit_loss', '损益类科目', 5, 1, 'admin', NOW(), 'admin', NOW());

-- 7. 合同状态字典
INSERT INTO sys_dict (id, dict_name, dict_code, description, del_flag, create_by, create_time, update_by, update_time, type) 
VALUES ('contract_status_dict', '合同状态', 'contract_status', '合同状态字典', 0, 'admin', NOW(), 'admin', NOW(), 0);

INSERT INTO sys_dict_item (id, dict_id, item_text, item_value, description, sort_order, status, create_by, create_time, update_by, update_time) VALUES
('contract_status_1', 'contract_status_dict', '草稿', 'draft', '草稿', 1, 1, 'admin', NOW(), 'admin', NOW()),
('contract_status_2', 'contract_status_dict', '审批中', 'approving', '审批中', 2, 1, 'admin', NOW(), 'admin', NOW()),
('contract_status_3', 'contract_status_dict', '执行中', 'executing', '执行中', 3, 1, 'admin', NOW(), 'admin', NOW()),
('contract_status_4', 'contract_status_dict', '已完成', 'completed', '已完成', 4, 1, 'admin', NOW(), 'admin', NOW()),
('contract_status_5', 'contract_status_dict', '已终止', 'terminated', '已终止', 5, 1, 'admin', NOW(), 'admin', NOW());

-- 8. 项目状态字典
INSERT INTO sys_dict (id, dict_name, dict_code, description, del_flag, create_by, create_time, update_by, update_time, type) 
VALUES ('project_status_dict', '项目状态', 'project_status', '项目状态字典', 0, 'admin', NOW(), 'admin', NOW(), 0);

INSERT INTO sys_dict_item (id, dict_id, item_text, item_value, description, sort_order, status, create_by, create_time, update_by, update_time) VALUES
('project_status_1', 'project_status_dict', '未开始', 'not_started', '未开始', 1, 1, 'admin', NOW(), 'admin', NOW()),
('project_status_2', 'project_status_dict', '进行中', 'in_progress', '进行中', 2, 1, 'admin', NOW(), 'admin', NOW()),
('project_status_3', 'project_status_dict', '已暂停', 'paused', '已暂停', 3, 1, 'admin', NOW(), 'admin', NOW()),
('project_status_4', 'project_status_dict', '已完成', 'completed', '已完成', 4, 1, 'admin', NOW(), 'admin', NOW()),
('project_status_5', 'project_status_dict', '已取消', 'cancelled', '已取消', 5, 1, 'admin', NOW(), 'admin', NOW());

-- 9. 预算状态字典
INSERT INTO sys_dict (id, dict_name, dict_code, description, del_flag, create_by, create_time, update_by, update_time, type) 
VALUES ('budget_status_dict', '预算状态', 'budget_status', '预算状态字典', 0, 'admin', NOW(), 'admin', NOW(), 0);

INSERT INTO sys_dict_item (id, dict_id, item_text, item_value, description, sort_order, status, create_by, create_time, update_by, update_time) VALUES
('budget_status_1', 'budget_status_dict', '草稿', 'draft', '草稿', 1, 1, 'admin', NOW(), 'admin', NOW()),
('budget_status_2', 'budget_status_dict', '审批中', 'approving', '审批中', 2, 1, 'admin', NOW(), 'admin', NOW()),
('budget_status_3', 'budget_status_dict', '已生效', 'active', '已生效', 3, 1, 'admin', NOW(), 'admin', NOW()),
('budget_status_4', 'budget_status_dict', '已失效', 'inactive', '已失效', 4, 1, 'admin', NOW(), 'admin', NOW());

-- 10. 通用状态字典（启用/禁用）
INSERT INTO sys_dict (id, dict_name, dict_code, description, del_flag, create_by, create_time, update_by, update_time, type) 
VALUES ('common_status_dict', '通用状态', 'common_status', '通用状态字典（启用/禁用）', 0, 'admin', NOW(), 'admin', NOW(), 0);

INSERT INTO sys_dict_item (id, dict_id, item_text, item_value, description, sort_order, status, create_by, create_time, update_by, update_time) VALUES
('common_status_1', 'common_status_dict', '启用', '1', '启用', 1, 1, 'admin', NOW(), 'admin', NOW()),
('common_status_2', 'common_status_dict', '禁用', '0', '禁用', 2, 1, 'admin', NOW(), 'admin', NOW());
