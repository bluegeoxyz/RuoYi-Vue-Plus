# V1.0 物业项目管理功能文档

## 文档元数据

| 项目 | 内容 |
|---|---|
| 功能名称 | 物业项目管理 |
| 所属菜单 | 物业管理 / 项目管理 |
| 适用岗位 | 具有项目功能权限且位于项目管理范围的用户；授权维护仅 SuperAdmin |
| 当前实现状态 | 已实现 |
| 文档状态 | 已核对 |
| 事实基线 | 2026-10-14 当前工作树与 TASK-V1-001～004B 文档 |

## 1. 功能概述

项目管理维护 `pm_project` 主档，提供列表、详情、新增、初始化、编辑、状态、删除、导出和候选项。项目成员与项目管理授权是两条独立链路：成员关系贡献日常业务范围，管理授权贡献项目管理范围，不得混为一谈。

后端：`ruoyi-modules/ruoyi-property/src/main/java/org/dromara/property/project/`；前端：`E:\space\project\zbm-platform-ui/src/views/property/project/`。

## 2. 功能清单

| 操作 | HTTP 接口 | 权限码 | 主要规则 |
|---|---|---|---|
| 分页查询 | `GET /property/project/list` | `property:project:list` | 使用管理范围 READ_PROJECT |
| 详情 | `GET /property/project/{id}` | `property:project:query` | 按持久化项目 ID 校验管理范围 |
| 新增 | `POST /property/project` | `property:project:add` | 仅管理范围 ALL；不能直接指定负责人 |
| 初始化项目 | `POST /property/project/initialize` | `property:project:add` | 一个事务创建项目、首批成员和负责人 |
| 编辑 | `PUT /property/project` | `property:project:edit` | WRITE_PROJECT；不能通过普通编辑变更负责人 |
| 状态变更 | `PUT /property/project/changeStatus` | `property:project:edit` | 校验管理范围和状态字典 |
| 删除 | `DELETE /property/project/{ids}` | `property:project:remove` | 有任何成员历史关系即拒绝 |
| 导出 | `POST /property/project/export` | `property:project:export` | 复用管理 READ_PROJECT 查询范围 |
| 管理候选 | `GET /property/project/options` | `property:project:list` | 管理 OPTION 范围，只返回启用项目 |
| 业务候选 | `GET /property/project/business-options` | `property:project:list` | PROJECT_BASE/OPTION 范围，只返回启用项目 |

## 3. 业务规则

- 项目编码、名称必填；编码先由 Service 检查，再由 `uk_pm_project_code` 保证全局唯一。
- 项目类型和状态必须是有效字典值；默认排序为 0、状态为正常。
- 经度必须在 -180～180，纬度必须在 -90～90。
- 项目主档没有直接部门字段；与部门的关系来自创建审计字段和用户/员工组织关系，不能描述为“项目属于某部门”。
- 项目负责人字段关联员工。负责人只能通过项目成员管理设置，必须为有效项目成员；普通新增/编辑不能直接写负责人。
- `initialize` 要求负责人包含在首批成员中，整个项目及成员创建使用同一事务，失败回滚。
- 删除为逻辑删除；只要存在成员历史关系即拒绝，建议停用项目。当前项目删除 Service 未见对楼栋、房屋、收费或仓储引用的统一检查，因此不能声称所有跨模块引用都被完整保护。
- 状态变更只更新项目自身，不级联停用成员、资源、客户关系、收费或仓库。

## 4. 权限控制

项目主档使用 `ProjectManagementAction`，不是项目成员业务范围：

- `READ_PROJECT`：列表、详情、导出；
- `WRITE_PROJECT`：编辑、状态和删除；
- `OPTION`：管理候选；
- `CREATE_PROJECT`：只有管理范围为 ALL 才允许创建。

管理范围来自 `pm_project_management_grant` 的用户/启用角色授权；SuperAdmin 为 ALL。业务候选另走 `PropertyBusinessDomain.PROJECT_BASE + OPTION`，当前普通用户来源是有效项目成员。详细机制见 [项目数据权限](../../business/property/project-access.md) 和 [统一权限体系](../../technical/permissions.md)。

## 5. 数据模型

`pm_project` 关键字段：`project_code`、名称/简称、类型、区域和地址、服务电话、`manager_employee_id`、交付日期、经纬度、排序、状态和逻辑删除标志。`manager_employee_id` 是应用层软关联，不代表数据库级联。

相关表：`pm_project_member`、`pm_project_management_grant`、`pm_project_management_grant_audit`。结构见 `script/sql/zbm/baseline/01_schema.sql`；SQL 存在不证明目标数据库已执行。

## 6. 接口对象

- 查询/写入：`ProjectBo`；返回：`ProjectVo`；导出：`ProjectExportVo`；候选：`ProjectOptionVo`。
- 初始化：`CreateManagedProjectCommand`，包含项目、首批成员与负责人员工 ID。
- Controller：`ProjectController`；Service：`ProjectServiceImpl`、`ProjectManagementApplicationServiceImpl`。

## 7. 测试与验收状态

`ProjectServiceImplTest` 10 项、`ProjectControllerPermissionTest` 以及项目范围/工作区测试存在；TASK-V1-002C 后端 43 模块编译和默认全量测试通过。数据库 migration 正式落地、浏览器 E2E 和真实岗位人工验收未确认。

## 8. 已知限制

- 项目停用没有统一级联；不同下游 Service 对停用状态的检查并不完全一致。
- 项目导出当前复用 READ_PROJECT 管理范围；未见独立管理 EXPORT action。
- 项目删除只明确保护成员历史关系，其他跨模块引用保护不能推断。
- 管理授权存在不自动产生项目成员业务权限。

## 9. 代码证据路径

- `ruoyi-modules/ruoyi-property/src/main/java/org/dromara/property/project/controller/ProjectController.java`
- `ruoyi-modules/ruoyi-property/src/main/java/org/dromara/property/project/service/impl/ProjectServiceImpl.java`
- `ruoyi-modules/ruoyi-property/src/main/java/org/dromara/property/project/service/impl/ProjectManagementApplicationServiceImpl.java`
- `ruoyi-modules/ruoyi-property/src/main/java/org/dromara/property/scope/impl/PropertyProjectScopeServiceImpl.java`
- `E:\space\project\zbm-platform-ui/src/api/property/project/`
- `script/sql/zbm/baseline/01_schema.sql`

