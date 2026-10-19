# TASK-006：统一项目数据权限与企业级项目管理授权架构设计（Phase A）

> 状态：设计完成，**未修改任何业务代码、数据库结构或既有权限逻辑**。  
> 基线：当前仓库 TASK-001 ～ TASK-005 的实际代码，RuoYi-Vue-Plus 6.0.0、Sa-Token、MyBatis-Plus。  
> 文档位置说明：仓库目前没有独立的 `docs/` 目录，故按根目录既有 `README.md` 文档入口放置本文件。

## 1. 目标与不可破坏的边界

本设计把权限拆成相互独立的三层，任何一层通过都不能替代另外两层：

| 层 | 回答的问题 | 授权来源 | 不能推导出的能力 |
|---|---|---|---|
| 功能权限（Function Permission） | 能否调用某个菜单/按钮/API？ | `sys_user_role` → `sys_role_menu` → `@SaCheckPermission` | 不能确定能操作哪一个项目 |
| 项目业务访问范围（Business Scope） | 能否读取/处理该项目的业务数据？ | 当前阶段为“登录账号绑定的在职员工”在 `pm_project_member` 中的有效成员关系；后续可叠加专项授权 | 不自动获得项目管理能力 |
| 项目管理范围（Management Scope） | 能否管理项目主数据、成员、负责人？ | 新的独立企业级项目管理授权 | 不自动获得项目资源、客户、收费等业务数据访问权 |

必须保持的规则：

1. 企业项目管理员可以在**不加入项目**的情况下管理其被授权项目，并可创建尚未有成员的新项目。
2. 项目成员仅贡献业务访问范围；不因成员身份获得项目管理权限。
3. 项目管理授权只贡献管理范围；不因该授权获得项目业务数据范围。
4. 无项目范围时默认拒绝（包括列表、详情、导出和 Options）。
5. `pm_project` 不是 `sys_dept`；不修改或复用部门数据权限的业务语义。
6. HR 继续只依赖 System；Property 可以调用 HR 公共 Service，但 HR 不得依赖 Property。
7. SuperAdmin（当前固定为 `SystemConstants.SUPER_ADMIN_USER_ID`）是明确、可审计的 bypass，不能用普通角色或项目成员关系模拟。

## 2. 当前代码核查结果

### 2.1 框架认证、RBAC、数据权限与缓存事实

| 主题 | 当前真实实现 | 对本设计的结论 |
|---|---|---|
| 登录与身份 | `LoginHelper.login` 将 `LoginUser` 放入 Sa-Token TokenSession，并把 `userId/deptId` 等写入 extra；`LoginHelper.getUserId()` 从当前会话读取 | 项目范围的主体应为 `sys_user.user_id`；不能把前端传入的员工或项目 ID 当作当前主体 |
| SuperAdmin | `LoginHelper.isSuperAdmin` 仅比较固定超级管理员用户 ID；`SysPermissionServiceImpl` 对其返回 `*:*:*` | 可作为唯一无范围过滤 bypass；普通“企业管理员”不能使用这个 bypass |
| 功能权限 | `SaPermissionImpl` 通过 `PermissionService` / `SysPermissionServiceImpl` 获取菜单权限；Controller 已使用 `@SaCheckPermission` | 继续复用 `property:project:*`、`property:projectMember:*`，而不是为每个项目写 Sa-Token 权限字符串 |
| 角色与系统内置超管保护 | `SysRoleServiceImpl.checkRoleAllowed` 保护内置超级管理员角色；`SysUserServiceImpl` 保护超管用户，角色变更还受现有角色数据权限约束 | 这只保护 SuperAdmin，不构成“企业项目管理高权限”的角色分级或不可委派策略 |
| 部门数据权限 | `@DataPermission`、`PlusDataPermissionHandler` 根据角色 `dataScope` 和部门 SQL 模板生成过滤条件；无适用角色时会按约束产生 `1 = 0` | 不能把 `pm_project.id` 伪装成 `dept_id`，也不能改 `DataScopeType` 以承载项目范围；否则会改变平台部门权限含义 |
| 数据权限生效条件 | Mapper 方法必须有 `@DataPermission` 上下文；当前 Project/ProjectMember Mapper 未标注该注解 | 当前项目模块没有任何项目行级过滤，不能误以为已有 DataPermission 已保护它 |
| 角色变更会话失效 | `OnlineUserCleanEvent` 在事务提交后由异步监听器清理受影响的 Sa-Token 在线会话 | 可复用“提交后失效会话”的模式；不能把异步踢下线当作同一事务内的授权判断 |
| 缓存 | Spring Cache / Redis 使用 `CacheNames`；部门范围已有 `SYS_ROLE_CUSTOM`、`SYS_DEPT_AND_CHILD` 缓存 | 新项目范围需要独立 cache name、按授权变更提交后失效，不能复用部门范围缓存 |

### 2.2 TASK-002 ～ TASK-005 的实际实现与设计差异

| 领域 | 已实现事实 | 差异 / 风险 |
|---|---|---|
| Employee | `hr_employee.user_id` 是账号绑定；绑定使用条件更新并保证一对一；删除通过 HR 定义的 `EmployeeReferenceChecker` SPI 让 Property 回答引用拦截理由 | 尚无“由 `userId` 查询当前有效 employeeId”的公开 HR 接口，也无账号绑定变化通知；项目业务范围不能安全、直接地从当前登录账号解析 |
| HR 依赖 | Property 通过 `IEmployeeService` 读取员工，不直接注入 `EmployeeMapper`；Property 实现 `EmployeeReferenceChecker`，HR 仅依赖自身 SPI | 该依赖方向正确，后续 scope 所需 HR 查询必须继续以 HR Service/Facade 暴露，不能让 HR 查询 `pm_project_member` |
| Project | Controller 仅有功能权限；Service 的 `queryById/page/list/export/options/update/delete/changeStatus` 都直接按 ID 或全表 Mapper 查询 | 具备 `property:project:*` 的用户可枚举、读取、导出或按猜测 ID 修改任何项目，存在 IDOR 与数据泄露 |
| Project `managerEmployeeId` | `ProjectServiceImpl` 仅校验员工在职启用，即允许新增/修改项目时直接写负责人 | 可绕过 `ProjectMemberServiceImpl` 的“负责人必须由成员关系维护、同步并清除其他负责人”规则；`pm_project.manager_employee_id` 与成员关系可能不一致 |
| ProjectMember | 新增/负责人切换以项目行 `FOR UPDATE` 串行化，能保证有效成员唯一和负责人同步；历史关系阻止项目/员工删除 | 这些是完整性并发控制，不是调用者授权；`queryById/page/list/export/options` 和所有修改入口未校验操作者是否可管理目标项目 |
| ProjectMember options | 可传 projectId，也允许 `null`；`null` 时查询所有有效成员 | 在有 `property:projectMember:list` 功能权限时可泄露全公司项目成员、员工编号、部门和岗位信息 |
| Project options | `ProjectMapper.selectOptions()` 返回全量启用项目 | 成员页面下拉框、其他复用方可看到无授权项目；不能作为未来业务模块的通用项目选择器 |
| 前端 | Project/Member 页面以菜单按钮权限控制显示，但页面 API 不携带服务端可验证的项目上下文 | 前端隐藏按钮不是安全边界；后端必须从会话与受控目标 ID 判定范围 |

