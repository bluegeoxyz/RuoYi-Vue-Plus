# V1.0 仓库数据权限

## 文档元数据

| 项目 | 内容 |
|---|---|
| 功能名称 | 仓库数据权限与仓储业务上下文 |
| 所属模块 | 仓储主数据、库存、仓储单据 |
| 适用岗位 | 具有相应 `warehouse:*` 权限且处于公司/项目仓库范围的用户 |
| 当前实现状态 | 部分实现 |
| 文档状态 | 已核对 |

## 1. 功能概述

仓储模块采用功能权限、BusinessContext 和 `WarehouseAccessService` 三层控制。仓库分为公司仓库与项目仓库：公司仓库使用系统部门数据范围，项目仓库使用物业项目的 `WAREHOUSE` 业务域。仓库上下文独立于前端物业工作项目。

之所以标记“部分实现”，不是因为代码无法编译：相关 Controller/Service 与权限测试存在；限制是 migration 执行状态不可证明，专用测试库缺少 16 张仓储业务表，尚未完成完整数据库与浏览器端到端验证。

## 2. 功能清单

| 范围能力 | 实现 | 说明 |
|---|---|---|
| 仓库列表过滤 | `WarehouseAccessService.applyScope` | 合并可访问公司和项目；无范围时返回空 |
| 仓库详情/操作 | `requireWarehouse` | 实体存在后按 COMPANY/PROJECT 分支校验 |
| 新增下级资料 | `requireEnabledWarehouse` | 在访问校验上追加仓库启用要求 |
| 公司候选 | 部门 Service | 受当前用户部门数据范围约束 |
| 项目候选 | `ProjectReferenceService.selectWarehouseOptions` | 使用物业 WAREHOUSE/OPTION 范围 |
| 物料公司范围 | `accessibleMaterialCompanyIds` | 部门公司 + 可访问项目仓库对应公司 |
| 上下文引用数据 | 多个 ContextOptionController | BusinessContext + ReferenceType + 实体范围 |
| SuperAdmin | 显式旁路 | 范围全量；业务状态与功能接口规则仍保留 |

## 3. 业务规则

- `wm_warehouse.warehouse_scope=COMPANY` 时必须有 `company_id`，访问依赖 `ISysDeptService.checkDeptDataScope`。
- `warehouse_scope=PROJECT` 时必须有 `project_id`，访问依赖 `ProjectReferenceService.resolveWarehouseScope()`。
- 项目仓库范围来自 `PropertyProjectScopeService` 的 `PropertyBusinessDomain.WAREHOUSE`，当前普通用户基础来源仍是有效项目成员。
- 公司与项目必须存在且启用；创建库区、库位等下级资料时仓库也必须启用。
- 物料主数据按公司控制；项目仓库会把其 `company_id` 合并进可访问物料公司集合。
- 公司来源是 `sys_dept` 组织节点，不能描述为已经实现的独立企业档案。
- 列表过滤与详情/状态操作必须分别校验；仅过滤列表不能保护直接 ID 请求。

## 4. 权限与 BusinessContext

主要功能权限遵循：

- 仓库：`warehouse:warehouse:list/query/add/edit/remove`
- 库区：`warehouse:zone:list/query/add/edit/remove`
- 库位：`warehouse:location:list/query/add/edit/remove`
- 分类：`warehouse:materialCategory:list/query/add/edit/remove`
- 物料：`warehouse:material:list/query/add/edit/remove`
- 库存：`warehouse:inventory:list/query`、`warehouse:inventoryTransaction:list/query`
- 单据：各自 `list/query/add/edit/remove/export` 及 `confirm/complete/cancel` 等状态操作权限。

BusinessContext 策略将查询和维护分开：

| 场景 | 上下文 | 允许读取的主要引用 |
|---|---|---|
| 库区 | `WAREHOUSE_ZONE_QUERY/MAINTAIN` | 仓库 |
| 库位 | `WAREHOUSE_LOCATION_QUERY/MAINTAIN` | 仓库、库区 |
| 物料分类 | `WAREHOUSE_MATERIAL_CATEGORY_QUERY/MAINTAIN` | 公司；维护另含分类 |
| 物料 | `WAREHOUSE_MATERIAL_QUERY/MAINTAIN` | 公司、分类、计量单位 |
| 库存/流水 | `INVENTORY_QUERY`、`INVENTORY_TRANSACTION_QUERY` | 公司、仓库、库区、库位、分类、单位、物料 |
| 入/出/调拨/盘点维护 | 对应 `*_MAINTAIN` | 仓库、库区、库位、单位、物料；部分含库存 |
| 入/出/调拨/盘点查询 | 对应 `*_QUERY` | 仓库 |

