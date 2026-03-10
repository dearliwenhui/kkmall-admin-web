-- KKMall database initialization

-- =========================
-- Security / auth tables
-- =========================
CREATE TABLE IF NOT EXISTS `sys_user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `username` VARCHAR(50) NOT NULL,
  `password` VARCHAR(100) NOT NULL,
  `nickname` VARCHAR(50) DEFAULT NULL,
  `avatar` VARCHAR(255) DEFAULT NULL,
  `email` VARCHAR(100) DEFAULT NULL,
  `phone` VARCHAR(20) DEFAULT NULL,
  `status` TINYINT DEFAULT 1,
  `deleted` TINYINT DEFAULT 0,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `sys_role` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `role_name` VARCHAR(50) NOT NULL,
  `role_code` VARCHAR(50) NOT NULL,
  `description` VARCHAR(200) DEFAULT NULL,
  `deleted` TINYINT DEFAULT 0,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `sys_permission` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `permission_name` VARCHAR(50) NOT NULL,
  `permission_code` VARCHAR(100) NOT NULL,
  `resource_type` TINYINT DEFAULT NULL,
  `path` VARCHAR(200) DEFAULT NULL,
  `description` VARCHAR(200) DEFAULT NULL,
  `deleted` TINYINT DEFAULT 0,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_permission_code` (`permission_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `sys_user_role` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `role_id` BIGINT NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `sys_role_permission` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `role_id` BIGINT NOT NULL,
  `permission_id` BIGINT NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_role_id` (`role_id`),
  KEY `idx_permission_id` (`permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =========================
-- Product table
-- =========================
CREATE TABLE IF NOT EXISTS `product` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `product_name` VARCHAR(100) NOT NULL,
  `product_code` VARCHAR(64) NOT NULL,
  `category_id` BIGINT NOT NULL,
  `price` DECIMAL(10,2) NOT NULL,
  `stock` INT NOT NULL DEFAULT 0,
  `description` LONGTEXT DEFAULT NULL,
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '1:on-sale,0:off-sale',
  `images` TEXT DEFAULT NULL COMMENT 'comma-separated image URLs',
  `deleted` TINYINT NOT NULL DEFAULT 0,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_product_code` (`product_code`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Keep schema compatible for existing deployments that already have product table
ALTER TABLE `product` MODIFY COLUMN `description` LONGTEXT NULL;
ALTER TABLE `product` MODIFY COLUMN `images` TEXT NULL;

-- =========================
-- Seed data
-- =========================
-- admin / admin123
INSERT INTO `sys_user` (`id`, `username`, `password`, `nickname`, `status`)
VALUES (1, 'admin', '$2a$10$C1ufEdlidFsbHkdmu2Ppm.HyDJvzvPw6yZzP2/blqEPnkBfelyMMa', 'System Admin', 1)
ON DUPLICATE KEY UPDATE `username` = VALUES(`username`);

INSERT INTO `sys_role` (`id`, `role_name`, `role_code`, `description`)
VALUES
  (1, 'Administrator', 'ADMIN', 'Full access'),
  (2, 'User', 'USER', 'Normal user')
ON DUPLICATE KEY UPDATE `role_code` = VALUES(`role_code`);

INSERT INTO `sys_permission` (`permission_name`, `permission_code`, `resource_type`, `path`)
VALUES
  ('Product Manage', 'product:manage', 1, '/api/products/**'),
  ('Order Manage', 'order:manage', 1, '/api/orders/**'),
  ('User Manage', 'user:manage', 1, '/api/users/**'),
  ('Product Add', 'product:add', 2, '/api/products'),
  ('Product Delete', 'product:delete', 2, '/api/products/*'),
  ('Product Edit', 'product:edit', 2, '/api/products/*'),
  ('User Add', 'user:add', 2, '/api/users'),
  ('User Edit', 'user:edit', 2, '/api/users/*'),
  ('User Delete', 'user:delete', 2, '/api/users/*'),
  ('Order Deliver', 'order:deliver', 2, '/api/orders/*/deliver'),
  ('Role Manage', 'role:manage', 1, '/api/roles/**'),
  ('Role Add', 'role:add', 2, '/api/roles'),
  ('Role Edit', 'role:edit', 2, '/api/roles/*'),
  ('Role Delete', 'role:delete', 2, '/api/roles/*'),
  ('Permission Manage', 'permission:manage', 1, '/api/permissions/**'),
  ('Permission Add', 'permission:add', 2, '/api/permissions'),
  ('Permission Edit', 'permission:edit', 2, '/api/permissions/*'),
  ('Permission Delete', 'permission:delete', 2, '/api/permissions/*')
ON DUPLICATE KEY UPDATE `permission_code` = VALUES(`permission_code`);

INSERT INTO `sys_user_role` (`user_id`, `role_id`)
VALUES (1, 1)
ON DUPLICATE KEY UPDATE `user_id` = VALUES(`user_id`);

INSERT INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT 1, p.id
FROM `sys_permission` p
ON DUPLICATE KEY UPDATE `role_id` = VALUES(`role_id`);

INSERT INTO `product` (`id`, `product_name`, `product_code`, `category_id`, `price`, `stock`, `description`, `status`, `images`)
VALUES
  (1, 'Demo Product A', 'P-1001', 1, 99.90, 120, 'Demo product for initialization', 1, NULL),
  (2, 'Demo Product B', 'P-1002', 1, 199.00, 60, 'Second demo product', 0, NULL)
ON DUPLICATE KEY UPDATE `product_code` = VALUES(`product_code`);
