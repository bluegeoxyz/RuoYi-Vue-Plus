# AUTH-011 房屋收费设置独立页面

## 审计结论

`ChargeBindingController`、`ChargeBindingServiceImpl` 及已有 `/property/charge/binding/list` 均只使用 `property:chargeBinding:*`，并通过 `PropertyProjectScopeService` 的 `FINANCE` 域校验项目范围、房屋真实归属及收费项目/规则归属。后端 CRUD 不依赖 `property:room:list`。

实际耦合位于菜单和前端入口：收费设置弹窗嵌入房屋管理页面，且 `chargeBinding:*` 按钮菜单均挂在房屋管理菜单下。用户必须先取得房屋管理菜单的 `property:room:list` 才能进入该页面，因此开发库财务主管出现了目标矩阵之外的 `property:room:list` 人工授权。

## 新旧入口与权限

| 用途 | 旧入口/接口 | 新入口/接口 | 权限 |
|---|---|---|---|
| 页面入口 | 房屋管理页面内收费设置弹窗 | 收费管理 / 房屋收费设置，`property/charge/binding/index` | `property:chargeBinding:list` |
| 项目筛选 | 房屋管理项目上下文 | `/property/charge/binding/context/list/projects` | `property:chargeBinding:list` |
| 楼栋筛选 | 房屋管理 Options | `/property/charge/binding/context/list/buildings` | `property:chargeBinding:list` |
| 单元筛选 | 房屋管理 Options | `/property/charge/binding/context/list/units` | `property:chargeBinding:list` |
| 房屋筛选 | 房屋管理 Options | `/property/charge/binding/context/list/rooms` | `property:chargeBinding:list` |
| 收费设置列表 | `/property/charge/binding/list` | 保持不变 | `property:chargeBinding:list` |
| 详情 | `/property/charge/binding/{id}` | 保持不变 | `property:chargeBinding:query` |
| 新增候选项 | `/context/create/items`、`/rules` | 保持不变 | `property:chargeBinding:add` |
| 编辑候选项 | `/context/edit/items`、`/rules` | 保持不变 | `property:chargeBinding:edit` |

新的只读筛选接口固定使用 `BusinessContext.CHARGE_BINDING_QUERY`，策略仅接受 `property:chargeBinding:list`，引用类型限定为 PROJECT、BUILDING、UNIT、ROOM。它只负责业务能力授权，实际数据仍按 FINANCE 项目范围和楼栋/单元/房屋真实父子关系过滤。`list` 不继承 `add` 或 `edit`，管理类 `property:room:list` 也不能调用该上下文。

房屋管理页面中的原入口暂时保留作为兼容快捷入口，但独立收费设置页面及其完整功能不再依赖房屋管理菜单。收费项目、计费规则、应收生成和计费事务逻辑均未修改。

## SQL 与执行顺序

增量脚本为 `script/sql/zbm/migration/V2.0.35__charge_binding_independent_menu.sql`。脚本只把既有 5 个 `chargeBinding` 菜单/按钮节点迁移到收费管理目录并设置独立组件，不修改任何角色关联。

AUTH-012 已新增并在开发库执行 `V2.0.33.1__menu_permission_preconditions.sql`，以安全补齐 V2.0.11、V2.0.12、V2.0.14 对应的三个菜单缺口并提前完成独立菜单迁移。标准顺序为：

1. V2.0.33 后执行 V2.0.33.1。
2. 重新运行 AUTH-010A-DB 前置差异检查。
3. 执行 V2.0.34，让其按目标矩阵移除财务主管的 `property:room:list`。
4. V2.0.35 作为兼容的幂等后续 migration 保留。

V2.0.34 的目标权限矩阵未改变；仅将已知数据库漂移 `property_finance_manager / property:room:list` 登记为可解释的待移除权限，失败关闭规则仍然保留。

## 验证结果

- 后端 Maven reactor 编译成功。
- `BusinessContextAccessServiceTest`：12 项通过。
- `ChargeBindingServiceSecurityTest`、收费上下文与应收生成安全测试：21 项通过。
- `PermissionContractAuditTest` 与 `PropertyBusinessRolePermissionTest`：12 项通过。
- 最新权限契约为 265 个 Controller 接口、66 个业务 Options；本任务新增 4 个固定上下文接口，全部复用既有 `property:chargeBinding:list`。
- 前端 `pnpm run build:prod` 通过，产物包含独立 `binding` 页面。
- 未执行 V2.0.35 或 V2.0.34，未修改数据库角色授权；因此财务主管真实账号登录验收需在测试库按上述顺序应用菜单脚本后完成。