### 2.3 当前必须优先处理的安全风险

| 优先级 | 风险 | 当前攻击/误操作路径 | Phase B 处置 |
|---|---|---|---|
| P0 | 项目 IDOR | 有任一项目功能权限的用户直接请求 `/property/project/{otherId}`、`PUT`、`DELETE/{ids}` 或成员详情/修改接口 | 每个目标 ID 进入 Service 后先做对应 scope assert；批量必须全量授权才执行 |
| P0 | 列表、导出、Options 泄露 | `/list`、`/export`、`/options` 直接使用全表查询 | 所有查询统一注入 scope；空范围生成零行；导出复用同一受限查询；Options 按用途分别受限 |
| P0 | 负责人绕过成员关系 | 直接调用 Project 新增/修改并传入任意在职员工 `managerEmployeeId` | 将负责人设置改为成员聚合命令；创建项目支持同一事务内首批成员 + 负责人 |
| P1 | 高权限角色自行提权 | 平台当前仅对内置 SuperAdmin 有特殊保护，普通可管理角色可被拥有角色管理能力者配置为高权限 | 在引入项目管理授权前增加不可委派高风险权限/角色策略；初始阶段仅 SuperAdmin 可授予或撤销该类角色与项目管理范围 |
| P1 | 授权撤销延迟 | Scope 缓存或已登录会话仍保留旧授权 | 授权变更提交后立即失效项目范围缓存，并按用户发布会话清理；关键写操作始终从受控 scope 决策 |
| P1 | 绑定关系变化未同步 | 员工换绑/解绑账户后，业务范围可能留在旧账号缓存中 | 增加 HR 公共的账号—员工解析能力与跨模块的绑定变更失效契约；完成前不要长期缓存该映射 |

## 3. 推荐最终架构

### 3.1 权限决策模型

```text
请求
  │
  ├─ 1. Sa-Token / @SaCheckPermission：功能权限
  │       失败 → 403
  │
  ├─ 2. PropertyProjectScopeService：按“业务”或“管理”意图计算范围
  │       SuperAdmin → 显式 ALL bypass
  │       非超管、无授权 → NONE（默认拒绝）
  │
  └─ 3. Service/Mapper：把范围绑定到查询、详情、写入、批量、导出、Options
          不匹配 → 统一的无权/不可见结果，不暴露其他项目存在性
```

范围计算必须带 `ScopePurpose`，严禁只暴露没有语义的 `getAccessibleProjectIds()`：

```java
public enum ScopePurpose {
    BUSINESS_READ,
    BUSINESS_WRITE,
    MANAGEMENT_READ,
    MANAGEMENT_WRITE,
    MANAGEMENT_CREATE
}

public interface PropertyProjectScopeService {
    PropertyProjectScope resolveCurrent(ScopePurpose purpose);

    void assertCurrentBusinessAccess(Long projectId, ProjectBusinessAction action);

    void assertCurrentManagementAccess(Long projectId, ProjectManagementAction action);

    void assertCurrentCanCreateProject();

    /**
     * 对单个项目 ID 做安全过滤；未授权返回不可见结果或由调用方抛统一拒绝异常。
     */
    boolean currentCanAccess(Long projectId, ScopePurpose purpose);
}
```

`PropertyProjectScope` 不应强制把“全企业范围”展开成全量 `projectId` 集合。建议表达为 `NONE`、`EXPLICIT_IDS`、`ALL` 三种形态，并提供给 Mapper 的受控 predicate：

* `NONE`：SQL 强制 `1 = 0`；绝不能因空 `IN` 被省略后退化为全表。
* `EXPLICIT_IDS`：`project_id IN (...)`，并为很大集合保留子查询/临时范围查询实现空间。
* `ALL`：仅 SuperAdmin，或管理范围中明确的企业级 `ALL` 授权；业务范围当前不因管理授权变为 `ALL`。

### 3.2 业务范围：以成员关系为当前授权来源，并预留扩展点

当前业务范围的计算链：

```text
LoginHelper.getUserId()
  → HR 公共接口查询该账号绑定且有效的 employeeId
  → pm_project_member(employee_id, member_status = ACTIVE, status = 0)
  → 当前账号的业务项目范围
```

建议新增到 Property 的可插拔贡献者接口，而不是在 ScopeService 内永久写死 Member 查询。A.1 已将其收敛为带业务域的最终契约，详见第 11.2 节；不得再使用不带业务域的全局项目集合接口。

```java
// 旧草案接口已废弃；最终接口见第 11.2 节。
```

第一期唯一实现为 `ProjectMemberBusinessScopeContributor`。未来财务、运营等专项授权以新的 contributor 叠加（并集）即可；本 TASK 不新增专项授权表。所有 contributor 都必须默认返回空集合，不允许“未实现即全量”。

HR 只需增加其公开能力，例如：

```java
Optional<Long> findActiveEmployeeIdByUserId(Long userId);
```

该接口位于 `IEmployeeService` 或 HR 专用 QueryService/Facade，由 HR 自己访问 `EmployeeMapper` 实现。Property 调用该接口；HR 不导入 Property 类型、不查询 Property 表。

### 3.3 管理范围：独立于 ProjectMember 的企业授权

建议在 Phase B 新增 Property 所有的授权模型。此前草案中的唯一约束包含可空 `project_id` 和 `del_flag`，在 MySQL 下不能安全保证活跃授权唯一性；A.1 修正后的可执行 DDL、更新规则见第 11.1 节，并以其为准：

