# TASK-AUTH-016-WH-DB-1 入库管理菜单类型漂移修复验收

## 1. 结论

本任务已在指定的本机开发环境完成执行，不是“仅编写、尚未执行”状态。

- WSL：`archlinux`
- Docker 容器：`jdpm-mysql`
- 数据库：`zbm-data`
- MySQL：`8.0.46`
- 数据库主机标识：`a3fe2a44b257`
- 执行结果：目标菜单由 `M` 修复为 `C`
- 影响范围：仅 `sys_menu.menu_id = 2609252000000000611` 的 `menu_type` 与 `update_time`
- 角色授权、菜单层级、路由路径、组件、权限码及业务数据均未调整

## 2. 变更前数据库状态

执行前只读检查确认目标记录唯一且确实处于异常状态：

| 字段 | 变更前值 |
| --- | --- |
| menu_id | `2609252000000000611` |
| parent_id | `2609252000000000006` |
| menu_name | `入库管理` |
| order_num | `30` |
| menu_type | `M` |
| path | `inbound` |
| component | `warehouse/inbound/index` |
| perms | `warehouse:inbound:list` |
| status | `0` |
| visible | `0` |

同时确认：

- `warehouse:inbound:list` 在 `sys_menu` 中仅有一条记录。
- 父菜单 `2609252000000000006` 为启用、可见的 `仓库管理` 目录节点。
- 目标记录下存在 query、add、edit、remove、confirm、complete、cancel、export 共 8 个按钮权限节点。
- 目标菜单存在 3 条角色菜单关联；本次不修改任何关联。
- 基线文件 `script/sql/zbm/baseline/03_menu_permission.sql` 和原入库 migration 均将该记录定义为 `C`，数据库中的 `M` 属于实际漂移。

## 3. 动态路由影响分析

`SysMenuController#getRouters` 通过 `SysMenuServiceImpl#selectMenuTreeByUserId` 读取用户菜单，再由 `buildMenus` 生成动态路由。当前实现对 `M` 节点按目录处理，对带组件的 `C` 节点按页面处理。

目标记录配置了真实页面组件 `warehouse/inbound/index`，却被错误标记为 `M`，会使路由构建语义与菜单基线不一致。修复为 `C` 后，该记录按组件页面解析；父级 `仓库管理` 继续保持 `M`，8 个按钮节点继续保持 `F`。

## 4. Migration

新增文件：

- `script/sql/zbm/migration/V2.0.36__fix_warehouse_inbound_menu_type.sql`

SHA-256：

```text
473DFB243A8700406B9D7EB8FA539AE480C4BF472B11787CADEE9AB4654AE8AB
```

脚本采用失败关闭策略：

1. 精确匹配 menu ID、父 ID、名称、路径、组件、权限码、状态和可见性。
2. 要求 `warehouse:inbound:list` 全库唯一。
3. 只在当前 `menu_type = 'M'` 时执行更新。
4. 已经为精确 `C` 状态时允许安全复跑，但不再次更新。
5. 更新后再次校验目标记录为精确的组件菜单，否则在提交前失败。
6. 不写入或伪造 Flyway 历史；本项目继续采用受控手工 SQL 执行记录。

## 5. 备份与执行记录

写操作前已备份完整 `sys_menu`、`sys_role_menu`，并保存目标菜单树和全部非目标菜单快照。

备份目录：

```text
/home/neo/space/docker/jdpm/data/backups/auth016-wh-db1-20261011-1610
```

| 文件 | SHA-256 |
| --- | --- |
| `zbm-data-auth016-wh-db1-menu.sql` | `95b3d3459c0ec8e7903410d5b5618856d5415f9b69065f0cf5dec1871415e10f` |
| `pre-target-and-tree.tsv` | `394a1159e8a740eff0ea703801cf3ece8c8de00b5ddff849b1436631b8d24929` |
| `pre-non-target-menu.tsv` | `7f812f0806ab737d188310173c9e68d2da5d4bb4eccde63fd39d3e65de7abed0` |

