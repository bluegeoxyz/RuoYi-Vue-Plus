# TASK-AUTH-016-WH-FIX-5C 验收报告

## 1. 结论

已为仓库、库区、库位、物料分类、计量单位、物料、入库、出库、调拨、盘点十个模块增加独立编辑初始化接口。接口仅接受对应模块的 `edit` 权限，查看详情仍只接受原 `query` 权限，列表、导出、删除、确认和完成权限均未合并或放宽。

前端编辑操作已切换到编辑初始化接口；只读查看仍调用原详情接口；新增表单继续使用原 MAINTAIN Context，不调用编辑初始化接口。

本任务未修改数据库、Migration、菜单权限码、七角色默认矩阵、库存写入逻辑或单据状态机。

## 2. 后端接口清单

| 模块 | 编辑初始化接口 | 权限 | 原详情接口 |
| --- | --- | --- | --- |
| 仓库 | `GET /warehouse/warehouse/{id}/context/edit` | `warehouse:warehouse:edit` | `GET /warehouse/warehouse/{id}`，仍为 `query` |
| 库区 | `GET /warehouse/zone/{id}/context/edit` | `warehouse:zone:edit` | `GET /warehouse/zone/{id}`，仍为 `query` |
| 库位 | `GET /warehouse/location/{id}/context/edit` | `warehouse:location:edit` | `GET /warehouse/location/{id}`，仍为 `query` |
| 物料分类 | `GET /warehouse/materialCategory/{id}/context/edit` | `warehouse:materialCategory:edit` | `GET /warehouse/materialCategory/{id}`，仍为 `query` |
| 计量单位 | `GET /warehouse/unit/{id}/context/edit` | `warehouse:unit:edit` | `GET /warehouse/unit/{id}`，仍为 `query` |
| 物料 | `GET /warehouse/material/{id}/context/edit` | `warehouse:material:edit` | `GET /warehouse/material/{id}`，仍为 `query` |
| 入库 | `GET /warehouse/inbound/{id}/context/edit` | `warehouse:inbound:edit` | `GET /warehouse/inbound/{id}`，仍为 `query` |
| 出库 | `GET /warehouse/outbound/{id}/context/edit` | `warehouse:outbound:edit` | `GET /warehouse/outbound/{id}`，仍为 `query` |
| 调拨 | `GET /warehouse/transfer/{id}/context/edit` | `warehouse:transfer:edit` | `GET /warehouse/transfer/{id}`，仍为 `query` |
| 盘点 | `GET /warehouse/stocktake/{id}/context/edit` | `warehouse:stocktake:edit` | `GET /warehouse/stocktake/{id}`，仍为 `query` |

所有路径由服务端固定，不接受前端传入 BusinessContext 或权限字符串。实际更新仍调用原 `PUT` 接口并再次执行 `edit` 权限、资源范围、状态和业务规则校验。

## 3. EditVO 字段审查

新增 `WarehouseEditInitializationVo`，按模块定义十类专用 EditVO，没有直接返回 Entity。

| 类型 | 返回的编辑必要字段 | 明确未返回 |
| --- | --- | --- |
| 仓库 | ID、公司/项目、仓库范围、编码、名称、负责人、联系方式、地址、状态、排序、备注 | 公司/项目展示名、审计字段、业务历史 |
| 库区 | ID、仓库、编码、名称、状态、排序、备注 | 仓库展示详情、审计字段 |
| 库位 | ID、仓库、库区、编码、名称、状态、排序、备注 | 仓库/库区完整对象、审计字段 |
| 物料分类 | ID、公司、父分类、编码、名称、排序、状态、备注 | 公司展示详情、审计字段 |
| 计量单位 | ID、编码、名称、符号、精度、状态、排序、备注 | 审计字段 |
| 物料 | ID、公司、编码、名称、分类、基础单位、规格、型号、条码、安全库存、状态、排序、备注 | 公司/分类完整对象、审计字段 |
| 入库 | ID、仓库、类型、入库时间、备注、可编辑明细 | 单号、公司/项目展示字段、确认/完成历史、审计字段 |
| 出库 | ID、仓库、类型、出库时间、备注、可编辑明细及当前库存提示值 | 单号、确认/完成历史、审计字段 |
| 调拨 | ID、源/目标仓、类型、调拨时间、备注、可编辑明细及源侧库存提示值 | 单号、公司/项目展示字段、流转历史、审计字段 |
| 盘点 | ID、仓库、类型、范围、库区/库位、盘点时间、备注、可编辑明细 | 单号、确认/完成历史、操作人及审计字段 |

单据 EditVO 不返回完整业务历史；库存提示和账面/差异数量仅保留现有编辑表单校验及展示所需字段，不构成新的库存写入口。

## 4. 资源范围与状态校验