```text
pm_project_management_grant
  id
  principal_type       USER | ROLE
  principal_id         sys_user.user_id 或 sys_role.role_id
  scope_type           ALL | PROJECT
  project_id           scope_type = PROJECT 时必填；ALL 时为空
  status               0/1
  create_dept/create_by/create_time/update_by/update_time/remark/del_flag
  唯一约束             以 A.1 的 active generated key 为准
  索引                 (principal_type, principal_id, status, del_flag),
                       (project_id, status, del_flag)
```

这是**范围表**而不是功能权限表：

* 用户/角色仍须通过既有 RBAC 获得 `property:project:*` 或 `property:projectMember:*` 才能调用管理 API。
* 管理范围由同一用户的 USER 授权和当前启用角色的 ROLE 授权求并集。
* `ALL` 允许企业项目管理员新增项目；仅有若干 PROJECT 授权时，只能管理这些既有项目，不能创建没有 ID 的新项目。
* `pm_project_member` 不参与管理范围计算；项目负责人身份也不参与管理范围计算。
* 不引入“DENY 覆盖 ALLOW”语义。初版采用正向授权并集，避免角色/用户冲突规则不透明；若未来确有显式拒绝需求，应单独设计优先级而非隐式加入。

建议把 `property:projectAdminScope:list/query/add/edit/remove` 作为管理授权维护页面的功能权限。该页面/接口和 ProjectMember 页面是不同业务：前者授予“谁可管理哪些项目”，后者维护“哪些员工是项目业务成员”。

### 3.4 高权限角色分配与反自提权策略

现有平台已保护内置 SuperAdmin，但没有通用角色层级/委派上限。因此在引入管理授权前必须实施以下治理：

1. 首期把含有以下任一权限的角色定义为**不可委派安全角色**：`system:role:*`、`system:user:*` 中可分配角色的能力、`property:projectAdminScope:*`，以及未来可跨企业读取项目数据的权限。
2. 只有 SuperAdmin 可以创建、修改菜单权限、分配、撤销这些不可委派安全角色，也只有 SuperAdmin 可以维护 `pm_project_management_grant`。这是首期最小、可验证的边界。
3. 企业项目管理员角色可以拥有项目/成员管理功能权限，但默认**不拥有**授权维护权限；即使它对某项目有 `ALL` 管理范围，也不能给自己或别人扩大范围。
4. `SysRoleService` / `SysUserService` 的角色编辑、用户授权、角色授权用户等所有路径都必须在事务写入前调用同一个“敏感角色/菜单不可委派”策略，不能只在前端隐藏或只保护一个 Controller。
5. 未来若业务要求安全管理员委派，单独在 System 域设计显式 `role delegation` 规则（委派人、可委派角色、有效期、审计），不能用部门数据范围替代，也不能让项目管理员编辑自己的安全角色。

这套策略复用现有 RBAC 存储与 `@SaCheckPermission`，只为“范围”增加 Property 授权表；不会把企业项目管理员伪装为 SuperAdmin。

## 4. Project 与 ProjectMember 的接口接入规范

### 4.1 现有接口的目标分类

| API / 操作 | 功能权限保持 | ScopePurpose | Phase B 规则 |
|---|---|---|---|
| `GET /property/project/list` | `property:project:list` | `MANAGEMENT_READ` | 查询 wrapper 注入管理范围；仅 PROJECT 授权可见对应行，NONE 返回空页 |
| `POST /property/project/export` | `property:project:export` | `MANAGEMENT_READ` | 与 list 使用同一个受限查询，不能先查全量再导出 |
| `GET /property/project/{id}` | `property:project:query` | `MANAGEMENT_READ` | 按 ID 断言管理范围后查询，或使用带 scope 的单条查询 |
| `POST /property/project` | `property:project:add` | `MANAGEMENT_CREATE` | 仅 SuperAdmin 或具有管理 `ALL` 授权者可创建；创建时项目尚无成员不构成拒绝理由 |
| `PUT /property/project`、状态、删除 | 原有 edit/remove | `MANAGEMENT_WRITE` | 先按 `bo.id` / 每个批量 ID 全量断言管理范围；禁止部分成功 |
| `GET /property/project/options` | `property:project:list` | `MANAGEMENT_READ` | 只返回可管理项目，且为成员管理用途命名为管理型 Options；未来业务选择器另设业务型 endpoint/方法 |
| `GET /property/project/member/list`、详情、导出 | 原有 member 权限 | `MANAGEMENT_READ` | 对 `pm_project_member.project_id` 注入管理范围；无 projectId 也只能看到管理范围内成员 |
| 新增成员、修改、结束、设/取消负责人、错误删除 | 原有 add/edit/remove | `MANAGEMENT_WRITE` | 先解析目标/请求中的 projectId 并断言；错误删除批量须按所有关联项目全量验证 |
| `GET /property/project/member/options` | `property:projectMember:list` | `MANAGEMENT_READ` | projectId 参数应成为必填或按管理范围过滤；`null` 不得返回公司全量成员 |

### 4.2 IDOR、批量、导出与 Options 的实现约束

1. **详情与写入**：Service 不应先用无范围的 `selectById` 取得完整对象再仅依赖 Controller；应提供 `requireManagedProject(id)`、`requireManagedMember(id)` 或带 scope 的 Mapper 查询。Controller 只做功能权限，Service 是跨入口的最终边界。
2. **请求体 ID**：更新或状态变更使用服务端读取到的项目 ID 作为范围判断依据；不得信任嵌套/前端额外传入的 projectId。
3. **批量操作**：去重后同时校验“记录存在数 == 请求数”和“每条记录项目均在管理范围”，任一失败整体回滚，禁止只修改获授权子集。
4. **导出**：只能调用与 list 相同的 `buildScopedQuery`，导出对象 enrichment 也只能对已过滤记录执行。
5. **Options**：按调用语义拆分为管理型与业务型；返回最小必要字段。Options 不是低风险接口，必须进行功能权限 + scope 判定。
6. **员工候选项**：ProjectMember 管理选人仍由 HR 提供公开查询，并遵循 HR 现有部门数据权限；Property 不可直连 HR Mapper。项目范围不会自动授权浏览全员 HR 档案。
7. **错误信息**：跨项目未授权 ID 建议统一返回“记录不存在或无权访问”，避免通过详情、批量差异或 options 探测项目存在性。

### 4.3 负责人和首批成员的聚合写入

