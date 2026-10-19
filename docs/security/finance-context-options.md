# 收费业务上下文 Options

AUTH-006 只迁移收费业务引用数据，不修改计费、收款、自动核销、作废或冲销事务规则。

## 原依赖与迁移

| 页面/动作 | 原依赖 | 新接口与业务权限 |
|---|---|---|
| 应收列表筛选 | `GET /property/charge/item/options`，要求 `property:chargeItem:list` | `GET /property/receivable/context/query/charge-items`，要求 `property:receivable:list` |
| 收款房屋 | 旧 `/property/payment/room-options` | `/property/payment/context/create/rooms`，要求 payment add + allocate |
| 收款客户 | `GET /property/customer/list`，要求客户管理 list | `/property/payment/context/create/customers`，要求 payment add + allocate，最小脱敏字段 |
| 待收应收与汇总 | receivable list + summary，额外要求 `property:receivable:list` | `/property/payment/context/create/receivables`，要求 payment add + allocate |
| 收费设置项目/规则 | chargeItem list + chargeRule list | `/property/charge/binding/context/{create|edit}/{items|rules}`，分别要求 binding add/edit |

收费项目与计费规则自身管理页面继续使用各自管理权限。AUTH-006 当时仅消除了收费设置弹窗对收费项目、计费规则管理权限的额外依赖；后续 AUTH-011 已将房屋收费设置迁移为收费管理下的独立页面，并消除了页面入口对 `property:room:list` 的依赖。

所有新接口固定服务端 `BusinessContext`，通过 `BusinessContextAccessService` 后仍调用 `PropertyProjectScopeService` 校验 FINANCE 项目范围。房屋和应收按数据库真实 `projectId`、`roomId` 校验；收费项目、规则限定启用状态和有效期。收款客户只返回脱敏姓名、手机号等最小候选字段。

## 验证结果

- `PermissionContractAuditTest`：6 项通过；当前 261 个 Controller 接口、62 个业务 Options 接口，本任务新增 8 个固定上下文接口。
- AUTH-006 相关权限、资源范围及收费核心回归：77 项通过，0 失败；另含本服务项目范围、收费项目归属、房屋归属和客户脱敏 4 项专项测试。
- 前端 `pnpm run build:prod` 通过。
- 原管理 Options 保持兼容，契约中的 22 项旧管理 Options 耦合基线不删除；本次消除的是应收、收款和收费设置业务页面对相应管理接口的调用。
- 未修改 migration、角色权限 SQL、计费/收款/核销/冲销事务逻辑。
