# V1.0 角色、菜单与权限管理功能文档

## 文档元数据

| 项目 | 内容 |
|---|---|
| 功能名称 | 角色、菜单与统一权限管理 |
| 所属菜单 | 系统管理 / 角色管理、菜单管理、用户管理 |
| 适用岗位 | 系统管理员；敏感权限委派仅 SuperAdmin |
| 当前实现状态 | 已实现 |
| 文档状态 | 已核对 |
| 事实基线 | 2026-10-13 当前工作树与 TASK-V1-001～002C 报告 |

## 1. 功能概述

权限管理以 Sa-Token 登录身份和用户—角色—菜单关系为功能授权基础，并叠加角色委派保护、物业项目范围、BusinessContext 引用数据策略、仓库资源范围和客户敏感数据权限。前端 `v-hasPermi` 只控制可见性，不能代替后端授权。

## 2. 功能清单

| 功能 | 实现 | 权限 |
|---|---|---|
| 角色 CRUD、启停、导出 | `/system/role` | `system:role:list/query/add/edit/remove/export` |
| 角色菜单授权 | 角色新增/编辑中的 `menuIds` | `system:role:add/edit` + 敏感委派保护 |
| 角色数据权限 | `PUT /system/role/permission` | `system:role:edit` + SuperAdmin 保护条件 |
| 用户分配角色 | `/system/user/authRole`、`/system/role/authUser/*` | `system:user:edit` 或 `system:role:edit` |
| 菜单 CRUD/树 | `/system/menu` | `system:menu:list/query/add/edit/remove` |
| 当前用户路由 | `GET /system/menu/getRouters` | 登录态与用户菜单集合 |
| 权限码计算 | `SysPermissionServiceImpl` | 角色菜单权限；SuperAdmin 通配 |
| 前端按钮控制 | `v-hasPermi` | 用户权限快照，仅 UI |
| 物业项目范围 | `PropertyProjectScopeService` | 业务域 + READ/WRITE/EXPORT/OPTION |
| 业务上下文 | `BusinessContextAccessService` | 上下文策略 + 引用类型 + 权限组合 |
| 仓库范围 | `WarehouseAccessService` | 部门公司范围或物业项目范围 |
| 客户敏感权限 | 客户访问策略与审计 | `property:customer:pii:view/export` |

## 3. 业务规则

- 用户通过 `sys_user_role` 关联角色，角色通过 `sys_role_menu` 关联菜单，菜单 `perms` 提供功能权限码。
- SuperAdmin 获得 admin/通配能力，并在项目、仓库等范围服务中存在显式全量旁路。
- 非 SuperAdmin 不能委派 admin、超级管理员角色或包含敏感权限的角色；相关检查位于 `RoleDelegationGuardImpl`，由用户与角色管理入口调用。
- 敏感集合包含系统用户/角色维护、项目管理授权、客户 PII 查看/导出和客户管理授权等权限。
- 物业业务不仅检查 Controller 权限，还应由 Service 解析业务域范围并校验具体 projectId。
- BusinessContext 只授权读取某类引用数据，不代替项目、公司、仓库或实体归属校验。
- 仓库范围按 `warehouse_scope=COMPANY/PROJECT` 分支；公司范围使用部门数据范围，项目仓库使用物业 `WAREHOUSE` 业务域。
- 客户主档普通访问还要求有效企业客户主档授权；敏感查看/导出实时读取数据库菜单授权并写审计，避免只依赖登录时权限快照。

## 4. 权限校验流程

```text
登录身份
  → Sa-Token / SuperAdmin 判定
  → Controller @SaCheckPermission
  → Service 功能权限或敏感委派检查
  → BusinessContext（仅引用数据接口）
  → 项目/部门/公司/仓库/客户资源范围
  → 实体状态、归属与业务规则
  → 执行业务
```

任一必需层级拒绝即停止。对未注册 BusinessContext、未授权 ReferenceType、没有 contributor 的物业业务域均采用默认拒绝。

## 5. 权限矩阵