现有 `ProjectServiceImpl` 可以独立写 `managerEmployeeId`，而 `ProjectMemberServiceImpl` 才维护成员负责人互斥与项目镜像字段。Phase B 应把此责任收敛为一个项目管理应用服务：

```text
CreateManagedProjectCommand
  ProjectBo project
  List<InitialProjectMemberCommand> initialMembers
  Long managerEmployeeId (可选，但存在时必须属于 initialMembers 中有效成员)
```

事务顺序：

1. `assertCurrentCanCreateProject()`；
2. 插入项目（负责人暂为空）；
3. 锁定项目行并创建首批成员；
4. 通过成员命令设置负责人，更新 `pm_project.manager_employee_id`；
5. 全部成功后提交并发布失效事件。

独立 Project 修改接口不再接受可直接改变负责人的字段；负责人切换统一走项目成员管理命令。这样企业项目管理员可创建“尚无成员”的项目，也可在同一事务内配置首批成员和负责人，不需要先把管理员本人加入项目。

## 5. 缓存、事务、变更生效与并发策略

### 5.1 缓存与即时生效

建议引入独立 cache name（示例）`property_project_scope#5m#1m#10000`，key 至少包含：`userId + scopePurpose + authorizationVersion`。实现前须先决定版本策略，不能只按固定 TTL 依赖最终过期。

| 变更 | 受影响范围 | 提交后动作 |
|---|---|---|
| 项目成员新增、结束、状态变化、错误删除 | 对应 employee 绑定的 userId 的业务范围 | 失效该用户 `BUSINESS_*` scope；无绑定时无需会话操作 |
| 员工绑定/解绑账号 | 旧 userId、新 userId 的业务范围 | HR 发布公共账号绑定变更契约，Property 监听并失效两侧 scope；在该契约落地前不长期缓存 user→employee 映射 |
| 项目管理授权新增、修改、撤销、状态变化 | 直接用户、或拥有被授权角色的所有用户 | 失效受影响用户的 `MANAGEMENT_*` scope，并发布会话清理 |
| 用户角色关系、角色菜单变化 | 角色拥有者 | 复用现有 `OnlineUserCleanEvent` 提交后清理；同时失效其管理 scope（ROLE principal） |

缓存失效和会话清理均使用事务 **AFTER_COMMIT** 事件。范围服务在每次请求都做功能权限和 scope 决策；异步踢下线只是缩短旧会话生命周期，不得作为授权撤销的唯一机制。

跨模块事件不应令 HR 依赖 Property。账号绑定变更事件应定义在已有公共/API 层，事件载荷只包含 `oldUserId/newUserId/employeeId`，Property 单向监听。

### 5.2 事务与并发

* 现有 ProjectMember 的“项目行 `FOR UPDATE` + 同事务清除负责人 + 更新镜像”机制保留，且在 scope 接入后仍是成员唯一性和负责人一致性的基础。
* 所有项目管理写操作在事务内先完成范围决策，再锁定目标项目；需要跨多个项目的批量操作按稳定的 projectId 顺序加锁，减少死锁。
* 管理授权表使用唯一约束防止重复 grant；授权修改/撤销按 principal + scope 行加锁或乐观版本校验，避免并发授予/撤销产生丢失更新。
* 授权判定与数据写入属于同一个应用事务。撤销与已通过判定的并发写入按数据库提交顺序线性化；撤销提交完成后，新的请求不得继续通过。
* `@RepeatSubmit` 继续只用于重复提交防护；它不能替代授权、唯一约束或行锁。

## 6. 后续 Building、BuildingUnit、Room、Customer 接入规范

| 模块 | 是否天然带 projectId | 统一接入要求 |
|---|---|---|
| Building | 是 | 所有读写、详情、导出、Options 按 `project_id` 使用 `BUSINESS_*` scope；新增时先断言请求 projectId |
| BuildingUnit | 直接有 `project_id` 且关联 building | 以自身 `project_id` 为权威；不能只按 `building_id` 信任前端，新增/修改时校验 building 属于同一已授权项目 |
| Room | 直接有 `project_id`，并关联 building/unit | 以 Room 的 `project_id` 做 scope；校验 building/unit/room 的项目一致性，避免跨项目拼接 IDOR |
| RoomCustomerRelation | 是 | 按 `project_id` 做业务范围；客户选项只返回该范围内可关联数据或按单独客户隐私策略处理 |
| Customer | 否，`pm_customer` 是企业级主数据 | 不应硬套项目 scope。另行设计企业客户主数据读写权限与敏感字段策略；若通过房客关系展示客户，先按 relation 的 project scope 过滤 |

规范要求：每一个 Property 业务 Service 都要先明确它是“项目管理功能”还是“项目业务功能”，并传入对应 `ScopePurpose`；禁止未来模块自行读取 `pm_project_member`、自行写 `LoginHelper.isSuperAdmin()` 分支或使用前端当前项目作为授权依据。

## 7. 计划新增与修改的文件清单（实施阶段，不在本 Phase A 执行）

### Property 模块

新增建议：

* `org.dromara.property.common.scope.PropertyProjectScopeService`
* `...PropertyProjectScopeServiceImpl`
* `...PropertyProjectScope`、`ScopePurpose`、`ProjectBusinessAction`、`ProjectManagementAction`
* `...PropertyProjectBusinessScopeContributor`、`...ProjectMemberBusinessScopeContributor`
* 管理授权的 `ProjectManagementGrant` Entity、BO、VO、Mapper、Service、Controller，以及 `pm_project_management_grant` 迁移 SQL
* 授权/成员变更后的缓存失效事件与监听器
* `ProjectManagementApplicationService`（创建项目、首批成员、负责人聚合命令）
* Project/ProjectMember scope 单元测试、Controller 授权测试、集成测试

修改建议：

* `ProjectController`、`ProjectServiceImpl`、`ProjectMapper`：对所有查询和写入加入管理范围；收敛 `managerEmployeeId` 直写。
* `ProjectMemberController`、`ProjectMemberServiceImpl`、`ProjectMemberMapper`：对所有关系操作加入管理范围；限制空 projectId 的 Options。
* `ProjectBo` / 前端 Project 表单：负责人改由首批成员/成员操作命令维护。
* `CacheNames`：增加独立 Property scope cache 常量（或使用本模块统一定义，保持命名规范）。

### HR 与 System / 公共层

修改建议：

