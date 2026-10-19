# V1.0 逐模块文档编写计划

## 1. 目标与原则

后续按本计划逐篇补充 `docs/business/`、`docs/technical/` 和 `docs/user-guide/`。每篇使用 [统一模板](document-template.md)，先建立事实证据，再描述操作流程；不得从菜单名反推功能，不得把规划写入 V1.0 已实现清单。

建议每篇采用“代码核对 → 权限核对 → 数据模型核对 → 测试证据核对 → 业务复核”的顺序。涉及仓储和数据库执行状态的文档必须保留“部分实现/未验证”标识，直到补齐专用 schema 与真实验证。

## 2. 编写批次

计划路径均相对于 `docs/`；代码证据中的 `property/`、`warehouse/` 分别指两个业务模块的 Java 包根目录，详细文档编写时必须展开为完整仓库相对路径。

| 优先级 | 计划文档 | 主要范围 | 关键证据 | 当前状态/重点 |
|---|---|---|---|---|
| P0 | `business/property/project.md` | 项目、成员、管理授权、工作项目 | `property/project/`、`property/workspace/`、前端 project/store | 已实现；解释候选项目与授权的区别 |
| P0 | `technical/permissions.md` | SuperAdmin、功能权限、BusinessContext、项目/仓库范围 | 权限审计、`common-security/business/`、scope/access service | 已有专题证据，需整合成稳定技术文档 |
| P0 | `business/customer/customer-security.md` | 客户 CRUD、加密、指纹、脱敏、审计 | `property/customer/`、客户 HTTP/DB 测试 | 已实现；保留 2 个触发器故障注入阻塞 |
| P0 | `business/customer/room-relation.md` | 业主/住户、迁入迁出、导入 | `customer/relation/`、resident、关系测试 | 已实现/导入部分实现；保留 1 个故障注入阻塞 |
| P0 | `business/charge/receivable.md` | 应收生成、查询、汇总、导出 | `charge/receivable/`、V2.0.20 | 已实现；明确应收不是独立账单模型 |
| P0 | `business/charge/payment.md` | 收款、核销、冲销 | `charge/payment/`、V2.0.21～22 | 已实现；重点记录事务、反向分摊与权限 |
| P1 | `business/property/resource.md` | 楼栋、单元、房屋、树导入 | `property/resource/`、前端资源页、导入 HTTP 测试 | CRUD 已实现；导入缺 E2E |
| P1 | `business/charge/charge-item-rule.md` | 收费项目、规则 | `charge/item/`、`charge/rule/` | 已实现；记录周期和适用范围 |
| P1 | `business/charge/charge-binding.md` | 房屋收费设置 | `charge/binding/`、V2.0.19/35 | 已实现；记录上下文与 FINANCE 范围 |
| P1 | `business/warehouse/master-data.md` | 仓库、库区、库位、分类、单位、物料 | `ruoyi-warehouse` 对应目录、V2.0.23～24 | 部分实现；测试库缺表 |
| P1 | `business/warehouse/inventory.md` | 余额、流水、库存变更 | `warehouse/inventory/`、V2.0.25～26 | 部分实现；不得把 Service UT 写成 DB 验收 |
| P1 | `business/warehouse/inbound-outbound.md` | 入库、出库状态流转 | `warehouse/inbound/`、`outbound/` | 部分实现；需补 DB/E2E |
| P2 | `business/warehouse/transfer.md` | 调拨 | `warehouse/transfer/`、V2.0.30 | 仅代码；先做完整数据库验证再提升状态 |
| P2 | `business/warehouse/stocktake.md` | 盘点与差异调整 | `warehouse/stocktake/`、V2.0.31 | 仅代码；先做完整数据库验证再提升状态 |
| P2 | `business/system/organization-and-employee.md` | 部门、岗位、用户、员工 | `ruoyi-system`、`ruoyi-hr` | 明确部门不等于企业主数据 |
| P2 | `business/system/roles-and-menus.md` | 角色、菜单、委派保护 | system Service、权限契约、菜单 SQL | 正式数据库角色菜单状态仍需核对 |
| P2 | `technical/database.md` | baseline、migration ledger、差异检查 | migration 清单、数据库差异报告、审计脚本 | 强调新装/升级路径隔离 |
| P2 | `technical/architecture.md` | 43 模块、前后端边界、公共能力 | root/module POM、模块清单 | 不把 demo/workflow/AI 归入物业核心功能 |
| P2 | `technical/api/README.md` | API 分组、鉴权和错误模型 | Controller、OpenAPI 配置、公共响应模型 | 先定义索引，再按业务模块展开 |
| P3 | `user-guide/system/` | 系统管理员操作 | 完成业务文档后编写 | 需要真实角色和浏览器验收 |
| P3 | `user-guide/property/` | 项目、资源、客户 | 同上 | 不先于 E2E/人工验收宣称步骤可用 |
| P3 | `user-guide/charge/` | 收费与收款岗位 | 同上 | 需明确不可逆/冲销操作和权限 |
| P3 | `user-guide/warehouse/` | 仓库岗位 | 同上 | 等仓储 DB 链路验证后再定稿 |
| P3 | `release/known-issues.md` | 当前缺陷、阻塞与限制 | 审计和逐模块文档 | 持续维护，不混入候选功能 |
| P3 | `release/v1.0-checklist.md` | 构建、测试、DB、权限、人工验收 | 002A～002C 报告 | 6 项阻塞和 E2E 未执行必须保留 |

