CREATE TABLE `pm_project_management_grant_audit` (
  `id` BIGINT NOT NULL COMMENT '审计ID',
  `grant_id` BIGINT DEFAULT NULL COMMENT '授权ID，可为空以记录失败操作',
  `operator_id` BIGINT NOT NULL COMMENT '操作人',
  `action` VARCHAR(16) NOT NULL COMMENT 'GRANT/DISABLE/ENABLE/REVOKE',
  `principal_type` VARCHAR(16) NOT NULL, `principal_id` BIGINT NOT NULL,
  `scope_type` VARCHAR(16) NOT NULL, `project_id` BIGINT NOT NULL DEFAULT 0,
  `before_status` CHAR(1) DEFAULT NULL, `after_status` CHAR(1) DEFAULT NULL,
  `result` CHAR(1) NOT NULL COMMENT '0成功 1失败', `trace_id` VARCHAR(64) DEFAULT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, `remark` VARCHAR(500) DEFAULT NULL,
  PRIMARY KEY (`id`), KEY `idx_pm_project_grant_audit_grant` (`grant_id`,`create_time`),
  KEY `idx_pm_project_grant_audit_principal` (`principal_type`,`principal_id`,`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目管理授权安全审计';

-- V2.0.0 baseline is immutable: all new navigation/permissions are versioned here.
INSERT INTO `sys_menu` (`menu_id`,`parent_id`,`menu_name`,`order_num`,`path`,`component`,`is_frame`,`is_cache`,`menu_type`,`visible`,`status`,`perms`,`icon`,`create_by`,`create_time`,`remark`) VALUES
(2609252000000000203,2609252000000000002,'项目管理授权',30,'admin-scope','property/project/admin-scope/index','N','Y','C','0','0','property:projectAdminScope:list','lock',1,NOW(),'企业项目管理范围授权'),
(2609252000000001206,2609252000000000203,'授权查询',1,'',NULL,'N','N','F','0','0','property:projectAdminScope:query','#',1,NOW(),NULL),
(2609252000000001207,2609252000000000203,'授权新增',2,'',NULL,'N','N','F','0','0','property:projectAdminScope:add','#',1,NOW(),NULL),
(2609252000000001208,2609252000000000203,'授权修改',3,'',NULL,'N','N','F','0','0','property:projectAdminScope:edit','#',1,NOW(),NULL),
(2609252000000001209,2609252000000000203,'授权撤销',4,'',NULL,'N','N','F','0','0','property:projectAdminScope:remove','#',1,NOW(),NULL)
ON DUPLICATE KEY UPDATE `menu_name`=VALUES(`menu_name`),`component`=VALUES(`component`),`perms`=VALUES(`perms`),`remark`=VALUES(`remark`);
