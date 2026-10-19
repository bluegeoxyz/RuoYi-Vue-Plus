# TASK-AUTH-015 动态角色授权与业务权限一致性审计

审计日期：2026-10-10  
范围：只读检查前后端源码、开发库 `zbm-data` 与运行日志。本次未修改代码、数据库、角色权限或业务数据。

## 1. 结论

系统的 RBAC 本身支持动态跨模块授权，并不按“客服、财务、运营”等角色名限制查询能力。当前问题来自三个更具体的原因：

1. **应收页面存在确定的 BusinessContext 策略缺陷。** `property:receivable:list` 已让客服通过 Controller、工作项目和 FINANCE 项目范围校验；列表和汇总真实请求均成功。失败的是页面并行加载收费项目筛选项时，`RECEIVABLE_QUERY` 没有声明 `ReferenceType.CHARGE_ITEM`，所以所有用户（包括 Admin）都会在该引用接口被拒绝。
2. **数据库实际授予的是“客户档案”，不是“住户管理”。** 客服角色当前新增了 `property:customer:list/query`。客户档案属于企业客户主档，除 RBAC 外还要求 `pm_customer_management_grant`；客服没有该授权，日志准确返回“无企业客户主档授权”。这是正确的数据授权拒绝，不应通过角色权限绕过。
3. **真正的住户页面仍有基础资料 Options 耦合。** 即使给角色完整授予 `property:resident:list/query`，页面初始化仍调用受 `property:building:list` 保护的管理 Options；进一步筛选还依赖 `property:unit:list`、`property:room:list`。因此菜单与住户核心列表可用，但筛选初始化可能 403。

所以答案是：**动态授权机制可用，但两个页面尚未完全遵守“页面只读权限自洽”契约。** 不需要固定角色白名单，也不需要扩大项目范围。

## 2. 当前数据库事实

`test_customer`：

- 用户和角色均启用、未删除。
- 角色：`property_customer_service`。
- ACTIVE 员工：存在。
- ACTIVE 项目成员：项目 `2199011000000000101`。
- 当前角色新增权限：
  - `property:receivable:list`
  - `property:receivable:query`
  - `property:customer:list`
  - `property:customer:query`
- 当前没有 `property:resident:list/query`。

这也解释了为何显示的是“客户档案”和“应收管理”菜单，而不是代码中的“住户管理”页面授权组合。

## 3. 应收管理页面完整依赖

| 时机 | API | Controller 权限 | BusinessContext | 项目范围 | 当前结果 |
|---|---|---|---|---|---|
| 登录初始化 | `GET /property/workspace/projects` | 多个物业权限 OR，包含 `receivable:list` | workspace policy 推导 FINANCE | ACTIVE 项目成员 | 通过 |
| 页面初始化 | `GET /property/receivable/list` | `property:receivable:list` | 无 | FINANCE / READ | 通过 |
| 页面初始化 | `GET /property/receivable/summary` | `property:receivable:list` | 无 | FINANCE / READ | 通过 |
| 页面初始化 | `GET /property/receivable/context/query/charge-items` | `property:receivable:list` | `RECEIVABLE_QUERY + CHARGE_ITEM` | FINANCE / OPTION | **BusinessContext 拒绝** |
| 打开生成记录 | `GET /property/receivable/batch/list` | `property:receivable:list` | 无 | FINANCE / READ | 应通过 |
| 查看详情 | `GET /property/receivable/{id}` | `property:receivable:query` | 无 | 从原记录校验 FINANCE / READ | 完整只读需 query |
| 查看收款历史 | `GET /property/receivable/{id}/payments` | `property:receivable:query` | 无 | 从应收原记录校验 FINANCE / READ | 完整只读需 query |
| 生成应收 | `POST /property/receivable/generate` | `property:receivable:generate` | `RECEIVABLE_GENERATE` 相关链路 | FINANCE / WRITE | 客服无权限，符合预期 |
| 作废 | `PUT /property/receivable/{id}/cancel` | `property:receivable:cancel` | 无 | FINANCE / WRITE + 状态规则 | 客服无权限，符合预期 |
| 导出 | `POST /property/receivable/export` | `property:receivable:export` | 无 | FINANCE / EXPORT | 客服无权限，符合预期 |

### 确切失败位置

调用链：

`receivable/index.vue load()`  
→ `receivableContextChargeItems(projectId)`  
→ `ReceivableContextOptionController#chargeItems`  
→ `FinanceContextOptionServiceImpl#chargeItems`  
→ `BusinessContextAccessServiceImpl#requireAccess(RECEIVABLE_QUERY, CHARGE_ITEM)`  
→ `BusinessContextPolicy` 中引用类型集合不含 `CHARGE_ITEM`  
→ `NotPermissionException("业务上下文未授权该引用类型")`

