-- 修复权限系统
-- 执行日期: 2026-03-10

-- 1. 添加 description 字段
ALTER TABLE `sys_permission` ADD COLUMN `description` VARCHAR(200) DEFAULT NULL AFTER `path`;

-- 2. 清理并重新插入权限数据
DELETE FROM `sys_role_permission`;
DELETE FROM `sys_permission`;

-- 3. 插入权限数据
INSERT INTO `sys_permission` (`id`, `permission_name`, `permission_code`, `resource_type`, `path`, `description`) VALUES
(1, 'Product Manage', 'product:manage', 1, '/api/products/**', '商品管理权限'),
(2, 'Order Manage', 'order:manage', 1, '/api/orders/**', '订单管理权限'),
(3, 'User Manage', 'user:manage', 1, '/api/users/**', '用户管理权限'),
(4, 'Product Add', 'product:add', 2, '/api/products', '添加商品'),
(5, 'Product Delete', 'product:delete', 2, '/api/products/*', '删除商品'),
(6, 'Product Edit', 'product:edit', 2, '/api/products/*', '编辑商品'),
(7, 'User Add', 'user:add', 2, '/api/users', '添加用户'),
(8, 'User Edit', 'user:edit', 2, '/api/users/*', '编辑用户'),
(9, 'User Delete', 'user:delete', 2, '/api/users/*', '删除用户'),
(10, 'Order Deliver', 'order:deliver', 2, '/api/orders/*/deliver', '订单发货'),
(11, 'Role Manage', 'role:manage', 1, '/api/roles/**', '角色管理权限'),
(12, 'Role Add', 'role:add', 2, '/api/roles', '添加角色'),
(13, 'Role Edit', 'role:edit', 2, '/api/roles/*', '编辑角色'),
(14, 'Role Delete', 'role:delete', 2, '/api/roles/*', '删除角色'),
(15, 'Permission Manage', 'permission:manage', 1, '/api/permissions/**', '权限管理权限'),
(16, 'Permission Add', 'permission:add', 2, '/api/permissions', '添加权限'),
(17, 'Permission Edit', 'permission:edit', 2, '/api/permissions/*', '编辑权限'),
(18, 'Permission Delete', 'permission:delete', 2, '/api/permissions/*', '删除权限');

-- 4. 为管理员角色分配所有权限
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT 1, id FROM `sys_permission`;

-- 5. 验证数据
SELECT '=== 权限数据 ===' AS info;
SELECT * FROM `sys_permission`;

SELECT '=== 角色权限关联 ===' AS info;
SELECT * FROM `sys_role_permission`;

SELECT '=== 用户权限查询 ===' AS info;
SELECT p.*
FROM sys_permission p
WHERE p.deleted = 0
  AND p.id IN (
    SELECT permission_id
    FROM sys_role_permission
    WHERE role_id IN (
      SELECT role_id
      FROM sys_user_role
      WHERE user_id = 1
    )
  );