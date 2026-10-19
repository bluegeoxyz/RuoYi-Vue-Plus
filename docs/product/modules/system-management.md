# V1.0 系统管理功能文档

## 文档元数据

| 项目 | 内容 |
|---|---|
| 功能名称 | 系统用户与组织管理 |
| 所属菜单 | 系统管理、系统监控 |
| 适用岗位 | 具有对应 `system:*`、`monitor:*` 权限的系统管理员；敏感角色操作仅 SuperAdmin |
| 当前实现状态 | 已实现 |
| 文档状态 | 已核对 |
| 事实基线 | 2026-10-13 当前工作树与 TASK-V1-001～002C 报告 |

## 1. 功能概述

系统管理提供用户账号、部门树、岗位、角色、菜单、字典、系统参数，以及操作日志和登录日志能力。系统用户是登录身份，部门和岗位用于组织归属、数据范围与人员分类；角色通过菜单关联获得功能权限，并可通过角色数据范围关联部门。

系统部门 `sys_dept` 是组织树和数据范围来源，**不是独立企业主数据模型**。当前没有企业统一社会信用代码、企业生命周期等独立档案能力；仓储中的“公司”也复用部门记录。

主要实现：

- 后端：`ruoyi-modules/ruoyi-system/src/main/java/org/dromara/system/`
- 前端：`E:\space\project\zbm-platform-ui/src/views/system/`、`src/views/monitor/`
- API：前端 `src/api/system/`
- MySQL 基础结构：`script/sql/ry_vue.sql`

## 2. 功能清单

| 功能 | 查询 | 新增 | 修改/状态 | 删除 | 导入/导出 | 特殊操作 |
|---|---|---|---|---|---|---|
| 用户 | 分页、详情、按部门、候选项 | 支持 | 编辑、启停、解锁、重置密码 | 批量删除 | 导入、模板、导出 | 分配角色、部门树 |
| 部门 | 树、详情、排除子树、候选项 | 支持 | 编辑、启停字段随实体维护 | 单个删除 | 无独立导入导出 | 父子结构校验由 Service 执行 |
| 岗位 | 分页、详情、候选项、部门树 | 支持 | 编辑、状态字段 | 批量删除 | 导出 | 可按部门查询岗位候选 |
| 角色 | 分页、详情、候选项 | 支持 | 编辑、数据权限、启停 | 批量删除 | 导出 | 菜单授权、分配/取消用户、部门范围树 |
| 菜单 | 列表、详情、树、角色菜单树 | 支持 | 编辑 | 单个或级联删除 | 无 | 当前用户动态路由 |
| 字典类型/数据 | 分页、详情、按类型读取 | 支持 | 编辑 | 批量删除 | 导出 | 刷新缓存 |
| 系统参数 | 分页、详情、按 key 读取 | 支持 | 编辑、按 key 更新 | 批量删除 | 导出 | 刷新缓存 |
| 操作日志 | 分页、详情由列表数据承载 | 不适用 | 不适用 | 批量删除、清空 | 导出 | 记录业务操作 |
| 登录日志 | 分页 | 不适用 | 不适用 | 批量删除、清空 | 导出 | 按用户名解锁 |

## 3. 业务规则

- 用户、角色、菜单、部门和岗位的唯一性、引用检查、数据范围及内置对象保护由对应 Service 执行，Controller 的权限注解不是唯一业务校验。
- 用户新增/编辑/角色分配会调用 `RoleDelegationGuard`；非 SuperAdmin 不能委派 admin、超级管理员角色或包含客户敏感、项目管理授权等敏感权限的角色。
- 角色权限定义或数据权限变更由 `RoleDelegationGuardImpl.assertCanChangeRoleDefinition` 保护；包含敏感菜单时要求 SuperAdmin。
- 用户与角色使用 `sys_user_role` 多对多关联；角色与菜单使用 `sys_role_menu` 多对多关联；角色部门数据范围使用 `sys_role_dept`。
- 用户与岗位还存在框架关系表 `sys_user_post`。员工模块的 `hr_employee.post_id` 是员工主岗位，二者不可默认视为自动同步。
- 菜单的 `perms` 是功能权限码来源之一；菜单存在不证明角色已经获得关联，migration 存在也不证明目标数据库已经执行。
- 字典类型、字典数据和系统参数存在缓存刷新接口；刷新动作分别使用 remove 权限或对应配置 remove 权限。
- 日志删除和清空是有权限保护的破坏性管理操作；本任务仅记录接口，没有执行。

## 4. 权限控制

| 模块 | 权限码 |
|---|---|
| 用户 | `system:user:list/query/add/edit/remove/import/export/resetPwd` |
| 部门 | `system:dept:list/query/add/edit/remove` |
| 岗位 | `system:post:list/query/add/edit/remove/export` |
| 角色 | `system:role:list/query/add/edit/remove/export` |
| 菜单 | `system:menu:list/query/add/edit/remove` |
| 字典 | `system:dict:list/query/add/edit/remove/export` |
| 参数 | `system:config:list/query/add/edit/remove/export` |
| 操作日志 | `monitor:operlog:list/export/remove` |
| 登录日志 | `monitor:logininfo:list/export/remove/unlock` |

前端页面使用 `v-hasPermi` 控制按钮显示；后端 Controller 使用 `@SaCheckPermission`。最终授权以后端为准。`/system/menu/getRouters`、字典按类型读取、参数按 key 读取等运行期接口没有同等的直接管理权限注解，它们服务于已登录用户的路由或公共配置读取，不能据此推断管理权限开放。

