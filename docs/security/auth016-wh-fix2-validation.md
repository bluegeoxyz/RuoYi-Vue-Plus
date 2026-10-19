# TASK-AUTH-016-WH-FIX-2 验收报告

## 1. 结论

本次已完成库区、库位、物料分类、物料四个基础资料页面的只读权限解耦。角色仅拥有对应模块的 `list` 或 `query` 权限时，页面列表、详情及只读筛选不再依赖其他基础资料模块的管理权限。

实现继续复用 `BusinessContext`、`BusinessContextAccessService` 和 `WarehouseAccessService`，没有新增数据库权限码、修改角色矩阵、修改数据库或执行 Migration。新增/编辑表单没有复用 QUERY Context；其现存跨模块 Options 依赖在第 8 节单独列出。

## 2. 修改文件

### 后端

- `ruoyi-common/ruoyi-common-security/src/main/java/org/dromara/common/security/business/BusinessContext.java`
- `ruoyi-common/ruoyi-common-security/src/main/java/org/dromara/common/security/business/ReferenceType.java`
- `ruoyi-common/ruoyi-common-security/src/main/java/org/dromara/common/security/business/BusinessContextPolicy.java`
- `ruoyi-common/ruoyi-common-security/src/test/java/org/dromara/common/security/business/BusinessContextAccessServiceTest.java`
- `ruoyi-modules/ruoyi-warehouse/src/main/java/org/dromara/warehouse/context/service/WarehouseMasterDataQueryOptionService.java`
- `ruoyi-modules/ruoyi-warehouse/src/main/java/org/dromara/warehouse/context/service/impl/WarehouseMasterDataQueryOptionServiceImpl.java`
- `ruoyi-modules/ruoyi-warehouse/src/main/java/org/dromara/warehouse/zone/controller/WarehouseZoneQueryContextOptionController.java`
- `ruoyi-modules/ruoyi-warehouse/src/main/java/org/dromara/warehouse/location/controller/WarehouseLocationQueryContextOptionController.java`
- `ruoyi-modules/ruoyi-warehouse/src/main/java/org/dromara/warehouse/material/category/controller/MaterialCategoryQueryContextOptionController.java`
- `ruoyi-modules/ruoyi-warehouse/src/main/java/org/dromara/warehouse/material/controller/MaterialQueryContextOptionController.java`
- `ruoyi-modules/ruoyi-warehouse/src/test/java/org/dromara/warehouse/context/controller/WarehouseMasterDataQueryContextControllerTest.java`
- `ruoyi-modules/ruoyi-warehouse/src/test/java/org/dromara/warehouse/context/service/impl/WarehouseMasterDataQueryOptionServiceImplTest.java`

### 前端

- `src/api/warehouse/zone/index.ts`
- `src/api/warehouse/location/index.ts`
- `src/api/warehouse/materialCategory/index.ts`
- `src/api/warehouse/material/index.ts`
- `src/views/warehouse/zone/index.vue`
- `src/views/warehouse/location/index.vue`
- `src/views/warehouse/materialCategory/index.vue`
- `src/views/warehouse/material/index.vue`

前端文件位于独立工程 `E:/space/project/zbm-platform-ui`。

## 3. QUERY Context 与接口

| BusinessContext | 权限规则（ANY） | ReferenceType | 固定接口 |
| --- | --- | --- | --- |
| `WAREHOUSE_ZONE_QUERY` | `warehouse:zone:list` / `warehouse:zone:query` | `WAREHOUSE` | `GET /warehouse/zone/context/query/warehouses` |
| `WAREHOUSE_LOCATION_QUERY` | `warehouse:location:list` / `warehouse:location:query` | `WAREHOUSE`, `ZONE` | `GET /warehouse/location/context/query/warehouses`; `GET /warehouse/location/context/query/zones?warehouseId=` |
| `WAREHOUSE_MATERIAL_CATEGORY_QUERY` | `warehouse:materialCategory:list` / `warehouse:materialCategory:query` | `COMPANY` | `GET /warehouse/materialCategory/context/query/companies` |
| `WAREHOUSE_MATERIAL_QUERY` | `warehouse:material:list` / `warehouse:material:query` | `COMPANY`, `MATERIAL_CATEGORY`, `MEASUREMENT_UNIT` | `GET /warehouse/material/context/query/companies`; `GET /warehouse/material/context/query/categories?companyId=`; `GET /warehouse/material/context/query/units` |

所有 Controller 固定传入服务端枚举，不接受前端指定 BusinessContext 或权限字符串。Controller 注解与 BusinessContext Policy 使用同一组 `list/query` ANY 权限语义。

## 4. 页面 API 依赖变化

| 页面 | 修改前只读筛选依赖 | 修改后只读筛选依赖 |
| --- | --- | --- |
| 库区 | `/warehouse/warehouse/options` | `/warehouse/zone/context/query/warehouses` |
| 库位 | `/warehouse/warehouse/options`, `/warehouse/zone/options` | `/warehouse/location/context/query/warehouses`, `/warehouse/location/context/query/zones` |
| 物料分类 | `/warehouse/material/company-options` | `/warehouse/materialCategory/context/query/companies` |
| 物料 | `/warehouse/material/company-options`, `/warehouse/materialCategory/options`，且初始化时加载单位维护 Options | `/warehouse/material/context/query/companies`, `/warehouse/material/context/query/categories`；列表不再加载单位维护 Options |

