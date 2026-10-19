# V1.0 统一权限体系技术说明

## 1. 范围与结论

当前系统采用多层授权，而不是单纯的菜单权限：Sa-Token 身份与权限码负责功能准入，物业项目/仓库服务负责数据范围，BusinessContext 负责受控引用数据，客户策略负责主档授权和 PII 审计。前端只承担路由、按钮和上下文体验，后端是最终授权方。

实现状态为“已实现”，但正式数据库中的角色菜单 migration 执行状态和浏览器岗位 E2E 尚未确认。

## 2. 身份与功能权限

### Sa-Token 与 SuperAdmin

- 登录态和权限检查使用 Sa-Token；Controller 主要通过 `@SaCheckPermission`。
- `SysPermissionServiceImpl` 从用户角色和菜单计算权限；SuperAdmin 获得 admin/通配能力。
- 业务模块通过 `LoginHelper.isSuperAdmin()` 实现显式范围旁路。
- SuperAdmin 是安全边界，不等同于“忽略所有业务规则”；状态机、数据合法性、审计等仍按具体实现执行。

### 用户、角色、菜单

```text
sys_user ── sys_user_role ── sys_role ── sys_role_menu ── sys_menu.perms
                                  └──── sys_role_dept（部门数据范围）
```

- Controller 权限：API 入口校验。
- Service 功能权限：物业核心逻辑可通过 `PropertyFunctionalPermissionServiceImpl` 等再次读取当前权限。
- 前端权限：`v-hasPermi` 与动态路由提供 UI 控制，不构成安全边界。
- 角色委派：`RoleDelegationGuardImpl` 限制 admin、SuperAdmin 及包含敏感权限角色的定义和分配。

## 3. 物业项目数据权限

唯一边界接口 `PropertyProjectScopeService` 提供：

- `resolveBusinessScope(domain, action)`：解析业务项目集合；
- `resolveManagementScope(action)`：解析项目管理授权；
- `assertBusinessAccess(projectId, domain, action)`：校验具体业务项目；
- `assertManagementAccess(projectId, action)`：校验具体管理项目；
- `assertCanCreateProject()`：项目创建要求管理范围为 ALL。

业务域 `PropertyBusinessDomain`：`PROJECT_BASE`、`RESOURCE`、`CUSTOMER_RELATION`、`FINANCE`、`WAREHOUSE`、`OPERATIONS`。动作 `ProjectBusinessAction`：`READ`、`WRITE`、`EXPORT`、`OPTION`。

当前 `ProjectMemberBusinessScopeContributor` 支持前五个域，不支持 `OPERATIONS`；无 contributor 时返回 NONE。它将登录用户映射到 ACTIVE 员工，再读取 ACTIVE 且启用的项目成员关系。因此未绑定有效员工或没有有效成员关系的普通用户获得空业务范围。

项目管理授权与项目成员业务范围是两条不同链路：管理授权从用户或启用角色的 `pm_project_management_grant` 实时合并，可为 ALL 或显式项目；业务访问当前由项目成员 contributor 提供。SuperAdmin 两者均为 ALL。

## 4. BusinessContext

`BusinessContextAccessServiceImpl.requireAccess(context, referenceType)` 的顺序是：

1. `StpUtil.checkLogin()`；
2. 从 `BusinessContextPolicy` 查找上下文；
3. 检查 `ReferenceType` 是否在允许集合；
4. 按 `PermissionMode.ANY/ALL` 检查所需权限；
5. 任一未满足则抛出 `NotPermissionException`。

未登记上下文、空引用类型、未声明引用类型和缺少权限全部默认拒绝。该服务不读取引用数据，也不替代项目、公司、仓库或具体资源校验。

| 上下文组 | 上下文 | 权限模式 | 主要引用类型 |
|---|---|---|---|
| 客户关系 | LIST/CREATE/EDIT | ALL | 项目、楼栋、单元、房屋；创建/编辑另含客户 |
| 住户 | RESIDENT_QUERY | ANY | 楼栋、单元、房屋 |
| 应收 | QUERY/GENERATE | ANY/ALL | 项目、资源、客户、应收、收费项目/规则 |
| 收款 | PAYMENT_CREATE | ALL | 项目、房屋、客户、应收；要求 add 与 allocate |
| 收费绑定 | QUERY/CREATE/EDIT | ALL | 项目、房屋、收费项目/规则 |
| 仓储单据 | INBOUND/OUTBOUND/TRANSFER/STOCKTAKE 查询与维护 | ANY | 仓库；维护另含库区、库位、物料、库存等 |
| 仓储主数据 | 库区/库位/分类/物料查询与维护 | ANY | 公司、仓库、库区、分类、单位 |
| 库存 | 余额/流水查询 | ANY | 公司、仓库、库区、库位、分类、单位、物料 |

精确权限与 ReferenceType 集合以 `BusinessContextPolicy.java` 为准。

## 5. 仓库数据权限

`WarehouseAccessService` 按仓库类型执行：

