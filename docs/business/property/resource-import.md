# V1.0 物业资源树形导入

## 文档元数据

| 项目 | 内容 |
|---|---|
| 功能名称 | 楼栋—单元—房屋树形原子导入 |
| 页面入口 | 房屋管理页面 |
| 当前实现状态 | 部分实现 |
| 文档状态 | 已核对 |

## 1. 功能概述

一次 Excel 导入在指定项目内创建或复用楼栋、单元和房屋。任何行校验失败则不写入；提交阶段任何并发或业务冲突会回滚整份文件并返回“写入 0 条”。项目 ID 由请求上下文传入，各级数据库 ID 由服务端解析或生成。

## 2. 接口与权限

| Method | URL | 请求/返回 | 权限 |
|---|---|---|---|
| GET | `/property/resource/tree-import/template` | Excel 模板 | `property:resource:treeImport` |
| POST | `/property/resource/tree-import?projectId=...` | multipart `file` / `ImportResult` | `property:resource:treeImport` |

Controller 使用 `@SaCheckPermission`；Service 在校验和提交阶段再次调用 `PropertyFunctionalPermissionService.assertCurrent`，并校验 RESOURCE/WRITE 项目范围。项目必须存在且启用。

## 3. 模板字段

每行代表一个房屋及其父级：楼栋编码/名称/类型/楼层/排序，单元编码/名称/楼层数/排序，房屋编码/房号/楼层/类型/产权、建筑面积、使用面积、交付状态、入住状态、排序和状态。

模板包含示例数据和工作表提示“任一行失败则全部不导入”。新房屋入住状态只能为空置，最终写入始终为 `VACANT`。

## 4. 校验规则

- 文件解析启用非 fail-fast、自动 trim/strip；解析错误直接返回失败结果，不进入 Service 写入。
- 文件不能为空，单次最多 2000 行。
- 必填、长度和面积非负由行模型校验。
- 楼栋类型、房屋类型、产权、交付、入住、启停等输入通过字典服务解析，可接受配置的代码/展示值。
- 同一文件内相同楼栋编码必须属性一致；相同楼栋+单元编码必须属性一致；房屋编码不得重复。
- 数据库已有同编码楼栋/单元/房屋时，只有全部相关属性和父级归属一致才视为幂等复用；不一致返回错误。
- 房屋编码唯一范围是项目；单元编码唯一范围是楼栋；楼栋编码唯一范围是项目。

## 5. 提交与失败处理

校验阶段只读。提交前执行可插拔 `PropertyResourceImportCommitHook`，随后 `TransactionTemplate` 在一个事务中：锁定启用项目，按编码锁定/创建楼栋和单元，再锁定/创建房屋。

- 已存在且一致：跳过创建，继续处理。
- 并发中属性发生变化：抛出异常并回滚。
- 任一插入或 hook 失败：捕获根因并返回 transaction 错误，明确本次写入 0 条。
- 不支持“部分成功”；返回的成功数仅在整份事务成功后成立。

## 6. 前端行为

入口在房屋页，要求当前工作项目。选择文件后若工作项目发生切换，前端关闭或忽略旧请求结果并提示重新选择。前端项目选择只是体验保护；后端仍独立校验请求 projectId。

## 7. 数据模型

写入/复用 `pm_building`、`pm_unit`、`pm_room`。不创建客户关系、收费绑定或住户记录，不级联修改既有关系。SQL 唯一约束来自 baseline 和 V2.0.3～V2.0.5；权限菜单来自 V2.0.11，但 migration 文件存在不代表目标库已执行。

## 8. 测试与验收状态

- `PropertyResourceImportServiceImplTest`：6 项，覆盖校验、原子提交和冲突等 Service 行为。
- `PropertyResourceTreeImportHttpIntegrationTest`：17 项；TASK-V1-002B 在隔离环境中已实际执行，并包含在三类导入 HTTP 59/59 通过结果中。
- TASK-V1-001 矩阵中“HTTP 测试本轮跳过”是当时状态；002B 已更新为实际通过。功能仍标记“部分实现”，因为没有浏览器 E2E、正式数据库 migration 证明或人工导入验收。

## 9. 已知限制与证据

- 最大 2000 行，不提供后台异步任务或断点续传。
- 只支持创建/幂等复用，不是批量更新工具。
- 导入模板下载也要求专用权限。
- 浏览器文件兼容性、超大文件体验和正式库执行状态未验证。
- 证据：`PropertyResourceImportController.java`、`PropertyResourceImportServiceImpl.java`、`PropertyResourceImportRow.java`、前端 `api/property/resource/treeImport/` 与房屋页、相关测试类。