* `IEmployeeService` 与 `EmployeeServiceImpl`：增加按 userId 查询有效员工的公开方法；账号绑定/解绑后发布不带 Property 依赖的公共变更事件。
* `SysRoleServiceImpl`、`SysUserServiceImpl` 及其所有角色分配入口：接入统一“敏感角色/菜单不可委派”策略，覆盖新增、修改、用户授权、角色授权用户。
* 会话失效：优先复用现有 `OnlineUserCleanEvent` 的提交后清理语义；若需避免 Property 对 System 实现事件的直接依赖，将稳定的失效契约提升到 System API / 公共层。
* 菜单与权限 SQL：新增管理授权页面及 `property:projectAdminScope:*` 按钮权限；不修改既有 ProjectMember 权限的含义。

## 8. 推荐实施阶段与顺序

### Phase B1：先封堵当前 IDOR 与泄露（不可单独作为最终上线验收）

1. 落地最终 `PropertyProjectScopeService` 的基础设施、业务域契约和最小数据库范围查询；业务范围仅成员关系，管理授权实现可与 B2 并行开发。
2. 对现有 Project、ProjectMember 全部接口统一接入管理范围，重点完成详情、修改、批量、导出、Options。
3. 增加 HR 的 `userId → active employeeId` 公共查询；不引入 HR → Property 依赖。
4. 收敛 Project 直接写负责人，避免破坏成员负责人一致性。

> B1 可用于预生产安全验证或受控灰度，但不能以“仅 SuperAdmin 可管理项目”作为产品最终验收状态。B1 与 B2 必须在同一个项目管理权限交付周期内联调、迁移、验收后共同发布。

### Phase B2：企业级项目管理授权（与 B1 合并验收）

1. 建 `pm_project_management_grant`、授权维护 CRUD、管理型 Options。
2. 实现 USER/ROLE principal 与 ALL/PROJECT scope 合并计算。
3. 实现企业管理员创建新项目、首批成员、负责人同事务配置。
4. 配置不可委派高风险角色/菜单策略与审计日志。

### Phase B3：失效、缓存与运营硬化

1. 缓存版本、AFTER_COMMIT 失效、账号绑定变更事件。
2. 管理授权和角色变更的在线会话清理。
3. 监控授权拒绝、范围计算失败、缓存命中/失效和超大范围查询。

### Phase C：后续业务模块与专项授权

1. Building → Unit → Room → RoomCustomerRelation 依次接入业务范围。
2. 单独设计 Customer 企业主数据访问策略。
3. 当财务/运营真实需求出现时新增专项授权模型及 `PropertyProjectBusinessScopeContributor` 实现，不提前建空表。

## 9. 自动化测试与验收矩阵

| 类别 | 场景 | 预期 |
|---|---|---|
| 功能 × 范围 | 有 `property:project:query` 但无管理 grant 请求项目详情 | 拒绝/不可见，不泄露项目名称 |
| 管理范围 | 企业管理员有 PROJECT grant、不是成员 | 可管理该项目和成员；不能读取该项目未来业务数据，除非也是成员/专项授权人 |
| 业务范围 | 项目成员仅有业务功能权限 | 可见自己成员项目的业务模块；不能访问 ProjectMember 管理接口 |
| 创建 | 管理 `ALL` grant 的企业管理员创建无成员项目 | 成功；不要求管理员成为成员 |
| 创建 | 仅单项目 grant 的管理员创建项目 | 拒绝 |
| 首批配置 | 创建项目时指定负责人不在初始成员中 | 事务整体失败，项目和成员均不落库 |
| 负责人一致性 | 切换负责人、结束负责人成员、并发两人设负责人 | 至多一个有效负责人，项目镜像一致；并发结果可解释且无脏状态 |
| 单条 IDOR | 修改/状态/结束成员时传入其他授权范围的 ID | 拒绝，原数据不变 |
| 批量 IDOR | 批量删除中混入一个无权项目 ID | 全部失败、无部分删除 |
| 查询泄露 | list/export/options 在 NONE、PROJECT、ALL 三种范围下 | 分别零行、精确子集、预期全集；Options 不外泄 |
| HR 绑定 | 员工换绑、解绑、停用、离职 | 旧账号业务范围失效；新账号按新绑定生效；HR 无 Property 编译依赖 |
| RBAC 提权 | 普通角色管理员尝试给自己/他人授予不可委派项目安全角色或修改其菜单 | 拒绝并记录审计；SuperAdmin 流程可控通过 |
| 撤销 | 撤销管理 grant、成员资格或高权限角色后再次请求 | 提交后新请求立即拒绝；受影响会话被清理 |
| 回归 | 部门角色 dataScope 的 System 用户、部门、角色列表 | 行为与 TASK-006 前保持一致 |
| 性能 | 千级项目成员、分页、导出、批量 Options | 无 N+1；范围过滤走索引；无全量 ID 集合导致内存/SQL 参数失控 |

验收证据应包含：Service/Controller 单测、真实数据库集成测试（验证 `FOR UPDATE` 与唯一约束）、权限回归测试、缓存失效测试，以及至少一次双会话撤销测试。仅用前端按钮隐藏或 mock Mapper 测试不能作为安全验收。

## 10. Phase A 结论

当前 TASK-005 的成员完整性事务实现是可复用的良好基础，但它尚未构成项目数据权限。最小安全演进路径是：先以独立 `PropertyProjectScopeService` 在服务端封堵 Project/ProjectMember 的所有读取和写入入口；再以独立的企业项目管理授权表赋予“可管理范围”；始终将项目业务范围保留给成员关系及未来专项授权贡献者。这样可以满足企业管理员初始化项目的需求，同时不会把管理权限、成员身份和业务数据访问混为一层，也不会改变 RuoYi 既有部门数据权限或打破 HR → Property 的依赖边界。

## 11. Phase A.1 定稿补充（本节覆盖前文冲突草案）

### 11.1 管理授权表：MySQL 可执行 DDL、唯一性与安全更新

#### 11.1.1 原草案的 MySQL 风险

原草案的 `(principal_type, principal_id, scope_type, project_id, del_flag)` 不能直接使用：

* MySQL 的 UNIQUE INDEX 允许出现多个 `NULL`。因此 `scope_type = ALL, project_id = NULL` 可以重复插入，无法保证企业级授权唯一。
* 把 `del_flag` 直接纳入唯一键，会让同一逻辑身份只能保留一条已删除记录；第二次逻辑删除会与第一条 `del_flag = 1` 冲突，反而妨碍“撤销后再授权”的正常审计历史。
* 仅依赖“先查再插”在并发 grant 时会产生竞态；唯一键必须是最终裁决，业务层再处理 Duplicate Key。