`SHA256SUMS` 校验全部通过。Migration 执行输出记录在同目录的 `V2.0.36-execution.log`，后置检查记录在 `postcheck.log`。

## 6. 变更后状态

目标记录执行后为：

| 字段 | 变更后值 |
| --- | --- |
| menu_id | `2609252000000000611` |
| parent_id | `2609252000000000006` |
| menu_name | `入库管理` |
| order_num | `30` |
| menu_type | `C` |
| path | `inbound` |
| component | `warehouse/inbound/index` |
| perms | `warehouse:inbound:list` |
| status | `0` |
| visible | `0` |
| update_time | `2026-10-11 16:24:55` |

验收结果：

- 目标权限仍唯一。
- 父菜单和 8 个按钮子节点保持不变，父子关系完整。
- 非目标菜单执行前后快照 SHA-256 均为 `7f812f0806ab737d188310173c9e68d2da5d4bb4eccde63fd39d3e65de7abed0`，确认其他菜单未变化。
- `sys_role_menu` 不存在重复的 `(role_id, menu_id)` 关系。
- `property_warehouse_keeper` 与 `property_warehouse_manager` 均为启用状态，且均具有仓库父菜单和入库页面菜单关联。
- `admin` 用户为启用状态，关联的 `superadmin` 角色为启用状态；源码路径会为 SuperAdmin 读取全部有效 M/C 菜单。
- 未修改七角色权限矩阵、用户角色关系、项目/仓库授权或任何仓库业务数据。

## 7. 幂等性验证

在首次成功执行后，使用同一脚本进行了第二次受控执行：

- 第二次执行成功。
- 目标仍为 `C`。
- `update_time` 仍为首次执行产生的 `2026-10-11 16:24:55`，证明复跑未再次命中 UPDATE。
- 其他菜单快照继续保持一致。

## 8. 权限契约测试

执行命令：

```text
mvn -pl ruoyi-admin -am -Dtest=PermissionContractAuditTest -Dsurefire.failIfNoSpecifiedTests=false test
```

结果：

- `PermissionContractAuditTest`：6 项通过，0 失败，0 错误。
- Maven Reactor：`BUILD SUCCESS`。
- 总耗时：21.932 秒。

本次仅修正菜单类型，不涉及 Controller/Service 权限码变化，因此权限契约清单无需调整。

## 9. 路由与真实登录态验收边界

已完成数据库和源码级路由验证：

- Admin/SuperAdmin 的菜单读取路径能够包含该有效 `C` 页面。
- 仓库管理员和仓库主管均具备父目录与目标页面关联。
- 动态授权角色只要被合法授予目标页面及必要祖先菜单，即可按同一逻辑生成页面路由；本任务没有扩大任何角色授权。

本次未持有可用的真实浏览器登录会话，因此未实际调用带登录态的 `/system/menu/getRouters`，也未在浏览器中点击入库页面。该部分不能宣称已完成 E2E 验收。应用运行时若缓存了旧动态路由，应退出并重新登录或刷新权限会话后再进行人工验证。

## 10. 辅助脚本

- `script/sql/zbm/diagnostic/auth016_wh_db1_precheck.sql`
- `script/sql/zbm/diagnostic/run_auth016_wh_db1_precheck.sh`
- `script/sql/zbm/diagnostic/backup_auth016_wh_db1.sh`
- `script/sql/zbm/diagnostic/run_auth016_wh_db1_migration.sh`
- `script/sql/zbm/diagnostic/auth016_wh_db1_postcheck.sql`
- `script/sql/zbm/diagnostic/run_auth016_wh_db1_postcheck.sh`

## 11. 遗留事项

仅剩真实登录态人工验收：分别以 Admin、仓库管理员以及临时动态授权角色重新登录，确认菜单展示、`getRouters` 返回结构和 `warehouse/inbound/index` 页面加载。数据库修复本身已完成，无需再次执行 V2.0.36。
