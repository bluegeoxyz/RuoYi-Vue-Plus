# 物业管理系统 V1.0 文档中心

- [TASK-V1-008 工程清理与部署审计](audit/V1.0-TASK-V1-008-工程清理与部署审计.md)
- [TASK-V1-008 构建与容器验证报告](audit/V1.0-TASK-V1-008-构建与容器验证报告.md)
- [TASK-V1-008 遗留问题清单](audit/V1.0-TASK-V1-008-遗留问题清单.md)
- [TASK-V1-008-A Spring Boot 配置与 Docker 环境适配验收报告](audit/V1.0-TASK-V1-008-A-Spring配置与Docker环境适配验收报告.md)
- [Docker 部署说明](deployment/docker-deployment.md)

本目录以当前前后端工作树、数据库脚本和实际测试报告为事实来源。文档描述“仓库中已经实现并得到何种程度验证的能力”，不把路线图、SQL 文件存在或菜单名称当作功能已交付证明。

## 阅读入口

| 目录 | 用途 | 当前内容 |
|---|---|---|
| [`audit/`](audit/) | 代码、功能、权限、数据库和测试的原始审计证据 | TASK-V1-001～002C 报告，保留原文 |
| [`security/`](security/) | 权限专项设计、修复与验证记录 | 既有安全专题文档，保留原位置 |
| [`product/`](product/) | 产品边界、功能矩阵、统一模板与编写计划 | [系统总览](product/overview.md)、[功能矩阵](product/feature-matrix.md)、[模块模板](product/document-template.md)、[编写计划](product/documentation-plan.md) |
| `business/` | 后续逐模块业务规则文档 | 预留 `property/`、`customer/`、`charge/`、`warehouse/` |
| `technical/` | 后续架构、权限、数据库和 API 文档 | 预留 `api/` |
| `user-guide/` | 后续按岗位编写的操作手册 | 预留 `system/`、`property/`、`charge/`、`warehouse/` |
| `release/` | 后续版本已知问题和发布核对表 | 目录已建立，内容待后续任务编写 |

## TASK-V1-005 阶段 A

- [阶段 A 审计报告](audit/V1.0-TASK-V1-005-阶段A审计报告.md)
- [工程模块依赖审计](audit/V1.0-TASK-V1-005-工程模块依赖审计.md)
- [数据库现状与 V1 基线方案](audit/V1.0-TASK-V1-005-数据库现状与基线方案.md)

上述文档只完成只读审计和方案设计。模块删除、Flyway 接入、V1 SQL 生成、空库验证及开发库切换均尚未实施，需逐阶段人工确认。

## 已完成模块文档

- [TASK-V1-005-B 工程模块清理与生产依赖隔离验收报告](audit/V1.0-TASK-V1-005-B-验收报告.md)
- [TASK-V1-005-C 数据库 V1 基线与 Flyway 接入验收报告](audit/V1.0-TASK-V1-005-C-验收报告.md)
- [数据库表去留矩阵](audit/V1.0-数据库表去留矩阵.md)
- [历史 Migration 归档对照表](audit/V1.0-历史Migration归档对照表.md)
- [V1 数据库基线说明](audit/V1.0-数据库基线说明.md)
- [Flyway 使用与版本管理规范](technical/flyway-version-management.md)
- [新环境数据库初始化说明](audit/V1.0-新环境数据库初始化说明.md)
- [新旧数据库结构差异报告](audit/V1.0-新旧数据库结构差异报告.md)
- [阶段 D 数据库切换方案](audit/V1.0-阶段D数据库切换方案.md)
- [TASK-V1-005-D 开发数据库切换验收报告](audit/V1.0-TASK-V1-005-D-验收报告.md)
- [开发数据库切换与回滚说明](audit/V1.0-开发数据库切换与回滚说明.md)
- [TASK-V1-005-E 最终验收报告](audit/V1.0-TASK-V1-005-E-最终验收报告.md)
- [浏览器 E2E 测试报告](audit/V1.0-浏览器E2E测试报告.md)
- [角色权限验收矩阵](audit/V1.0-角色权限验收矩阵.md)
- [物业业务验收记录](audit/V1.0-物业业务验收记录.md)
- [收费业务验收记录](audit/V1.0-收费业务验收记录.md)
- [仓库业务验收记录](audit/V1.0-仓库业务验收记录.md)
- [Flyway 基线核验报告](audit/V1.0-Flyway基线核验报告.md)
- [工程构建与测试报告](audit/V1.0-工程构建与测试报告.md)
- [Git 基线记录](audit/V1.0-Git基线记录.md)
- [遗留问题清单](audit/V1.0-遗留问题清单.md)
- [TASK-V1-006-B 前端菜单与动态路由修复验收报告](audit/V1.0-TASK-V1-006-B-修复验收报告.md)
- [顶部菜单布局修复说明](audit/V1.0-顶部菜单布局修复说明.md)
- [首页授权入口修复说明](audit/V1.0-首页授权入口修复说明.md)
- [动态路由重置机制说明](audit/V1.0-动态路由重置机制说明.md)
- [TASK-V1-006-B 浏览器 E2E 记录](audit/V1.0-TASK-V1-006-B-浏览器E2E记录.md)
- [TASK-V1-007 总验收报告](audit/V1.0-TASK-V1-007-总验收报告.md)
- [E1 登录与权限验收报告](audit/V1.0-TASK-V1-007-E1登录与权限验收报告.md)
- [E2 项目上下文与 IDOR 验收报告](audit/V1.0-TASK-V1-007-E2项目上下文与IDOR验收报告.md)
- [E3 物业业务 E2E 报告](audit/V1.0-TASK-V1-007-E3物业业务E2E报告.md)
- [E4 收费业务 E2E 报告](audit/V1.0-TASK-V1-007-E4收费业务E2E报告.md)
- [E5 仓库业务 E2E 报告](audit/V1.0-TASK-V1-007-E5仓库业务E2E报告.md)
- [TASK-V1-007 自动化测试报告](audit/V1.0-TASK-V1-007-自动化测试报告.md)
- [TASK-V1-007 Flyway 核验报告](audit/V1.0-TASK-V1-007-Flyway核验报告.md)
- [TASK-V1-007 Git 工作区整理及基线记录](audit/V1.0-TASK-V1-007-Git工作区整理及基线记录.md)
- [TASK-V1-007 遗留问题与风险清单](audit/V1.0-TASK-V1-007-遗留问题与风险清单.md)
- [Docker 部署与发布说明](deployment/docker-deployment.md)
- [TASK-V1-008 Docker 部署结构整理报告](audit/V1.0-TASK-V1-008-Docker部署结构整理报告.md)
- [TASK-V1-008 构建与容器验证报告](audit/V1.0-TASK-V1-008-构建与容器验证报告.md)
- [TASK-V1-008 遗留问题清单](audit/V1.0-TASK-V1-008-遗留问题清单.md)

