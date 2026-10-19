# TASK-AUTH-016-WH-TEST-1 仓库管理动态授权综合验收

## 1. 最终结论

本轮结论为 **部分通过，不具备 E2E 通过条件**。

- 后端仓库、BusinessContext、权限契约自动化测试全部通过。
- 前端仓库权限模式测试、ESLint、生产构建通过。
- 前端 TypeScript 检查未通过，共确认 10 个错误，其中 4 个位于入库页面。
- V2.0.36 数据库状态、入库 `C` 页面菜单、仓库角色菜单关联均保持正确。
- 浏览器已加载当前开发前端登录页，但未完成登录后的真实页面和 Network 验收。
- 10 种动态授权组合未通过真实角色管理接口逐一执行，因此不得宣称动态授权综合 E2E 通过。
- 本轮没有写入仓库业务数据，没有确认、完成、库存调整或状态流转操作。

## 2. 测试环境与构建版本

| 项目 | 实际状态 |
| --- | --- |
| 后端工作区 | `E:/space/project/zbm-platform` |
| 后端 Git HEAD | `925dd18f560ec0b7dee618d940b9e740c890f0f3` |
| 前端工作区 | `E:/space/project/zbm-platform-ui` |
| WSL | `archlinux` |
| MySQL 容器 | `jdpm-mysql`，运行中 |
| MySQL | `8.0.46` |
| 数据库 | `zbm-data` |
| Redis 容器 | `jdpm-redis`，运行中 |
| 后端运行版本 | 本轮重新执行 `mvn package -DskipTests` 后生成的 `ruoyi-admin.jar` |
| 前端运行版本 | 本轮由当前源码启动的 Vite development server |
| 浏览器地址 | `http://127.0.0.1:81`；80 端口已有其他进程，当前 Vite 自动选择 81 |

本轮启动的后端和 Vite 进程在验收结束后均已停止，不保留旧验收进程。

## 3. 数据库与菜单基线

只读核验结果：

- 入库页面菜单 `menu_id=2609252000000000611`。
- `menu_type=C`。
- `path=inbound`。
- `component=warehouse/inbound/index`。
- `perms=warehouse:inbound:list`。
- status/visible 均为 `0`。
- query、add、edit、remove、confirm、complete、cancel、export 八个按钮节点完整。
- `property_warehouse_keeper` 和 `property_warehouse_manager` 均具有仓库父菜单与入库页面菜单。
- `sys_role_menu` 重复关系为 0。
- Admin 用户和 `superadmin` 角色均为启用状态。

AUTH-010B 数据核验：

| 账号 | 角色 | 项目范围 |
| --- | --- | --- |
| test_warehouse | property_warehouse_keeper | TEST_AUTH_A |
| test_wh_manager | property_warehouse_manager | TEST_AUTH_A、TEST_AUTH_B |

仓库角色权限数量仍为：

- 仓库管理员：20。
- 仓库主管：72。

另发现 `property_customer_service` 当前为 10 项权限，而最初 AUTH-010A 目标数量为 6。该漂移不属于仓库角色，但说明开发库经历过后续动态授权；本轮未修改或回收该角色权限。

## 4. 自动化测试

### 4.1 仓库后端测试

命令：

```text
mvn -pl ruoyi-modules/ruoyi-warehouse -am -Dtest=*Warehouse*Test,*Inventory*Test,*Inbound*Test,*Outbound*Test,*Transfer*Test,*Stocktake*Test,*Material*Test,*Unit*Test -Dsurefire.failIfNoSpecifiedTests=false test
```

结果：**通过**。

- 133 tests。
- 0 failures。
- 0 errors。
- 覆盖 WarehouseAccessService、库存余额/流水、库存变更、四类单据、查询/维护 Context、编辑初始化 Controller/Service 和权限注解契约。

受限沙箱第一次运行时 Mockito/ByteBuddy 无法打开 Windows JVM attach pipe，Surefire 产生 `could not open pipe` dump；在获准的本机测试进程中重跑后 133 项全部通过。该问题属于测试执行环境限制，不是产品测试失败。

### 4.2 BusinessContext 与权限契约

结果：**通过**。

- `BusinessContextAccessServiceTest`：19/19。
- `PermissionContractAuditTest`：6/6。
- Reactor：BUILD SUCCESS。

### 4.3 前端权限测试

命令：

```text
pnpm exec vitest run src/views/warehouse/useWarehousePageMode.test.ts src/views/warehouse/warehousePageModeIntegration.test.ts src/views/warehouse/warehouseOperationPermissions.test.ts
```

