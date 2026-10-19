# V1.0 物业工作项目初始化

## 文档元数据

| 项目 | 内容 |
|---|---|
| 功能名称 | 登录后物业工作项目初始化与切换 |
| 所属模块 | 物业工作区 |
| 适用岗位 | 具有物业工作区权限的登录用户 |
| 当前实现状态 | 已实现 |
| 文档状态 | 已核对 |

## 1. 功能概述

物业工作项目用于前端页面保存当前操作项目偏好，并提供单项目自动选择和多项目切换体验。候选集由后端从用户真实物业业务范围聚合。**当前项目不是后端授权依据**，业务接口仍必须校验请求中的 projectId。

## 2. 初始化流程

```text
登录成功并持有 token
  → 前端拉取 user info
  → 生成动态路由
  → currentProjectStore.initialize()
  → currentProjectPolicy 判断是否需要物业工作区
      ├─ 否：清空选择和候选，不请求接口
      └─ 是：GET /property/workspace/projects
             → 后端 OR 功能权限检查
             → 权限映射为业务域
             → 每个业务域解析 OPTION 项目范围
             → 合并、去重并查询启用候选项目
  → 校验当前用户缓存
      ├─ 候选仅一个：自动选择并锁定
      ├─ 候选多个：保留有效缓存或等待用户选择
      └─ 缓存失效：清除并提示
```

## 3. 功能与规则

| 行为 | 实现规则 |
|---|---|
| 是否初始化 | 权限包含物业白名单之一或 `*:*:*` |
| 缓存隔离 | key 为 `zbm:current-project:<userId>` |
| 候选加载 | `/property/workspace/projects` |
| 单项目 | 自动选择；`locked=true` |
| 多项目 | 允许用户切换；必须属于候选集 |
| 无候选 | 保持未选择，不伪造项目 |
| 失效项目 | 清空内存与 localStorage，并提示重新选择 |
| 切换通知 | 派发 `zbm:current-project-changed` 事件 |
| 无物业权限 | 清空状态、标记已初始化、不发起候选请求 |
| 仓储账号 | `warehouse:*` 不在白名单，不自动加载物业项目 |

## 4. 权限控制

前后端物业白名单覆盖项目、成员、资源、客户/住户/关系、收费项目/规则/绑定、应收与收款的既有权限。后端 `PropertyWorkspaceController` 使用 `SaMode.OR`，只要具备一项允许权限即可进入候选接口。

`WorkspaceProjectPermissionPolicy` 把功能权限映射到 `PROJECT_BASE`、`RESOURCE`、`CUSTOMER_RELATION` 或 `FINANCE` 等域；`PropertyWorkspaceProjectServiceImpl` 对每个域调用 `resolveBusinessScope(domain, OPTION)` 并合并结果。无任何业务权限时 fail-closed；SuperAdmin 查询全量候选。

权限检查分工：

- 前端白名单：避免无关账号发起请求，提供界面状态；
- Controller OR 权限：保护候选接口；
- Service OPTION 范围：限制候选项目；
- 具体业务 Service READ/WRITE/EXPORT：最终校验业务请求。

## 5. 数据与接口

工作区没有独立持久化业务表。候选来自 `pm_project`，范围来自员工绑定和 `pm_project_member` 等现有授权数据；用户选择仅保存在浏览器 localStorage。

| Method | URL | Controller | 返回 |
|---|---|---|---|
| GET | `/property/workspace/projects` | `PropertyWorkspaceController#projects` | `WorkspaceProjectOptionVo[]`：字符串 ID、编码、名称、简称 |

使用字符串项目 ID 是为了避免前端 JavaScript 对大整数精度丢失。

## 6. 与仓库上下文的区别

| 物业工作项目 | 仓库上下文 |
|---|---|
| 前端全局 UI 偏好 | 仓储页面按查询/维护场景获取候选 |
| 候选按物业业务域 OPTION 合并 | 候选按公司、项目、仓库和 BusinessContext 控制 |
| 不参与后端最终授权 | `WarehouseAccessService` 是后端资源授权边界 |
| `warehouse:*` 不触发初始化 | 可独立服务纯仓储账号 |

## 7. 测试与验收状态

- `PropertyWorkspaceControllerTest`：验证 OR 语义，且不混入 warehouse/hr 权限。
- `WorkspaceProjectPermissionPolicyTest`：验证 FINANCE、CUSTOMER_RELATION 域及仓储/HR 不触发。
- `PropertyWorkspaceProjectServiceImplTest`：验证无权限拒绝、显式范围、空范围和大整数 ID。
- 前端 `currentProject.test.ts`：包含纯仓储账号不请求物业项目的回归场景。
- TASK-V1-002C：前端 30 项 Vitest、typecheck、lint、build 通过。
- 浏览器 E2E、多角色人工切换验收未执行。

## 8. 已知限制

- `PROPERTY_WORKSPACE_PERMISSIONS` 是手工清单；新增物业页面或权限码时必须同步前后端映射并由契约测试发现遗漏。
- 登录期间后台变更角色后，前端候选和按钮可能暂时保持旧快照；后端业务接口仍应实时拒绝。
- localStorage 只是体验状态，不能防止构造跨项目请求。

## 9. 证据路径

- `ruoyi-modules/ruoyi-property/src/main/java/org/dromara/property/workspace/`
- `ruoyi-modules/ruoyi-property/src/test/java/org/dromara/property/workspace/`
- `E:\space\project\zbm-platform-ui/src/store/modules/currentProject.ts`
- `E:\space\project\zbm-platform-ui/src/store/modules/currentProjectPolicy.ts`
- `E:\space\project\zbm-platform-ui/src/store/modules/currentProject.test.ts`
- `E:\space\project\zbm-platform-ui/src/permission.ts`
- [项目数据权限](project-access.md)
- [统一权限技术说明](../../technical/permissions.md)
