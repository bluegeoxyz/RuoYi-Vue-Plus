# TASK-AUTH-016-WH-FIX-1 验收报告

> 完成日期：2026-10-11  
> 范围：库存余额、库存流水动态只读 Context 与前端页面  
> 未修改：角色矩阵、数据库、Migration、库存写接口、单据状态机、其他仓库业务页面

## 1. 实施结果

新增两个固定只读业务上下文：

| BusinessContext | 权限组合 | ReferenceType |
| --- | --- | --- |
| `INVENTORY_QUERY` | `warehouse:inventory:list OR warehouse:inventory:query` | WAREHOUSE、ZONE、LOCATION、MATERIAL_CATEGORY、MEASUREMENT_UNIT、MATERIAL |
| `INVENTORY_TRANSACTION_QUERY` | `warehouse:inventoryTransaction:list OR warehouse:inventoryTransaction:query` | WAREHOUSE、ZONE、LOCATION、MATERIAL_CATEGORY、MEASUREMENT_UNIT、MATERIAL |

两个上下文不复用 MAINTAIN，不接受前端传入的 Context 或权限字符串，不包含 INVENTORY 写能力，也不新增数据库权限码。

前端库存余额和库存流水页面已完全移除对以下管理 Options 的调用：

- `warehouse/warehouse/options`
- `warehouse/material/company-options`
- `warehouse/materialCategory/options`
- `warehouse/zone/options`
- `warehouse/location/options`

页面现在先独立请求核心列表，同时异步加载筛选 Options。辅助 Options 失败会显示具体错误，但不会清空或阻断已经成功加载的核心列表。

## 2. 新接口映射

### 2.1 库存余额

基础路径：`/warehouse/inventory/context`

| 方法 | 路径 | 返回 | Controller 权限 |
| --- | --- | --- | --- |
| GET | `/companies` | 授权仓库涉及的精简公司 | inventory list OR query |
| GET | `/warehouses` | 授权且启用的仓库 | inventory list OR query |
| GET | `/categories?companyId=` | 授权仓库公司下的启用分类树 | inventory list OR query |
| GET | `/zones?warehouseId=` | 指定授权仓库的启用库区 | inventory list OR query |
| GET | `/locations?warehouseId=&zoneId=` | 指定授权仓库/库区的启用库位 | inventory list OR query |
| GET | `/materials?warehouseId=` | 指定授权仓库同公司的启用物料与单位 | inventory list OR query |

### 2.2 库存流水

基础路径：`/warehouse/inventoryTransaction/context`，接口集合与库存余额相同，Controller 权限固定为 `warehouse:inventoryTransaction:list OR warehouse:inventoryTransaction:query`。

权限契约将 12 个新增接口识别为 `BUSINESS_OPTIONS`。当前生成结果为 256 个总接口契约，其中仓库 119 个。

## 3. 范围与安全校验

| 引用数据 | 校验方式 |
| --- | --- |
| 仓库 | `WarehouseAccessService.accessibleWarehouseIds()`；公司仓继续走部门数据范围，项目仓继续走 ACTIVE 项目成员范围 |
| 公司 | 从当前授权且启用的仓库集合提取 companyId，再与可见启用部门交集，不返回无仓库公司的候选项 |
| 分类 | companyId 必须属于当前授权仓库公司；否则拒绝；仅返回启用分类 |
| 库区 | 先 `requireWarehouse(warehouseId)`，再按真实 warehouseId 查询启用库区 |
| 库位 | 校验仓库访问；zoneId 必须属于当前仓库；结果再次按库区集合过滤 |
| 物料 | 先校验仓库；只查该仓库 companyId 下的启用物料；单位只作为最小展示字段关联返回 |

项目仓范围仍为：`sys_user -> active hr_employee -> ACTIVE 且 status=0 的 pm_project_member`。公司仓仍调用部门数据范围。SuperAdmin 可读取全部合法仓库候选，但仍受实体存在、启用状态、父子归属和物料公司一致性约束。

仅持有 warehouse/material/zone/location 等基础资料 list 权限不能反向进入库存 Context。余额与流水两个查询 Context 也不能互相授权。

本次没有新增或暴露任何库存修改接口；库存只读权限无法调用入库、出库、调拨、盘点的写接口，原 Controller 权限保持不变。

## 4. 修改文件

### 后端

- `ruoyi-common/ruoyi-common-security/src/main/java/org/dromara/common/security/business/BusinessContext.java`
- `ruoyi-common/ruoyi-common-security/src/main/java/org/dromara/common/security/business/BusinessContextPolicy.java`
- `ruoyi-modules/ruoyi-warehouse/src/main/java/org/dromara/warehouse/context/domain/vo/ContextCompanyOptionVo.java`
- `ruoyi-modules/ruoyi-warehouse/src/main/java/org/dromara/warehouse/context/service/WarehouseDocumentContextOptionService.java`
- `ruoyi-modules/ruoyi-warehouse/src/main/java/org/dromara/warehouse/context/service/impl/WarehouseDocumentContextOptionServiceImpl.java`
- `ruoyi-modules/ruoyi-warehouse/src/main/java/org/dromara/warehouse/inventory/controller/InventoryContextOptionController.java`
- `ruoyi-modules/ruoyi-warehouse/src/main/java/org/dromara/warehouse/inventory/transaction/controller/InventoryTransactionContextOptionController.java`

