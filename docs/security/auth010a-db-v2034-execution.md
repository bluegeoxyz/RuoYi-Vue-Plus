# AUTH-010A-DB V2.0.34 执行记录

## 环境与备份

- 执行时间：2026-10-09 21:32（Asia/Shanghai）
- WSL：`archlinux`
- Docker：`jdpm-mysql`（容器 ID `a3fe2a44b257`）
- MySQL：8.0.46
- 数据库：`zbm-data`
- 数据库用户：`root@localhost`

备份目录：

`/home/neo/space/docker/jdpm/data/backups/auth010a-v2034-20261009-2132`

三表备份文件：`zbm-data-v2034-role-menu.sql`

SHA-256：`293187a39ca3dd172e7461c8f2d4d16ced446f64b17549b5f5e3bfb74b34293c`

目录同时保存迁移前后目标角色、非目标角色、用户角色和项目成员快照，以及 preflight、execution、postflight 日志和 `SHA256SUMS`。

## 迁移前检查

- 三个 AUTH-012 权限存在且各一条。
- ChargeBinding 是收费管理下独立 C 菜单，路径 `binding`，组件 `property/charge/binding/index`。
- 七个目标 role_key 均存在且唯一。
- `sys_role_menu` 无重复关联。
- V2.0.34 失败关闭 guard 全部通过。
- `missing_or_disabled_permission` 为零。
- 没有新的未知人工授权。

迁移前权限数量：客服 12、运营主管 48、收费员 8、财务主管 29、仓管 26、仓库主管 72、项目经理 48。

待移除 35 条菜单关联：收费员 2、客服 7、财务主管 2、运营主管 11、项目经理 6、仓管 7。待新增 7 条：运营主管 1、项目经理 6。完整明细保存在 `V2.0.34-preflight.log`。

## 执行结果

已执行 `script/sql/zbm/migration/V2.0.34__converge_property_business_role_permissions.sql`，MySQL 返回成功。脚本持久化部分只在事务内删除和插入目标七角色的 `sys_role_menu`；临时表 DDL 位于事务外，但不修改持久化业务表。

迁移后再次运行写入前置段，新增、移除、缺失和未知权限结果均为空。

## 最终权限集合

### 物业客服：6

`property:customerRelation:add`, `property:customerRelation:edit`, `property:customerRelation:list`, `property:customerRelation:moveIn`, `property:customerRelation:moveOut`, `property:customerRelation:query`

### 物业运营主管：38

`property:building:add`, `property:building:edit`, `property:building:export`, `property:building:list`, `property:building:query`, `property:building:remove`, `property:customer:add`, `property:customer:edit`, `property:customer:export`, `property:customer:list`, `property:customer:query`, `property:customer:remove`, `property:customerRelation:add`, `property:customerRelation:edit`, `property:customerRelation:export`, `property:customerRelation:list`, `property:customerRelation:moveIn`, `property:customerRelation:moveOut`, `property:customerRelation:query`, `property:customerRelation:remove`, `property:project:list`, `property:project:query`, `property:resident:export`, `property:resident:list`, `property:resident:query`, `property:resource:treeImport`, `property:room:add`, `property:room:edit`, `property:room:export`, `property:room:list`, `property:room:query`, `property:room:remove`, `property:unit:add`, `property:unit:edit`, `property:unit:export`, `property:unit:list`, `property:unit:query`, `property:unit:remove`

### 收费员：6

`property:payment:add`, `property:payment:allocate`, `property:payment:list`, `property:payment:query`, `property:receivable:list`, `property:receivable:query`

### 财务主管：28

`property:chargeBinding:add`, `property:chargeBinding:edit`, `property:chargeBinding:list`, `property:chargeBinding:query`, `property:chargeBinding:remove`, `property:chargeItem:add`, `property:chargeItem:edit`, `property:chargeItem:export`, `property:chargeItem:list`, `property:chargeItem:query`, `property:chargeItem:remove`, `property:chargeRule:add`, `property:chargeRule:edit`, `property:chargeRule:export`, `property:chargeRule:list`, `property:chargeRule:query`, `property:chargeRule:remove`, `property:payment:add`, `property:payment:allocate`, `property:payment:export`, `property:payment:list`, `property:payment:query`, `property:payment:reverse`, `property:receivable:cancel`, `property:receivable:export`, `property:receivable:generate`, `property:receivable:list`, `property:receivable:query`

### 仓库管理员：20

`warehouse:inbound:add`, `warehouse:inbound:cancel`, `warehouse:inbound:edit`, `warehouse:inbound:export`, `warehouse:inbound:list`, `warehouse:inbound:query`, `warehouse:inbound:remove`, `warehouse:inventory:export`, `warehouse:inventory:list`, `warehouse:inventory:query`, `warehouse:inventoryTransaction:export`, `warehouse:inventoryTransaction:list`, `warehouse:inventoryTransaction:query`, `warehouse:outbound:add`, `warehouse:outbound:cancel`, `warehouse:outbound:edit`, `warehouse:outbound:export`, `warehouse:outbound:list`, `warehouse:outbound:query`, `warehouse:outbound:remove`

### 仓库主管：72

