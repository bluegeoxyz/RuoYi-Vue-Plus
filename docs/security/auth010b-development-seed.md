# TASK-AUTH-010B 开发测试身份初始化报告

## 环境与安全边界

- 执行日期：2026-10-09（Asia/Shanghai）
- 环境：本机 WSL `archlinux`、Docker `jdpm-mysql`、MySQL 8.0.46、数据库 `zbm-data`
- 仅使用 `TEST-AUTH-010B` 标识和 `2199011000000000000` 保留 ID 段，不接触既有业务项目。
- Seed 必须显式设置 `@auth010b_allow_development_seed = 'AUTH010B_LOCAL_DEV'`，且必须从外部传入 BCrypt 哈希。
- 明文初始密码和 BCrypt 哈希均在 Git 外保存，文件权限为 `0600`；报告和 SQL 不包含密码。
- 七个标准角色的 `data_scope`、状态和菜单矩阵未修改。

## 初始化内容

| 账号 | 员工编号 | 角色 | 项目成员 | 管理授权 |
| --- | --- | --- | --- | --- |
| test_customer | TEST-AUTH-001 | property_customer_service | A | 无 |
| test_operation | TEST-AUTH-002 | property_operation_manager | A | 无 |
| test_cashier | TEST-AUTH-003 | property_cashier | A | 无 |
| test_finance | TEST-AUTH-004 | property_finance_manager | A、B | 无 |
| test_warehouse | TEST-AUTH-005 | property_warehouse_keeper | A | 无 |
| test_wh_manager | TEST-AUTH-006 | property_warehouse_manager | A、B | 无 |
| test_pm_a | TEST-AUTH-007 | property_project_manager | A（经理） | PROJECT A |
| test_pm_b | TEST-AUTH-008 | property_project_manager | B（经理） | PROJECT B |
| test_multi_role | TEST-AUTH-009 | 客服 + 收费员 | A | 无 |
| test_no_project | TEST-AUTH-010 | property_customer_service | 无 | 无 |

员工均为 `FORMAL / ACTIVE / status=0`，每个账号绑定独立员工和 TEST 岗位。项目业务范围通过 `sys_user -> hr_employee -> pm_project_member` 建立；项目管理授权仅给两个项目经理账号。

隔离样本包括：

- `TEST_AUTH_A`、`TEST_AUTH_B` 两个独立测试项目；各有楼栋、单元、房屋、客户关系、收费项目、规则、绑定、应收样本。
- `TEST-A-WH`、`TEST-B-WH` 两个项目仓，分别包含库存 100、200。
- `TEST-COMPANY-WH` 公司仓作为负向样本。标准角色仍为 SELF 数据范围，因此不会因角色自动获得公司仓访问权。

## 脚本

- 初始化：`script/sql/zbm/seed/auth010b_property_test_identities.sql`
- 本机安全执行器：`script/sql/zbm/seed/run_auth010b_seed_local.sh`
- 凭据轮换：`script/sql/zbm/seed/rotate_auth010b_password_local.ps1`
- 定向清理：`script/sql/zbm/seed/auth010b_cleanup.sql`
- 数据库核对：`script/sql/zbm/diagnostic/auth010b_verify.sql`
- 真实认证/API 核对：`script/sql/zbm/diagnostic/auth010b_http_verify.ps1`

这些脚本不在 Flyway 生产 migration 链中。Seed 使用固定 TEST ID、唯一业务键、`INSERT IGNORE` 和失败关闭前置检查；不会重置已存在账号密码。

## 数据库差异与幂等性

- 新增：10 个用户、10 个员工、11 条用户角色、10 条用户岗位、11 条有效项目成员、2 条项目管理授权。
- 新增：1 个 TEST 公司部门、2 个项目部门、1 个岗位、2 个项目及配套业务样本、3 个仓库。
- SuperAdmin 测试用户：0。
- 重复用户角色：0；重复项目成员：0；无项目账号成员数：0。
- 七角色权限数量保持：6 / 38 / 6 / 28 / 20 / 72 / 48。
- Seed 第二次执行后诊断快照与第一次 SHA-256 均为 `b28f4719b4cf957b272fd8b61f1a8fe5b76da5c169138530bc02735ef5fd723c`，证明重复执行未新增或改变授权数据。

## 自动化验证

后端最新代码打包成功。权限测试共 53 项通过、0 失败、0 错误，包括：

- BusinessContextAccessServiceTest：12
- 物业 Context Controller/Service：11
- 仓库 Context Controller/Service：18
- PermissionContractAuditTest：6
- PropertyBusinessRolePermissionTest：6

本机真实 HTTP 验收结果：

- 10 个账号均通过 `/auth/login` 认证并可读取自身权限。
- 客服仅获得项目 A 入住 Options；无项目客服返回空范围。
- 收费员可读取项目 A 收款候选，伪造项目 B 被拒绝。
- 财务主管可访问 A、B 的独立房屋收费设置上下文，且没有 `property:room:list`。
- 仓管仅获得项目 A 仓库；仓库主管获得 A、B；两者均未因 SELF 数据范围获得公司仓。
- 多角色账号同时获得客服和收费员权限合集。
- 仓管入库确认、收费员冲销等未授权能力被拒绝。

## 备份与凭据

备份目录：`/home/neo/space/docker/jdpm/data/backups/auth010b-seed-20261009-2246`

- 初始化前受影响表快照：`zbm-data-auth010b-affected-tables.sql`，SHA-256 `84d0acc697d91e9784562ede614fc35f14974fca8e233440fd4adce7c696000a`
- 初始化后受影响表快照：`zbm-data-auth010b-post-state.sql`，SHA-256 `02056e9ecccfc0597794b807c3241947943081dda30b6bfb5063a043c990d5ed`
- 实际 HTTP 验收摘要：`http-verification.json`，SHA-256 `5575ebc9db1dde76476c839e196444762d911d116d8c5f04098c421f827e2663`
- 初始密码：`initial-password.txt`（0600，仅本机）；哈希：`password.bcrypt`（0600，仅本机）。

## 清理与人工验收

`auth010b_cleanup.sql` 按外键反向顺序，仅删除固定 TEST ID/标识的数据，并清理测试登录日志和系统欢迎消息。执行前仍应再次备份并确认目标库为 `zbm-data`；清理后如有活跃会话，应让测试账号退出或清理其 Sa-Token 会话。

本任务未执行浏览器人工验收。下一阶段建议逐账号验证菜单可见性、级联选择交互、错误提示、A/B 跨项目请求以及公司仓部门数据范围；涉及确认、完成、冲销和删除的验收应使用专门草稿数据，避免修改共享样本。
