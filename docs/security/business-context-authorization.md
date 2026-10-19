# 业务上下文授权服务

## 定位与边界

`BusinessContextAccessService` 只判断当前登录用户是否具备特定业务场景下读取引用数据的能力。
它不查询业务数据，不替代 `PropertyProjectScopeService` 或 `WarehouseAccessService`，也不授予项目、公司、仓库或具体资源范围。

后续 Options 接口的固定调用顺序为：

1. 使用服务端常量选择 `BusinessContext` 和 `ReferenceType`；
2. 调用 `requireAccess(context, referenceType)`；
3. 调用现有项目或仓库范围服务；
4. 根据真实资源归属过滤；
5. 返回最小字段的 Option VO。

禁止将前端字符串转换为权限码，也禁止在通过上下文校验后跳过资源范围校验。

## 场景策略

| BusinessContext | 组合 | 现有业务权限 | 可访问的 ReferenceType |
|---|---|---|---|
| `CUSTOMER_RELATION_LIST` | ALL | `property:customerRelation:list` | PROJECT, BUILDING, UNIT, ROOM |
| `CUSTOMER_RELATION_CREATE` | ALL | `property:customerRelation:add` | PROJECT, BUILDING, UNIT, ROOM, CUSTOMER |
| `CUSTOMER_RELATION_EDIT` | ALL | `property:customerRelation:edit` | PROJECT, BUILDING, UNIT, ROOM, CUSTOMER |
| `CHARGE_BINDING_CREATE` | ALL | `property:chargeBinding:add` | PROJECT, ROOM, CHARGE_ITEM, CHARGE_RULE |
| `CHARGE_BINDING_EDIT` | ALL | `property:chargeBinding:edit` | PROJECT, ROOM, CHARGE_ITEM, CHARGE_RULE |
| `RECEIVABLE_QUERY` | ANY | `property:receivable:list`, `property:receivable:query` | PROJECT, BUILDING, UNIT, ROOM, CUSTOMER, RECEIVABLE |
| `RECEIVABLE_GENERATE` | ALL | `property:receivable:generate` | PROJECT, BUILDING, UNIT, ROOM, CUSTOMER, CHARGE_ITEM, CHARGE_RULE |
| `PAYMENT_CREATE` | ALL | `property:payment:add`, `property:payment:allocate` | PROJECT, ROOM, CUSTOMER, RECEIVABLE |
| `INBOUND_MAINTAIN` | ANY | `warehouse:inbound:add`, `warehouse:inbound:edit` | WAREHOUSE, ZONE, LOCATION, MEASUREMENT_UNIT, MATERIAL |
| `OUTBOUND_MAINTAIN` | ANY | `warehouse:outbound:add`, `warehouse:outbound:edit` | PROJECT, WAREHOUSE, ZONE, LOCATION, MATERIAL_CATEGORY, MEASUREMENT_UNIT, MATERIAL, INVENTORY |
| `TRANSFER_MAINTAIN` | ANY | `warehouse:transfer:add`, `warehouse:transfer:edit` | PROJECT, WAREHOUSE, ZONE, LOCATION, MATERIAL_CATEGORY, MEASUREMENT_UNIT, MATERIAL, INVENTORY |
| `STOCKTAKE_MAINTAIN` | ANY | `warehouse:stocktake:add`, `warehouse:stocktake:edit` | PROJECT, WAREHOUSE, ZONE, LOCATION, MATERIAL_CATEGORY, MEASUREMENT_UNIT, MATERIAL, INVENTORY |

仓库维护场景显式使用 ANY，是因为同一单据编辑器后续可能同时服务新增和编辑入口；这不是全局地将新增、编辑权限视为等价。收款创建继续使用当前 Controller 已存在的 `add + allocate` ALL 语义。

## 默认拒绝规则

- 未登录：由 `StpUtil.checkLogin()` 拒绝；
- 未登记或空的业务上下文：拒绝；
- 上下文未声明的引用类型：拒绝；
- 不满足 ANY/ALL 权限组合：拒绝；
- 仅拥有基础资料管理 `list` 权限：不能通过业务上下文授权；
- 一个业务场景的权限：不能授权另一个场景。

拒绝使用 Sa-Token 的 `NotLoginException` 或 `NotPermissionException`，继续复用现有全局异常处理。

## AUTH-004 接入建议

先选择一个读路径清晰、范围服务成熟的仓库 Options 页面做单点接入：

1. 新增专用业务 Options 端点，不直接放开原管理 Options；
2. Controller 固定传入上下文枚举，不接收上下文或权限字符串请求参数；
3. Service 先校验业务上下文，再调用 `WarehouseAccessService` 和项目范围服务；
4. 使用精简 Option VO，避免复用管理列表 VO；
5. 补充越权、跨项目、跨仓库和管理 `list` 权限绕过测试；
6. 前端迁移完成后，才从 AUTH-002 的已知耦合基线删除该接口。

建议首个切入点为入库单物料/仓库/库位 Options，验证完成后再按出库、调拨、盘点、入住、收费顺序迁移。
