CREATE TABLE IF NOT EXISTS `mall_refund_audit_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `refund_id` BIGINT NOT NULL COMMENT '退款ID',
  `refund_no` VARCHAR(64) NOT NULL COMMENT '退款单号',
  `action_code` VARCHAR(32) NOT NULL COMMENT '动作编码',
  `operator_type` VARCHAR(16) NOT NULL COMMENT '操作人类型(USER/ADMIN/SYSTEM)',
  `operator_id` BIGINT DEFAULT NULL COMMENT '操作人ID',
  `operator_name` VARCHAR(100) DEFAULT NULL COMMENT '操作人名称',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_refund_id` (`refund_id`),
  KEY `idx_refund_no` (`refund_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='退款审计日志表';