`RECEIVABLE_QUERY` 当前权限组合是 `property:receivable:list OR property:receivable:query`，因此动态授予 list 后已经能获得业务能力；失败与角色名称无关，也不是项目范围失败。

前端使用 `Promise.all(list, summary, charge-items)`，任何一个辅助请求失败都会让整个加载 Promise 失败，从而产生“页面进不去”的用户感受，即使列表与汇总实际已经成功。

现有 `BusinessContextAccessServiceTest` 还明确断言 `RECEIVABLE_QUERY + CHARGE_ITEM` 必须拒绝，这一回归基线与当前前端真实需求相冲突，需要随修复同步调整。

## 4. 住户管理页面完整依赖

| 时机 | API | 当前权限 | 项目范围 | 问题 |
|---|---|---|---|---|
| 登录初始化 | `GET /property/workspace/projects` | workspace OR 包含 `property:resident:list` | RESOURCE 域成员范围 | 正常 |
| 页面初始化 | `GET /property/resident/list` | `property:resident:list` | CUSTOMER_RELATION / READ | 核心列表自洽；范围外返回空列表 |
| 页面初始化 | `GET /property/resource/building/options` | `property:building:list` | RESOURCE 范围 | **跨模块管理权限耦合** |
| 选择楼栋 | `GET /property/resource/unit/options` | `property:unit:list` | RESOURCE 范围与归属 | **跨模块管理权限耦合** |
| 选择单元 | `GET /property/resource/room/options` | `property:room:list` | RESOURCE 范围与归属 | **跨模块管理权限耦合** |
| 查看详情 | `GET /property/resident/{customerId}` | `property:resident:query` | CUSTOMER_RELATION / READ，按原始关系查归属 | 完整只读需 query |
| 导出 | `POST /property/resident/export` | `property:resident:export` | CUSTOMER_RELATION / EXPORT | 独立授权，正确 |
| 字典 | `GET /system/dict/data/type/pm_customer_type` | 登录态 | 无业务数据范围 | 正常 |

住户 Service 没有角色名判断。它先通过 `PropertyFunctionalPermissionService` 实时核对数据库权限，再通过 `PropertyProjectScopeService` 获取项目成员范围：

- list 查询目标项目不在范围内：返回空列表。
- detail 查询指定项目越权：返回“无项目业务访问权限”。
- 找不到范围内关系：返回“住户不存在或不在当前项目业务范围内”。

这种差异符合列表最小泄露、详情明确拒绝的设计，可以保留。

## 5. 客户档案并不是住户查询

当前数据库授权与日志对应的是：

`GET /property/customer/list`  
→ Controller 的 `property:customer:list` 已通过  
→ `CustomerAccessPolicyImpl#assertEnterpriseMasterAccess`  
→ 检查 `pm_customer_management_grant`  
→ 无有效 USER/ROLE 企业客户授权  
→ `ServiceException("无企业客户主档授权")`

分类：**资源管理授权问题，不是菜单或 Controller 权限缺失。**

客户档案允许访问企业级客户主数据；住户管理是按项目关系生成的脱敏视图。给客服动态增加住户只读能力，应选择 `property:resident:list/query`，不能以 `property:customer:list/query` 替代。也不能因为具有 customer RBAC 就自动授予企业客户主档范围。

## 6. 项目范围判断

- `PropertyProjectScopeServiceImpl` 不按角色名称判断。
- PROJECT_BASE、RESOURCE、CUSTOMER_RELATION、FINANCE、WAREHOUSE 当前均由 `ProjectMemberBusinessScopeContributor` 基于 `sys_user → hr_employee → ACTIVE pm_project_member` 解析。
- `test_customer` 有有效项目成员关系，所以应收列表、汇总和住户列表的项目范围均满足。
- 当前没有单独的 FINANCE contributor；FINANCE 与普通项目业务共用成员范围。
- 功能权限存在但无有效项目成员时：workspace 返回空项目，列表应为空，不应回退全部项目。
- 详情或带明确资源 ID 的访问仍须根据数据库原始记录检查项目归属，不能信任前端项目 ID。

本次失败均不是项目范围不足；日志显示应收 list/summary 已成功查询授权项目。

## 7. 登录权限快照与动态角色修改

登录时 `SysLoginService#buildLoginUser` 从数据库读取角色、菜单权限和数据范围映射，写入 Token Session 的 `LoginUser`。之后：