`warehouse:inbound:add`, `warehouse:inbound:cancel`, `warehouse:inbound:complete`, `warehouse:inbound:confirm`, `warehouse:inbound:edit`, `warehouse:inbound:export`, `warehouse:inbound:list`, `warehouse:inbound:query`, `warehouse:inbound:remove`, `warehouse:inventory:export`, `warehouse:inventory:list`, `warehouse:inventory:query`, `warehouse:inventoryTransaction:export`, `warehouse:inventoryTransaction:list`, `warehouse:inventoryTransaction:query`, `warehouse:location:add`, `warehouse:location:edit`, `warehouse:location:list`, `warehouse:location:query`, `warehouse:location:remove`, `warehouse:material:add`, `warehouse:material:edit`, `warehouse:material:list`, `warehouse:material:query`, `warehouse:material:remove`, `warehouse:materialCategory:add`, `warehouse:materialCategory:edit`, `warehouse:materialCategory:list`, `warehouse:materialCategory:query`, `warehouse:materialCategory:remove`, `warehouse:outbound:add`, `warehouse:outbound:cancel`, `warehouse:outbound:complete`, `warehouse:outbound:confirm`, `warehouse:outbound:edit`, `warehouse:outbound:export`, `warehouse:outbound:list`, `warehouse:outbound:query`, `warehouse:outbound:remove`, `warehouse:stocktake:add`, `warehouse:stocktake:cancel`, `warehouse:stocktake:complete`, `warehouse:stocktake:confirm`, `warehouse:stocktake:edit`, `warehouse:stocktake:export`, `warehouse:stocktake:list`, `warehouse:stocktake:query`, `warehouse:stocktake:remove`, `warehouse:transfer:add`, `warehouse:transfer:cancel`, `warehouse:transfer:complete`, `warehouse:transfer:confirm`, `warehouse:transfer:edit`, `warehouse:transfer:export`, `warehouse:transfer:list`, `warehouse:transfer:query`, `warehouse:transfer:remove`, `warehouse:unit:add`, `warehouse:unit:edit`, `warehouse:unit:list`, `warehouse:unit:query`, `warehouse:unit:remove`, `warehouse:warehouse:add`, `warehouse:warehouse:edit`, `warehouse:warehouse:list`, `warehouse:warehouse:query`, `warehouse:warehouse:remove`, `warehouse:zone:add`, `warehouse:zone:edit`, `warehouse:zone:list`, `warehouse:zone:query`, `warehouse:zone:remove`

### 物业项目经理：48

`property:building:add`, `property:building:edit`, `property:building:export`, `property:building:list`, `property:building:query`, `property:building:remove`, `property:customer:add`, `property:customer:edit`, `property:customer:export`, `property:customer:list`, `property:customer:query`, `property:customer:remove`, `property:customerRelation:add`, `property:customerRelation:edit`, `property:customerRelation:export`, `property:customerRelation:list`, `property:customerRelation:moveIn`, `property:customerRelation:moveOut`, `property:customerRelation:query`, `property:customerRelation:remove`, `property:payment:list`, `property:payment:query`, `property:project:list`, `property:project:query`, `property:projectMember:add`, `property:projectMember:edit`, `property:projectMember:export`, `property:projectMember:list`, `property:projectMember:query`, `property:projectMember:remove`, `property:receivable:list`, `property:receivable:query`, `property:resident:export`, `property:resident:list`, `property:resident:query`, `property:resource:treeImport`, `property:room:add`, `property:room:edit`, `property:room:export`, `property:room:list`, `property:room:query`, `property:room:remove`, `property:unit:add`, `property:unit:edit`, `property:unit:export`, `property:unit:list`, `property:unit:query`, `property:unit:remove`

## 保护项核对

- 非目标角色菜单快照迁移前后 SHA-256：`4b16d8e807bfa34ca59aa1443a5f6c3541ed81e06c6fa66f0b85e81a91ec6a34`。
- 七个 `sys_role` 完整行迁移前后 SHA-256：`da390c2b0715e4256fd2f6bc793e1e423f5429a5bfa41fa0fe89c4c07fb6d0f3`，因此 data_scope、status 等未变化。
- `sys_user_role` 迁移前后 SHA-256：`dd4132d41f32a80f933b831f741fe6311fc98e9f0cf27b83b59b13f5274319b1`。
- `pm_project_member` 迁移前后 SHA-256：`f6c0e775f9d52ca1a9a3258513c6408b38aebc7ac00b66be430ac60128aae61d`。
- 非目标角色包含 SuperAdmin；其关联快照未变化。SuperAdmin 仍使用 role_key `superadmin`、data_scope `1`，没有显式 `sys_role_menu` 记录。
- 七角色没有 `system:*` 或 `monitor:*`。
- 全库没有重复 `sys_role_menu(role_id, menu_id)`。
- 七角色所有已授权叶子菜单的祖先节点均已授权，缺失祖先数量为零。
- 财务主管 `property:room*` 数量为零，ChargeBinding 权限数量为 5，且收费管理到独立收费设置菜单链路完整。
- 客服房屋管理权限为零；收费员收费配置、冲销/作废权限为零；仓管基础资料与 confirm/complete 权限为零。

本任务没有创建员工、修改项目成员、修改用户角色、修改角色数据范围、修改菜单定义或执行 AUTH-010B。