结果：**通过**。

- 3 test files。
- 23 tests。
- 覆盖 list/add/edit 页面模式隔离、十个页面接入约束和单据按钮权限映射。

## 5. 前端质量检查

| 检查 | 结果 | 说明 |
| --- | --- | --- |
| ESLint | 通过 | `pnpm lint` 无错误 |
| TypeScript | 未通过 | `pnpm exec vue-tsc --noEmit` 返回错误 |
| Production Build | 通过 | Vite 生产构建成功，3444 modules transformed |

TypeScript 已确认错误：

1. `src/views/warehouse/inbound/index.vue` 有 4 个 `DefaultRow` 不能赋给 `InboundVO` 的错误。
2. `src/views/property/charge/binding/index.vue` 有 4 个 `DefaultRow` 不能赋给 `ChargeBindingVO` 的错误。
3. 同一 ChargeBinding 页面仍使用不存在的 `ElFormInstance` 导出类型。
4. 同一 ChargeBinding 页面访问了当前组件实例类型中不存在的 `useDict`。

生产构建成功不能替代 TypeScript 检查；当前前端验收状态因此为未完全通过。

## 6. 动态权限组合结果

下表严格区分自动化覆盖和真实登录覆盖：

| 权限组合 | 自动化/静态结果 | 真实浏览器结果 | 本轮结论 |
| --- | --- | --- | --- |
| list-only | 页面模式测试确认只调用列表路径，不开放详情/维护按钮 | 未执行 | 自动化通过，E2E 未执行 |
| query-only | Controller/按钮契约确认只读详情与维护接口隔离 | 未执行 | 自动化通过，E2E 未执行 |
| add-only | 页面模式测试确认可进入 add 模式且不应调用 list/query | 未执行 | 自动化通过，E2E 未执行 |
| edit-only | 编辑初始化 Controller/Service 与页面模式测试通过 | 未执行 | 自动化通过，E2E 未执行 |
| list + query | QUERY Context 与页面模式测试通过 | 未执行 | 自动化通过，E2E 未执行 |
| list + add | 权限模式组合逻辑通过 | 未执行 | 自动化通过，E2E 未执行 |
| list + edit | 权限模式组合逻辑通过 | 未执行 | 自动化通过，E2E 未执行 |
| add + edit | MAINTAIN Context 和编辑初始化相互独立的测试通过 | 未执行 | 自动化通过，E2E 未执行 |
| 无仓库权限 | BusinessContext 反向授权和 Controller 权限测试通过 | 未执行 | 自动化通过，E2E 未执行 |
| SuperAdmin | 数据库与源码确认 Admin/SuperAdmin 启用，菜单读取包含有效 M/C 节点 | 未执行 | 数据库/源码通过，E2E 未执行 |

没有使用静态检查冒充真实动态角色授权结果。

## 7. 菜单、路由与页面验收

### 已通过

- V2.0.36 将入库管理保持为 `C` 页面。
- 仓库目录仍为 `M`，按钮为 `F`，父子结构正确。
- 当前 Vite 前端在浏览器中真实加载到登录页面。
- 前端开发资源包含最新 `useWarehousePageMode` 实现。
- 十个仓库页面的静态集成测试确认：没有 list 权限时列表区不初始化；add/edit 模式按权限分流。

### 未执行

- 登录后的 `getInfo` 实际权限集合。
- 登录后的 `getRouters` 实际 JSON。
- 仓库侧边栏及入库 `C` 页面点击加载。
- add-only 独立入口、edit-only 合法目标 ID 入口的实际 Network 请求。
- 手工 URL 越权访问的真实浏览器响应。

因此本轮没有足够证据判断是否需要单独 NAV 修复任务。若后续真实验证发现 add-only 没有合法入口，应新增 NAV 专项，而不是授予 list 权限。

## 8. 公司仓、项目仓与业务安全

### 自动化通过

- `WarehouseAccessServiceTest` 4 项通过。
- 项目仓项目成员范围、公司仓部门范围、资源真实归属校验相关 Service 测试通过。
- 仓库/库区/库位父子归属测试通过。
- 物料公司范围测试通过。
- 调拨来源仓和目标仓双侧访问校验测试通过。
- 查询 Context 不能调用 MAINTAIN Context。
- edit-only 不能获得 query/list/export/remove。
- 非 DRAFT 单据编辑初始化被拒绝。
- 入库、出库、调拨、盘点状态机与库存变更测试继续通过。
- SuperAdmin bypass 不会跳过库存不足、状态合法性和重复处理等业务规则。

