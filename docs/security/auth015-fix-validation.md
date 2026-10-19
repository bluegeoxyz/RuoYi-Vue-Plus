# TASK-AUTH-015-FIX 验收报告

## 1. 修复结果

本次修复未修改七类标准角色权限矩阵、数据库、Migration 或项目数据范围规则。

- `RECEIVABLE_QUERY` 现在允许访问 `CHARGE_ITEM` 引用，仍要求 `property:receivable:list` 或 `property:receivable:query`，收费项目管理权限不能替代应收查询权限。
- 新增 `RESIDENT_QUERY`，仅由 `property:resident:list` 或 `property:resident:query` 激活，并仅允许读取 `BUILDING`、`UNIT`、`ROOM` 三类引用。
- 住户列表页改用住户查询上下文接口，不再调用楼栋、单元、房屋管理 Options。
- 应收页将列表与汇总视为核心请求，将收费项目选项视为辅助请求。辅助请求失败会清空选项并明确提示权限配置错误，不再遮蔽已经成功的核心数据；核心请求失败仍正常抛出，不会静默忽略。

## 2. BusinessContext 变更

| Context | 权限组合 | ReferenceType | 项目范围 |
| --- | --- | --- | --- |
| `RECEIVABLE_QUERY` | `property:receivable:list OR property:receivable:query` | 原有引用 + `CHARGE_ITEM` | `FINANCE / OPTION` |
| `RESIDENT_QUERY` | `property:resident:list OR property:resident:query` | `BUILDING`、`UNIT`、`ROOM` | `CUSTOMER_RELATION / OPTION` |

上述 Context 均由服务端固定，不接受前端传入 Context 或权限字符串。住户选项服务逐级验证项目、楼栋、单元和房屋归属，并仅查询启用记录及返回显示所需字段。

## 3. 前端 API 依赖变化

| 页面用途 | 旧接口 | 新接口 |
| --- | --- | --- |
| 住户楼栋筛选 | `GET /property/resource/building/options` | `GET /property/resident/context/query/buildings` |
| 住户单元筛选 | `GET /property/resource/unit/options` | `GET /property/resident/context/query/units` |
| 住户房屋筛选 | `GET /property/resource/room/options` | `GET /property/resident/context/query/rooms` |
| 应收收费项目筛选 | `GET /property/receivable/context/query/charge-items` | 路径不变；修复 Context 对 `CHARGE_ITEM` 的授权 |

三个住户新接口均使用既有 `property:resident:list OR property:resident:query` 权限，不新增权限码。原基础资料管理 Options 保持不变。

## 4. 自动化测试与构建

- `BusinessContextAccessServiceTest`：13 项通过，0 失败。
- `FinanceContextOptionControllerPermissionTest`、`FinanceContextOptionServiceImplTest`、`ResidentContextOptionControllerPermissionTest`、`ResidentContextOptionServiceImplTest`：合计 12 项通过，0 失败。
- `PermissionContractAuditTest`：6 项通过，0 失败；生成契约共 269 条，新增的 3 条住户上下文路由均为 OR 权限语义。
- 前端目标文件 `oxlint`：通过，无输出。
- 前端 `pnpm run build:prod`：通过，3442 个模块完成转换，生产构建成功。

Windows 环境下 Mockito inline mock maker 不能自行附加到当前 JVM；验收命令显式加载项目已使用的 Byte Buddy agent 后测试通过。第一次沙箱内构建还受到本机编译插件目录写权限限制，已在获准的构建环境中完整复测通过。

## 5. 真实登录态验收

本次未改动开发数据库角色授权，也未使用或输出测试账号凭据，因此未宣告真实登录验收通过。现有数据库审计显示客服角色的动态授权包含应收查询，但当时授予的“住客查询”实际对应客户主档权限，而不是 `property:resident:list/query`；要验证住户页面，需由管理员明确授予住户列表/详情权限并重新登录。

建议人工按以下顺序验收：

1. 为测试客服临时授予 `property:receivable:list/query` 与 `property:resident:list/query`，重新登录。
2. 验证应收列表、汇总、收费项目筛选和详情；确认生成、作废、收款仍不可用。
3. 验证住户列表、详情及楼栋/单元/房屋级联；确认客户关系维护和客户主档仍不可用。
4. 验证项目外资源 ID 被拒绝；撤销权限并重新登录后，相应接口不可访问。
5. 验收结束后恢复测试角色的临时动态授权。

## 6. 遗留风险

- 角色权限是登录会话快照；通过角色管理变更会清理相关在线会话，直接执行 SQL 则不会自动失效旧 Token。动态授权验收必须重新登录。
- 当前生产构建通过，但未在本次任务中启动前后端并执行浏览器端真实账号流程。
- 应收辅助筛选失败现在不会抹掉核心列表，但会明确提示“收费项目筛选项加载失败”；这能区分部分可用与完整权限配置错误，仍需运维通过日志定位具体 403/业务范围原因。
