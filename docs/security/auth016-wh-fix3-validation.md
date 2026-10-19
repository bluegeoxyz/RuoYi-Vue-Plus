# TASK-AUTH-016-WH-FIX-3 验收报告

## 1. 结论

入库、出库、调拨、盘点四类单据已建立彼此独立的只读查询上下文。只具有对应 `list` 或 `query` 权限的用户，可以加载其仓库访问范围内的列表筛选仓库；不再依赖 `warehouse:warehouse:list`，也不能借此调用制单所需的 MAINTAIN Options。

本次没有修改数据库、Migration、七角色权限矩阵、单据状态机或库存写入逻辑。

## 2. BusinessContext 与接口映射

| BusinessContext | ANY 权限 | 允许的 ReferenceType | 固定服务端接口 |
| --- | --- | --- | --- |
| `INBOUND_QUERY` | `warehouse:inbound:list` / `warehouse:inbound:query` | `WAREHOUSE` | `GET /warehouse/inbound/context/query/warehouses` |
| `OUTBOUND_QUERY` | `warehouse:outbound:list` / `warehouse:outbound:query` | `WAREHOUSE` | `GET /warehouse/outbound/context/query/warehouses` |
| `TRANSFER_QUERY` | `warehouse:transfer:list` / `warehouse:transfer:query` | `WAREHOUSE` | `GET /warehouse/transfer/context/query/warehouses` |
| `STOCKTAKE_QUERY` | `warehouse:stocktake:list` / `warehouse:stocktake:query` | `WAREHOUSE` | `GET /warehouse/stocktake/context/query/warehouses` |

四个 Controller 均固定自身 BusinessContext，不接受场景或权限字符串参数。接口注解与策略均采用对应单据 `list OR query`，基础资料 `warehouse:warehouse:list` 不能反向进入这些上下文。

查询上下文仅声明 `WAREHOUSE`，不包含 `ZONE`、`LOCATION`、`MATERIAL`、`INVENTORY` 等制单引用类型。原 `INBOUND_MAINTAIN`、`OUTBOUND_MAINTAIN`、`TRANSFER_MAINTAIN`、`STOCKTAKE_MAINTAIN` 及其 add/edit 权限保持不变。

## 3. 资源范围与写操作边界

查询接口复用 `WarehouseDocumentContextOptionService.selectWarehouses(context)`：

1. 先由 `BusinessContextAccessService` 校验当前单据只读业务能力；
2. 再由 `WarehouseAccessService.accessibleWarehouseIds()` 计算授权仓库；
3. 无仓库范围返回空列表；
4. 仅按授权仓库 ID 查询启用的精简 Warehouse Option。

因此公司仓仍沿用员工部门/公司数据范围，项目仓仍沿用 ACTIVE 项目成员链路。调拨查询的源仓和目标仓使用同一份“当前用户合法仓库”候选集合，不会由一个已授权源仓推导出未授权目标仓。

新增、编辑仍调用原 MAINTAIN Options；确认、完成、取消和库存变化仍由原 Controller 权限、仓库范围、单据状态机及库存事务服务控制。QUERY Context 不参与任何写操作。

## 4. 前端调用变化

四个列表页初始化均改为并行、相互独立地发起：

- 核心单据列表请求；
- 对应 `/context/query/warehouses` 筛选项请求。

辅助 Options 失败时显示“筛选仓库选项加载失败，列表数据仍可继续查询”，不会清空已成功返回的列表。新增和编辑仍通过原 `/context/warehouses` 及物料、库位、库存接口加载 MAINTAIN 候选数据。详情继续使用单据及明细快照补齐只读名称，不要求 add/edit 权限。

## 5. 修改文件

### 后端

- `ruoyi-common/ruoyi-common-security/src/main/java/org/dromara/common/security/business/BusinessContext.java`
- `ruoyi-common/ruoyi-common-security/src/main/java/org/dromara/common/security/business/BusinessContextPolicy.java`
- `ruoyi-modules/ruoyi-warehouse/src/main/java/org/dromara/warehouse/inbound/controller/InboundQueryContextOptionController.java`
- `ruoyi-modules/ruoyi-warehouse/src/main/java/org/dromara/warehouse/outbound/controller/OutboundQueryContextOptionController.java`
- `ruoyi-modules/ruoyi-warehouse/src/main/java/org/dromara/warehouse/transfer/controller/TransferQueryContextOptionController.java`
- `ruoyi-modules/ruoyi-warehouse/src/main/java/org/dromara/warehouse/stocktake/controller/StocktakeQueryContextOptionController.java`

### 自动化测试

- `ruoyi-common/ruoyi-common-security/src/test/java/org/dromara/common/security/business/BusinessContextAccessServiceTest.java`
- `ruoyi-modules/ruoyi-warehouse/src/test/java/org/dromara/warehouse/context/controller/WarehouseDocumentContextOptionControllerTest.java`
- `ruoyi-modules/ruoyi-warehouse/src/test/java/org/dromara/warehouse/context/service/impl/WarehouseDocumentContextOptionServiceImplTest.java`

### 前端

- `src/api/warehouse/inbound/index.ts`
- `src/api/warehouse/outbound/index.ts`
- `src/api/warehouse/transfer/index.ts`
- `src/api/warehouse/stocktake/index.ts`
- `src/views/warehouse/inbound/index.vue`
- `src/views/warehouse/outbound/index.vue`
- `src/views/warehouse/transfer/index.vue`
- `src/views/warehouse/stocktake/index.vue`

前端文件位于 `E:/space/project/zbm-platform-ui`。

## 6. 自动化验收

| 验收项 | 结果 |
| --- | --- |
| 后端模块编译 | 通过；`ruoyi-warehouse` 及依赖模块成功编译 |
| BusinessContext 针对性测试 | 17/17 通过 |
| 查询 Controller 与共享范围服务测试 | 13/13 通过 |
| 仓库模块完整测试 | 122/122 通过，0 failure/error/skipped；覆盖原单据状态机与库存事务测试 |
| `PermissionContractAuditTest` | 6/6 通过 |
| 权限契约 CSV | `ruoyi-admin/target/permission-contract.csv`；285 个总接口、123 个仓库接口；四个新增接口均分类为 `BUSINESS_OPTIONS`，权限模式均为 OR |
| 前端 Lint | 通过，0 error |
| 前端生产构建 | 通过；Vite 转换 3442 个模块 |
| 前端类型检查 | 未通过；共 10 个既有错误：房屋收费设置 6 个、入库页面 `DefaultRow` 4 个，与 FIX-1 基线相同；遵循任务边界未处理无关 TypeScript 错误 |

类型检查命令为 `pnpm exec vue-tsc --noEmit`。生产构建和 Lint 均证明本次新增 API、导入及页面初始化代码可正常打包和通过静态规则；全量类型检查仍受既有基线问题阻塞，不能宣称通过。

## 7. 未执行项与遗留风险

- 未使用真实浏览器和真实登录账号执行 E2E，因此未宣称真实登录态验收通过。
- 建议后续分别使用仅有四类单据 list/query 权限的账号验证列表、详情和仓库筛选，并验证调用 MAINTAIN Options 返回拒绝。
- 自动化测试已覆盖策略隔离、管理 list 权限不可绕过、仓库范围空集及现有公司仓/项目仓隔离服务；真实数据库中的组织、项目成员和仓库样本仍需浏览器验收确认。
- 前端全量类型检查的 10 个既有错误需由独立任务处理，本任务未扩大范围。