上下文只允许访问引用类型；随后仍需 `WarehouseAccessService` 及具体 Service 校验范围。

## 5. 数据模型

| 表/字段 | 用途 |
|---|---|
| `wm_warehouse` | 仓库主档；`company_id`、`project_id`、`warehouse_scope`、状态 |
| `wm_warehouse_zone` | 库区，归属仓库 |
| `wm_warehouse_location` | 库位，归属仓库和库区 |
| `wm_material_category`、`wm_material` | 公司范围物料主数据 |
| `wm_inventory`、`wm_inventory_transaction` | 库存余额和流水 |
| 仓储订单/明细表 | 入库、出库、调拨、盘点数据 |
| `sys_dept` | 公司组织范围来源 |
| `pm_project`、`pm_project_member` | 项目仓库范围来源 |

仓储 SQL 主要位于 migration V2.0.23～V2.0.31 和 baseline。文件存在不代表测试、开发或生产数据库已经应用。

## 6. 主要接口

| 基础路径 | 作用 | 权限/范围 |
|---|---|---|
| `/warehouse/warehouse` | 仓库 CRUD、状态、公司/项目候选 | 功能权限 + WarehouseAccessService |
| `/warehouse/zone` | 库区 CRUD/候选 | 功能权限 + 仓库范围 |
| `/warehouse/location` | 库位 CRUD/候选 | 功能权限 + 仓库/库区归属 |
| `/warehouse/material` | 物料 CRUD/候选 | 功能权限 + 物料公司范围 |
| `/warehouse/inventory` | 库存查询 | 功能权限 + 仓库范围 |
| `/warehouse/inventoryTransaction` | 库存流水查询 | 功能权限 + 仓库范围 |
| `/warehouse/inbound`、`/outbound` | 单据与状态操作 | 功能权限 + 单据仓库范围 |
| `/warehouse/transfer`、`/stocktake` | 调拨/盘点 | 代码存在；完整可用性未确认 |
| `/warehouse/**/context/**` | 查询/维护引用选项 | BusinessContext + ReferenceType + 资源范围 |

## 7. 典型访问场景

- 只有某部门数据范围的用户：可访问该范围内 COMPANY 仓库，不因此获得其他项目仓库。
- 有物业项目成员关系的仓储用户：可访问对应 PROJECT 仓库；仍需相应 `warehouse:*` 功能权限。
- 同时具备两类范围：列表合并两类仓库。
- 没有公司或项目范围：列表返回空，直接访问仓库 ID 被拒绝。
- 只有仓储权限：不初始化物业全局工作项目，使用仓储页面自己的上下文候选。
- SuperAdmin：范围不受公司/项目过滤。

## 8. 测试与限制

`WarehouseAccessServiceTest`、仓储上下文 Controller 测试、模块 133 项历史测试和 TASK-V1-002C 全量门禁通过。仓储代码可编译，但专用测试库缺少 16 张仓储表，数据库集成链路未完成；调拨和盘点仍是“仅存在代码但不可确认可用”。浏览器 E2E 未执行。

## 9. 证据路径

- `ruoyi-modules/ruoyi-warehouse/src/main/java/org/dromara/warehouse/security/WarehouseAccessService.java`
- `ruoyi-modules/ruoyi-warehouse/src/main/java/org/dromara/warehouse/**/controller/`
- `ruoyi-api/src/main/java/org/dromara/property/api/ProjectReferenceService.java`
- `ruoyi-modules/ruoyi-property/src/main/java/org/dromara/property/project/service/impl/ProjectReferenceServiceImpl.java`
- `ruoyi-common/ruoyi-common-security/src/main/java/org/dromara/common/security/business/BusinessContextPolicy.java`
- `script/sql/zbm/migration/V2.0.23__warehouse_location_schema_permission.sql`
- `docs/audit/V1.0-数据库差异检查报告.md`