- 基础资料编辑初始化复用各模块现有 `queryById` 资源读取链路；仓库、库区、库位继续通过 `WarehouseAccessService` 校验真实仓库归属。
- 物料与物料分类继续执行现有公司可见性校验；计量单位维持现有全局基础资料规则。
- 入库、出库、盘点读取真实单据仓库后执行仓库范围校验；调拨对源仓和目标仓分别校验。
- 项目仓继续依赖有效项目成员关系，公司仓继续依赖既有公司/部门范围，未使用请求中的 projectId 或 companyId 替代真实记录归属。
- 四类单据只有 `DRAFT` 状态可以取得编辑初始化数据；SuperAdmin 也不能绕过该状态规则。
- 更新提交端点保持原实现并再次检查权限、资源归属及状态，初始化成功不形成后续写操作授权凭证。

## 5. 修改文件

### 后端

- `ruoyi-modules/ruoyi-warehouse/src/main/java/org/dromara/warehouse/context/domain/vo/WarehouseEditInitializationVo.java`
- `ruoyi-modules/ruoyi-warehouse/src/main/java/org/dromara/warehouse/context/service/WarehouseEditInitializationService.java`
- `ruoyi-modules/ruoyi-warehouse/src/main/java/org/dromara/warehouse/context/service/impl/WarehouseEditInitializationServiceImpl.java`
- 十个模块现有 Controller：新增对应 `/{id}/context/edit` 路由。
- `WarehouseEditInitializationControllerTest.java`
- `WarehouseEditInitializationServiceImplTest.java`

### 前端

- `src/api/warehouse/{warehouse,zone,location,materialCategory,unit,material,inbound,outbound,transfer,stocktake}/index.ts`
- `src/views/warehouse/{warehouse,zone,location,materialCategory,unit,material,inbound,outbound,transfer,stocktake}/index.vue`

## 6. 权限覆盖与自动化结果

| 验证项 | 结果 |
| --- | --- |
| 十个 edit-context 均精确绑定本模块 `edit` 权限 | 通过，反射测试覆盖 |
| 原详情接口仍精确绑定本模块 `query` 权限 | 通过，反射测试覆盖 |
| 基础资料复用现有受范围保护的按 ID 读取 | 通过，服务测试验证委托链路 |
| 四类单据非 DRAFT 状态拒绝编辑初始化 | 通过，四种单据均覆盖 |
| 专用 EditVO 不返回展示/历史字段 | 通过，字段映射测试与字段审查 |
| 仓库模块完整测试 | 133 项通过，0 失败、0 错误 |
| `PermissionContractAuditTest` | 6 项通过；契约总数 310，仓库接口 148，新增 edit-context 10 |
| 后端编译 | 通过 |
| 前端 lint | 通过 |
| 前端生产构建 | 通过 |
| 前端 TypeScript 类型检查 | 未通过；仅存在任务前已知的 10 个历史错误，见下节 |

权限反向边界由独立注解契约保证：edit-only 不满足原 query/list/export/remove 端点权限；query-only 不满足 edit-context；写操作继续由原提交接口的 `edit` 注解独立保护。未新增 `OR query` 或角色名称白名单。

## 7. TypeScript 历史问题

`pnpm exec vue-tsc --noEmit` 仍报告 10 个任务前已有错误：

- `src/views/property/charge/binding/index.vue`：6 个，涉及 `DefaultRow`、`ElFormInstance`、`useDict`。
- `src/views/warehouse/inbound/index.vue` 模板：4 个 `DefaultRow` 与 `InboundVO` 类型不匹配。

本次新增 API 和编辑/查看分流没有产生新的 TypeScript 错误。根据任务边界未顺带修改上述历史问题；Vite 生产构建成功。

## 8. 真实登录态验收

本次未使用真实 edit-only、query-only 或 SuperAdmin 登录会话执行浏览器 E2E，因此不宣称真实登录态验收通过。建议 FIX-5D 前使用隔离测试账号完成：

1. edit-only 打开合法记录编辑表单并保存；确认详情、列表、导出、删除仍被拒绝。
2. query-only 查看详情成功、编辑初始化返回 403。
3. 项目 A 用户请求项目 B 单据或基础资料被拒绝。
4. 公司仓跨公司请求被拒绝；调拨源/目标任一侧无权均被拒绝。
5. SuperAdmin 可读取合法资源，但非 DRAFT 单据编辑初始化仍被拒绝。

## 9. 遗留风险与 FIX-5D 准备

- add-only/edit-only 页面导航入口问题仍按任务边界保留；本任务只保证已获得合法记录 ID 后的编辑初始化契约。
- 真实 HTTP 403、跨项目/跨公司以及会话撤权行为仍需登录态集成或浏览器验证。
- 前端历史 TypeScript 错误应单独治理，避免长期掩盖后续新增类型回归。
- FIX-5D 可在本实现之上聚焦入口可达性与真实登录态矩阵，不需要放宽详情 query 权限，也不需要修改角色默认矩阵。