- `COMPANY`：调用 `ISysDeptService.checkDeptDataScope(companyId)`；
- `PROJECT`：通过 `ProjectReferenceService.resolveWarehouseScope()` 使用物业 `WAREHOUSE/READ` 范围；
- SuperAdmin：显式跳过范围过滤；
- 列表：`applyScope` 组合公司集合与项目集合，无任何范围时追加不可能命中条件；
- 详情/操作：`requireWarehouse` 校验存在与范围，新增下级资料使用 `requireEnabledWarehouse` 再要求启用；
- 物料：通过 `accessibleMaterialCompanyIds` 合并部门公司和可访问项目仓库对应公司。

仓库 Controller/Service 仍需各自功能权限与 BusinessContext。物业全局工作项目选择器不控制仓库授权。

## 6. 客户敏感数据权限

客户权限由 `CustomerAccessPolicyImpl` 组合：

- 普通主档操作需要 `property:customer:*` 功能权限和有效用户/角色企业客户主档 grant；
- PII 查看需要 `property:customer:pii:view`；
- PII 导出需要 `property:customer:pii:export`，同时入口仍使用 `property:customer:export`；
- 敏感权限实时调用 `ISysMenuService.selectMenuPermsByUserId`，不依赖登录时快照；
- 允许、拒绝和导出生成失败写入 `pm_customer_sensitive_access_audit`；审计不能成功时敏感操作按 fail-closed 设计。

证件号写入前由 AES 字段加密服务处理，并以 HMAC-SHA-256 规范化指纹支持唯一性检测。`CustomerSecurityConfiguration` 要求 mybatis encryptor 启用、算法为 AES，且密钥/指纹配置完整；缺失时应用启动失败。

`RoleDelegationGuardImpl` 将 PII 和客户管理授权权限列为敏感权限，非 SuperAdmin 不能委派相关角色。

## 7. 工作项目初始化与前后端职责

登录后 `src/permission.ts` 先拉取用户信息、生成动态路由，再调用 `currentProjectStore.initialize()`。`currentProjectPolicy.ts` 仅在权限包含物业白名单或 `*:*:*` 时请求 `/property/workspace/projects`。

后端 Controller 使用同一组物业权限的 OR 校验；Service 再按每个允许业务域的 OPTION 范围聚合项目。单项目自动选择，多项目保留用户选择；缓存 key 包含 userId；失效选择会清除并提示。

职责边界：

| 前端 | 后端 |
|---|---|
| 决定是否需要显示/初始化物业工作项目 | 判断用户是否能调用候选接口 |
| 保存当前用户 UI 偏好 | 从真实业务域范围生成候选项目 |
| 单项目自动选中、多项目切换 | 对每个业务 API 的 projectId 再授权 |
| 按权限隐藏按钮 | Controller/Service 最终拒绝越权 |

仓储账号不因 `warehouse:*` 自动初始化物业工作项目；仓储使用自己的公司/项目/仓库上下文。

## 8. 防回归测试与已知风险

| 检查 | 状态 | 证据 |
|---|---|---|
| 四方权限码契约 | 通过 | `script/audit/Test-PermissionContract.ps1`、`PermissionContractAuditTest` |
| BusinessContext 映射 | 通过 | `BusinessContextAccessServiceTest` |
| 工作项目白名单 | 通过 | `WorkspaceProjectPermissionPolicyTest`、前端 currentProject 测试 |
| 仓库访问 | 通过已有 UT | `WarehouseAccessServiceTest`、上下文 Controller 测试 |
| 客户敏感安全 | 主路径通过、3 项环境阻塞 | 002B 集成测试报告 |
| 正式角色菜单数据 | 未确认 | migration history 不可证明 |
| 浏览器 E2E | 未执行 | 无报告 |

主要风险：手工 migration 漂移、工作项目白名单新增遗漏、新 Service 绕过统一范围服务、登录期前端权限快照陈旧。后端实时校验仍是防线。

## 9. 代码证据

- `ruoyi-modules/ruoyi-system/src/main/java/org/dromara/system/service/impl/SysPermissionServiceImpl.java`
- `ruoyi-modules/ruoyi-system/src/main/java/org/dromara/system/service/impl/RoleDelegationGuardImpl.java`
- `ruoyi-common/ruoyi-common-security/src/main/java/org/dromara/common/security/business/`
- `ruoyi-modules/ruoyi-property/src/main/java/org/dromara/property/scope/`
- `ruoyi-modules/ruoyi-property/src/main/java/org/dromara/property/customer/service/impl/CustomerAccessPolicyImpl.java`
- `ruoyi-modules/ruoyi-property/src/main/java/org/dromara/property/customer/security/`
- `ruoyi-modules/ruoyi-warehouse/src/main/java/org/dromara/warehouse/security/WarehouseAccessService.java`
- `E:\space\project\zbm-platform-ui/src/store/modules/currentProject.ts`
- `docs/audit/V1.0-权限体系审计.md`
- `docs/audit/V1.0-权限契约检查报告.md`