### 后端测试

- `ruoyi-common/ruoyi-common-security/src/test/java/org/dromara/common/security/business/BusinessContextAccessServiceTest.java`
- `ruoyi-modules/ruoyi-warehouse/src/test/java/org/dromara/warehouse/context/controller/WarehouseDocumentContextOptionControllerTest.java`
- `ruoyi-modules/ruoyi-warehouse/src/test/java/org/dromara/warehouse/context/service/impl/WarehouseDocumentContextOptionServiceImplTest.java`
- `ruoyi-modules/ruoyi-warehouse/src/test/java/org/dromara/warehouse/security/WarehouseAccessServiceTest.java`

### 前端

- `zbm-platform-ui/src/api/warehouse/context/types.ts`
- `zbm-platform-ui/src/api/warehouse/inventory/index.ts`
- `zbm-platform-ui/src/api/warehouse/inventoryTransaction/index.ts`
- `zbm-platform-ui/src/views/warehouse/inventory/index.vue`
- `zbm-platform-ui/src/views/warehouse/inventoryTransaction/index.vue`

## 5. 自动化验证

| 验证 | 结果 |
| --- | --- |
| 后端 reactor 编译/install（跳过测试） | 通过，22/22 模块成功 |
| BusinessContext 公共安全测试 | 18/18 通过 |
| 仓库模块回归测试 | 121/121 通过，0 failure/error/skipped |
| `PermissionContractAuditTest` | 6/6 通过 |
| 前端 `pnpm lint` | 通过 |
| 修改文件 `oxfmt --check` | 通过 |
| 前端 `pnpm build:prod` | 通过，3442 modules transformed |
| 前端 `vue-tsc --noEmit` | 未通过；仅报告本任务外既有 10 个错误，详见下一节 |

新增测试覆盖：

- inventory list 或 query 均可独立使用 `INVENTORY_QUERY`。
- inventoryTransaction list 或 query 均可独立使用对应 Context。
- 两个只读 Context 互不授权。
- 只有基础资料管理 list 权限不能调用库存 Context。
- 无项目仓范围时 `requireWarehouse` 拒绝。
- 无公司仓部门范围时 `requireWarehouse` 拒绝。
- 未授权分类公司被拒绝，查询不会到达分类 Mapper。
- 跨仓库库位、跨公司物料被拒绝。
- SuperAdmin 的仓库范围行为保持原测试覆盖。
- Controller 的 12 个 Context 接口全部固定使用各自 list/query OR 权限。

## 6. TypeScript 遗留问题

全量 `vue-tsc --noEmit` 报告 10 个既有、与本次改动无关的错误：

- `src/views/property/charge/binding/index.vue`：6 个类型错误。
- `src/views/warehouse/inbound/index.vue`：4 个 `DefaultRow` 与 `InboundVO` 不兼容错误。

本次修改的库存余额、库存流水及 API 文件没有出现在错误输出中。因任务明确要求“不改变其他业务页面”，本次未修改上述文件。生产构建和 lint 均通过。建议建立独立前端类型基线修复任务后，再将全量 `vue-tsc` 作为强制门禁。

## 7. 真实浏览器验收

本次未修改数据库角色授权，也未使用真实账号临时增减权限，因此未执行真实登录态浏览器验收。建议后续在测试环境验证：

1. 仅有 inventory list/query 的普通用户可加载余额列表、详情及全部筛选项。
2. 仅有 inventoryTransaction list/query 的普通用户可加载流水列表、详情及筛选项。
3. 移除管理基础资料权限后页面仍可用。
4. 项目成员失效后项目仓不再出现在 Options，伪造 warehouseId 返回拒绝。
5. 公司仓范围外仓库不出现，直接请求其 zone/location/material 也被拒绝。
6. 撤销库存权限并刷新会话后，列表和 Context 均不可访问。

## 8. 遗留风险与下一步

- 全量前端类型检查仍有任务外基线错误，不能宣告全仓 `vue-tsc` 通过。
- 当前范围测试以单元测试为主，尚未使用真实多公司、多项目数据完成 HTTP 集成验证。
- Context API 同时提供 materials/categories，以便保持完整的只读引用能力；当前两个页面只实际消费公司、仓库、分类、库区和库位。
- 未开始 FIX-3；四类仓库单据的只读筛选 Context 仍按 AUTH-016 审计报告中的原状态保留。
