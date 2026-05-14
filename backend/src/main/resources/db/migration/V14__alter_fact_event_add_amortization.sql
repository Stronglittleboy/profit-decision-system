ALTER TABLE `fact_event`
  ADD COLUMN `amortize_start` DATE          DEFAULT NULL COMMENT '分摊起始月(含)' AFTER `cost_category`,
  ADD COLUMN `amortize_end`   DATE          DEFAULT NULL COMMENT '分摊截止月(含)' AFTER `amortize_start`,
  ADD COLUMN `amortize_method` VARCHAR(20)  DEFAULT NULL COMMENT '分摊方式: linear' AFTER `amortize_end`;
