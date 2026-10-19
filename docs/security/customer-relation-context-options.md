# 客户关系业务上下文 Options

AUTH-005 将客户关系列表筛选与新增制单所需引用数据从项目、楼栋、单元、房屋管理权限中解耦。旧管理 Options 和旧客户关系 Options 保留兼容，不删除、不放宽权限。

| 场景 | 固定上下文 | Controller 权限 | 引用数据 |
|---|---|---|---|
| 列表筛选 | `CUSTOMER_RELATION_LIST` | `property:customerRelation:list` | 项目、楼栋、单元、房屋 |
| 新增关系 | `CUSTOMER_RELATION_CREATE` | `property:customerRelation:add` | 项目、楼栋、单元、房屋、脱敏客户 |
| 编辑关系 | 无 Options | `property:customerRelation:edit` | 仅修改主要关系人和备注，使用列表行快照 |
| 只读详情 | 无 Options | `property:customerRelation:query` | 详情接口直接返回展示名称 |
| 迁入/迁出 | 无 Options | 原独立权限 | 仅使用关系 ID 与业务日期 |

新接口位于 `/property/customer-relation/context/{list|create}/...`。路由在服务端固定业务上下文，客户端不能提交权限字符串。通过能力校验后，仍由 `PropertyProjectScopeService` 校验有效项目成员范围，并逐级验证楼栋、单元、房屋的真实归属和启用状态。客户搜索继续要求至少两字符、最多二十条、仅有效客户，并对自然人姓名和手机号脱敏。

权限契约回归结果：253 个 Controller 接口、54 个业务 Options 接口，其中本任务新增 9 个固定上下文接口；原 22 个旧管理 Options 耦合基线继续保留，已知 Controller/Service 冲突从 3 个降为 0。
