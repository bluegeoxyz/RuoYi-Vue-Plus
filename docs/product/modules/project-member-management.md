# V1.0 项目成员与管理授权功能文档

## 文档元数据

| 项目 | 内容 |
|---|---|
| 功能名称 | 项目成员管理与项目管理授权 |
| 所属菜单 | 物业管理 / 项目成员、项目管理授权 |
| 当前实现状态 | 已实现 |
| 文档状态 | 已核对 |

## 1. 功能概述

项目成员把员工加入具体物业项目，并作为普通用户业务数据范围的来源。项目管理授权把用户或角色授权到全部项目或指定项目，仅用于项目主档和成员等管理能力。两者互不替代。

## 2. 项目成员功能

| 操作 | 接口 | 权限码 | 规则 |
|---|---|---|---|
| 列表 | `GET /property/project/member/list` | `property:projectMember:list` | READ_MEMBER 管理范围 |
| 详情 | `GET /property/project/member/{id}` | `property:projectMember:query` | 按记录 projectId 校验 |
| 新增 | `POST /property/project/member` | `property:projectMember:add` | WRITE_MEMBER；项目、员工须有效 |
| 编辑 | `PUT /property/project/member` | `property:projectMember:edit` | 项目和员工不可更换；历史关系不可编辑 |
| 移出项目 | `PUT /property/project/member/{id}/end` | `property:projectMember:remove` | 结束关系并保留历史 |
| 设置/取消负责人 | `PUT /property/project/member/{id}/set-manager`、`PUT /property/project/member/{id}/cancel-manager` | `property:projectMember:edit` | 同步 `pm_project.manager_employee_id` |
| 删除错误关系 | `DELETE /property/project/member/{ids}` | `property:projectMember:remove` | 负责人关系不能直接删除 |
| 导出/候选 | `POST /property/project/member/export`、`GET /property/project/member/options` | export/list | 管理范围 |

成员规则：

- 只有启用且在职员工可加入；项目须存在且启用。
- 同一项目、同一员工不能同时存在多个有效成员关系；Service 预检并保留历史记录。
- 有效成员必须有开始日期且不能有结束日期；结束成员必须有结束日期且不能为负责人；结束日期不得早于开始日期。
- 项目和员工是成员关系的身份字段，普通编辑不能改变。
- 设置负责人只允许有效、启用成员；同步清除同项目其他负责人并更新项目负责人。
- 结束当前负责人会清除项目负责人；结束普通成员不会影响另一负责人。
- `ProjectMemberEmployeeReferenceChecker` 使被项目成员历史引用的员工删除受到保护。

## 3. 项目管理授权功能

| 操作 | 接口 | 权限码 | 规则 |
|---|---|---|---|
| 授权列表 | `GET /property/project/admin-scope/list` | `property:projectAdminScope:list` | Service 再要求 SuperAdmin |
| “历史”入口 | `GET /property/project/admin-scope/history` | `property:projectAdminScope:query` | Service 再要求 SuperAdmin；当前实际查询授权表，不查询审计表 |
| 新增/恢复授权 | `POST /property/project/admin-scope` | `property:projectAdminScope:add` | USER/ROLE，ALL/PROJECT |
| 启停 | `PUT /property/project/admin-scope/{id}/status` | `property:projectAdminScope:edit` | 乐观锁 version |
| 撤销 | `DELETE /property/project/admin-scope/{id}?version=` | `property:projectAdminScope:remove` | 逻辑删除、乐观锁 |

- B2 当前实现只允许 SuperAdmin 维护，即使普通用户拥有菜单权限也会被 Service 拒绝。
- ALL 范围固定 `project_id=0`；PROJECT 范围必须引用存在项目。
- 主体必须是存在的系统用户或角色；角色只有启用时才参与范围解析。
- 相同有效授权幂等恢复或拒绝并发重复；正常新增、恢复、状态和撤销执行路径写授权审计，并在成功时清理相关用户在线状态。参数校验或数据库异常发生在审计调用之前时，不应声称所有失败尝试均有审计记录。

## 4. 权限与范围分离

| 关系 | 贡献范围 | 不产生的能力 |
|---|---|---|
| ACTIVE、启用的 `pm_project_member` | PROJECT_BASE、RESOURCE、CUSTOMER_RELATION、FINANCE、WAREHOUSE 业务范围 | 不自动授予任何菜单权限；不自动获得项目管理权 |
| 有效 `pm_project_management_grant` | 项目主档/成员管理范围 | 不自动成为项目成员；不自动获得资源、客户或收费业务范围 |
| SuperAdmin | 管理与业务范围 ALL | 不绕过业务状态、唯一性和事务规则 |

## 5. 数据模型

- `pm_project_member`：项目、员工、成员角色、负责人标识、起止日期、成员状态、启停和逻辑删除。
- `pm_project_management_grant`：主体类型/ID、范围类型、项目、状态、version、逻辑删除；生成列唯一约束防止重复有效授权。
- `pm_project_management_grant_audit`：授权动作、主体、范围、前后状态和结果。
- `pm_project.manager_employee_id`：当前负责人快照，由成员 Service 同步。

## 6. 接口对象

成员使用 `ProjectMemberBo/Vo/ExportVo/OptionVo`；授权使用 `ProjectManagementGrantBo/Vo`。前端分别位于 `views/property/project/member/`、`views/property/project/admin-scope/`。

## 7. 测试状态

`ProjectMemberServiceImplTest` 10 项覆盖成员、负责人和历史规则；Controller 权限测试存在。授权 Service 和范围解析测试已有审计证据。浏览器 E2E、正式数据库授权数据及并发全场景未人工验收。

## 8. 已知限制

- `pm_project_member` baseline 未见等同生成列的“单一有效成员”数据库唯一约束，主要依赖 Service 锁和检查；并发完整性需以专项 DB 测试为准。
- 授权审计表记录 Service 执行结果，但不能据此证明正式环境授权与菜单 migration 已落地。
- 名为 `/history` 的当前接口调用 `grantMapper.selectVoList`，没有读取 `pm_project_management_grant_audit`；因此不能将该接口描述为完整审计历史查询。

## 9. 证据路径

- `ruoyi-modules/ruoyi-property/src/main/java/org/dromara/property/project/controller/ProjectMemberController.java`
- `ruoyi-modules/ruoyi-property/src/main/java/org/dromara/property/project/service/impl/ProjectMemberServiceImpl.java`
- `ruoyi-modules/ruoyi-property/src/main/java/org/dromara/property/project/controller/ProjectManagementGrantController.java`
- `ruoyi-modules/ruoyi-property/src/main/java/org/dromara/property/project/service/impl/ProjectManagementGrantServiceImpl.java`
- [项目数据权限](../../business/property/project-access.md)

