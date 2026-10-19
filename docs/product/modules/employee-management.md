# V1.0 员工管理功能文档

## 文档元数据

| 项目 | 内容 |
|---|---|
| 功能名称 | 员工管理 |
| 所属菜单 | 人事管理 / 员工管理 |
| 适用岗位 | 具有 `hr:employee:*` 相应权限的人员 |
| 当前实现状态 | 已实现 |
| 文档状态 | 已核对 |
| 事实基线 | 2026-10-13 当前工作树与 TASK-V1-001～002C 报告 |

## 1. 功能概述

员工管理维护人员档案、所属部门、主岗位、用工状态和启停状态，并允许将员工可选地绑定到一个系统登录账号。员工档案与系统账号是两个模型：`hr_employee` 保存人员业务信息，`sys_user` 保存登录身份。

后端位于 `ruoyi-modules/ruoyi-hr/src/main/java/org/dromara/hr/employee/`；前端页面为 `E:\space\project\zbm-platform-ui/src/views/hr/employee/index.vue`，API 与类型位于 `src/api/hr/employee/`。

## 2. 功能清单

| 操作 | 实现 | 权限码 | 说明 |
|---|---|---|---|
| 分页查询 | `GET /hr/employee/list` | `hr:employee:list` | 支持员工、部门等条件；部门条件展开下级范围 |
| 详情 | `GET /hr/employee/{id}` | `hr:employee:query` | 返回部门、岗位和绑定账号展示信息 |
| 新增 | `POST /hr/employee` | `hr:employee:add` | 新增时不直接绑定账号 |
| 修改 | `PUT /hr/employee` | `hr:employee:edit` | 保留已有 `userId`，不能借普通编辑更换绑定 |
| 启停 | `PUT /hr/employee/changeStatus` | `hr:employee:edit` | 仅修改员工档案的 `status` |
| 删除 | `DELETE /hr/employee/{ids}` | `hr:employee:remove` | 批量逻辑删除；绑定账号或被业务引用时拒绝 |
| 导出 | `POST /hr/employee/export` | `hr:employee:export` | 手机、邮箱在导出模型中脱敏 |
| 绑定账号 | `PUT /hr/employee/{employeeId}/bind-user` | `hr:employee:bindUser` | 员工与账号一对一，不能绑定 SuperAdmin |
| 解绑账号 | `DELETE /hr/employee/{employeeId}/unbind-user` | `hr:employee:bindUser` | 已无绑定时幂等返回成功 |
| 账号候选 | `GET /hr/employee/user-options` | `hr:employee:bindUser` | 排除 SuperAdmin，并标识账号是否已绑定 |
| 员工候选 | `GET /hr/employee/options` | `hr:employee:list` | 提供引用选择 |
| 部门/岗位候选 | `GET /deptTree`、`GET /postOptions` | `hr:employee:list` | 岗位按部门读取 |

未发现员工导入接口；当前功能清单不包含员工批量导入。

## 3. 业务规则

### 创建与编辑

- 员工编号、姓名、部门、在职状态必填；编辑时员工 ID 必填。
- 员工编号通过 Service 预检查和数据库唯一索引 `uk_hr_employee_no` 双重保证唯一。
- 部门必须存在、启用且在当前用户部门数据范围内。
- 主岗位可为空；填写时必须存在、启用且属于所选部门。
- 性别、证件类型、用工类型、员工状态和启停状态须匹配对应字典。
- 离职日期不能早于入职日期。
- 新增时默认排序为 0、档案状态为正常、员工状态为 `ACTIVE`；新增不会带入账号绑定。

### 账号绑定

- 员工和账号是可选的一对一关系；`hr_employee.user_id` 保存绑定账号。
- 只有 `employment_status=ACTIVE` 且档案 `status` 正常的员工可绑定。
- SuperAdmin 账号禁止绑定；账号必须存在且在调用者用户数据范围内。
- 已绑定员工必须先解绑后换绑；同一系统账号不能绑定多个员工。
- 绑定/解绑成功后发布 `OnlineUserCleanEvent` 清理相应用户在线状态。
- 绑定同一个已有账号以及解绑一个未绑定员工按幂等成功处理。

### 状态与删除

- 员工档案启停、员工在离职状态与 `sys_user.status` **没有自动联动代码证据**。员工停用或离职不会由该 Service 自动禁用系统账号。
- 已绑定系统用户的员工不能删除，必须先解绑。
- 删除前逐个调用 `EmployeeReferenceChecker`；任何业务引用返回阻塞原因时拒绝删除。
- 使用 `@TableLogic` 的 `del_flag` 逻辑删除。

### 敏感信息

详情 VO 的手机、邮箱和证件号使用 `@Sensitive`；具有 `hr:employee:edit` 权限时可按框架敏感策略显示，否则脱敏。导出 VO 始终显式脱敏手机和邮箱。当前员工模块未发现等同客户模块的字段加密、指纹和专用访问审计实现。

## 4. 权限控制