| 场景 | 功能权限 | 资源范围 | 特殊规则 |
|---|---|---|---|
| 系统角色维护 | `system:role:*` | 角色数据范围/保护规则 | 敏感定义或委派要求 SuperAdmin |
| 物业列表 | 对应 `property:*:list` | 业务域 READ | 当前工作项目不是授权来源 |
| 物业修改 | 对应 add/edit/remove 等 | 业务域 WRITE | Service 校验实体 projectId |
| 物业导出 | 对应 export | 业务域 EXPORT | 不应以 list 替代 export |
| 引用选项 | 对应上下文权限 | OPTION 或仓库/实体范围 | Context 与 ReferenceType 显式映射 |
| 公司仓库 | `warehouse:*` | `sys_dept` 数据范围 | 公司是组织节点，不是企业主档 |
| 项目仓库 | `warehouse:*` | 物业 WAREHOUSE 域 | 通过跨模块 `ProjectReferenceService` |
| 客户敏感查看 | query + `pii:view` | 客户主档授权 | 必须写允许/拒绝审计 |
| 客户敏感导出 | export + `pii:export` | 客户主档授权 | 审计结果含记录数/过滤条件摘要 |

## 6. 数据模型

- 身份权限：`sys_user`、`sys_role`、`sys_menu`、`sys_user_role`、`sys_role_menu`、`sys_role_dept`。
- 项目权限：`pm_project_member`、`pm_project_management_grant`、`pm_project_management_grant_audit`。
- 客户权限：`pm_customer_management_grant`、`pm_customer_management_grant_audit`、`pm_customer_sensitive_access_audit`。
- 仓库范围：`wm_warehouse.company_id/project_id/warehouse_scope`，公司数据源为 `sys_dept`。

## 7. 典型访问场景

1. **SuperAdmin**：功能权限与项目/仓库范围为全量；可维护敏感角色和管理授权。该旁路不表示可以删除审计或业务状态校验。
2. **普通物业员工**：必须绑定 ACTIVE 员工并存在 ACTIVE 项目成员关系，才能获得对应业务域项目集合；仍需业务功能权限。
3. **有多个物业项目的员工**：前端允许选择一个工作项目；后端每次仍按真实范围校验请求中的 projectId。
4. **纯仓储账号**：若只有 `warehouse:*`，前端不请求物业工作项目；仓库页面使用自身公司/项目/仓库上下文。
5. **普通客户管理员**：有客户 CRUD 权限仍需企业主档 grant；没有 PII 权限只能看到脱敏数据。
6. **具有客户 PII 权限的角色**：角色委派受 SuperAdmin 保护；敏感查看和导出要求审计写入，审计失败场景按 fail-closed 设计。

## 8. 接口与验证状态

角色与菜单接口详见 [系统管理文档](system-management.md)。项目、仓库和工作区接口分别见 [项目数据权限](../../business/property/project-access.md)、[仓库数据权限](../../business/warehouse/warehouse-access.md)、[工作项目初始化](../../business/property/workspace-initialization.md)。

| 层级 | 状态 | 证据 |
|---|---|---|
| 代码/权限码四方检查 | 通过 | 166 个后端、131 个前端、169 个菜单权限；硬缺失 0，见 002B 报告 |
| Java 权限契约 | 通过 | `PermissionContractAuditTest` 6/6 |
| BusinessContext 防回归 | 通过 | `BusinessContextAccessServiceTest` |
| 工作项目策略 | 通过 | 后端与前端策略测试 |
| 客户权限测试 | 部分通过 | 主路径通过；3 个触发器故障注入测试受环境高权限阻塞（其中客户 2、关系 1） |
| 数据库实际菜单/角色 | 未完全确认 | migration 有清单，但正式环境执行历史不可证明 |
| 浏览器岗位 E2E | 未执行 | 无报告 |

## 9. 已知限制与代码证据

- `PROPERTY_WORKSPACE_PERMISSIONS` 是手工白名单；新增物业页面时必须同步，已有契约测试用于发现遗漏。
- 新业务若直接调用 Mapper 绕过统一范围服务，当前没有覆盖全仓所有 Service 的静态规则。
- 登录中途权限变化时前端可能显示旧按钮，后端仍必须实时拒绝。
- 代码证据：`SysPermissionServiceImpl.java`、`RoleDelegationGuardImpl.java`、`PropertyProjectScopeServiceImpl.java`、`BusinessContextPolicy.java`、`WarehouseAccessService.java`、`CustomerAccessPolicyImpl.java`。

完整技术机制见 [技术权限说明](../../technical/permissions.md)。