#### 11.1.2 最终 DDL

`project_id` 使用非空的 `0` 作为 `ALL` 范围哨兵值。当前 Snowflake ID 体系不会产生 `0`；服务层也必须校验 `PROJECT` 范围的 ID 大于零。逻辑删除行通过 generated column 进入 `NULL` 唯一键分支，使历史可重复保留、活跃授权严格唯一。

```sql
CREATE TABLE `pm_project_management_grant`
(
    `id`             BIGINT       NOT NULL COMMENT '授权ID',

    `principal_type` VARCHAR(16)  NOT NULL COMMENT '授权主体类型（USER/ROLE）',
    `principal_id`   BIGINT       NOT NULL COMMENT 'sys_user.user_id 或 sys_role.role_id',
    `scope_type`     VARCHAR(16)  NOT NULL COMMENT '范围类型（ALL/PROJECT）',
    `project_id`     BIGINT       NOT NULL DEFAULT 0 COMMENT '项目ID；ALL 时固定为 0',
    `status`         CHAR(1)      NOT NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
    `version`        INT          NOT NULL DEFAULT 0 COMMENT '乐观锁版本',

    `create_dept`    BIGINT       DEFAULT NULL COMMENT '创建部门',
    `create_by`      BIGINT       DEFAULT NULL COMMENT '创建者',
    `create_time`    DATETIME     DEFAULT NULL COMMENT '创建时间',
    `update_by`      BIGINT       DEFAULT NULL COMMENT '更新者',
    `update_time`    DATETIME     DEFAULT NULL COMMENT '更新时间',
    `remark`         VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `del_flag`       CHAR(1)      NOT NULL DEFAULT '0' COMMENT '删除标志（0存在 1删除）',

    /* 仅未逻辑删除的行参与唯一约束；删除行值为 NULL，可保留多次审计历史。 */
    `active_project_id` BIGINT GENERATED ALWAYS AS
        (CASE WHEN `del_flag` = '0' THEN `project_id` ELSE NULL END) STORED,

    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_pm_project_management_grant_active`
        (`principal_type`, `principal_id`, `scope_type`, `active_project_id`),
    KEY `idx_pm_project_management_grant_principal`
        (`principal_type`, `principal_id`, `status`, `del_flag`),
    KEY `idx_pm_project_management_grant_project`
        (`project_id`, `status`, `del_flag`),
    CONSTRAINT `ck_pm_project_management_grant_scope`
        CHECK ((`scope_type` = 'ALL' AND `project_id` = 0)
            OR (`scope_type` = 'PROJECT' AND `project_id` > 0)),
    CONSTRAINT `ck_pm_project_management_grant_principal`
        CHECK (`principal_type` IN ('USER', 'ROLE')),
    CONSTRAINT `ck_pm_project_management_grant_scope_type`
        CHECK (`scope_type` IN ('ALL', 'PROJECT'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='企业项目管理范围授权表';
```

部署前提为 MySQL 8.0.16+（CHECK 约束可执行）及支持 stored generated column 的 MySQL 版本。即使数据库支持 CHECK，BO/Service 仍要重复校验枚举和 `ALL → projectId=0` 归一化，不能把输入校验交给数据库异常。

#### 11.1.3 安全命令与并发规则

授权身份键（`principal_type/principal_id/scope_type/project_id`）不可通过通用编辑接口原地改变。范围变更必须是“撤销旧授权 + 新增新授权”两个审计动作，避免一次 `UPDATE` 在唯一键间移动导致混乱。

| 命令 | 安全写法 |
|---|---|
| Grant | 先标准化范围键、校验主体存在和操作者的高权限资格；事务内按活跃身份键查行并锁定。存在停用行则以 version 条件更新为启用；不存在则 INSERT。并发 INSERT 由唯一键裁决，捕获 Duplicate Key 后重新读取并按幂等/冲突语义返回，绝不静默创建第二行。 |
| Disable / Enable | `UPDATE ... WHERE id = ? AND version = ? AND del_flag = '0'`，更新 `status` 和 `version = version + 1`。停用不释放唯一键，以便同一身份只能有一条可重新启用的当前授权。 |
| Revoke | `UPDATE ... SET del_flag = '1', version = version + 1 ... WHERE id = ? AND version = ? AND del_flag = '0'`。generated `active_project_id` 变为 NULL，随后可以安全地重新 grant。 |
| 批量 Grant/Revoke | 先对所有主体/项目完成授权资格与存在性校验；按稳定身份键排序锁定；任一失败则整个事务回滚，不允许部分授权。 |

`status = 1` 不是撤销：它保留当前身份键并可被显式重新启用；`del_flag = 1` 才是撤销历史。授权计算只接受 `status = '0' AND del_flag = '0'` 的行。

### 11.2 最终 ScopeService、业务域与 Contributor 契约

项目范围不是单一集合。最终接口强制请求方声明业务域和动作，防止 FINANCE、OPERATIONS 的未来 contributor 被错误用于 RESOURCE、CUSTOMER 等全部模块。

```java
public enum PropertyBusinessDomain {
    PROJECT_BASE,
    RESOURCE,
    CUSTOMER_RELATION,
    FINANCE,
    OPERATIONS
}

public enum ProjectBusinessAction { READ, WRITE, EXPORT, OPTION }

public enum ProjectManagementAction {
    READ_PROJECT, WRITE_PROJECT, CREATE_PROJECT,
    READ_MEMBER, WRITE_MEMBER, EXPORT, OPTION
}

public interface PropertyProjectBusinessScopeContributor {
    String sourceCode();

    /** 只可返回该 contributor 明确支持的领域和动作的项目范围。 */
    boolean supports(PropertyBusinessDomain domain, ProjectBusinessAction action);

    PropertyProjectScope resolve(Long userId,
                                 PropertyBusinessDomain domain,
                                 ProjectBusinessAction action);
}

public interface PropertyProjectScopeService {
    PropertyProjectScope resolveBusinessScope(PropertyBusinessDomain domain,
                                               ProjectBusinessAction action);

    PropertyProjectScope resolveManagementScope(ProjectManagementAction action);

    void assertBusinessAccess(Long projectId,
                              PropertyBusinessDomain domain,
                              ProjectBusinessAction action);

    void assertManagementAccess(Long projectId, ProjectManagementAction action);

    void assertCanCreateProject();
}
```

`PropertyProjectScope` 仍以 `NONE`、`EXPLICIT_IDS`、`ALL` 表达，但 `ALL` 必须绑定到调用的 domain 和 action；它不是全局“物业全权限”。服务只能把 Scope 转化为受控 SQL predicate 或受控的单条/批量断言，不能向 Controller 或前端暴露可随意复用的项目 ID 集合。

最终 contributor 规则：

| 来源 | 支持业务域 | 是否需要有效 ProjectMember | 说明 |
|---|---|---|---|
| `PROJECT_MEMBER`（本期实现） | `PROJECT_BASE`、已明确纳入基线的 `RESOURCE` / `CUSTOMER_RELATION` | 是 | 只能返回本人有效成员项目；绝不支持 `FINANCE`、`OPERATIONS` |
| 未来 `FINANCE_*` | 仅 `FINANCE` | 由财务领域策略明确决定 | 即使允许非成员财务专员跨项目访问，也只能返回 FINANCE 项目范围，不能自动进入 RESOURCE/客户/项目管理范围 |
| 未来 `OPERATIONS_*` | 仅 `OPERATIONS` | 由运营领域策略明确决定 | 同上，范围和动作必须按域隔离 |

未注册 contributor、贡献者不支持该 domain/action、或所有 contributor 返回 `NONE` 时，一律拒绝。不可因为用户有 `PROJECT_BASE`、项目管理 `ALL`、或某一项 FINANCE 专项授权，就推导出别的业务域权限。

### 11.3 ProjectMember 的最终授权语义与决策矩阵

ProjectMember 是“人员—项目业务基础资格”记录，而不是系统功能角色，也不是企业项目管理员授权。

一次业务调用的最终允许条件如下：

```text
允许 = 已登录
    AND 具备对应系统功能权限
    AND 对目标业务域/动作存在项目范围
    AND 目标记录 project_id 位于该范围
```

其中，当前基线业务域的范围由“账号绑定的有效员工 + 有效且启用的 ProjectMember”贡献；未来专项域按其 contributor 贡献。管理调用则使用管理范围替代业务范围：

```text
允许管理 = 已登录
        AND 具备 Project / ProjectMember 管理功能权限
        AND 对目标项目存在 management grant（或 SuperAdmin）
```

| 主体状态 | 系统功能权限 | 成员基础资格 | 业务域/专项范围 | 管理授权 | 允许结果 |
|---|---|---|---|---|---|
| 普通成员 | 有资源功能权限 | 有 | RESOURCE 基线范围含目标项目 | 无 | 可操作该项目资源；不可管理项目/成员 |
| 普通成员 | 有项目管理功能权限 | 有 | 任意 | 无 | 仍不可管理；功能权限不等于范围 |
| 企业项目管理员 | 有项目/成员管理功能权限 | 无 | 无 | PROJECT 含目标项目 | 可管理目标项目及成员；不可读取其资源/客户业务数据 |
| 企业管理员 | 有 project:add | 无 | 无 | ALL | 可创建新项目，并能配置首批成员/负责人；仍无自动业务访问 |
| 财务专项人员（未来） | 有财务功能权限 | 可无 | FINANCE contributor 含目标项目 | 无 | 仅可访问财务域；不可访问资源、客户关系或项目管理 |
| 任意主体 | 无对应功能权限 | 任意 | 任意 | 任意 | 拒绝 |
| 任意非超管 | 有功能权限 | 无 | NONE | 无 | 拒绝（默认拒绝） |

`PROJECT_BASE` 只表示可被识别为该项目的基础业务参与者；它不等价于“所有业务域”。本期显式把哪些既有模块视为成员基线域写入 `PROJECT_MEMBER` contributor；新模块若没有明确 domain policy，默认 `NONE`。

### 11.4 Project API 的最终访问语义

必须拆分管理型和业务型 Project 读取，不以一个 `/property/project/options` 同时服务两种人群：

| API 类型 | 使用者 | 功能权限 | Scope | 返回内容 |
|---|---|---|---|---|
| 管理型 Project API（保留现有 CRUD 路由） | 企业项目管理员、SuperAdmin | `property:project:*` | `MANAGEMENT_*` | 可管理项目、成员配置所需最小项目字段 |
| 管理型 Options | 项目/成员管理页面 | `property:project:list` | `MANAGEMENT_OPTION` | 仅当前可管理项目 |
| 业务型 Project Context / Options（新增专用路由） | Building、Room、关系、未来业务页面 | 该业务页面对应的功能权限 | `BUSINESS(domain, OPTION)` | 仅当前领域可访问项目，最小化 ID/编码/名称 |

普通项目成员不应调用管理型 `/property/project/options`；前端也不应把“当前选择项目”作为后端授权凭据。后端业务 API 每次都以当前账号的 `BUSINESS(domain, action)` 重新过滤，前端项目选择仅是查询条件。

### 11.5 高权限角色分配安全策略（基于实际 System 授权路径）

当前角色相关入口至少包括：

| 路径 | 当前入口 | 必须新增的统一拦截 |
|---|---|---|
| 新建角色并附带菜单 | `POST /system/role` → `insertRole` | 校验新角色的 `menuIds` 不含不可委派权限，除非操作者为 SuperAdmin |
| 修改角色菜单/部门范围 | `PUT /system/role/permission` → `updateRolePermission` | 校验目标角色和新旧菜单集合；不能通过编辑普通角色加入敏感菜单 |
| 用户新增携带角色 | `POST /system/user` → `insertUser` → `insertUserRole` | 校验角色集合和操作者，不只检查目标 userId |
| 用户编辑替换角色 | `PUT /system/user` → `updateUser` → `insertUserRole(..., true)` | 同上；先校验再删除旧角色，避免校验失败导致角色丢失 |
| 用户侧授权角色 | `PUT /system/user/authRole` → `insertUserAuth` | 校验被授权角色、目标用户、操作者；提交后同步失效 |
| 角色侧批量授予用户 | `PUT /system/role/authUser/selectAll` → `insertAuthUsers` | 校验 roleId 是否可委派、所有目标用户是否可操作 |
| 角色侧撤销单个/批量用户 | `/authUser/cancel`、`/authUser/cancelAll` | 同样限制不可委派角色，防止非 SuperAdmin 撤销安全管理员或造成越权拒绝服务 |
| 角色删除/状态/基础信息 | `DELETE`、`changeStatus`、`PUT /system/role` | 对敏感角色执行同一策略，并保留现有 SuperAdmin 特殊保护 |

实际 `SysUserServiceImpl.insertUserRole` 目前依据**目标 userId** 是否为 SuperAdmin 来移除内置超管角色；它不是操作者授权层级检查。因此 Phase B 不可把该逻辑当作高权限角色委派保护。

不可委派识别采用“菜单权限闭包 + 固定角色标识”双重规则：

```text
敏感角色 = 内置 SuperAdmin 角色
        OR roleKey 位于安全角色配置集合
        OR role 的任一启用菜单权限命中敏感权限集合

敏感权限集合（首期）=
  system:role:add/edit/remove
  system:user:add/edit/remove
  （其中 system:user:edit 还覆盖现有 /system/user/authRole）
  （其中 system:role:edit 还覆盖现有 /system/role/permission 与 /authUser/*）
  property:projectAdminScope:list/query/add/edit/remove
  以及未来声明为 cross-enterprise / security-admin 的权限
```

实现上应在 System 域提供一个唯一的 `RoleDelegationGuard`（或等价策略 Service），由 Controller 和 Service 共用；Service 层必须作为最终检查点以覆盖内部调用。首期规则是**仅 SuperAdmin**可创建、编辑、授予、撤销敏感角色/权限及管理授权记录。所有允许/拒绝的敏感操作写入审计：操作者、请求来源、目标用户/角色、变更前后 role/menu/grant 身份键、原因、事务结果、追踪 ID。保留 `@Log(BusinessType.GRANT)`，并为授权表维护使用不可篡改的 Property 安全审计记录；通用操作日志不能替代包含前后身份键的授权审计。

### 11.6 撤销、缓存与正在执行写事务的一致性边界

#### B1/B2 的强制策略：授权范围不做正向缓存

B1/B2 的安全路径中，`PropertyProjectScopeService` 每次请求直接读取数据库中的有效成员关系、有效管理授权和当前有效角色；不缓存“允许访问”的 scope 结果。这样成员结束、grant revoke、grant disable、账号解绑在数据库提交后，后续请求的范围判断立即以数据库事实为准。

可缓存展示数据，但必须在 scope 过滤**之后**，且缓存 key 不得被无范围调用复用。管理/业务 Options 在 B1/B2 不建议缓存。

现有 `OnlineUserCleanEvent` 的 AFTER_COMMIT + `@Async` 踢下线继续保留，作用是清除 Sa-Token 中旧的菜单/角色快照；它是防御加固，不是项目范围撤销的安全前提。对管理型高风险 API，除 `@SaCheckPermission` 外，scope 决策还要使用数据库当前角色/授权状态进行二次防线，避免异步会话清理前旧 `LoginUser` 快照单独放行。

#### 线性化边界

* 授权判定在写事务内、目标数据锁定前完成，并在不可逆写入前再次确认目标仍位于范围。
* 授权撤销提交后发起的请求必须拒绝；已在撤销提交前完成最终授权判定的事务可以继续提交。这是数据库事务无法回溯已执行操作的明确边界，不能声称“撤销会取消在途事务”。
* 对需要更强人工处置的极端场景（例如紧急冻结），应引入独立的同步全局/项目冻结状态，并让每个写命令在最终写入前检查；不应误用异步踢线实现该能力。
* 成员负责人一致性仍由现有项目行锁处理；授权 grant 的 grant/revoke 由第 11.1 节唯一键、行锁和 version 处理。两类锁解决的对象不同，不能互相替代。

#### B3 之后的缓存前提

只有在拥有“每个受影响用户的单调授权 revision、事务提交后同步递增、每次读取先验证 revision”的完整机制后，才可缓存正向 scope。角色范围授权会影响多个用户，必须在同一授权变更流程内枚举并更新全部受影响用户 revision；如果无法保证此 fan-out 的事务一致性，则继续保持 B1/B2 的数据库直读，而不是以 TTL 接受撤销窗口。

### 11.7 B1/B2 交付与验收边界

| 项目 | B1 完成定义 | B2 完成定义 | 联合最终验收 |
|---|---|---|---|
| Scope 基础设施 | 最终 domain-aware 接口、NONE 默认拒绝、Project/Member 所有现有入口已接入 | USER/ROLE + ALL/PROJECT 管理 grant 接入 | 所有管理/业务/Options/导出使用正确 scope |
| 数据库 | 不以临时 SuperAdmin-only 数据模型作为产品状态 | 执行第 11.1 节 DDL、迁移和审计模型 | 并发重复 grant、撤销再授权、逻辑删除历史均通过 |
| 管理能力 | 可在受控环境验证 IDOR 被拒绝 | 企业管理员无需成员身份即可管理指定/ALL 项目，ALL 可创建新项目 | 不能把“仅 SuperAdmin”视为正式用户验收结果 |
| 高权限治理 | `RoleDelegationGuard` 已覆盖全部表列入口并有测试桩 | 安全角色、菜单、grant 授权 UI/API 与审计完成 | 自提权、跨人提权、撤销安全角色均不可绕过 |
| 一致性 | DB 直读范围；撤销后新请求拒绝 | AFTER_COMMIT 会话清理、绑定变更联动完成 | 双会话撤销和在途事务边界测试通过 |

因此 B1 和 B2 可以按依赖并行开发，但必须合并进行数据库迁移、前后端联调、权限回归和业务验收；B1 的受控安全修复不是最终产品发布版本。

### 11.8 A.1 直接实施清单

1. 以第 11.1 节 DDL 创建授权表及对应迁移，不使用 nullable project_id 或 `del_flag` 直拼唯一键。
2. 落地第 11.2 节最终接口，先实现 `PROJECT_MEMBER` contributor；Finance/Operations 仅保留 enum 和默认拒绝，不建空授权表。
3. 按第 11.3、11.4 节为 Project/ProjectMember 接入管理 scope，并新增业务型 Project Context/Options，不复用管理型 Options。
4. 用聚合命令收敛负责人写入，完成“企业管理员创建 + 首批成员 + 负责人”单事务流程。
5. 实现 `RoleDelegationGuard` 并覆盖第 11.5 节全部现有路径；补齐授权审计。
6. B1/B2 使用数据库直读 scope，按第 11.6 节定义撤销边界；完成所有联合验收矩阵后再进入 B3 缓存优化。
