# V1.0 物业项目数据权限

## 文档元数据

| 项目 | 内容 |
|---|---|
| 功能名称 | 物业项目数据权限 |
| 所属模块 | 物业项目 / 项目成员 / 项目管理授权 |
| 适用岗位 | 物业项目成员、项目管理授权对象、SuperAdmin |
| 当前实现状态 | 已实现 |
| 文档状态 | 已核对 |

## 1. 功能概述

物业项目权限区分“项目管理范围”和“项目业务范围”。管理范围控制项目创建、维护及管理授权；业务范围控制资源、客户关系、收费和项目仓库等日常业务。统一入口为 `PropertyProjectScopeService`。

## 2. 功能与规则

| 能力 | 来源 | 规则 |
|---|---|---|
| 项目业务范围 | `pm_project_member` | 当前用户须绑定 ACTIVE 员工；成员关系须 ACTIVE 且启用 |
| 项目管理范围 | `pm_project_management_grant` | 合并用户 grant 与启用角色 grant；支持 ALL/PROJECT |
| 项目创建 | 管理范围 | 只有范围为 ALL 才能创建 |
| 列表查询 | `resolve*Scope` | 查询条件应用范围；NONE 返回空集合 |
| 详情/修改/删除 | `assert*Access` | 加载或写入前校验具体 projectId |
| 导出 | `ProjectBusinessAction.EXPORT` | 与 READ 独立表达，不能默认复用 |
| 引用候选 | `ProjectBusinessAction.OPTION` | 用于受控下拉选项，不替代业务操作授权 |

业务域为 `PROJECT_BASE`、`RESOURCE`、`CUSTOMER_RELATION`、`FINANCE`、`WAREHOUSE`、`OPERATIONS`。当前项目成员 contributor 支持前五个域；`OPERATIONS` 没有当前 contributor 时默认 NONE。

## 3. 权限控制

- Controller 使用 `property:project:*`、`property:projectMember:*`、`property:projectAdminScope:*` 等功能权限。
- Service 使用 `PropertyProjectScopeService` 解析/断言范围。
- 项目管理 grant 的维护 Service 仅允许 SuperAdmin；授权变更写管理审计表。
- SuperAdmin 的业务范围和管理范围均为 ALL。
- 项目成员业务范围不因为拥有菜单权限自动获得；菜单权限和成员关系缺一不可。

## 4. 数据模型

| 表 | 用途 | 关键字段 |
|---|---|---|
| `pm_project` | 项目主档 | 项目编码、名称、状态 |
| `hr_employee` | 登录用户到员工的桥接 | `user_id`、员工状态 |
| `pm_project_member` | 员工项目成员关系 | `project_id`、`employee_id`、`member_status`、`status` |
| `pm_project_management_grant` | 用户/角色项目管理授权 | subject 类型/ID、scope 类型、projectId、状态/有效期 |
| `pm_project_management_grant_audit` | 管理授权审计 | 操作、前后状态、结果 |

SQL 位于 `script/sql/zbm/baseline/01_schema.sql` 和 migration V2.0.1～V2.0.2。SQL 存在不证明目标库已执行。

## 5. 接口与代码

项目 CRUD、成员和管理授权接口分别由：

- `property/project/controller/ProjectController.java`
- `property/project/controller/ProjectMemberController.java`
- `property/project/controller/ProjectManagementGrantController.java`

范围接口：

- `scope/PropertyProjectScopeService.java`
- `scope/impl/PropertyProjectScopeServiceImpl.java`
- `scope/impl/ProjectMemberBusinessScopeContributor.java`

跨模块仓库项目读取通过 `ruoyi-api/.../ProjectReferenceService.java` 与 `ProjectReferenceServiceImpl.java`，避免仓储直接访问物业 Mapper。

## 6. 典型场景

- 普通用户有项目菜单但没有员工绑定：业务范围 NONE。
- 用户绑定有效员工但没有 ACTIVE 项目成员：业务范围 NONE。
- 有项目成员但没有对应功能权限：Controller 或 Service 功能权限拒绝。
- 有多个项目成员：范围包含多个显式 ID，前端可选工作项目，但每个接口仍校验请求 projectId。
- 有项目管理 grant 但不是项目成员：可以按 grant 执行管理能力，不据此自动获得所有业务域访问。
- SuperAdmin：范围 ALL。

## 7. 测试状态

项目 Service/Controller、项目范围、工作项目策略和权限契约测试已通过；后端 43 模块编译及默认全量测试通过。浏览器 E2E、真实岗位跨项目人工验证和正式数据库 grant 数据未确认。

## 8. 已知限制

- 当前业务 contributor 只基于项目成员，未来新增授权来源必须显式实现并测试；默认拒绝。
- 缺少阻止所有未来 Service 直接绕过范围服务的全仓静态架构规则。
- 工作项目选择只是 UI 偏好，不是授权证据。

### 与历史审计表述的口径澄清

`docs/audit/V1.0-权限体系审计.md` 将“项目成员和管理授权贡献项目范围”合并描述。当前代码显示两者并非共同贡献同一种范围：`ProjectMemberBusinessScopeContributor` 只贡献业务范围，`PropertyProjectScopeServiceImpl.resolveManagementScope` 只从管理 grant 解析管理范围。本文按当前代码拆开记录；未修改原始审计报告。

## 9. 证据路径

- `ruoyi-modules/ruoyi-property/src/main/java/org/dromara/property/scope/`
- `ruoyi-modules/ruoyi-property/src/main/java/org/dromara/property/project/`
- `ruoyi-api/src/main/java/org/dromara/property/api/`
- `script/sql/zbm/migration/V2.0.1__project_management_grant.sql`
- `script/sql/zbm/migration/V2.0.2__project_management_grant_audit_and_menu.sql`
- `docs/audit/V1.0-权限体系审计.md`
