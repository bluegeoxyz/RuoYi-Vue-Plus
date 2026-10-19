# 出库、调拨、盘点 Options 权限解耦

## 原页面依赖

| 模块 | 原业务用途 | 原接口 | 被迫依赖的管理权限 |
|---|---|---|---|
| 出库 | 仓库 | `GET /warehouse/warehouse/options` | `warehouse:warehouse:list` |
| 出库 | 物料、基础单位 | `GET /warehouse/material/options`、`GET /warehouse/unit/options` | `warehouse:material:list`、`warehouse:unit:list` |
| 出库 | 库位、可用库存 | `GET /warehouse/location/options`、`GET /warehouse/inventory/list` | `warehouse:location:list`、`warehouse:inventory:list` |
| 调拨 | 源仓、目标仓 | `GET /warehouse/warehouse/options` | `warehouse:warehouse:list` |
| 调拨 | 双侧库区、库位 | `GET /warehouse/zone/options`、`GET /warehouse/location/options` | `warehouse:zone:list`、`warehouse:location:list` |
| 调拨 | 物料、基础单位、源仓可用库存 | `GET /warehouse/material/options`、`GET /warehouse/unit/options`、`GET /warehouse/inventory/list` | `warehouse:material:list`、`warehouse:unit:list`、`warehouse:inventory:list` |
| 盘点 | 仓库、库区、库位 | `GET /warehouse/warehouse/options`、`GET /warehouse/zone/options`、`GET /warehouse/location/options` | 对应基础资料 `list` 权限 |
| 盘点 | 物料、基础单位 | `GET /warehouse/material/options`、`GET /warehouse/unit/options` | `warehouse:material:list`、`warehouse:unit:list` |

盘点创建时的范围库存仍由 `StocktakeOrderServiceImpl` 在服务端生成；新库存上下文接口只用于手工明细的账面量预览，保存时服务端仍重新读取库存，前端值不作为可信依据。

## 新旧接口对照

三个模块均提供以下固定场景接口，其中 `{module}` 为 `outbound`、`transfer` 或 `stocktake`：

| 新接口 | ReferenceType | 替代页面调用 |
|---|---|---|
| `GET /warehouse/{module}/context/warehouses` | `WAREHOUSE` | `/warehouse/warehouse/options` |
| `GET /warehouse/{module}/context/zones?warehouseId=` | `ZONE` | `/warehouse/zone/options` |
| `GET /warehouse/{module}/context/locations?warehouseId=&zoneId=` | `LOCATION` | `/warehouse/location/options` |
| `GET /warehouse/{module}/context/materials?warehouseId=` | `MATERIAL`、`MEASUREMENT_UNIT` | `/warehouse/material/options`、`/warehouse/unit/options` |
| `GET /warehouse/{module}/context/inventory?warehouseId=&locationId=&materialId=` | `INVENTORY` | `/warehouse/inventory/list` |

Controller 分别固定使用 `OUTBOUND_MAINTAIN`、`TRANSFER_MAINTAIN`、`STOCKTAKE_MAINTAIN`，权限为相应模块现有 `add OR edit`。接口不接收业务场景或权限字符串。

## 范围与安全边界

- 仓库清单来自 `WarehouseAccessService.accessibleWarehouseIds()`；无仓库范围返回空列表。
- 每次读取库区、库位、物料或库存前都调用 `requireWarehouse`。公司仓继续使用部门数据范围，项目仓继续使用项目仓授权范围。
- 库位必须启用、属于请求仓库，且其库区也必须启用并属于该仓库。
- 物料必须启用且公司与仓库公司一致；公司范围由仓库反推，不接受前端 companyId。
- 库存只允许以 `warehouseId + locationId + materialId` 精确维度查询；不存在余额时返回零，不执行库存管理列表查询。
- 调拨源仓与目标仓的库区、库位分别调用上下文接口，因此各自独立经过 `requireWarehouse`；源仓授权不能推导目标仓授权。Phase 1 的同公司限制仍由调拨单写服务强制执行。
- 单据新增、编辑、确认、完成、取消、删除的原 Controller 权限和服务端状态机未改变。
- 原基础资料 Options 保留，基础资料管理页面及其他尚未迁移页面仍可兼容使用。

## 前端迁移

- 三个制单页不再调用仓库、库区、库位、物料、单位或库存管理接口。
- 物料 Option 同时携带基础单位名称和精度，消除全局单位 Options 依赖。
- 切换出库/盘点仓库、调拨源仓或目标仓时，若会丢失明细或目标库位，会先要求用户确认。
- 调拨目标仓仍按源仓公司过滤候选项，服务端保存时再次验证不得跨公司。
- 详情只读页直接使用单据详情返回的仓库、物料、库区、库位和单位快照，不调用维护上下文接口，因此只有 `query` 权限也可展示。
- 仅有基础资料管理权限的用户不能调用这些业务上下文接口；列表筛选在具备仓库管理权限时仍可使用原仓库 Options。

## 自动化结果

- `BusinessContextAccessServiceTest`：10 项通过，覆盖三场景 add/edit ANY 语义、基础资料 list 权限不可绕过及场景隔离。
- `WarehouseDocumentContextOptionServiceImplTest`：7 项通过，覆盖无资源、范围拒绝、跨仓库库位、跨公司物料、精确库存及零余额。
- `WarehouseDocumentContextOptionControllerTest`：2 项通过，覆盖 15 个接口的既有权限 OR 契约，以及新增/编辑写操作权限保持独立。
- `PermissionContractAuditTest`：6 项通过；当前 244 个 Controller 接口、45 个业务 Options 接口、22 个旧管理 Options 耦合基线、3 个 Controller/Service 冲突基线。
- 后端 Maven 编译通过；前端 `pnpm build` 通过。

旧管理 Options 接口仍然存在且可能被基础资料管理或其他页面使用，因此契约中的 22 项“接口自身仍耦合管理 list 权限”基线不删除。本次确认消除的是出库、调拨、盘点三个前端业务页面对这些旧接口的调用关系。
