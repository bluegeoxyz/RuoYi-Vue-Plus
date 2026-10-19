# 物业系统权限契约基线

## 范围

本契约扫描以下 Controller 包：

- `org.dromara.property`
- `org.dromara.warehouse`
- `org.dromara.hr`

契约只描述当前代码事实，不代表角色已经获得相应授权，也不改变 RuoYi RBAC、项目范围或仓库范围。

## 自动化产物

执行：

```text
mvn -pl ruoyi-admin -am "-Dtest=PermissionContractAuditTest" "-Dsurefire.failIfNoSpecifiedTests=false" test
```

生成：

```text
ruoyi-admin/target/permission-contract.csv
```

字段包括模块、接口分类、HTTP 方法、完整路径、权限组合模式、权限标识、Controller 和处理方法。

## 强制契约

1. 物业、仓库、HR 业务路由必须声明 `@SaCheckPermission`。
2. Controller 使用的权限必须存在于当前 `sys_menu` 菜单基线。
3. 同一个权限标识不得绑定多个不同菜单 ID。
4. 已知 Options 管理权限耦合必须显式登记；接口或权限变化后必须同步完成语义审计。
5. Service 二次鉴权本身不是错误；只有 Controller 与 Service 表达不同业务能力时才登记为冲突。

## 当前已知 Controller/Service 语义冲突

AUTH-005 已对齐客户关系三个旧 Options 接口的 Controller 与 Service 权限语义，当前基线不再保留已知冲突。

## 当前已知 Options 管理权限耦合

| 业务引用 | 当前依赖权限 | 主要消费场景 |
|---|---|---|
| 项目 Options | `property:project:list` | 项目成员、资源及其他物业业务页面 |
| 员工 Options | `hr:employee:list` | 项目负责人、项目成员维护 |
| 楼栋 Options | `property:building:list` | 单元、房屋及未迁移业务；客户关系页面已改用业务上下文接口 |
| 单元 Options | `property:unit:list` | 房屋及未迁移业务；客户关系页面已改用业务上下文接口 |
| 房屋 Options | `property:room:list` | 收费、收款及未迁移业务；客户关系页面已改用业务上下文接口 |
| 收费项目 Options | `property:chargeItem:list` | 计费规则、应收、房屋收费设置 |
| 计费规则 Options | `property:chargeRule:list` | 房屋收费设置 |
| 仓库 Options | `warehouse:warehouse:list` | 基础资料管理和未迁移页面；四类仓库单据制单已改用业务上下文接口 |
| 库区 Options | `warehouse:zone:list` | 基础资料管理和未迁移页面；仓库单据制单已解耦 |
| 库位 Options | `warehouse:location:list` | 基础资料管理和未迁移页面；四类仓库单据制单已解耦 |
| 物料 Options | `warehouse:material:list` | 基础资料管理和未迁移页面；四类仓库单据制单已解耦 |
| 计量单位 Options | `warehouse:unit:list` | 基础资料管理和未迁移页面；四类仓库单据已从物料业务 Option 获取单位快照 |

## 权限与数据范围的边界

- RBAC 决定用户能否执行某类功能。
- 物业项目业务范围当前由有效员工对应的 `pm_project_member` 关系提供；代码库不存在 `pm_user_project`。
- 项目管理范围由 `pm_project_management_grant` 提供，不等同项目业务范围。
- 公司仓使用 RuoYi 部门范围，项目仓使用物业项目范围。
- Options 权限通过后仍必须执行项目、公司、仓库和具体资源校验。

## AUTH-003 最小设计

新增一个受控的业务上下文授权层，不新增权限框架、不新增数据库表：

```text
BusinessContext
  INBOUND_CREATE
  OUTBOUND_CREATE
  TRANSFER_CREATE
  STOCKTAKE_CREATE
  CUSTOMER_RELATION_EDIT
  RECEIVABLE_QUERY
  CHARGE_BINDING_EDIT
  PROJECT_MEMBER_EDIT
```

每个上下文在服务端静态映射：

1. 可接受的现有 RBAC 操作权限；
2. 允许读取的引用数据类型；
3. 使用的 `PropertyBusinessDomain` 和 `ProjectBusinessAction`；
4. 是否需要仓库、公司或项目逐资源校验；
5. 返回的精简 Option VO。

第一阶段只建立接口和策略测试，不替换现有 Options。第二阶段按业务页面逐个迁移，迁移完成后再从本契约中删除对应已知耦合项。
