# TASK-AUTH-013 工作项目上下文交付报告

## 实际调用链

整改前的登录后调用链为：

`src/permission.ts` → `useCurrentProjectStore().initialize()` → `loadAvailableProjects()` → `businessProjectOptions()` → `GET /property/project/business-options`。

该初始化在所有已登录用户生成动态路由后执行；旧接口由 `ProjectController.businessOptions()` 使用
`property:project:list` 保护，因此收费员、客服、HR-only 和公司仓用户都会被无差别带入项目管理权限判断。
顶部 `Navbar` 同时对所有桌面用户渲染项目选择器。

## 设计与接口

新增 `GET /property/workspace/projects`：

- 仅使用现有物业业务权限，采用 OR 语义，不新增权限码；
- 服务端以静态策略将真实权限映射到 `PROJECT_BASE`、`RESOURCE`、`CUSTOMER_RELATION`、`FINANCE`；
- 每个实际拥有的业务域分别调用 `PropertyProjectScopeService.resolveBusinessScope(domain, OPTION)`，再合并范围；
- 不把 FINANCE、RESOURCE 或普通成员范围互相冒充；
- 不接收前端业务场景或权限字符串；
- `WAREHOUSE` 和 HR 权限不触发全局物业工作项目，仓库继续使用 `WarehouseAccessService` 和各单据 Context；
- 无有效项目返回空列表，不回退全量；SuperAdmin 保留原范围规则；
- 仅返回项目 ID、编码、名称、简称，ID 明确序列化为字符串。

工作项目只是前端偏好和默认筛选。所有业务页面继续调用自身 BusinessContext 接口，后端继续执行项目/仓库逐资源校验。

## 前端状态

- 无相关业务权限：初始化完成但不调用任何物业项目 API，顶部选择器不显示。
- 0 项目：显示“暂无可用项目”，不选择任何项目。
- 1 项目：自动选择唯一项目并锁定选择器。
- N 项目：恢复仍有效的本地选择；无保存选择时保持未选择，不默认第一项。
- 已保存项目失效：清除本地选择并提示重新选择。
- 切换项目：切换前提示保存正在编辑的内容；现有页面对 `currentProjectId` 的 watch 会取消旧请求、清理明细/筛选并重新加载。
- 旧 `/property/project/business-options` 与项目管理 Options 保持兼容，但登录初始化已无调用者。

## 文件

后端：

- `ruoyi-modules/ruoyi-property/src/main/java/org/dromara/property/workspace/controller/PropertyWorkspaceController.java`
- `ruoyi-modules/ruoyi-property/src/main/java/org/dromara/property/workspace/domain/vo/WorkspaceProjectOptionVo.java`
- `ruoyi-modules/ruoyi-property/src/main/java/org/dromara/property/workspace/service/PropertyWorkspaceProjectService.java`
- `ruoyi-modules/ruoyi-property/src/main/java/org/dromara/property/workspace/service/WorkspaceProjectPermissionPolicy.java`
- `ruoyi-modules/ruoyi-property/src/main/java/org/dromara/property/workspace/service/impl/PropertyWorkspaceProjectServiceImpl.java`
- `ruoyi-modules/ruoyi-property/src/main/java/org/dromara/property/project/mapper/ProjectMapper.java`
- `ruoyi-modules/ruoyi-property/src/test/java/org/dromara/property/workspace/**`

前端：

- `src/api/property/workspace/index.ts`
- `src/api/property/workspace/types.ts`
- `src/store/modules/currentProjectPolicy.ts`
- `src/store/modules/currentProject.ts`
- `src/store/modules/currentProject.test.ts`
- `src/layout/components/CurrentProjectSwitcher.vue`
- `src/layout/components/Navbar.vue`

## 验证结果

- 后端编译与打包：通过。
- 工作区授权后端单测：7/7 通过。
- 前端 0/1/N、HR-only 不请求、保存项目恢复、撤权失效：6/6 通过。
- 前端生产构建：通过。
- `PermissionContractAuditTest`：6/6 通过。
- 最新权限契约：266 个 Controller 接口；新增接口使用 21 个既有权限的 OR 组合，无新权限标识。
- 旧 `project/business-options` 仍在契约耦合基线中，源码扫描确认前端已没有调用者。
- 本地真实 API：`test_finance` 返回 A、B 两个项目；`test_no_project` 返回空列表；仓库角色调用工作项目接口被拒绝。

## 浏览器验收状态

本地前端登录页和新构建已在浏览器中打开。`test_cashier` 的真实登录验收未能完成：数据库中该账号的密码哈希已偏离 AUTH-010B 当前凭据文件，且连续失败后账号被临时锁定；`test_customer` 也存在独立的密码漂移。为避免未经授权重置既有测试账号密码，本任务没有修改数据库凭据。

这不影响自动化的单项目状态和授权范围验证，但 `test_cashier` 的真实浏览器“自动选中并锁定”仍需在恢复该账号有效凭据后补验。

## 数据库影响

本任务未执行 SQL，未修改 V2.0.34、角色权限、项目成员、仓库授权或任何业务数据。
