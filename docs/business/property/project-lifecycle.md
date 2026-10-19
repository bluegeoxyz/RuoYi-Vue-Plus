# V1.0 项目生命周期与跨模块影响

## 1. 状态与操作

项目没有独立审批状态机；核心状态为正常 `0` / 停用 `1`，另有逻辑删除。新增默认正常，状态接口只修改 `pm_project.status`，删除只在没有成员历史关系时逻辑删除。

| 操作 | 条件 | 事务/影响 |
|---|---|---|
| 普通新增 | CREATE_PROJECT 管理范围 ALL；编码唯一 | 单表事务；负责人必须另由成员管理设置 |
| 初始化新增 | 同上；负责人属于首批成员 | 项目、成员、负责人同一事务 |
| 编辑 | WRITE_PROJECT 管理范围 | 不允许普通编辑改负责人 |
| 停用/启用 | WRITE_PROJECT；状态字典有效 | 只更新项目，不级联 |
| 删除 | WRITE_PROJECT；项目存在；没有任何成员历史 | 逻辑删除；未统一检查其他模块引用 |

## 2. 停用项目的真实影响

- 新增项目成员：拒绝，`lockActiveProject` 要求项目启用。
- 新增单元、房屋和资源树导入：拒绝，明确检查项目启用。
- 新增楼栋：当前 `BuildingServiceImpl.insert` 只检查项目存在，没有检查状态。这与其他资源层级不同，记录为现有限制，不推断为设计意图。
- 已有资源的普通编辑、状态和删除：各 Service 主要依据记录所属 projectId 和 WRITE 范围；未统一要求项目仍启用。
- 项目候选：管理和业务 options 只返回启用项目。
- 客户关系、收费、仓储等下游是否拒绝停用项目由各自 Service 决定，没有统一级联停用。

## 3. 项目、部门和员工

- `pm_project` 没有所属部门字段；项目并非部门子实体。
- 员工通过 `pm_project_member` 加入项目，且用户需先绑定 ACTIVE 员工才能从成员关系获得业务范围。
- 项目负责人是成员中的一个员工，并同步到 `manager_employee_id`。
- 项目管理授权主体是系统用户或角色，与部门、员工和项目成员是不同关系。

## 4. 数据权限变化

- 新增/启用成员后，该员工绑定用户可获得相应项目业务范围；结束、停用成员后范围消失。
- 管理 grant 变更后清理用户在线状态；它只改变项目管理范围。
- 前端已保存的工作项目若不再属于候选集，会在刷新/初始化时被清除；后端无论前端是否刷新都会按实时范围拒绝越权项目 ID。

## 5. 删除与跨模块引用

当前明确保护：项目成员历史阻止项目删除；楼栋下单元/房屋阻止楼栋删除；单元下房屋阻止单元删除；房屋有效客户关系阻止房屋删除。

当前不能确认：项目删除对楼栋/房屋/收费/仓库引用的统一保护；房屋删除对收费绑定、应收历史等引用的全面保护。数据库表之间主要采用应用层软关联，不能因为有关联字段就推断级联删除。

## 6. 权限与测试

项目主档使用管理范围；资源使用 `PropertyBusinessDomain.RESOURCE` 的 READ/WRITE/EXPORT/OPTION。SuperAdmin 范围 ALL。测试覆盖项目 Service、成员 Service、权限入口和部分并发/唯一性，但浏览器生命周期 E2E 未执行。

## 7. 证据

- `project/service/impl/ProjectServiceImpl.java`
- `project/service/impl/ProjectMemberServiceImpl.java`
- `project/service/impl/ProjectManagementApplicationServiceImpl.java`
- `resource/building/service/impl/BuildingServiceImpl.java`
- `resource/unit/service/impl/BuildingUnitServiceImpl.java`
- `resource/room/service/impl/RoomServiceImpl.java`
- [项目数据权限](project-access.md)
