# TASK-AUTH-016-WH-FIX-5D 验收报告

## 1. 结论

仓库、库区、库位、物料分类、计量单位、物料、入库、出库、调拨、盘点十个页面已接入统一页面操作模式。页面不再在初始化时无条件请求列表：

- 默认模式仅在拥有本模块 `list` 权限时加载列表和 QUERY Context。
- `mode=add` 仅在拥有本模块 `add` 权限时打开新增表单，并使用既有 MAINTAIN Context。
- `mode=edit&id=...` 仅在拥有本模块 `edit` 权限时调用 FIX-5C 编辑初始化接口。
- 查看详情仍由列表中的查看入口调用原 `query` 接口，没有使用编辑初始化接口代替详情。
- 路由参数只决定前端导航模式，Controller 权限和资源范围仍是最终安全边界。

本任务没有修改后端 Controller、权限码、数据库、Migration、角色矩阵、库存事务或单据状态机。

## 2. 统一模式实现

新增：

- `src/views/warehouse/warehousePageModePolicy.ts`：无副作用的模式与权限决策函数。
- `src/views/warehouse/useWarehousePageMode.ts`：读取当前会话权限和路由参数，调度页面 list/add/edit 回调。
- `src/views/warehouse/useWarehousePageMode.test.ts`：0/1 模式与权限组合测试。
- `src/views/warehouse/warehousePageModeIntegration.test.ts`：十个页面接入契约测试。

仅接受以下显式模式：

| 路由 | 最小权限 | 页面行为 |
| --- | --- | --- |
| 无 `mode` 或未知 `mode` | `<module>:list` | 加载列表及对应 QUERY Options |
| `?mode=add` | `<module>:add` | 打开空白新增表单并加载 MAINTAIN Options |
| `?mode=edit&id=<recordId>` | `<module>:edit` | 调用 FIX-5C `/{id}/context/edit` 并加载 MAINTAIN Options |

缺少权限时不会降级到其他模式：add 无权限不会加载列表；edit 无权限不会尝试 query；默认模式无 list 权限不会请求列表。edit 缺少 ID 时明确提示“编辑模式缺少目标记录 ID”。

## 3. 十个页面实现状态

| 页面 | 列表模式 | 新增模式 | 编辑模式 | 编辑初始化 |
| --- | --- | --- | --- | --- |
| 仓库 | `warehouse:warehouse:list` | `warehouse:warehouse:add` | `warehouse:warehouse:edit` | `/warehouse/warehouse/{id}/context/edit` |
| 库区 | `warehouse:zone:list` | `warehouse:zone:add` | `warehouse:zone:edit` | `/warehouse/zone/{id}/context/edit` |
| 库位 | `warehouse:location:list` | `warehouse:location:add` | `warehouse:location:edit` | `/warehouse/location/{id}/context/edit` |
| 物料分类 | `warehouse:materialCategory:list` | `warehouse:materialCategory:add` | `warehouse:materialCategory:edit` | `/warehouse/materialCategory/{id}/context/edit` |
| 计量单位 | `warehouse:unit:list` | `warehouse:unit:add` | `warehouse:unit:edit` | `/warehouse/unit/{id}/context/edit` |
| 物料 | `warehouse:material:list` | `warehouse:material:add` | `warehouse:material:edit` | `/warehouse/material/{id}/context/edit` |
| 入库 | `warehouse:inbound:list` | `warehouse:inbound:add` | `warehouse:inbound:edit` | `/warehouse/inbound/{id}/context/edit` |
| 出库 | `warehouse:outbound:list` | `warehouse:outbound:add` | `warehouse:outbound:edit` | `/warehouse/outbound/{id}/context/edit` |
| 调拨 | `warehouse:transfer:list` | `warehouse:transfer:add` | `warehouse:transfer:edit` | `/warehouse/transfer/{id}/context/edit` |
| 盘点 | `warehouse:stocktake:list` | `warehouse:stocktake:add` | `warehouse:stocktake:edit` | `/warehouse/stocktake/{id}/context/edit` |

十个页面的列表函数还增加了 `canList` 硬保护。因此即使页面内部保存、删除或状态操作完成后调用刷新函数，add-only/edit-only 会话也不会意外发出列表请求。

