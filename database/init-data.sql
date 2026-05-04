-- 初始化组织数据
INSERT INTO org_unit (id, name, type, parent_id) VALUES
(1, '公司总部', 'company', NULL),
(2, '销售部', 'dept', 1),
(3, '研发部', 'dept', 1),
(4, '阿米巴A组', 'amb', 2),
(5, '阿米巴B组', 'amb', 2),
(6, '项目X', 'project', 3);

-- 初始化用户
INSERT INTO user (id, username, password, role, org_unit_id) VALUES
(1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'admin', 1),
(2, 'manager', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'manager', 2);

-- 初始化归因规则
INSERT INTO attribution_rule (id, rule_type, `condition`, strategy, priority, enabled) VALUES
(1, 'map', '{"type": "income", "org_unit_id": 2}', '{"target": "direct"}', 10, 1),
(2, 'split', '{"type": "cost", "category": "shared"}', '{"method": "equal", "targets": [4, 5]}', 5, 1);

-- 初始化目标
INSERT INTO goal (org_unit_id, period, target_profit, target_cost, target_roi) VALUES
(1, '2026-05', 50000.00, 30000.00, 1.6667),
(2, '2026-05', 25000.00, 15000.00, 1.6667);