列表数据和辅助 Options 使用独立请求与错误处理。Options 失败会显示明确提示，但不会清空已经成功返回的列表数据。新增/编辑时才按需加载原有维护型 Options。

## 5. 数据范围与资源归属

- 仓库选项通过现有 `WarehouseAccessService.accessibleWarehouseIds()` 过滤；公司仓、项目仓继续沿用部门/公司范围和 ACTIVE 项目成员范围。
- 库区查询在读取前调用 `WarehouseAccessService.requireWarehouse(warehouseId)`，随后按真实 `warehouse_id` 查询，不能用伪造 warehouseId 枚举其他仓库库区。
- 物料公司选项复用 `WarehouseAccessService.selectMaterialCompanyOptions()`。
- 物料分类按 `accessibleMaterialCompanyIds()` 限定公司集合；传入 companyId 时先验证其位于当前可见公司集合内，再查询该公司的有效分类树。
- 计量单位只返回启用状态的精简 Option，不授予单位维护权限。
- SuperAdmin 继续由既有 WarehouseAccessService 规则获得全局合法资源范围，但资源存在性、状态与写接口权限不受本次变更影响。

## 6. 权限正向与反向测试

新增自动化覆盖：

- 四个 QUERY Context 分别接受自身 `list` 或 `query` 权限。
- 其他基础资料管理权限不能反向调用当前业务 Context。
- `WAREHOUSE_MATERIAL_QUERY` 不能授权 `WAREHOUSE_MATERIAL_CATEGORY_QUERY`。
- 未声明 ReferenceType 默认拒绝。
- 四组 Controller 的每个接口权限注解与预期 ANY 语义一致。
- 仓库/库区查询委托既有仓库访问链路。
- 物料分类按可见公司过滤，并拒绝跨公司 companyId。
- 单位 Options 必须先通过物料 QUERY Context。
- 既有仓库范围测试继续覆盖公司仓、项目仓与 SuperAdmin 分支。
- 查询权限没有接入任何 add/edit/remove 接口，写操作仍由原 Controller 权限保护。

## 7. 自动化与构建结果

| 验证项 | 结果 |
| --- | --- |
| 定向 BusinessContext + FIX-2 Controller/Service 测试 | 通过；23 个测试（BusinessContext 18，FIX-2 新增 5） |
| 仓库模块全量测试 | 通过；127/127，0 failure，0 error，0 skipped |
| `PermissionContractAuditTest` | 通过；6/6 |
| 权限契约 CSV | 已生成；292 个 Controller 接口，其中仓库 130 个；新增 7 个接口均登记为既有权限 |
| 前端 ESLint | 通过；0 error |
| 前端生产构建 | 通过；Vite 8.2.2，3442 modules transformed |
| TypeScript `vue-tsc --noEmit` | 未通过；仍为既有 10 个无关基线错误，FIX-2 文件没有新增错误 |

TypeScript 基线错误分布为：房屋收费设置页面 6 个、入库页面 4 个。依据任务边界，本次没有修改这些无关问题，因此不能宣称全量类型检查通过。

## 8. 新增/编辑表单耦合审计

QUERY Context 没有被新增/编辑表单复用。当前仍存在以下写入表单依赖：

| 模块 | 现存维护型 Options 依赖 | 最小后续建议 |
| --- | --- | --- |
| 库区 add/edit | 仓库管理 `warehouse/options` | 单独建立 `WAREHOUSE_ZONE_MAINTAIN`，绑定 zone add/edit，并复用仓库范围校验 |
| 库位 add/edit | 仓库管理 `warehouse/options`、库区管理 `zone/options` | 单独建立 `WAREHOUSE_LOCATION_MAINTAIN`，绑定 location add/edit，并逐仓校验库区归属 |
| 物料分类 add/edit | 物料公司 Options、分类父节点 Options | 单独建立分类 MAINTAIN 场景，绑定 category add/edit；限制公司范围与父分类归属 |
| 物料 add/edit | 公司、计量单位、物料分类管理 Options | 单独建立 material MAINTAIN 场景，绑定 material add/edit；仅开放精简引用数据，不授予单位/分类 CRUD |

上述建议本次未实现，避免未经专项测试把查询上下文扩大为写入候选数据授权。

## 9. 真实浏览器验收

本次未使用真实账号执行浏览器 E2E，因此不宣称真实登录态验收通过。建议后续分别给临时测试角色只授予四个模块的 `list/query`，验证列表、详情、级联筛选、越权 ID 拒绝以及 add/edit/remove 按钮和接口拒绝。

## 10. 遗留问题

1. 四个新增/编辑表单仍有第 8 节列出的维护型 Options 耦合，需要独立任务设计 MAINTAIN Context。
2. 全量 TypeScript 检查被 10 个既有无关错误阻塞；生产构建与 ESLint 已通过。
3. 未完成真实浏览器和真实账号验收。