- `getInfo` 返回登录快照中的 permissions/roles。
- Sa-Token Controller 校验通常读取同一登录快照。
- `getRouters` 每次从数据库读取当前菜单关联。
- Property 的 `PropertyFunctionalPermissionService` 对关键功能权限再次实时读取数据库。

通过角色管理的 `PUT /system/role/permission` 修改权限后，会在事务提交后异步发布 `OnlineUserCleanEvent`，注销拥有该角色的在线 Token。因此正常行为是用户被迫重新登录并获得新快照。

风险点：清理是异步的。如果清理失败或存在短暂窗口，`getRouters` 可能已显示新菜单，而 `getInfo` 和 Controller 仍使用旧快照，形成“菜单可见、接口 403”。日志会记录清理失败。直接执行 SQL 则完全不会发布该事件，必须人工注销相关会话。

本次 `test_customer` 在授权后于 17:07 重新登录，随后应收 list/summary 已通过，因此旧快照不是当前主要根因。

## 8. 问题分类

| 现象 | 分类 | 结论 |
|---|---|---|
| 应收菜单显示 | 菜单授权 | 正常 |
| 应收 list/summary | 接口权限 + 项目范围 | 正常 |
| 应收收费项目筛选失败 | BusinessContext | 确定缺陷：缺少 `CHARGE_ITEM` 引用声明 |
| 客户档案报错 | 企业客户主档资源授权 | 正确拒绝；授予的不是住户权限 |
| 真正住户列表 | 接口权限 + 项目范围 | 授予 resident:list 后可用 |
| 住户楼栋/单元/房间筛选 | Options 权限耦合 | 缺陷：依赖 building/unit/room 管理 list |
| 授权后短时菜单/API不一致 | 登录快照 | 可能存在异步注销窗口；重新登录后应一致 |

## 9. 最小修复方案

### AUTH-015A：修复应收查询上下文引用类型

- 在 `BusinessContextPolicy.RECEIVABLE_QUERY` 中加入 `ReferenceType.CHARGE_ITEM`。
- 保留权限组合 `receivable:list OR receivable:query`。
- 保留 `FinanceContextOptionServiceImpl` 的 FINANCE 项目范围与有效状态过滤。
- 更新当前“必须拒绝”的错误测试，并增加：只有 receivable list 的用户可取收费项目选项；只有 chargeItem list 的用户仍不可取。
- 不需要 SQL 或 Migration。

### AUTH-015B：住户只读 Options 解耦

- 增加受控 `RESIDENT_QUERY` BusinessContext，权限使用现有 `property:resident:list/query`，不新增权限码。
- 提供住户业务专用的楼栋、单元、房屋精简 Options，固定服务端上下文。
- 继续使用 `PropertyProjectScopeService` 和真实父子归属校验。
- 前端住户页面改用专用 Options，不再调用基础资料管理 Options。
- 不需要数据库 Migration；不改变七角色默认矩阵。

### AUTH-015C：授权 UI 语义提示

- 在角色菜单授权页面明确区分“客户档案（企业主档授权另行要求）”与“住户管理（项目关系脱敏视图）”。
- 对具有二级资源授权要求的菜单展示说明，避免把 RBAC 菜单勾选误解为数据范围授权。
- 后端错误应保留“缺少企业客户主档授权”“业务上下文引用未声明”等准确消息，前端不要统一替换为“联系管理员”。
- 通常不需要 SQL；若修改数据库菜单名称/备注，才需要独立增量 SQL。

### AUTH-015D：动态授权一致性回归

- 测试角色管理修改后受影响 Token 被注销。
- 新登录的 `getInfo`、`getRouters`、Controller 权限集合一致。
- 撤销权限后旧 Token 不得继续访问；数据库实时校验继续作为撤权保护。
- 不需要 SQL。

## 10. 对验收场景的判断

| 场景 | 当前状态 |
|---|---|
| 客服原始权限只能访问原业务 | 满足 |
| 增加应收完整只读权限后查询应收 | list/summary 满足；筛选项被策略缺陷阻断 |
| 增加住户完整只读权限后查询住户 | 核心接口满足；筛选 Options 尚未解耦 |
| 无 generate/cancel/payment 权限不能写财务数据 | 满足 |
| 不能查询其他项目 | 项目成员范围和资源归属校验仍生效 |
| 撤销权限并刷新会话后拒绝 | 设计满足；应补集成测试覆盖异步注销 |
| Admin 不受普通范围影响 | 满足；但同样会命中“未声明 ReferenceType”这种策略结构拒绝 |

最终结论：需要代码修复，不需要数据库 Migration；数据库当前动态授权本身无需扩大或重做。
