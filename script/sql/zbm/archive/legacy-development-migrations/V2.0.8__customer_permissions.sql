-- TASK-012 B1-2 permissions. Baseline and published migrations remain immutable.
-- Customer grant has no UI in B1-2; permissions are attached to the existing Customer node.
INSERT INTO `sys_menu`
(`menu_id`,`parent_id`,`menu_name`,`order_num`,`path`,`component`,`is_frame`,`is_cache`,
 `menu_type`,`visible`,`status`,`perms`,`icon`,`create_by`,`create_time`,`remark`)
VALUES
(2609252000000001606,2609252000000000401,'敏感资料查看',6,'',NULL,'N','N','F','0','0','property:customer:pii:view','#',1,NOW(),'完整客户敏感字段查看'),
(2609252000000001607,2609252000000000401,'敏感资料导出',7,'',NULL,'N','N','F','0','0','property:customer:pii:export','#',1,NOW(),'客户敏感字段导出'),
(2609252000000001611,2609252000000000401,'客户授权列表',11,'',NULL,'N','N','F','0','0','property:customerGrant:list','#',1,NOW(),'企业客户主档授权列表'),
(2609252000000001612,2609252000000000401,'客户授权查询',12,'',NULL,'N','N','F','0','0','property:customerGrant:query','#',1,NOW(),'企业客户主档授权历史'),
(2609252000000001613,2609252000000000401,'客户授权新增',13,'',NULL,'N','N','F','0','0','property:customerGrant:add','#',1,NOW(),'企业客户主档授权新增'),
(2609252000000001614,2609252000000000401,'客户授权修改',14,'',NULL,'N','N','F','0','0','property:customerGrant:edit','#',1,NOW(),'企业客户主档授权启停'),
(2609252000000001615,2609252000000000401,'客户授权撤销',15,'',NULL,'N','N','F','0','0','property:customerGrant:remove','#',1,NOW(),'企业客户主档授权撤销')
ON DUPLICATE KEY UPDATE
`menu_name`=VALUES(`menu_name`),`perms`=VALUES(`perms`),`remark`=VALUES(`remark`);
