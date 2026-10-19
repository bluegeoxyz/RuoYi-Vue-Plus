# 物业管理系统 V1.0 功能矩阵

## 1. 判定说明

“实现状态”沿用 TASK-V1-001 的业务结论；“验证状态”吸收 TASK-V1-002A～002C 的后续结果。数据库支持表示仓库内存在模型/SQL，不代表开发或生产数据库已执行 migration。所有功能均未取得浏览器 E2E 通过证据。

缩写：`UT` 为单元/组件测试，`DB` 为专用 MySQL 集成测试，`HTTP` 为后端 HTTP 集成测试，`E2E` 为浏览器端到端测试。

路径简写：表内 `property/` 指 `ruoyi-modules/ruoyi-property/src/main/java/org/dromara/property/`，`warehouse/` 指 `ruoyi-modules/ruoyi-warehouse/src/main/java/org/dromara/warehouse/`；“前端”路径相对于 `E:\space\project\zbm-platform-ui/src/`。类名可在对应目录中唯一定位。

## 2. 功能矩阵

| 领域 | 功能 | 后端/前端证据 | 数据库与权限证据 | 验证状态 | 实现状态 |
|---|---|---|---|---|---|
| 系统 | 用户、部门、岗位 | `ruoyi-system/.../SysUserController.java`、`SysDeptController.java`、`SysPostController.java`；前端 `src/views/system/` | 框架 `sys_user/sys_dept/sys_post`；Sa-Token 与数据权限 | 编译、既有模块测试通过；E2E 未执行 | 已实现 |
| 系统 | 角色与菜单授权 | `SysRoleController.java`、`SysMenuController.java`、相关 Service；前端系统管理页面 | `sys_role*`、`sys_menu`；SuperAdmin/委派保护 | 权限契约测试通过；目标库角色菜单实际状态未完全证明 | 已实现 |
| 系统 | 字典、配置、日志、通知、OSS | `ruoyi-system` 对应 Controller/Service；前端 `views/system`、`views/monitor` | 框架系统表及权限码 | 编译与既有测试通过；外部服务和 E2E 未验证 | 已实现 |
| 人事 | 员工 CRUD | `ruoyi-hr/.../EmployeeController.java`；前端 `views/hr/employee/index.vue` | `hr_employee`；`hr:employee:*` | 编译与模块测试通过；E2E 未执行 | 已实现 |
| 组织 | 独立企业主数据 | 未发现独立 Controller/Service/页面 | 无独立企业表；仓储公司范围复用 `sys_dept` | 不适用 | 未实现 |
| 项目 | 项目 CRUD | `property/project/controller/ProjectController.java`；前端 `views/property/project/index.vue` | `pm_project`；`property:project:*` + 项目范围 | UT/权限测试通过；E2E 未执行 | 已实现 |
| 项目 | 项目成员 | `ProjectMemberController.java`；前端 `project/member/index.vue` | `pm_project_member`；成员范围贡献器 | Service/Controller 测试通过 | 已实现 |
| 项目 | 项目管理授权 | `ProjectManagementGrantController.java`、对应 Service；前端 `project/admin-scope/index.vue` | grant/audit 表；仅 SuperAdmin 维护 | Service 与权限测试通过；正式库 migration 状态未证明 | 已实现 |
| 项目 | 工作项目初始化/切换 | `property/workspace/`；前端 `currentProject.ts`、`currentProjectPolicy.ts` | 候选集来自真实项目范围；前端选择不是授权依据 | 后端策略测试和前端 Vitest 通过 | 已实现 |
| 物业资源 | 楼栋 | `resource/building/BuildingController.java`；前端 building 页面 | `pm_building`；功能权限 + 项目范围 | 安全与 DB 唯一约束测试通过 | 已实现 |
| 物业资源 | 单元 | `resource/unit/BuildingUnitController.java`；前端 unit 页面 | `pm_unit`；功能权限 + 项目范围 | 安全与 DB 唯一约束测试通过 | 已实现 |
| 物业资源 | 房屋 | `resource/room/RoomController.java`；前端 room 页面 | `pm_room`；功能权限 + 项目范围 | 安全、事务与 DB 约束测试通过 | 已实现 |
| 物业资源 | 树形导入 | `resource/importer/PropertyResourceImportController.java`；前端 treeImport API/资源页 | 资源表；导入专用权限 | 对应 HTTP 导入测试已纳入 59/59 通过；E2E 未执行 | 部分实现 |
| 客户 | 客户 CRUD、加密、脱敏与审计 | `customer/CustomerController.java`、`customer/security/`；前端 customer 页面 | `pm_customer` 安全列；PII view/export 权限 | 安全 UT 与 DB 测试有通过证据；3 个故障注入场景中 2 个客户场景受触发器权限阻塞 | 已实现 |
| 客户 | 客户导入 | `customer/importer/CustomerImportController.java`；前端客户页/API | 复用客户表；import 权限 | HTTP 导入测试已纳入 59/59 通过；E2E 未执行 | 部分实现 |
| 客户 | 住户聚合查询 | `resident/ResidentController.java`；前端 resident 页面 | 基于客户、关系、房屋；列表权限 + 项目范围 | Service/DB/权限测试通过 | 已实现 |
| 客户 | 房屋关系、迁入迁出 | `customer/relation/RoomCustomerRelationController.java`；前端 relation 页面 | `pm_room_customer_rel`；操作权限 + 项目范围 | 安全、并发、上下文测试通过；1 个审计故障注入场景阻塞 | 已实现 |
| 客户 | 房屋关系导入 | `relation/importer/RoomCustomerRelationImportController.java`；前端 relation API | 关系表；import 权限 | HTTP 导入测试已纳入 59/59 通过；E2E 未执行 | 部分实现 |
| 收费 | 收费项目 | `charge/item/ChargeItemController.java`；前端 item 页面 | `pm_charge_item`；`property:chargeItem:*` + FINANCE 范围 | 安全 UT 与 DB 测试通过 | 已实现 |
| 收费 | 收费规则 | `charge/rule/ChargeRuleController.java`；前端 rule 页面 | `pm_charge_rule`；功能权限 + FINANCE 范围 | 安全 UT 与 DB 测试通过 | 已实现 |
| 收费 | 房屋收费绑定 | `charge/binding/ChargeBindingController.java`；前端 binding 页面 | `pm_charge_binding`；上下文 + FINANCE 范围 | 安全、上下文与 DB 测试通过 | 已实现 |
| 收费 | 应收生成 | `ReceivableGenerationController.java`、receivable engine/worker；前端 receivable 页面 | batch + `pm_receivable`；generate + WRITE 范围 | 引擎/Service/安全测试通过；1 个 schema 等价测试因 DROP 权限阻塞 | 已实现 |
| 收费 | 应收查询、汇总、详情、导出 | `ReceivableController.java`、`ReceivableServiceImpl.java`；前端 receivable 页面 | `pm_receivable`；list/query/export + READ/EXPORT 范围 | Service 测试通过；E2E 未执行 | 已实现 |
| 收费 | 独立账单模型 | 未发现账单头/明细 Controller、实体和页面 | 当前由 `pm_receivable` 承载应收语义 | 不适用 | 未实现 |
| 收费 | 收款与核销 | `charge/payment/PaymentController.java`、自动分配引擎；前端 payment 与 PaymentDialog | payment/allocation 表；add/allocate + WRITE 范围 | 引擎/安全/DB 测试通过；1 个 schema 等价测试因 DROP 权限阻塞 | 已实现 |
| 收费 | 冲销 | `PaymentServiceImpl.java`；前端 PaymentReverseDialog | `pm_payment_reversal`；reverse 权限 | 事务/Controller/DB 测试通过；1 个 schema 等价测试因 DROP 权限阻塞 | 已实现 |
| 仓储主数据 | 仓库、库区、库位 | `warehouse/{warehouse,zone,location}/`；对应前端页面 | `wm_*` SQL；菜单权限 + `WarehouseAccessService` | 相关 UT/权限/上下文测试通过；测试库缺少仓储表，DB 集成未完成 | 部分实现 |
| 仓储主数据 | 分类、单位、物料 | `warehouse/{material,unit}/`；对应前端页面 | `wm_*` SQL；公司/仓库上下文 | 相关 UT/上下文测试通过；DB 集成未完成 | 部分实现 |
| 库存 | 库存余额 | `inventory/InventoryController.java`、`InventoryMutationService.java`；前端 inventory 页面 | `wm_inventory`；仓库范围 | Service 测试通过；测试库 schema 不完整；E2E 未执行 | 部分实现 |
| 库存 | 库存流水 | `inventory/transaction/InventoryTransactionController.java`；前端 inventoryTransaction 页面 | `wm_inventory_transaction`；查询上下文 + 仓库范围 | Service 测试通过；DB 集成未完成 | 部分实现 |
| 仓储单据 | 入库 | `inbound/InboundOrderController.java`；前端 inbound 页面 | 订单/明细 SQL；维护/查询上下文 | Controller/Service 测试通过；DB/E2E 未完成 | 部分实现 |
| 仓储单据 | 出库 | `outbound/OutboundOrderController.java`；前端 outbound 页面 | 订单/明细 SQL；维护/查询上下文 | Service 测试通过；DB/E2E 未完成 | 部分实现 |
| 仓储单据 | 调拨 | `transfer/TransferOrderController.java`；前端 transfer 页面 | V2.0.30 SQL；维护/查询上下文 | 代码可编译；完整 DB 与 E2E 未验证 | 仅存在代码但不可确认可用 |
| 仓储单据 | 盘点 | `stocktake/StocktakeOrderController.java`；前端 stocktake 页面 | V2.0.31 SQL；维护/查询上下文 | 代码可编译；完整 DB 与 E2E 未验证 | 仅存在代码但不可确认可用 |

## 3. 全局验证结论

- 后端：43 模块编译通过；默认 Maven 全量测试 786 项、0 failure、0 error、155 skipped。
- 隔离集成环境：原 155 项已真实启用，149 项通过、6 项因高风险 DDL 权限边界阻塞。
- 前端：typecheck、lint、30 项 Vitest、生产构建通过。
- 未执行：浏览器 E2E、正式岗位人工验收、生产数据库 migration 验证。

证据：`docs/audit/V1.0-集成测试执行报告.md`、`docs/audit/V1.0-TASK-V1-002B-验收报告.md`、`docs/audit/V1.0-TASK-V1-002C-验收报告.md`。