### 数据库样本确认

- `TEST-A-WH`：项目 A 仓，库存样本 100。
- `TEST-B-WH`：项目 B 仓，库存样本 200。
- `TEST-COMPANY-WH`：公司仓负向样本。

### 未执行

没有以真实 Token 对 A/B/公司仓逐个发送浏览器请求，因此数据范围 E2E 状态为未执行。

## 9. 登录、授权与撤权

项目已有 `auth010b_http_verify.ps1` 不能直接用于当前后端：它向带 `@ApiEncrypt` 的 `/auth/login` 发送未加密 JSON，`CryptoFilter` 返回 403：

| URL | 期望前置 | 实际错误 | 分类 |
| --- | --- | --- | --- |
| `POST /auth/login` | `@ApiEncrypt` 请求头与加密载荷 | code 403，`没有访问权限，请联系管理员授权` | 诊断脚本与加密契约不兼容，不是 RBAC 拒绝 |

源码位置：`ruoyi-common/ruoyi-common-encrypt/.../CryptoFilter.java`。当前错误信息把“缺少加密载荷”表现为通用权限不足，容易误判为角色问题。

本轮使用项目安全轮换脚本为 10 个 AUTH-010B 隔离账号重新生成了随机密码，明文与 BCrypt 哈希继续只保存在 WSL 本机 `0600` 凭据文件中，未写入 Git、报告或日志。账号状态和角色关联未改变。

由于没有可用于自动化会话的 Admin 凭据，且安全审批拒绝了扩大到全部账号并落盘已知临时明文凭据的方案，本轮未通过正式角色管理接口创建临时角色或逐项变更菜单权限。

因此：

- 授权/撤权真实 API：未执行。
- 旧 Token 撤权后失效：未执行。
- 新登录权限刷新：未执行。
- 异步注销失败日志：没有产生本轮撤权/注销事件，故无可验收事件。
- 临时角色授权恢复：不适用；本轮没有创建或修改临时角色授权。

## 10. 业务数据与环境恢复

- 仓库业务数据写入：**无**。
- 库存余额变化：**无**。
- 库存流水变化：**无**。
- 单据状态变化：**无**。
- 角色菜单变化：**无**。
- 用户角色变化：**无**。
- 项目成员与仓库范围变化：**无**。
- 测试账号密码：执行了安全随机轮换，凭据仍留在原 AUTH-010B 本机受保护目录。
- 本轮启动的后端和前端进程：已停止。

## 11. 剩余问题与建议任务

### P1：真实动态授权 E2E 未完成

需要一套明确授权的、可自动登录的 Admin 测试凭据，或新增不含敏感明文的本地 E2E 凭据注入机制。随后通过正式角色管理接口完成 10 种组合、撤权和旧 Token 验证。

### P1：TypeScript 检查失败

建议独立修复仓库入库行类型与 ChargeBinding 历史类型问题，然后重新执行 `vue-tsc --noEmit`。本任务按边界未修改前端实现。

### P2：HTTP 验证脚本未适配 ApiEncrypt

应让 `auth010b_http_verify.ps1` 复用前端相同的 RSA/AES 请求加密流程，或提供受控的本地 E2E 客户端；不能关闭 `/auth/login` 加密来迁就脚本。

### P2：登录加密错误反馈过于像 RBAC 失败

`CryptoFilter` 对缺少加密头统一返回“没有访问权限，请联系管理员授权”，会误导排障。建议后续单独评估是否在不泄露安全细节的前提下使用更准确的客户端协议错误提示。

## 12. 验收状态汇总

| 验收域 | 状态 |
| --- | --- |
| V2.0.36 菜单数据库状态 | 通过 |
| 后端仓库自动化 | 通过 |
| BusinessContext 自动化 | 通过 |
| PermissionContractAuditTest | 通过 |
| 前端仓库权限测试 | 通过 |
| ESLint | 通过 |
| TypeScript | 未通过 |
| 前端生产构建 | 通过 |
| 浏览器登录后页面/Network | 未执行 |
| 动态角色 10 种组合 | E2E 未执行 |
| 正式授权及撤权 | 未执行 |
| 业务数据安全 | 通过，无仓库业务写入 |

综上，当前代码层权限治理回归稳定，但综合验收不能关闭。完成 TypeScript 修复与真实动态授权浏览器验收后，才能给出最终 E2E 通过结论。