## 4. 页面展示与生命周期

- 无 list 权限时隐藏列表搜索区、列表表格和查询工具栏。
- 入库、出库、调拨、盘点无 list 权限时同时隐藏导出入口；盘点查询表单也被隐藏。
- 新增按钮继续由原 `v-hasPermi` 的 add 权限控制。
- 查看按钮继续由 query 权限控制，编辑按钮继续由 edit 权限控制。
- 路由的 `mode` 或 `id` 改变时重新计算入口模式。
- 重新初始化前关闭旧弹窗，清空旧列表、表单、仓库/物料/库位候选项，避免跨记录残留。
- Options 与核心数据仍保持原有错误边界；路由切换不会把上一记录数据留在新表单中。

## 5. 各模式 API 调用

### list

调用本模块 `/list` 和此前建立的 QUERY Context。不会调用 MAINTAIN Context 或编辑初始化接口。

### add

不调用 `/list`、原详情或编辑初始化接口。仅初始化空表单，调用本模块已有 MAINTAIN Context，提交时调用原 `POST` 新增接口。

### edit

不调用 `/list` 或原 query 详情。调用本模块 `/{id}/context/edit`，并加载本模块已有 MAINTAIN Context；保存仍调用原 `PUT` 接口，由服务端重新校验 edit 权限、资源范围、真实归属和状态。

### detail

仍由列表内明确查看动作进入并调用原 `GET /{id}`；必须具备 query 权限。没有为 query-only 用户增加编辑入口。

## 6. 合法编辑入口及限制

当前实现支持同一页面的显式入口：

```text
<module-page>?mode=edit&id=<known-record-id>
```

该 ID 必须来自其他已授权业务流程、通知、待办或管理员明确提供的链接。当前仓库系统不存在面向 edit-only 用户的通用“可编辑记录发现”机制，本任务也没有新增枚举接口。

因此：

- 拥有 list+edit 的用户仍可从列表行进入编辑。
- edit-only 用户只有在获得合法、明确的目标链接时才能编辑。
- 手工修改 ID 不会扩大权限；FIX-5C 接口根据数据库真实记录重新校验公司仓、项目仓及单据状态。
- 四类单据非 `DRAFT` 状态仍由服务端拒绝。

## 7. 自动化与构建结果

| 项目 | 结果 |
| --- | --- |
| 页面模式纯函数及现有按钮权限测试 | 通过 |
| 十页面模式接入测试 | 通过 |
| 前端相关测试合计 | 23 项通过，0 失败 |
| 前端 lint | 通过 |
| 前端生产构建 | 通过，3444 个模块完成转换 |
| `PermissionContractAuditTest` | 6 项通过，后端权限契约未变化 |
| TypeScript 类型检查 | 未通过；仍为任务前已知 10 个错误，本次无新增错误 |

TypeScript 历史错误仍位于：

- `src/views/property/charge/binding/index.vue`：6 个。
- `src/views/warehouse/inbound/index.vue` 模板：4 个 `DefaultRow` / `InboundVO` 类型错误。

本任务按边界未修改这些既有问题。

## 8. 未覆盖组合与真实浏览器验收

未使用真实 add-only、edit-only、list-only、query-only 和 SuperAdmin 会话完成浏览器 E2E，因此不宣称真实登录态验收通过。DB-1 / TEST-1 应至少验证：

1. add-only 直接进入 `mode=add`，网络面板没有 list/query 请求。
2. edit-only 通过合法链接进入，网络面板只有 edit-context 与 MAINTAIN 请求。
3. list-only 默认进入列表，不请求 MAINTAIN 或 edit-context。
4. query-only 手工输入 add/edit URL 被前端拒绝，直接调用后端也返回 403。
5. edit-only 修改为越权 ID、非 DRAFT 单据 ID 时服务端拒绝。
6. 页面内切换 add/edit/list 后没有旧记录或旧候选项残留。
7. SuperAdmin 可进入全部合法模式，但仍受资源存在性、状态机和库存规则约束。

## 9. 后续阶段

代码层面已具备进入 DB-1 与 TEST-1 的条件。本任务未自动修改数据库或创建测试授权；后续应使用隔离测试角色和明确测试记录进行真实权限组合验证。