## 2.1 TASK-V1-004B 完成情况

以下计划项已于 2026-10-13 完成首轮正式编写并完成代码证据核对：

- `product/modules/system-management.md`：用户、部门、岗位、字典、参数、日志。
- `product/modules/employee-management.md`：员工档案、部门/岗位、账号绑定及边界。
- `product/modules/permission-management.md`：角色、菜单与产品视角权限说明。
- `technical/permissions.md`：Sa-Token、SuperAdmin、BusinessContext、项目/仓库范围、客户敏感权限。
- `business/property/project-access.md`：项目成员业务范围与项目管理授权。
- `business/warehouse/warehouse-access.md`：公司/项目仓库及仓储上下文。
- `business/property/workspace-initialization.md`：登录后候选项目、自动选择与切换。

原计划中的 `business/system/organization-and-employee.md`、`business/system/roles-and-menus.md` 已分别由上述 system/employee/permission 三份模块文档覆盖，不再创建重复目录和重复文档。后续如需拆分，应从现有文档迁移而非复制。

## 2.2 TASK-V1-004C 完成情况

以下项目与资源文档已于 2026-10-14 完成首轮正式编写和代码证据核对：

- `product/modules/project-management.md`
- `product/modules/project-member-management.md`
- `product/modules/property-resource-management.md`
- `business/property/project-lifecycle.md`
- `business/property/resource-hierarchy.md`
- `business/property/resource-import.md`

原计划 `business/property/project.md` 已拆为产品模块文档、项目成员文档和 `project-lifecycle.md`，并复用既有 `project-access.md`；原计划 `business/property/resource.md` 已拆为资源管理、层级和导入三份文档。此拆分避免在单文件中混合管理范围、业务范围和导入验收状态。

## 3. 每篇完成定义

一篇模块文档只有同时满足以下条件才能标记“已核对”：

1. 页面、API、Controller、Service、模型/SQL 路径均已实际核对，缺失项明确写出。
2. CRUD、导入导出、状态流转和特殊操作逐项列出，不以一个“完整 CRUD”概括。
3. 权限码同时核对前端、Controller、菜单 SQL 和角色 SQL，并说明合法差异。
4. 项目/仓库范围和 BusinessContext 引用真实 Service，不以当前前端选择代替授权。
5. 数据库表、唯一约束、逻辑删除及 migration 执行状态分别记录。
6. 测试按编译、UT、DB、HTTP、E2E、人工验收分层填写。
7. 当前缺陷、未完成验证、V1.0 不包含和后续候选四类内容没有混写。

## 4. 待产品或业务确认的问题

- V1.0 是否将 `sys_dept` 的公司节点仅作为组织/范围来源，还是未来需要独立企业档案。
- 产品界面中的“账单”是否接受当前应收模型语义，还是后续需要账单头/明细生命周期。
- 调拨、盘点进入可用清单前所需的岗位、状态流转和差异处理验收标准。
- 仓储 16 张缺失表的隔离测试初始化方案和 6 项高权限故障注入测试的安全运行环境。

这些问题只影响后续文档定稿或版本规划，不改变当前 V1.0 事实矩阵。
