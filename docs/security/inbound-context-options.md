# 入库业务 Options 权限解耦试点

## 原页面依赖

| 页面用途 | 原接口 | 原权限 | 问题 |
|---|---|---|---|
| 仓库筛选及制单仓库 | `GET /warehouse/warehouse/options` | `warehouse:warehouse:list` | 入库制单依赖仓库管理权限 |
| 仓库对应物料 | `GET /warehouse/material/options?companyId=...` | `warehouse:material:list` | 公司 ID 由前端选择后传入，且依赖物料管理权限 |
| 仓库对应库位 | `GET /warehouse/location/options?warehouseId=...` | `warehouse:location:list` | 入库制单依赖库位管理权限 |
| 物料基础单位 | `GET /warehouse/unit/options` | `warehouse:unit:list` | 页面初始化依赖计量单位管理权限 |

原页面未单独展示库区下拉框，但入库保存要求库位所属库区启用且与仓库一致，因此新业务接口显式纳入库区过滤。

## 新接口

四个接口都固定使用 `BusinessContext.INBOUND_MAINTAIN`，Controller 使用现有
`warehouse:inbound:add OR warehouse:inbound:edit`，不接受上下文或权限字符串参数。

| 新接口 | ReferenceType | 范围来源 |
|---|---|---|
| `GET /warehouse/inbound/context/warehouses` | WAREHOUSE | `WarehouseAccessService.accessibleWarehouseIds()` |
| `GET /warehouse/inbound/context/zones?warehouseId=` | ZONE | `requireWarehouse` 后仅返回该仓启用库区 |
| `GET /warehouse/inbound/context/locations?warehouseId=&zoneId=` | LOCATION | `requireWarehouse`，校验库区归属，只返回启用库区下的启用库位 |
| `GET /warehouse/inbound/context/materials?warehouseId=` | MATERIAL、MEASUREMENT_UNIT | `requireWarehouse` 后从仓库反推公司，仅返回该公司启用物料及其启用基础单位 |

项目仓继续由物业项目仓范围控制，公司仓继续走部门数据范围。上下文服务只判断业务能力，资源范围仍由 `WarehouseAccessService` 强制执行。

## 前端迁移

- 新增或编辑权限用户使用新的入库上下文接口。
- 切换仓库会清空原明细、库区、库位和物料选项。
- 物料选项直接返回基础单位名称和精度，不再读取全局单位管理 Options。
- 查看详情不请求制单 Options，使用详情返回的仓库、物料和库位展示字段。
- 只有仓库管理权限、没有入库新增或编辑权限的用户，列表筛选仍可使用原仓库 Options。
- 无授权仓库返回空列表；权限不足在打开制单页面时提示。

旧管理 Options 未删除，出库、调拨、盘点及基础资料管理页面仍在使用，因此 AUTH-002 的 22 项耦合基线暂不减少。