- [系统用户与组织管理](product/modules/system-management.md)
- [员工管理](product/modules/employee-management.md)
- [角色、菜单与权限管理](product/modules/permission-management.md)
- [统一权限体系技术说明](technical/permissions.md)
- [物业项目数据权限](business/property/project-access.md)
- [仓库数据权限](business/warehouse/warehouse-access.md)
- [物业工作项目初始化](business/property/workspace-initialization.md)
- [物业项目管理](product/modules/project-management.md)
- [项目成员与管理授权](product/modules/project-member-management.md)
- [物业基础资源管理](product/modules/property-resource-management.md)
- [项目生命周期与跨模块影响](business/property/project-lifecycle.md)
- [物业资源层级](business/property/resource-hierarchy.md)
- [物业资源树形导入](business/property/resource-import.md)

## V1.0 文档口径

1. 功能状态使用“已实现、部分实现、仅存在代码但不可确认可用、未实现、疑似废弃”。
2. 验证状态独立记录“代码证据、编译、自动化测试、数据库集成、浏览器 E2E”，不得相互替代。
3. SQL 文件存在只表示仓库具备定义，不表示目标数据库已经执行。
4. 前端按钮隐藏只表示界面控制；授权结论必须引用后端 Controller/Service 和范围服务。
5. 所有路径默认相对于后端仓库 `E:\space\project\zbm-platform`；标注“前端”的路径相对于 `E:\space\project\zbm-platform-ui`。
6. 规划项必须放在“后续候选”，不得进入 V1.0 已实现清单。

## 状态标识

| 标识 | 含义 |
|---|---|
| `已实现` | 主链路具有真实代码、数据模型和权限证据，并有相应构建或自动化验证；不等于 E2E 验收通过 |
| `部分实现` | 主链路存在，但数据库落地、关键子能力或端到端验证不完整 |
| `仅代码` | 代码、页面或 SQL 存在，但当前无法确认部署与可用性 |
| `未实现` | 未发现构成功能的代码链路 |
| `疑似废弃` | 有遗留代码但缺少入口或调用证据；在进一步验证前不得删除 |
| `通过` | 对应层级的检查真实执行并成功 |
| `阻塞` | 检查已尝试，因明确环境或权限条件未完成 |
| `未执行` | 没有实际执行证据 |
| `不适用` | 该功能不需要该层级验证 |

## 证据引用规范

- 优先引用最接近结论的文件或目录：页面、Controller、Service、Entity/Mapper、SQL、测试报告。
- 文件引用使用仓库相对路径；需要定位规则时追加类名、方法名或行号，避免只写模糊模块名。
- 测试结论同时写明命令、数量、结果和执行环境；历史结果注明报告名称与日期。
- 数据库结论区分 `baseline`、`migration`、测试库实测和开发/生产库实测。
- 多项证据使用分号分隔，并说明各自证明的是“存在”“授权”“结构”还是“运行结果”。
- 发现代码与历史文档冲突时，以当前代码和最新可重复验证结果为准，同时记录差异。

更新时间：2026-10-15。
