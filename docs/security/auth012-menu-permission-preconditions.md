# AUTH-012 菜单权限前置收敛执行记录

## 环境确认

- 执行日期：2026-10-09（Asia/Shanghai）
- WSL：`archlinux`
- Docker 容器：`jdpm-mysql`
- 镜像：`mysql:8.0`
- MySQL：8.0.46
- 实例主机名：`a3fe2a44b257`
- 目标库：`zbm-data`
- 未发现 Flyway 或 schema history 表；本项目当前按 `script/sql/zbm/migration/README.md` 人工记录 migration。

未连接或修改其他数据库环境。

## 执行前差异

历史 migration 实际定义的是三个独立导入按钮权限，而不是三个模块级通配权限：

| 历史脚本 | 缺失权限 | 已存在父菜单 | 其他业务变更 |
|---|---|---|---|
| V2.0.11 | `property:resource:treeImport` | `2609252000000000301` 楼栋管理 | 无 |
| V2.0.12 | `property:customer:import` | `2609252000000000401` 客户档案 | 无 |
| V2.0.14 | `property:customer-relation:import` | `2609252000000000402` 房客关系 | 无 |

三个权限执行前均不存在，三个目标 menu_id 均未被占用，父菜单均唯一存在。历史脚本仅包含相应菜单 INSERT，但没有统一的失败关闭检查，因此没有直接重放。

ChargeBinding 五个菜单记录均存在且唯一，但全部是房屋管理 `2609252000000000303` 下的按钮节点；其中 list 节点不是独立页面。数据库状态与 AUTH-011 审计的已知旧结构一致。

全库已有四组与本任务无关的系统权限重复：`system:ossConfig:list`、`system:role:edit`、`system:user:edit`、`tool:gen:edit`。本任务没有新增或修改这些记录。

## 备份

执行任何数据库写入前已备份 `sys_menu`、`sys_role`、`sys_role_menu`，并保存完整菜单快照和逐角色授权校验值：

`/home/neo/space/docker/jdpm/data/backups/auth012-menu-20261009-1815`

主要文件：

- `zbm-data-auth012-menu-role.sql`
- `preflight-metadata.txt`
- `pre-menu-snapshot.tsv`
- `pre-role-menu-checksums.tsv`
- `SHA256SUMS`
- `V2.0.33.1-execution.log`

数据库备份 SHA-256：`d52ccbde13b4729dfe1ef357afb20690bc3a1cd0a047ae74a8ec2f75350a0065`。

## 修复方式与执行结果

新增并执行：

`script/sql/zbm/migration/V2.0.33.1__menu_permission_preconditions.sql`

脚本先验证父节点、权限唯一性、目标 ID 冲突及 ChargeBinding 已知旧/新结构；出现未知差异时通过主键冲突失败关闭。验证通过后在同一事务中：

1. 只补三个缺失导入按钮。
2. 将 ChargeBinding list 节点转换为收费管理下的独立 C 菜单。
3. 将四个操作按钮改挂到独立菜单。
4. 不读写 `sys_role_menu`。

迁移执行成功。修复后八个目标权限均存在且各只有一条；ChargeBinding 路径为 `binding`，组件为 `property/charge/binding/index`，父菜单为收费管理 `2609252000000000005`。

迁移前后逐角色授权快照 SHA-256 完全相同：

`2b44b64c1842d01a2f73df36f9c6febf77d380d1af5fb7729a86a7f1f9bcf57e`

`sys_role_menu` 行数保持 378，证明目标角色和非目标角色均未被授予或移除菜单。

## V2.0.34 / V2.0.35 顺序

V2.0.34 不直接按父节点 ID检查 ChargeBinding，但它会从每个目标权限向上展开菜单祖先。如果在旧结构上执行，财务主管的 ChargeBinding 权限会把房屋管理祖先一起纳入目标菜单集，从而无法真正解除 `property:room:list` 耦合。因此必须在 V2.0.34 之前完成独立菜单迁移。

新增版本 `V2.0.33.1` 位于 V2.0.33 与 V2.0.34 之间，使新环境按语义版本升序执行时安全。V2.0.35 保留为兼容的幂等后续 migration；当前数据库已经由 V2.0.33.1 达到相同最终状态，因此没有重复执行 V2.0.35。

V2.0.34 的 `START TRANSACTION` 前置段已单独运行：七角色唯一性、未知人工授权白名单和全部目标菜单存在性检查均通过；`missing_or_disabled_permission` 结果为空。输出的新增/移除项目与已审查角色矩阵一致。

本任务没有执行 V2.0.34。当前菜单前置条件已经满足，后续仍应按 AUTH-010A-DB 流程重新备份并人工审核 V2.0.34 输出后再决定执行。
