# AUTH-010A 物业业务角色最小权限收敛

## 审计结论

`V2.0.32__property_business_roles.sql` 按当时尚未解耦的页面依赖授予了基础资料权限。AUTH-004A 至 AUTH-006 完成后，下列授权已不再是普通业务角色的必要条件：

| 角色 | 旧基线多余授权 | 收敛依据 |
|---|---|---|
| 物业客服 | `resident:list/query`、`room:list/query`、`customer:list/query` | 入住页面已使用 `CUSTOMER_RELATION_*` 固定上下文接口；不再借用房屋、客户管理 Options |
| 物业收费员 | `receivable:export`、`payment:export` | 收费员最小职责为授权项目应收查询及收款/核销；导出不是完成流程的必要能力 |
| 仓库管理员 | `materialCategory:list/query`、`unit:list/query`、`material:list/query` | 入库、出库业务已使用固定仓库单据上下文；不再依赖主数据管理查询权限 |
| 物业运营主管 | 项目成员维护、房屋收费设置 | 项目成员授权交给项目经理；收费设置归财务主管 |
| 物业项目经理 | 房屋收费设置、应收/收款导出 | 项目经理保留综合运营和财务只读，不维护财务配置，也不批量导出财务数据 |

仓库主管和财务主管的旧权限集合与当前实现职责基本一致，但新 migration 将通配式授权改为显式权限清单，避免未来新增权限被自动继承。

当前机器没有 MySQL/MariaDB 客户端，3306 也没有监听服务，无法读取 `sys_role`、`sys_menu`、`sys_role_menu` 的真实数据。因此 migration 尚未应用。SQL 在变更前会输出 `permission_to_remove`、`permission_to_add` 和缺失菜单三份差异，生产执行前必须先审阅这些结果。

## 最终矩阵

| 角色 | 权限数 | 最终业务能力 |
|---|---:|---|
| `property_customer_service` | 6 | 客户关系 list/query/add/edit/moveIn/moveOut；引用数据由入住 Context 提供 |
| `property_operation_manager` | 38 | 授权项目 project 只读；楼栋、单元、房屋、客户、客户关系和住户日常运营 |
| `property_cashier` | 6 | 应收 list/query；收款 list/query/add/allocate |
| `property_finance_manager` | 28 | 收费项目、计费规则、房屋收费设置、应收、收款全部已实现财务能力，含独立 cancel/reverse |
| `property_warehouse_keeper` | 20 | 库存及流水查询；入库/出库 list/query/add/edit/remove/cancel/export，不含 confirm/complete |
| `property_warehouse_manager` | 72 | 当前实际存在的仓库、库区、库位、物料主数据、库存及四类单据全部能力 |
| `property_project_manager` | 48 | 项目 list/query、项目成员维护、资源和住户运营、应收与收款只读 |

所有角色均排除 `system:*`、`monitor:*`。项目经理排除 `property:project:add/edit/remove` 和 `property:projectAdminScope:*`；收费员排除收费配置、应收作废和收款冲销；仓管排除基础资料 CRUD、调拨/盘点以及单据 confirm/complete。

## SQL 行为

增量 migration：`script/sql/zbm/migration/V2.0.34__converge_property_business_role_permissions.sql`

- 不创建或更新角色，不修改 `data_scope`。
- 仅匹配七个精确 `role_key`。
- 先按实际 `sys_menu.perms` 解析叶子菜单和导航祖先。
- 以 V2.0.32 已知授权加最终矩阵建立漂移白名单；发现目标角色存在仓库基线之外的人工授权时，在任何写入前以失败关闭方式终止，要求先审阅差异。
- 删除目标角色中不属于最终矩阵的菜单关系，再幂等补齐缺失关系。
- 不使用固定角色 ID 或菜单 ID。
- 不使用临时表 UNION，避免 MySQL 1137 `Can't reopen table`。
- 不影响 SuperAdmin、其他系统角色或非目标角色。
- 变更后检查角色缺失/重复、授权缺失/多余、系统权限及重复关系。

角色权限只表达业务能力。`sys_user -> hr_employee -> pm_project_member`、`pm_project_management_grant`、`PropertyProjectScopeService` 和 `WarehouseAccessService` 均保持不变。

## 验证结果

- 相关后端模块编译：通过。
- 权限上下文、仓库范围、收费安全与业务回归、角色矩阵、权限契约共 99 项：全部通过，0 失败、0 错误、0 跳过。
- `PermissionContractAuditTest`：6 项通过。
- `PropertyBusinessRolePermissionTest`：6 项通过。
- 最新契约：261 个 Controller 接口、62 个业务 Options、22 个仍保留的旧管理 Options 耦合基线。
- 前端 `pnpm run build:prod`：通过，3438 个模块完成转换。
- MySQL 集成测试及 migration 实际应用：未执行，原因是本机无可用数据库服务。

## AUTH-010B 建议

后续测试员工初始化应使用独立测试 migration 或可回滚测试夹具：每个标准角色至少创建一个员工/用户，再创建一个多角色用户。项目角色通过 `pm_project_member` 授予单个测试项目；仓库角色分别准备项目仓与公司仓范围。测试数据必须带明确的测试前缀，并提供按精确 ID 回滚脚本，不要通过扩大 `data_scope` 或授予 SuperAdmin 完成验收。
