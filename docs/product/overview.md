# 物业管理系统 V1.0 系统总览

## 1. 产品定位与范围

V1.0 是基于 RuoYi-Vue-Plus 技术体系构建的物业运营管理系统。当前核心范围覆盖系统组织与权限、员工、物业项目、楼栋/单元/房屋、客户与房屋关系、收费应收与收款，以及处于不同验证阶段的仓储能力。

本总览依据当前工作树、TASK-V1-001 审计、TASK-V1-002A～002C 验收记录和专用测试数据库结果。仓库当时存在未提交文件，因此这里描述的是“被审计工作树基线”，不是某个 Git Tag。

## 2. 系统组成

| 层次 | 实现位置 | 作用 |
|---|---|---|
| 后端入口 | `ruoyi-admin/` | Spring Boot 启动、认证配置和 HTTP 集成测试 |
| 系统能力 | `ruoyi-modules/ruoyi-system/` | 用户、部门、岗位、角色、菜单、字典、配置、日志、OSS 等 |
| 员工 | `ruoyi-modules/ruoyi-hr/` | 员工业务 |
| 物业业务 | `ruoyi-modules/ruoyi-property/` | 项目、资源、客户、住户、收费与项目工作区 |
| 仓储业务 | `ruoyi-modules/ruoyi-warehouse/` | 仓库主数据、库存及仓储单据 |
| 公共能力 | `ruoyi-common/`、`ruoyi-api/` | 安全、数据权限、Web、MyBatis、Redis、跨模块接口等 |
| 前端 | `E:\space\project\zbm-platform-ui/src/` | Vue 3 页面、API、类型、权限指令和状态管理 |
| 数据库定义 | `script/sql/zbm/` | baseline、V2.0.1～V2.0.36 migration、seed 与诊断脚本 |

后端 Maven reactor 共 43 个模块。`ruoyi-demo`、`ruoyi-workflow`、`ruoyi-ai` 仍被 reactor 和 `ruoyi-admin` 引用；它们不是物业 V1 核心验收范围，也不能在没有进一步依赖验证时视为可删除模块。

## 3. V1.0 功能边界

### 已实现的核心能力

- 系统用户、部门、岗位、角色、菜单、字典、配置与日志。
- 员工 CRUD。
- 物业项目、项目成员、项目管理授权与登录后工作项目候选集。
- 楼栋、单元、房屋及父子资源约束。
- 客户加密/指纹/脱敏、敏感访问审计、客户导入。
- 房屋客户关系、迁入迁出、关系导入和住户聚合查询。
- 收费项目、收费规则、房屋收费绑定。
- 应收批次生成、应收查询/汇总/导出，以及收款、核销和冲销。

上述“已实现”基于代码、数据库定义、权限与自动化测试证据，不代表浏览器 E2E 或生产数据库升级已经验收。

### 部分实现或尚不能确认完整可用

- 物业资源树导入、客户导入、关系导入的 HTTP 测试已在隔离测试环境中真实通过 59 项；整体功能仍缺少浏览器 E2E 和目标环境数据库验收，因此不提升为端到端已验收。
- 仓库、库区、库位、分类、单位、物料、库存、入库和出库具备前后端及单元测试代码，但 migration 执行状态不可追溯，专用测试库还缺少 16 张仓储业务表，统一标记为“部分实现”。
- 调拨和盘点虽有页面、Controller、Service 与 SQL，但尚不能确认完整数据库链路和端到端可用性，标记为“仅存在代码但不可确认可用”。

### 明确不应混淆的概念

- **企业主数据不等于系统部门。** 当前未发现独立企业档案、统一社会信用代码和企业生命周期模块。仓储公司范围复用 `sys_dept`。
- **应收记录不等于独立账单模型。** 当前以 `pm_receivable` 承载应收/账单语义，没有独立账单头、账单明细、发布和撤回模型。
- **SQL 文件不等于已执行版本。** baseline 和 migration 在仓库中存在，但开发/生产数据库缺少可信的执行账本。
- **菜单和按钮不等于授权。** 后端 Controller 权限、BusinessContext、项目/仓库资源范围才是最终授权依据。

### V1.0 未包含

- 独立企业主数据。
- 独立账单聚合模型。
- 退款、预收款余额、押金、坏账、财务结账。
- 采购、供应商、销售、批次/序列号和成本核算。

这些能力仅作为后续候选，不属于 V1.0 已实现清单。

## 4. 权限模型

权限由四层共同组成：身份与 SuperAdmin、角色菜单功能权限、`BusinessContext` 引用数据上下文、物业项目或仓储资源范围。物业使用 `PropertyProjectScopeService` 等统一范围服务；仓储使用 `WarehouseAccessService`；当前项目仅是前端 UI 偏好，不能作为后端授权依据。

关键证据：

- `ruoyi-modules/ruoyi-system/src/main/java/org/dromara/system/service/impl/SysPermissionServiceImpl.java`
- `ruoyi-common/ruoyi-common-security/src/main/java/org/dromara/common/security/business/`
- `ruoyi-modules/ruoyi-property/src/main/java/org/dromara/property/scope/`
- `ruoyi-modules/ruoyi-warehouse/src/main/java/org/dromara/warehouse/security/WarehouseAccessService.java`
- `docs/audit/V1.0-权限体系审计.md`

## 5. 数据库与验证现状

- 新装环境应使用 baseline；存量环境应使用经过验证的增量 migration，两条路径禁止混用。
- migration 清单覆盖 V2.0.1～V2.0.36，含 V2.0.33.1，但没有证据证明正式环境已全部执行。
- TASK-V1-002B 对原 155 项集成测试进行了真实启用：149 项通过，6 项因安全的 DDL 权限边界阻塞；不是全绿。
- 6 项阻塞包括 3 项 schema 等价测试所需 DROP 权限，以及 3 项触发器故障注入所需高权限。
- TASK-V1-002C 后端 43 模块编译与全量默认测试通过；默认全量测试仍按环境门禁跳过上述 155 项。前端 typecheck、lint、30 项 Vitest 和生产构建通过。
- 浏览器 E2E、真实岗位人工验收、生产 migration 验证均未执行。

## 6. 后续阅读

- [V1.0 功能矩阵](feature-matrix.md)
- [统一模块文档模板](document-template.md)
- [逐模块编写计划](documentation-plan.md)
- [TASK-V1-001 功能矩阵](../audit/V1.0-功能实现矩阵.md)
- [TASK-V1-002B 集成测试执行报告](../audit/V1.0-集成测试执行报告.md)
