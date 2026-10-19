# V1.0 物业基础资源管理功能文档

## 文档元数据

| 项目 | 内容 |
|---|---|
| 功能名称 | 楼栋、单元与房屋管理 |
| 所属菜单 | 物业资源 / 楼栋、单元、房屋 |
| 适用岗位 | 具有对应资源权限和项目 RESOURCE 范围的用户 |
| 当前实现状态 | CRUD 已实现；树形导入部分实现 |
| 文档状态 | 已核对 |

## 1. 功能概述

资源层级为项目 → 楼栋 → 单元 → 房屋。系统提供三个独立管理页面、列表/详情/CRUD/状态/导出/候选接口，以及从房屋页发起的树形原子导入。当前没有独立“整棵资源树查询”Controller；树结构由分层字段和候选接口表达。

## 2. 楼栋管理

- 接口根路径：`/property/resource/building`；权限：`property:building:list/query/add/edit/remove/export`。
- 字段：项目、编码、名称、类型、地上/地下层数、排序、状态。
- 同一项目内未删除楼栋编码唯一，Service 预检并由 `uk_pm_building_active_code(project_id, active_building_code)` 兜底；逻辑删除后可复用编码。
- 新增校验 RESOURCE/WRITE 与项目存在；**当前未检查项目是否启用**。
- 编辑不允许跨项目迁移；状态接口只更新楼栋自身。
- 删除对楼栋、单元和房屋范围加锁；存在未删除单元或房屋时拒绝，不级联删除。

## 3. 单元管理

- 接口根路径：`/property/resource/unit`；权限：`property:unit:list/query/add/edit/remove/export`。
- 字段：项目、楼栋、编码、名称、楼层数、排序、状态。
- 同一楼栋内未删除单元编码唯一，数据库索引为 `uk_pm_unit_active_code(building_id, active_unit_code)`。
- 新增要求项目和楼栋均存在、启用，且楼栋属于目标项目；使用行锁防止父级并发变化。
- 普通编辑不能改变所属项目或楼栋。
- 删除时锁定楼栋、单元和房屋范围；归属发生变化或存在未删除房屋时拒绝。
- 状态只更新单元，不级联更新房屋。

## 4. 房屋管理

- 接口根路径：`/property/resource/room`；权限：`property:room:list/query/add/edit/remove/export`。
- 字段：项目、楼栋、单元、房屋编码、房号、楼层、房屋类型、产权属性、建筑/使用面积、交付状态、入住状态、排序和状态。
- 同一项目内未删除房屋编码唯一，数据库索引为 `uk_pm_room_active_code(project_id, active_room_code)`；房号另有按单元的普通索引，不是已审计的唯一约束。
- 新增要求项目、楼栋、单元存在且启用，三者归属一致；入住状态由 Service 强制设为 `VACANT`。
- 普通编辑不能改变项目、楼栋或单元。虽然 `RoomBo` 含 `occupancyStatus`，Service 不接受其作为最终值，而是根据有效入住关系重新计算 `VACANT/OCCUPIED`。
- 状态接口修改的是资源启停 `status`，不是入住状态。
- 删除只明确检查有效客户关系；有关系则整批事务拒绝。当前未见对收费绑定、应收历史等所有引用的统一删除保护。

## 5. 入住状态与客户关系

入住状态的真实写入口：

- 新建/导入房屋：固定 `VACANT`；导入文件填写非空置会校验失败。
- 房屋普通编辑：根据当前有效入住关系重新计算，不允许表单任意指定。
- 客户关系迁入：关系改为 ACTIVE 后调用 `RoomOccupancyStatusService.recalculate`。
- 客户关系迁出：关系改为 ENDED 后重新计算；仍有其他有效入住关系时保持 OCCUPIED。

因此不能把普通房屋编辑表单描述为入住状态维护入口。

## 6. 权限与数据范围

Controller 校验功能权限；Service 使用 `PropertyBusinessDomain.RESOURCE`：列表/详情为 READ，写操作为 WRITE，导出为 EXPORT，候选为 OPTION。详情和写操作从持久化记录取得 projectId 再校验，可防止伪造查询参数跨项目访问。SuperAdmin 范围为 ALL。

详见 [统一权限体系](../../technical/permissions.md) 和 [项目数据权限](../../business/property/project-access.md)。

## 7. 数据模型与接口

| 表 | 父级 | 唯一性 | 删除保护 |
|---|---|---|---|
| `pm_building` | `pm_project` | 项目内有效编码唯一 | 未删除单元或房屋 |
| `pm_unit` | 项目 + 楼栋 | 楼栋内有效编码唯一 | 未删除房屋 |
| `pm_room` | 项目 + 楼栋 + 单元 | 项目内有效编码唯一 | 有效客户关系 |

三个 Controller 均提供 `GET /list`、`GET /{id}`、`POST`、`PUT`、`PUT /changeStatus`、`DELETE /{ids}`、`POST /export` 和 `GET /options`；楼栋另提供 `GET /project-options`。

BO/VO：`BuildingBo/Vo/ExportVo`、`BuildingUnitBo/Vo`、`RoomBo/Vo/ExportVo`。前端页面和类型位于 `src/views/property/resource/`、`src/api/property/resource/`。

## 8. 测试与验收状态

- 楼栋、单元、房屋安全测试各 8 项。
- MySQL 唯一性测试覆盖生成列、逻辑删除复用与并发重复插入。
- `ResourceParentChildConcurrencyMySqlTest` 覆盖父子并发新增/删除。
- `RoomDeletionTransactionMySqlTest` 覆盖关系保护与批量回滚。
- TASK-V1-002B 的相关专用 MySQL 测试已执行；资源树 HTTP 类 17 项包含在 59/59 导入 HTTP 测试通过结果中。
- 浏览器 E2E 和真实岗位人工验收未执行。

## 9. 已知限制与证据

- 没有独立资源树查询接口。
- 楼栋新增未检查项目启用状态；父子状态没有自动级联。
- 房屋删除的跨收费引用保护不完整或未确认。
- SQL/migration 存在不证明目标数据库已执行。
- 证据：`property/resource/**/controller`、`**/service/impl`、`script/sql/zbm/baseline/01_schema.sql`、migration V2.0.3～V2.0.5、相关测试目录。

