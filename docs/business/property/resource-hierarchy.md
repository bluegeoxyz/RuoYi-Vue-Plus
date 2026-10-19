# V1.0 物业资源层级与跨模块规则

## 1. 层级结构

```text
pm_project
  └─ pm_building (project_id)
       └─ pm_unit (project_id, building_id)
            └─ pm_room (project_id, building_id, unit_id)
                 ├─ pm_room_customer_rel
                 └─ pm_charge_binding / 应收与收款引用链路
```

这是逻辑层级，不代表数据库配置了外键级联。当前主要依靠 Service 校验父子归属和应用层锁。

## 2. 节点与查询

| 节点 | 标识与展示 | 查询范围 |
|---|---|---|
| 项目 | ID、编码、名称/简称 | 项目管理范围或业务 OPTION 范围 |
| 楼栋 | 项目 ID、楼栋编码/名称 | RESOURCE READ/OPTION |
| 单元 | 项目 ID、楼栋 ID、单元编码/名称 | RESOURCE READ/OPTION |
| 房屋 | 项目/楼栋/单元 ID、房屋编码/房号 | RESOURCE READ/OPTION |

当前没有返回 Project→Building→Unit→Room 整棵树的独立接口。页面通过项目选择和楼栋/单元/房屋列表、options 分级查询；“资源树”一词主要出现在树形导入能力。

## 3. 父子归属与状态

- 楼栋普通编辑不能跨项目。
- 单元新增校验楼栋属于项目；普通编辑不能跨项目或楼栋。
- 房屋新增校验项目、楼栋、单元都启用且归属一致；普通编辑不能迁移父级。
- 楼栋新增只验证项目存在，未验证启用；这是当前代码差异。
- 停用任一节点只更新自身，不自动停用子节点。
- options 通常只返回本级状态正常记录，并叠加项目范围。

## 4. 删除与并发

- 楼栋删除锁定楼栋与子资源范围，存在单元或房屋时拒绝。
- 单元删除锁定父楼栋、本单元与房屋范围，存在房屋时拒绝。
- 房屋删除锁定房屋并检查有效客户关系，任一受保护房屋使批量事务回滚。
- 唯一编码采用逻辑删除感知的生成列唯一索引，允许删除后复用并保留历史。
- 没有数据库外键级联证据；不得描述为自动级联删除。

## 5. 客户、住户与入住状态

`pm_room_customer_rel` 关联房屋和客户。迁入、迁出后调用统一入住状态服务，根据全部有效入住关系计算 `OCCUPIED/VACANT`。住户页面是客户、关系和资源的聚合视图，不是新的资源所有权表。

## 6. 收费关系

房屋收费绑定以 `object_type=ROOM`、`object_id=roomId` 引用房屋，并在创建时校验项目、楼栋、单元和房屋归属。资源页面可进入收费设置和收费台账。

当前 `RoomServiceImpl.delete` 只明确检查有效客户关系，未见在删除前检查 `pm_charge_binding` 或应收历史。因此只能确认“收费使用房屋”，不能确认“所有收费引用都会阻止房屋删除”。

## 7. 项目范围变化

资源查询和操作实时解析 `PropertyBusinessDomain.RESOURCE`。项目成员结束或停用后，普通用户范围缩小；保存的前端项目不会扩大权限。详情和写操作按数据库中的 projectId 校验，避免跨项目 IDOR。

## 8. 状态与验证

楼栋、单元、房屋 CRUD 为已实现；树形导入为部分实现；独立树查询未实现。自动化测试覆盖范围、唯一性、父子并发和删除事务；浏览器 E2E 未执行。

## 9. 证据

- `ruoyi-modules/ruoyi-property/src/main/java/org/dromara/property/resource/`
- `ruoyi-modules/ruoyi-property/src/main/java/org/dromara/property/customer/relation/service/impl/RoomCustomerRelationServiceImpl.java`
- `ruoyi-modules/ruoyi-property/src/main/java/org/dromara/property/charge/binding/service/impl/ChargeBindingServiceImpl.java`
- `ruoyi-modules/ruoyi-property/src/test/java/org/dromara/property/resource/`
- [资源管理功能](../../product/modules/property-resource-management.md)

