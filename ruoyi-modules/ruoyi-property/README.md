# ruoyi-property

物业业务模块，负责项目、资源、客户及物业公共能力。

- Java 根包：`org.dromara.property`
- 依赖方向：`ruoyi-property -> ruoyi-hr -> ruoyi-system`
- HR 模块禁止反向依赖本模块
- `pm_project` 是独立物业项目，不使用 `sys_dept` 表示项目
- `pm_project_member` 维护员工与物业项目关系，该关系不归 HR 模块维护
- Property 读取员工信息时必须调用 HR 公开的 Service、QueryService 或 Facade，不得注入 HR Mapper

顶层业务包按需使用 `project`、`resource`、`customer`、`common`。未来的 `PropertyProjectScopeService` 位于 `common.scope`，负责可访问项目查询、项目访问校验和 SuperAdmin bypass；TASK-001 不实现数据权限 SQL。`PropertyContext` 和 `CurrentProjectContext` 同样留待后续任务实现。

核心实体命名固定为 `Project`、`ProjectMember`、`Building`、`BuildingUnit`、`Room`、`Customer`、`RoomCustomerRelation`，其中 `pm_unit` 对应 `BuildingUnit`。