SuperAdmin 具有框架通配权限及多处显式旁路，但敏感边界仍以 `LoginHelper.isSuperAdmin()` 和保护服务的真实实现为准。详见 [统一权限体系](permission-management.md) 和 [技术权限说明](../../technical/permissions.md)。

## 5. 数据模型

| 表 | 作用 | 关键关系/字段 |
|---|---|---|
| `sys_user` | 登录账号 | `user_id`、`dept_id`、账号、密码、状态、删除标志 |
| `sys_dept` | 组织树 | `dept_id`、`parent_id`、祖级路径、状态 |
| `sys_post` | 岗位 | 岗位编码、名称、状态 |
| `sys_role` | 角色 | `role_key`、数据范围、状态 |
| `sys_menu` | 菜单/按钮/权限码 | 父菜单、类型、路由、组件、`perms`、状态 |
| `sys_user_role` | 用户—角色 | 联合关系 |
| `sys_role_menu` | 角色—菜单 | 联合关系 |
| `sys_role_dept` | 角色—部门数据范围 | 联合关系 |
| `sys_user_post` | 用户—岗位 | 联合关系 |
| `sys_dict_type`、`sys_dict_data` | 字典类型和值 | `dict_type` 关联 |
| `sys_config` | 系统参数 | `config_key`、`config_value` |
| `sys_oper_log` | 操作日志 | 操作人、接口、业务类型、状态等 |
| `sys_login_info` | 登录日志 | 用户名、地址、状态、时间等 |

上述基础表定义见 `script/sql/ry_vue.sql`。表结构存在不代表某个目标数据库已按当前脚本初始化。

## 6. 接口清单

| 模块 | 基础路径 | 主要接口 | Controller |
|---|---|---|---|
| 用户 | `/system/user` | `GET /list`、`GET /{userId}`、`POST`、`PUT`、`DELETE /{userIds}`、`POST /importData`、`POST /export`、`PUT /changeStatus`、`PUT /resetPwd`、`PUT /authRole` | `SysUserController` |
| 部门 | `/system/dept` | `GET /list`、`GET /{deptId}`、`POST`、`PUT`、`DELETE /{deptId}`、`GET /optionselect` | `SysDeptController` |
| 岗位 | `/system/post` | `GET /list`、`GET /{postId}`、`POST`、`PUT`、`DELETE /{postIds}`、`POST /export` | `SysPostController` |
| 角色 | `/system/role` | CRUD、`PUT /permission`、`PUT /changeStatus`、`/authUser/*`、`GET /deptTree/{roleId}` | `SysRoleController` |
| 菜单 | `/system/menu` | `GET /list`、`GET /{menuId}`、`POST`、`PUT`、删除、树与路由接口 | `SysMenuController` |
| 字典 | `/system/dict/type`、`/system/dict/data` | CRUD、导出、缓存刷新、按类型读取 | `SysDictTypeController`、`SysDictDataController` |
| 参数 | `/system/config` | CRUD、导出、按 key 查询/更新、缓存刷新 | `SysConfigController` |
| 操作日志 | `/monitor/operlog` | `GET /list`、`POST /export`、删除、清空 | `SysOperlogController` |
| 登录日志 | `/monitor/loginInfo` | `GET /list`、`POST /export`、删除、清空、解锁 | `SysLoginInfoController` |

请求/响应模型位于 `ruoyi-modules/ruoyi-system/src/main/java/org/dromara/system/domain/bo/` 和 `domain/vo/`；前端契约位于 `src/api/system/**/types.ts`。

## 7. 测试与验收状态

| 验证层级 | 状态 | 证据 | 限制 |
|---|---|---|---|
| 代码存在 | 已确认 | Controller、Service、Mapper、前端页面/API 均存在 | 不等于运行验收 |
| 后端编译 | 通过 | TASK-V1-002C：43/43 模块 | 不证明所有管理规则均被人工覆盖 |
| 自动化测试 | 通过（已有范围） | system/common 测试、权限契约测试 | 未逐一列出所有系统 CRUD 场景 |
| 权限契约 | 通过 | `PermissionContractAuditTest` 6 项 | 数据库实际角色菜单仍依赖 migration 落地 |
| 浏览器 E2E | 未执行 | 无报告 | 页面操作未作端到端验收 |
| 人工岗位验收 | 未确认 | 无正式记录 | 不得写为已验收 |

## 8. 已知限制

- 系统部门不是独立企业主数据。
- migration/菜单 SQL 存在不证明目标库角色菜单已同步。
- 登录态内权限变更后，前端可能继续显示旧按钮直到重新拉取用户信息；后端必须最终拒绝。
- 外部 OSS、短信、邮件等配置未在本任务验证。

## 9. 代码证据路径

- `ruoyi-modules/ruoyi-system/src/main/java/org/dromara/system/controller/`
- `ruoyi-modules/ruoyi-system/src/main/java/org/dromara/system/service/impl/`
- `ruoyi-modules/ruoyi-system/src/main/java/org/dromara/system/service/impl/RoleDelegationGuardImpl.java`
- `E:\space\project\zbm-platform-ui/src/views/system/`
- `E:\space\project\zbm-platform-ui/src/views/monitor/`
- `script/sql/ry_vue.sql`
- `docs/audit/V1.0-权限体系审计.md`
