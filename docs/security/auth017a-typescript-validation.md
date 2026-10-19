# TASK-AUTH-017A 前端存量 TypeScript 错误清零验证报告

## 1. 验收结论

前端全量 TypeScript 错误已从 10 个降为 0 个。`vue-tsc`、ESLint、相关仓库权限测试和生产构建均通过。

本次未修改业务权限、菜单、角色矩阵、后端接口、数据库、收费规则或仓库单据状态机。

## 2. 原始错误与根因

| 文件 | 原始位置 | 数量 | 根因 |
| --- | --- | ---: | --- |
| `src/views/warehouse/inbound/index.vue` | 原第 13 行的确认、完成、取消和删除事件 | 4 | Element Plus 表格插槽的 `scope.row` 被声明为通用 `DefaultRow`，但事件函数错误地要求完整 `InboundVO`。 |
| `src/views/property/charge/binding/index.vue` | 原第 43、47、48、49 行的状态、详情、修改和删除事件 | 4 | 同样将通用 `DefaultRow` 直接传给要求完整 `ChargeBindingVO` 的函数。 |
| `src/views/property/charge/binding/index.vue` | 原第 88 行 | 1 | Element Plus 实际导出类型为 `FormInstance`，不存在被直接导入的 `ElFormInstance`。 |
| `src/views/property/charge/binding/index.vue` | 原第 116 行 | 1 | `useDict` 是项目工具函数，不是 `ComponentPublicInstance` 的成员；通过 `proxy?.useDict` 调用与现有项目规范不符。 |

## 3. 修改文件

- `E:/space/project/zbm-platform-ui/src/views/warehouse/inbound/index.vue`
- `E:/space/project/zbm-platform-ui/src/views/property/charge/binding/index.vue`
- `E:/space/project/zbm-platform/docs/security/auth017a-typescript-validation.md`

## 4. 类型修复方案

### 4.1 入库管理

- 将表格行事件入参改为 `Partial<InboundVO>`，与 Element Plus 插槽行类型契约兼容。
- 新增 `getInboundActionRow`，在调用确认、完成、取消或删除 API 前显式校验 `id` 和 `status`。
- 删除确认文案在单号缺失时回退显示 ID，不使用类型断言。

### 4.2 房屋收费设置

- 将表格行事件入参改为 `Partial<ChargeBindingVO>`。
- 新增 `getBindingId`，在详情、修改、删除和状态切换前校验记录 ID；状态切换额外校验状态值为 `0` 或 `1`。
- 将表单引用改为 Element Plus 官方 `FormInstance`。
- 按项目现有用法从 `@/utils/dict` 直接导入并调用 `useDict`。

未使用 `any` 扩散、`@ts-ignore`、关闭类型检查、放宽 `tsconfig` 或强制类型断言。

## 5. 自动化验证

| 验证项 | 命令 | 结果 |
| --- | --- | --- |
| 修复前类型基线 | `pnpm exec vue-tsc --noEmit` | 失败，10 个错误 |
| 修复后全量类型检查 | `pnpm exec vue-tsc --noEmit` | 通过，0 个错误 |
| ESLint | `pnpm lint` | 通过 |
| 仓库前端权限回归 | `pnpm exec vitest run src/views/warehouse/useWarehousePageMode.test.ts src/views/warehouse/warehousePageModeIntegration.test.ts src/views/warehouse/warehouseOperationPermissions.test.ts` | 3 个测试文件、23 个用例全部通过 |
| 生产构建 | `pnpm build:prod` | 通过，3444 个模块转换完成 |

## 6. 业务影响

- 正常 API 返回的表格数据与用户操作流程不变。
- 只对异常的缺失 ID、缺失入库状态或非法收费设置状态增加明确错误提示，防止继续发送无效请求。
- 未改变接口请求结构、权限码、资源范围或业务规则。

## 7. 未覆盖项

本任务未要求真实浏览器验收，且改动不涉及页面交互流程或后端契约；本次以全量类型检查、权限回归、ESLint 和生产构建作为验收依据。
