# TASK-AUTH-016-WH-FIX-4 验收报告

## 1. 结论

仓库前端操作入口已与后端 Controller 权限契约对齐。四类单据的查看、编辑、删除、确认、完成、取消、导出均按独立权限显示；编辑、删除、确认、完成和取消同时保留单据状态条件。库区、库位遗漏的行级修改/删除权限控制也已补齐。

本次没有修改后端权限码、Controller、资源范围、单据状态机、库存事务、角色矩阵、数据库或 Migration。

## 2. 修改文件

前端工程 `E:/space/project/zbm-platform-ui`：

- `src/views/warehouse/inbound/index.vue`
- `src/views/warehouse/outbound/index.vue`
- `src/views/warehouse/transfer/index.vue`
- `src/views/warehouse/stocktake/index.vue`
- `src/views/warehouse/zone/index.vue`
- `src/views/warehouse/location/index.vue`
- `src/views/warehouse/warehouseOperationPermissions.test.ts`

后端工程：

- `docs/security/auth016-wh-fix4-validation.md`

## 3. 单据页面按钮权限矩阵

| 操作 | 入库 | 出库 | 调拨 | 盘点 | 状态限制 |
| --- | --- | --- | --- | --- | --- |
| 查看详情 | `warehouse:inbound:query` | `warehouse:outbound:query` | `warehouse:transfer:query` | `warehouse:stocktake:query` | 无额外状态限制 |
| 新增 | `warehouse:inbound:add` | `warehouse:outbound:add` | `warehouse:transfer:add` | `warehouse:stocktake:add` | 新单入口 |
| 编辑 | `warehouse:inbound:edit` | `warehouse:outbound:edit` | `warehouse:transfer:edit` | `warehouse:stocktake:edit` | 仅 `DRAFT` |
| 删除 | `warehouse:inbound:remove` | `warehouse:outbound:remove` | `warehouse:transfer:remove` | `warehouse:stocktake:remove` | 仅 `DRAFT` |
| 确认 | `warehouse:inbound:confirm` | `warehouse:outbound:confirm` | `warehouse:transfer:confirm` | `warehouse:stocktake:confirm` | 仅 `DRAFT` |
| 完成 | `warehouse:inbound:complete` | `warehouse:outbound:complete` | `warehouse:transfer:complete` | `warehouse:stocktake:complete` | 仅 `CONFIRMED` |
| 取消 | `warehouse:inbound:cancel` | `warehouse:outbound:cancel` | `warehouse:transfer:cancel` | `warehouse:stocktake:cancel` | `DRAFT` 或 `CONFIRMED` |
| 导出 | `warehouse:inbound:export` | `warehouse:outbound:export` | `warehouse:transfer:export` | `warehouse:stocktake:export` | 无额外状态限制 |

`list` 只负责页面列表加载，没有被用于查看、编辑或状态操作按钮。拥有 add 不会显示 confirm/complete；confirm 与 complete 也互不替代。

## 4. 实际修复点

- 入库：为“查看”补充 `warehouse:inbound:query`。
- 出库：为“查看、编辑、取消、删除”分别补充 query/edit/cancel/remove。
- 调拨：为“查看、编辑、取消、删除”分别补充 query/edit/cancel/remove。
- 盘点：原有八类操作权限与状态条件已完整，保留现状；仅调整失败反馈，避免覆盖后端准确原因。
- 库区：行级“修改、删除”补充 edit/remove；工具栏和状态切换原本已受控。
- 库位：行级“修改、删除”补充 edit/remove；工具栏和状态切换原本已受控。
- 库存余额、库存流水：详情 query、导出 export 原本已受控，无需修改。
- 仓库、物料、物料分类、计量单位：新增、修改、删除、状态切换原本已按 Controller 权限控制，无需修改。

## 5. 状态与后端安全边界

前端只在“权限 + 合法状态”同时满足时展示状态操作：

- 草稿：编辑、删除、确认、取消。
- 已确认：完成、取消。
- 已完成或已取消：不展示编辑、删除、确认、完成、取消。

前端指令只负责交互入口。直接调用接口仍由各 Controller 的 `@SaCheckPermission` 拒绝无权限请求；Service 的状态机、仓库范围、项目范围、库存不足、重复确认与并发约束没有改动。SuperAdmin 可通过既有通配权限看到合法操作，但按钮仍受相同状态条件约束。

## 6. 异常反馈

现有公共请求层继续按响应类型处理：

- 401：明确提示登录状态过期，并提供重新登录。
- 权限不足：展示后端权限异常消息。
- 仓库/项目资源范围不足：展示后端资源范围校验消息。
- 单据状态不允许、库存不足等业务异常：展示后端业务消息。
- 服务器异常：展示服务端安全消息，不在页面输出堆栈、SQL、Token。
- 网络异常：使用统一提取后的网络错误消息。

四类单据新增/编辑加载引用数据失败时，不再用“无权限或资源不在范围”这一组合文案覆盖真实后端原因。公共请求拦截器已负责展示准确消息，页面只停止打开编辑弹窗。

## 7. 自动化测试与构建

| 验证项 | 结果 |
| --- | --- |
| 前端操作权限静态契约测试 | 通过，9/9 |
| 前端 Lint | 通过，0 error |
| 前端生产构建 | 通过，Vite 8.2.2，3442 modules transformed |
| `PermissionContractAuditTest` | 通过，6/6 |
| 仓库后端回归测试 | 通过，127/127，0 failure，0 error，0 skipped |
| TypeScript `vue-tsc --noEmit` | 未通过；仍为既有 10 个无关基线错误，本任务未新增错误 |

新增前端测试逐页校验四类单据均声明 query/add/edit/confirm/complete/cancel/remove/export，并校验 edit、confirm、complete、cancel、remove 同时存在状态和权限条件；同时覆盖库区、库位行级权限。

后端回归继续验证 Controller 权限、仓库范围、状态机与库存变更约束。权限契约接口总数保持 292，仓库接口保持 130，本任务没有改变后端契约。

## 8. 真实浏览器验收

本次未使用不同权限组合的真实账号执行浏览器 E2E，因此不宣称真实登录态验收通过。建议使用临时角色分别只授予 list、query、add、confirm、complete、cancel、export，并针对 DRAFT、CONFIRMED、COMPLETED、CANCELLED 单据检查按钮组合和直接接口拒绝。

## 9. 未处理的历史问题

1. TypeScript 全量检查仍有 10 个既有错误：房屋收费设置 6 个、入库页面既有 `DefaultRow` 类型错误 4 个。按照任务边界未顺带修复。
2. 基础资料新增/编辑表单的跨模块引用数据权限耦合属于 FIX-2 报告中的后续 MAINTAIN Context 工作，本次未处理。
3. 未执行真实浏览器多权限组合验收。