Controller 权限为 `list/query/add/edit/remove/export/bindUser`。前端对应按钮使用相同 `v-hasPermi`。Service 进一步调用：

- `ISysDeptService.checkDeptDataScope` 校验部门访问范围；
- `ISysUserService.checkUserDataScope` 校验待绑定账号；
- Mapper 查询结合部门集合约束列表；
- SuperAdmin 用户 ID 不能作为普通员工绑定对象。

员工权限不使用物业项目范围或仓库范围。前端按钮控制不替代 Controller 与 Service 校验。

## 5. 数据模型

主表 `hr_employee` 定义于 `script/sql/zbm/baseline/01_schema.sql`。

| 字段 | 含义 |
|---|---|
| `id` | 员工主键 |
| `employee_no`、`employee_name` | 员工编号与姓名 |
| `dept_id`、`post_id` | 所属部门与主岗位 |
| `user_id` | 可选绑定系统账号 |
| `gender`、`mobile`、`email`、`id_type`、`id_no` | 人员信息 |
| `birth_date`、`entry_date`、`leave_date` | 日期信息 |
| `employment_type`、`employment_status` | 用工类型和在离职状态 |
| `status` | 员工档案启停状态 |
| `del_flag` | 逻辑删除 |

数据库为员工编号建立唯一索引；`user_id` 当前审计到的是普通索引，账号一对一还依赖 Service 的占用检查和条件更新。部门、岗位、用户关系主要按应用层软外键校验。

## 6. 接口清单

| Method | URL | Controller#method | 请求/返回 | 限制 |
|---|---|---|---|---|
| GET | `/hr/employee/list` | `EmployeeController#list` | `EmployeeBo`、分页 `EmployeeVo` | 部门范围 |
| GET | `/hr/employee/{id}` | `#getInfo` | ID / `EmployeeVo` | 不存在或无权时拒绝 |
| POST | `/hr/employee` | `#add` | `EmployeeBo` | AddGroup 校验 |
| PUT | `/hr/employee` | `#edit` | `EmployeeBo` | EditGroup 校验；保留绑定 |
| PUT | `/hr/employee/changeStatus` | `#changeStatus` | ID、status | 字典值校验 |
| DELETE | `/hr/employee/{ids}` | `#remove` | ID 数组 | 绑定与引用保护 |
| POST | `/hr/employee/export` | `#export` | 查询 BO / Excel | 脱敏导出 |
| PUT | `/hr/employee/{employeeId}/bind-user` | `#bindUser` | `userId` | 一对一与状态限制 |
| DELETE | `/hr/employee/{employeeId}/unbind-user` | `#unbindUser` | employeeId | 幂等 |
| GET | `/hr/employee/user-options` | `#userOptions` | 查询/分页候选 | 排除 SuperAdmin |

## 7. 测试与验收状态

| 验证层级 | 状态 | 证据/限制 |
|---|---|---|
| 代码与前端契约 | 已确认 | Controller、Service、Mapper、页面、API、types 均存在 |
| 后端编译 | 通过 | TASK-V1-002C 43 模块编译 |
| 自动化测试 | 存在并通过已有门禁 | `ruoyi-hr` 测试与全量 Maven 门禁；未据此声称全部规则逐项覆盖 |
| 前端静态/组件门禁 | 通过 | typecheck、lint、30 项全局 Vitest、build；不等于员工页面 E2E |
| 数据库集成 | 未单独确认 | 未在 002B 报告中看到员工专项 MySQL 用例结论 |
| 浏览器 E2E/人工验收 | 未执行 | 无正式报告 |

## 8. 已知限制

- 员工状态与账号状态不自动联动。
- `hr_employee.post_id` 与 `sys_user_post` 未确认存在自动同步。
- 未发现员工导入功能。
- 员工敏感信息有展示/导出脱敏，但未发现客户级别的专用加密、指纹和访问审计。
- `user_id` 的数据库唯一性未由已审计索引证明，仍需依赖 Service 并发控制；是否完全覆盖并发绑定需专项数据库测试确认。

## 9. 代码证据路径

- `ruoyi-modules/ruoyi-hr/src/main/java/org/dromara/hr/employee/controller/EmployeeController.java`
- `ruoyi-modules/ruoyi-hr/src/main/java/org/dromara/hr/employee/service/impl/EmployeeServiceImpl.java`
- `ruoyi-modules/ruoyi-hr/src/main/java/org/dromara/hr/employee/domain/Employee.java`
- `ruoyi-modules/ruoyi-hr/src/main/java/org/dromara/hr/employee/domain/bo/EmployeeBo.java`
- `ruoyi-modules/ruoyi-hr/src/main/java/org/dromara/hr/employee/domain/vo/EmployeeVo.java`
- `script/sql/zbm/baseline/01_schema.sql`
- `E:\space\project\zbm-platform-ui/src/views/hr/employee/index.vue`
- `E:\space\project\zbm-platform-ui/src/api/hr/employee/`
