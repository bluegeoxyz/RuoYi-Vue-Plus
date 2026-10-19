# TASK-012：企业级住户档案架构设计（Phase A）

> 状态：Phase A-FINAL 已按业务决策定稿；本文件未实施数据库 Migration、Customer CRUD、前端页面或 RoomCustomerRelation。  
> 依据：当前仓库实际代码与 `script/sql/zbm/baseline/phase1`，RuoYi-Vue-Plus 6.0.0，TASK-006～TASK-011 已落地代码。  
> 标记规则：**[事实]** 表示仓库现状，**[方案]** 表示推荐实施方案，**[决策]** 表示编码前仍需业务确认。

## 1. 设计目标与边界

TASK-012 只建设企业级 Customer 主数据。Customer 不是 Employee，也不是 SysUser：

- `SysUser` 是登录与 RBAC 主体；`Employee` 是企业内部员工档案，可选择绑定一个 SysUser。
- `Customer` 是被服务的个人或企业客户，不要求拥有系统账号，也不通过 HR 管理。
- Customer 与房屋是多对多关系，关系及其入住/迁出历史由 TASK-013 的 `RoomCustomerRelation` 负责。
- 停用、注销或删除 Customer，和终止房屋关系是两个独立命令，任何一方都不得自动清除另一方历史。

本设计保持依赖方向：Property 可调用 HR/System 的公开能力；HR 不依赖 Property；不改变 RuoYi 部门数据权限语义。

## 2. 当前代码与数据库检查结论

### 2.1 `pm_customer` 真实结构

**[事实]** Phase 1 Baseline 将 `pm_customer` 定义为“企业级客户主数据”，明确无 `project_id`。字段如下：

| 类别 | 实际字段 | 结论 |
|---|---|---|
| 主键/编号 | `id BIGINT`、`customer_no VARCHAR(32)` | `customer_no` 当前为全表唯一 `uk_pm_customer_no` |
| 分类/名称 | `customer_type`、`customer_name` | 类型字典已有 `PERSON`、`COMPANY`；同一字段同时承载个人姓名与企业名称 |
| 基础信息 | `gender`、`birth_date` | 更适合个人客户；企业客户使用时应为空 |
| 联系信息 | `mobile`、`phone`、`email` | 当前均为明文列；手机号只有普通索引，不唯一 |
| 证件信息 | `id_type`、`id_no` | 字典已有身份证、护照、其他；当前为明文列，只有普通组合索引 |
| 生命周期 | `status`、`del_flag` | 状态仅 0/1，尚无合并、注销等明确业务状态 |
| 审计 | `create_dept/create_by/create_time/update_by/update_time/remark` | 可沿用 `BaseEntity` 自动填充；无敏感读取审计 |

当前表没有详细联系地址字段，没有企业统一社会信用代码专用字段，也没有 `enterprise_id` / `tenant_id`。V2 首发已确定为单企业部署，`pm_customer` 暂不增加企业字段；该结论只适用于单企业运行边界，不能把同一数据库实例直接扩展成多物业企业共享部署。

### 2.2 `pm_room_customer_rel` 真实结构

**[事实]** 关系表保存 `project_id`、`room_id`、`customer_id`，以及：

- `relation_type`：OWNER、TENANT、FAMILY、RESIDENT、CONTACT、OTHER。
- `is_primary`、`start_date/end_date`、`move_in_date/move_out_date`。
- `relation_status`：PENDING、ACTIVE、ENDED；另有通用 `status`、`del_flag` 和审计字段。
- 已有面向 project、room、customer、room+customer+type、project+customer 的普通索引。
- 没有数据库外键，也没有防止重复有效关系或“一个房屋多个主要关系人”的唯一约束。

该模型支持一个 Customer 关联多个项目的多套房屋，也支持一个 Room 关联多个 Customer；Customer 基础资料更新不破坏历史关系。

### 2.3 代码、菜单与字典现状

- **[事实]** `org.dromara.property.customer` 仅有 `package-info.java`，没有 Customer 或 RoomCustomerRelation 的 Entity、Mapper、Service、Controller。
- **[事实]** Room 删除通过 `RoomMapper.existsActiveCustomerRelation` 阻止删除仍有 ACTIVE、启用、未删除关系的房屋；Room 删除先锁 Room 行，再检查关系。
- **[事实]** 菜单已存在“客户档案”与“房客关系”，component 分别是 `property/customer/index`、`property/customer/relation/index`。
- **[事实]** 已有 `property:customer:list/query/add/edit/remove/export` 和 `property:customerRelation:list/query/add/edit/remove/export/moveIn/moveOut`。
- **[事实]** 已有客户类型、关系类型、关系状态和证件类型字典；暂未看到 Customer 生命周期、合并原因等字典。

### 2.4 现有权限、安全与审计能力

| 能力 | 仓库事实 | 设计使用方式 |
|---|---|---|
| 功能权限 | Sa-Token + `@SaCheckPermission` | 继续控制 API 能否调用，但不替代 Customer 数据范围 |
| 项目业务范围 | `PropertyProjectScopeService` 支持 `CUSTOMER_RELATION`；当前成员 contributor 对其返回有效项目成员范围 | 用于项目住户查询及关系维护；不能推导企业级 Customer 管理权 |
| 项目管理范围 | 独立 `pm_project_management_grant` | 不得转换为 Customer 业务范围或企业客户档案权 |
| 反自提权 | `RoleDelegationGuard` 识别敏感菜单并限制为 SuperAdmin 委派 | 新增 Customer 企业管理/敏感查看权限时必须加入敏感集合并覆盖所有角色授予入口 |
| 响应脱敏 | `@Sensitive` 支持 PHONE、EMAIL、ID_CARD 等策略并可按权限放行；Employee/SysUser 已使用 | Customer 列表/普通详情可直接复用，但必须使用分离 VO，不能返回 Entity |
| API 传输加密 | `@ApiEncrypt` 及前端加密链路存在 | 可保护敏感写入和敏感详情在传输层；不等于数据库静态加密 |
| 字段加密 | `@EncryptField` 基础设施存在，当前全局开关默认关闭，生产代码仅发现 demo 示例 | Phase B 启用 AES 配置并对 Customer 字段落地；密钥只能来自环境变量/外部配置 |
| 操作日志 | `@Log`、`sys_oper_log` | 可记录增删改导出；不适合直接记录敏感明文，也不足以审计每次敏感读取 |
| 请求日志保护 | Web 调用耗时拦截器有结构化敏感参数脱敏 | 仍需为 Customer BO 字段名、异常消息和业务审计制定白名单/排除规则 |

## 3. Customer 最终实体设计

### 3.1 聚合定位

**[方案]** Customer 是企业边界内唯一、独立的客户主档聚合根：

```text
Customer (企业主数据)
  1 ──< RoomCustomerRelation >── 1 Room (项目资源)
```

Customer 不保存“当前项目”“当前房屋”或“业主/租客”字段；这些都是有时间范围的关系事实。列表中的“关联项目数/房屋数”应由受权限过滤的关系查询派生，不能冗余写入主档后直接暴露。

### 3.2 字段建议

沿用字段：`id`、`customer_no`、`customer_type`、`customer_name`、`gender`、`mobile`、`phone`、`email`、`id_type`、`id_no`、`birth_date`、`status`、审计字段和 `del_flag`。

建议新增但需 Migration 的字段：

| 字段 | 目的 | 说明 |
|---|---|---|
| `identity_fingerprint` | 并发去重和等值检索 | 对“企业 + 证件类型 + 标准化证件号”做带密钥 HMAC；不得存普通 SHA 明文彩虹表可枚举值 |
| `mobile_fingerprint` | 辅助查重 | 仅检索候选，不加唯一约束；允许家庭共享手机号 |
| `merged_into_customer_id` | 合并追踪 | 首期可保留为空；未来合并后指向保留主档 |
| `lifecycle_status` | 明确生命周期 | 建议 ACTIVE、INACTIVE、CANCELLED、MERGED；不要把所有语义压进通用 `status` |
| `version` | 乐观并发 | 防止两名档案管理员互相覆盖资料；敏感归属/合并仍须悲观锁或专用命令 |

详细联系地址目前不存在。若业务确认需要，应采用独立结构化联系地址或地址表，不把多地址塞进 `remark`。

### 3.3 单企业部署与数据边界（最终决策）

**[决策]** V2 首发仅支持单企业部署，`pm_customer` 暂不增加 `enterprise_id`。企业边界由“一个应用部署 + 一个业务数据库/Schema + 一套 Customer Grant 管理域”共同形成，而不是由行级 tenant 字段形成。

必须满足以下运行约束：

1. 同一业务数据库不得混装第二家物业企业的数据；备份、报表、测试库也应按部署实例隔离。
2. `CustomerAccessPolicy` 和独立 Customer Grant 仍是应用内授权边界；单企业不等于所有登录用户都可访问客户档案。
3. 环境配置必须显式标识 single-enterprise 模式，启动检查不得接受“多企业共享库”配置。
4. 将来支持多企业属于独立架构迁移：需同时改造 Project、Customer、Relation、Grant、唯一键、登录主体和历史数据，不能仅给 `pm_customer` 补一列后宣布完成。

## 4. Customer 数据访问模型

### 4.1 三类访问范围与敏感能力

```text
功能权限（RBAC）
  × Customer 数据范围（ENTERPRISE / PROJECT_RELATION / NONE）
  × 敏感能力（PII_VIEW / PII_EXPORT）
  = 最终允许动作
```

三者必须同时满足；任一缺失即拒绝。项目 RESOURCE、项目 Management ALL、Customer ID 本身均不能推导企业级客户访问权。

### 4.2 `CustomerAccessPolicy` 最终建议接口

```java
public interface CustomerAccessPolicy {
    CustomerAccessDecision resolve(CustomerAction action);

    void assertEnterpriseMasterAccess(CustomerAction action);

    void assertCustomerVisible(Long customerId, CustomerAction action);

    void assertProjectRelationAccess(Long projectId, CustomerRelationAction action);

    boolean canViewSensitive(CustomerSensitiveField field);

    boolean canExportSensitive();
}

public record CustomerAccessDecision(
    boolean enterpriseWide,
    PropertyProjectScope relationProjectScope,
    Set<CustomerSensitiveField> visibleSensitiveFields,
    boolean sensitiveExportAllowed) {}
```

`CustomerAction` 至少分 LIST、DETAIL、CREATE、UPDATE、STATUS、DELETE、EXPORT、OPTION；关系动作分 READ、WRITE、EXPORT、OPTION。不要使用一个宽泛的 `canAccessCustomer` 覆盖所有用途。

### 4.3 企业级档案授权来源

**[方案]** 功能权限之外新增 Property 自有的 `pm_customer_management_grant`，主体支持 USER/ROLE，只表达“企业 Customer 主档范围”，不复用 `pm_project_management_grant`。当前单企业版本只支持 ENTERPRISE scope；未来增加组织或区域范围时再扩展。

- 当前用户的有效 USER grant 与启用角色的 ROLE grant 求并集。
- 有 `property:customer:*` 但没有有效 grant：默认拒绝企业主档列表、详情和写入。
- grant 不授予 PII 查看/导出；PII 仍需独立敏感功能权限。
- grant 维护权限本身属于不可委派敏感权限，仅 SuperAdmin 可创建、修改、撤销或授予包含它的角色。
- 授权变更必须持久化审计，并在事务提交后清理受影响会话；新请求仍以数据库实时授权为准，踢线不是唯一防线。

### 4.4 项目范围内客户资料规则

项目页面不读取企业全量 Customer 表后在内存过滤，而应从关系表驱动：

```sql
pm_room_customer_rel r
JOIN pm_customer c ON c.id = r.customer_id AND c.del_flag = '0'
WHERE r.project_id IN (:CUSTOMER_RELATION_PROJECT_SCOPE)
  AND r.del_flag = '0'
```

- 项目查询依赖 `CUSTOMER_RELATION + READ/OPTION` 项目范围和对应功能权限。
- Customer 详情按“请求项目 + customerId”验证存在可见关系，只返回该项目内的关系；不能附带其他项目房屋。
- 企业级管理员可通过独立企业 API 查看主档，但项目业务 API 仍按项目过滤。
- 普通成员不得使用裸 Customer ID 调用企业级详情；项目 Options 仅返回最小字段（ID、脱敏名称/联系方式、关系所需状态），并必须绑定 projectId。
- 未实现专项 contributor 的域仍返回 NONE；Management Scope 不参与计算。

## 5. 最终权限决策矩阵

| 场景 | 功能权限 | 数据范围 | PII 权限 | 结果 |
|---|---|---|---|---|
| 企业客户列表（脱敏） | customer:list | 企业 Customer grant | 不要求 | 允许企业内脱敏列表 |
| 企业客户详情（完整证件/手机） | customer:query | 企业 Customer grant | customer:pii:view | 允许，并写敏感读取审计 |
| 新增/修改/停用 | customer:add/edit | 企业 Customer grant | 写敏感字段时使用安全写接口 | 允许 |
| 企业导出（脱敏） | customer:export | 企业 Customer grant | 无 | 仅脱敏导出模板 |
| 企业导出（明文敏感） | customer:export | 企业 Customer grant | customer:pii:export | 允许，强审计；建议二次确认/审批扩展点 |
| 项目住户列表 | relation:list/query | CUSTOMER_RELATION 项目范围 | 无 | 仅关联项目、默认脱敏 |
| 项目住户敏感详情 | relation:query | CUSTOMER_RELATION 项目范围 | customer:pii:view | 仅该项目关系 + 允许字段 |
| 房屋关系维护 | relation:add/edit/moveIn/moveOut | CUSTOMER_RELATION WRITE | 无 | 允许；仍须锁定 Room/Customer 并复核 |
| 仅 Management ALL | 任意 | 无 Customer/Relation 业务范围 | 任意 | 默认拒绝 Customer/关系业务数据 |
| 无任何范围 | 任意 | NONE | 任意 | 列表空、详情/写入拒绝 |
| SuperAdmin | `*:*:*` | 明确 bypass | 明确 bypass | 可访问但仍写敏感审计，不应绕过审计 |

建议新增权限：`property:customer:pii:view`、`property:customer:pii:export`、`property:customerGrant:list/query/add/edit/remove`。既有权限编码保持不变。

## 6. 个人信息保护策略

### 6.1 字段分级与最小返回

| 级别 | 字段 | 默认行为 |
|---|---|---|
| L1 标识 | id、customer_no、customer_type、status | 按数据范围返回 |
| L2 一般个人信息 | customer_name、gender、birth_date、email | 列表最小化；生日不进入普通 Options |
| L3 敏感个人信息 | mobile、phone、id_type、id_no、详细地址（未来） | 默认脱敏；明文详情需要 PII_VIEW |
| L4 高风险输出 | L3 的批量导出、证件完整值 | 独立 PII_EXPORT + 审计；不得复用普通 export 自动放行 |

应定义至少三种 VO：`CustomerListVo`（脱敏且最小）、`CustomerDetailVo`（按策略处理）、`CustomerOptionVo`（最小候选）；导出另用脱敏/敏感两个模型。Entity 永不直接作为响应。

### 6.2 可复用与需新增能力

可直接复用：`@Sensitive`、SensitiveStrategy、`@ApiEncrypt`、`@Log`、ExcelBuilder、请求参数日志脱敏、Sa-Token 权限。

#### 6.2.1 `@EncryptField` 真实实现核查

**[事实]** 当前框架不是 TypeHandler，而是两个 MyBatis 插件：

- `MybatisEncryptInterceptor` 在 `ParameterHandler.setParameters` 前递归查找参数对象中带 `@EncryptField` 的 String 字段，加密后绑定 JDBC 参数，执行结束恢复 Java 对象明文。
- `MybatisDecryptInterceptor` 在 `ResultSetHandler` 返回结果后递归解密，包含普通结果、Collection、Map value 和 MyBatis Cursor。
- 密文增加 `ENC_` 前缀；已有明文（无前缀）读取时保持原样，便于分批迁移。
- 只有实际 MyBatis 参数/结果对象上的 String 字段声明注解才会处理。Entity、直接映射的 VO/DTO 均需按真实查询路径标注；Map/scalar、自定义投影和 wrapper 内普通字符串不会自动获得正确的等值加密语义。
- 全局 `mybatis-encryptor.enable` 当前默认 `false`，默认算法是 BASE64（仅编码、不是安全加密）。启用 AES/SM4 时启动配置会校验 password 非空。
- 当前 Processor 捕获字段处理异常后只记录日志，没有向上抛出；Phase B 必须将 Customer 敏感写入验证为 fail-closed，防止异常时把明文继续交给 MyBatis。

因此，Customer 不能仅“加一个注解”就视为完成：必须启用安全算法、验证所有读写映射、扩容数据库列，并对失败行为和批量/VO/游标路径编写集成测试。

#### 6.2.2 V2 最简字段加密方案（最终方案）

1. V2 使用框架现有 `@EncryptField` + AES + BASE64 编码；Customer Entity 的 `idNo` 必须标注，所有可能直接接收 MyBatis 结果的明文 VO 也必须标注。列表/Options VO 不包含完整证件号。
2. 注解只写 `@EncryptField`，禁止填写 `password`。统一配置示例仅使用占位符：

   ```yaml
   mybatis-encryptor:
     enable: ${ZBM_FIELD_ENCRYPT_ENABLED:true}
     algorithm: AES
     encode: BASE64
     password: ${ZBM_CUSTOMER_FIELD_ENCRYPT_KEY}
   ```

   真实密钥由部署环境变量、容器 Secret 或外部配置中心注入，禁止写入 Git、Migration、日志、测试快照或错误响应。
3. 当前 AES 工具只接受 16/24/32 字节密钥。Phase B 启动检查除“非空”外还须校验长度；测试密钥也从专用测试环境变量注入。
4. `id_no VARCHAR(64)` 不足以可靠保存 `ENC_` + AES/Base64 密文。按 UTF-8 最坏长度及未来格式预留，V2 将 `id_no` 扩为 `VARCHAR(512)`；如后续加密 mobile/phone/email，分别建议至少 `VARCHAR(256)`。Migration 不包含密钥，也不负责在 SQL 中加密。
5. 旧明文数据采用应用侧批处理：读取明文 → 标准化并计算 HMAC → 通过带注解 Entity 更新为密文 → 读回验证。迁移期间先阻止并发写入或使用版本条件，失败可按批次回滚。
6. 加密密文不参与搜索、排序、去重或唯一约束。按证件检索只使用 `identity_fingerprint`；返回证件号仍通过密文解密并按 PII 权限脱敏。
7. 新增 `pii_key_version VARCHAR(32)`，首期写固定配置版本（例如外部配置的 `v1`），为后续重新加密提供识别信息。现有拦截器只支持单一当前密钥，V2 首期不实现自动轮换；更换密钥前必须先提供双读/批量重加密工具，不能直接替换环境变量。
8. 加密密钥丢失时，现有密文不可恢复；数据库备份和 HMAC fingerprint 均不能还原证件号。上线前必须对加密密钥做独立、受控、可审计的备份与恢复演练。HMAC 仍可判断“是否同一身份”，但不能替代原文恢复。

#### 6.2.3 HMAC 与字段加密密钥隔离

- 字段加密密钥用于可逆 AES 加解密，泄露会暴露证件原文。
- HMAC 密钥用于不可逆 `HmacSHA256(normalizedIdType + ':' + normalizedIdNo)`，只生成固定 32 字节 fingerprint，用于查重与唯一索引。
- 两者必须使用不同的随机密钥、不同环境变量和不同访问权限，例如 `ZBM_CUSTOMER_FIELD_ENCRYPT_KEY` 与 `ZBM_CUSTOMER_IDENTITY_HMAC_KEY`；禁止由一个密钥派生或复用。
- HMAC 密钥丢失不影响已存 fingerprint 比较，但无法为新输入计算相同 fingerprint；更换 HMAC 密钥需要全量解密原证件并重新计算，因此也必须受控备份。

其他必须新增能力：敏感访问审计记录 actor、customerId、接口/目的、字段级别、项目上下文、结果、时间、traceId，且不记录明文；`@Log` 排除 idNo/mobile 原值；异常不拼接敏感值；Excel 只记录条件摘要和行数。

## 7. 去重、唯一性与生命周期

### 7.1 身份识别规则

- 强身份：单企业内标准化后的 `(id_type, identity_fingerprint)`。身份证大小写/空格、护照格式按证件类型分别标准化。
- 辅助匹配：姓名 + 出生日期 + 手机尾号、企业名称等，只提示人工复核，不能自动合并。
- 手机号不唯一；家庭成员、企业前台和代办人均可能共享号码。
- 无证件客户允许建档，生成不可复用的 `customer_no`，并标记待核验；不得依靠姓名唯一。
- 企业客户的统一社会信用代码应作为独立证件类型或专用字段由业务确认，不能默认套个人身份证规则。

### 7.2 并发唯一性建议

最终数据库键（单企业模式）：

```sql
identity_fingerprint BINARY(32) NULL,
active_identity_key BINARY(32)
  GENERATED ALWAYS AS (
    CASE WHEN del_flag = '0' AND identity_fingerprint IS NOT NULL
         THEN identity_fingerprint ELSE NULL END
  ) STORED,
UNIQUE KEY uk_pm_customer_active_identity
  (id_type, active_identity_key)
```

MySQL 唯一索引允许多个 NULL，因而无证件客户可并存；有效强身份在并发插入时最多一条成功。Service 先给出友好重复提示，数据库唯一键作为最终防线，并捕获 DuplicateKeyException。

**[最终决策]** 生成列仅在 `del_flag = '0'` 时返回 fingerprint；逻辑删除后生成列为 NULL，允许重新创建相同证件身份。任何时刻只允许一份相同 `(id_type, identity_fingerprint)` 的未删除档案。数据库保留已删除历史，新的 Customer 使用新 ID 和新 customer_no。

这里的“有效身份档案”以 `del_flag = '0'` 为唯一性口径，包含 ACTIVE、INACTIVE、CANCELLED 等尚未逻辑删除的生命周期状态；停用或注销不会释放证件身份。只有满足删除条件并完成逻辑删除后，唯一键才释放。

`customer_no` 建议永不复用，逻辑删除后也不重建相同编号；现有全局唯一键在单企业模式可继续保留。

### 7.3 生命周期规则

1. 新增：标准化 → fingerprint 查重 → 人工候选确认 → 数据库唯一键兜底。
2. 修改：不能通过普通更新改变 Customer 身份归属；强身份变更走专门命令和审计。
3. 停用：禁止新增 ACTIVE 关系；历史关系可查。是否自动结束现有关系：**不自动**。
4. 合并：首期不实现自动合并。人工将重复档案停用并记录疑似主档 ID/工单，不搬迁关系。
5. 注销/删除：存在任何历史关系时禁止逻辑删除；仅允许停用/注销。历史关系检查不按 ACTIVE、status 或 del_flag 排除，必须用专用 Mapper SQL 检查物理存在过的关系记录，避免关系结束或逻辑删除后绕过。完全无关系且满足保留策略时才可逻辑删除。
6. 后续合并命令必须在同一事务锁定两个 Customer（按 ID 升序），迁移/合并关系、记录映射与审计，不物理删除历史。

## 8. Migration 草案（本阶段不执行）

当前最高版本为 V2.0.5；后续不得修改 Baseline 或历史 Migration。版本号需在实施时重新确认。

建议拆分而不是一次大迁移：

1. `V2.0.6__customer_security_columns.sql`：不增加 enterprise_id；新增 `identity_fingerprint BINARY(32)`、`active_identity_key` STORED 生成列、`pii_key_version`、lifecycle/version/merge 字段，扩容密文字段，并创建 `(id_type, active_identity_key)` 唯一索引；执行前检查重复有效身份。
2. `V2.0.7__customer_access_grant_and_audit.sql`：Customer 企业授权表、授权审计和敏感访问审计表。
3. `V2.0.8__customer_permissions.sql`：新增 PII 与 grant 管理菜单权限；历史菜单不修改。

Migration 必须：先只读报告重复数据；发现冲突时显式 `SIGNAL SQLSTATE '45000'` 中止，而不是仅 SELECT 后继续建索引；DDL 只准备列和约束，不包含密钥或加密表达式。历史 fingerprint 与密文由配置好两把独立密钥的应用批处理回填，验证完成后再把唯一索引投入正式写流量。

## 9. 后端与前端模块建议

### 9.1 TASK-012 后端

建议分层：`customer/domain/Customer`、BO、分离 VO、Mapper、`ICustomerService`、`CustomerServiceImpl`、`CustomerController`、`CustomerAccessPolicy` 及 grant/audit 子模块。

建议 API 明确分离：

- `/property/customer/master/*`：企业主档管理，需要企业 grant。
- `/property/customer/project/*`：项目住户只读视图，必须携带 projectId 并从关系表过滤。
- `/property/customer/project-options`：TASK-013 关系维护候选，projectId 必填、最小返回、不可提供全企业 Options。

如为兼容现有菜单保留 `/property/customer` 前缀，也必须在 Controller/Service 方法命名上明确 master 与 project，不复用无上下文的 `options()`。

### 9.2 前端

- “客户档案”是企业级页面，不应强制选择 CurrentProject；进入页面需企业 Customer grant。
- “项目住户”属于项目业务页面，依赖 CurrentProject，并显式验证 `CUSTOMER_RELATION` 域。
- 两者菜单、API 和 Store 语义分开；只有项目管理权限、没有 Customer grant/关系范围的用户不能浏览客户资料。
- 敏感字段默认掩码，明文查看采用显式操作并展示访问提示；不要在表格响应里先下发明文再由 CSS 掩码。
- Options 仅查询当前项目已授权候选，支持服务端搜索与最小字段，不加载全企业 Customer 后前端过滤。

## 10. TASK-013 公共接口与事务锁协议

### 10.1 TASK-012 提供的最小公共接口

```java
public interface CustomerReferenceService {
    CustomerReferenceSnapshot requireActiveForRelation(Long customerId);
    CustomerReferenceSnapshot lockAndRequireActiveForRelation(Long customerId);
    boolean hasAnyRoomRelation(Long customerId);
}

public record CustomerReferenceSnapshot(
    Long customerId, Long enterpriseId, String customerType, String lifecycleStatus) {}
```

该接口只返回关系建立所需字段，不返回手机或证件。TASK-013 不直连 Customer Mapper，也不通过 Customer Options 代替写入校验。

### 10.2 统一锁顺序

关系新增与 Room 删除必须共享协议：

```text
关系新增：Room FOR UPDATE → Customer FOR UPDATE → 复核 Room/Project/RESOURCE WRITE
         → 复核 Customer 单企业数据边界与可引用状态 → INSERT Relation

Room 删除：Room FOR UPDATE → 锁定/检查该 Room 的关系索引范围 → 无有效关系才删除

Customer 停用/删除：Customer FOR UPDATE → 锁定/检查该 Customer 的关系索引范围
                  → 按生命周期规则更新/拒绝；绝不反向锁 Room
```

规则：

- 跨多个 Room/Customer 的批量命令先按父 ID 排序，逐一遵循 Room → Customer 顺序。
- Customer 停用路径不能在持有 Customer 锁后再锁 Room，否则会与关系新增形成反向锁死。
- 所有关系写入口必须遵循协议，父表行锁才可作为“关系插入与父删除/停用”之间的互斥点。
- 关系表索引必须支持 `room_id` 和 `customer_id` 范围检查；现有索引可作为起点，需用 MySQL EXPLAIN 和双连接测试验证。
- TASK-013 仍须重新校验 `pm_room.project_id == relation.project_id`，不得相信请求 projectId。

## 11. 自动化测试矩阵

| 场景 | Service | 真实 MySQL | 真实 HTTP + Sa-Token |
|---|:---:|:---:|:---:|
| 企业 grant 的 USER/ROLE 合并、无 grant 默认拒绝 | ✓ | ✓ | ✓ |
| 项目住户仅返回 CUSTOMER_RELATION 范围 | ✓ | ✓ | ✓ |
| Customer IDOR、跨项目关系泄露 | ✓ |  | ✓ |
| Management ALL 不扩大 Customer/关系范围 | ✓ |  | ✓ |
| 列表/详情/Options 最小字段与脱敏 | ✓ |  | ✓（检查 JSON） |
| PII_VIEW 与 PII_EXPORT 分离 | ✓ |  | ✓（含导出文件内容） |
| 敏感访问审计不含明文 | ✓ | ✓ | ✓ |
| 强身份重复、NULL 身份、不同证件类型 | ✓ | ✓ |  |
| AES 密文长度、MyBatis Entity/VO/批量/游标读写兼容 | ✓ | ✓ |  |
| 加密失败不得落明文（fail-closed） | ✓ | ✓ | ✓ |
| HMAC 与字段密钥不同、fingerprint 可重复计算 | ✓ | ✓ |  |
| 逻辑删除后同身份重建、同时间仅一份有效档案 | ✓ | ✓ | ✓ |
| 两连接并发重复新增最多一条成功 |  | ✓ |  |
| 手机共享、无证件客户并存 | ✓ | ✓ |  |
| 停用 Customer 后禁止新关系 | ✓ | ✓ | ✓ |
| 停用不删除历史关系 | ✓ | ✓ | ✓ |
| 有历史关系时 Customer 删除被拒绝 | ✓ | ✓ | ✓ |
| Room 删除与关系新增两种并发顺序 |  | ✓ |  |
| Customer 停用与关系新增两种并发顺序 |  | ✓ |  |
| 普通角色管理员无法授予 Customer 高权限 | ✓ | ✓ | ✓ |
| 批量操作混入无权 Customer 整体回滚 | ✓ | ✓ | ✓ |

Service 测试可验证规则分支，但不能模拟最终 Scope 结果冒充安全验收；范围、撤销即时生效和 IDOR 必须经过真实认证、Controller、Service、Mapper、MySQL。唯一约束、锁和回滚必须使用 MySQL 8 双连接测试，不能用 H2。

## 12. 分阶段实施清单

### Phase B1：模型与安全基础

1. 固化单企业启动/部署约束，不新增 enterprise_id。
2. 接入两个独立外部 Secret：AES 字段加密密钥和 HMAC-SHA256 身份密钥；增加启动校验、密钥版本配置和恢复操作说明。
3. 对现有 `@EncryptField` 做 Customer 读写兼容与 fail-closed 加固，覆盖 Entity、VO、批量、Cursor 和异常路径。
4. 执行重复有效身份扫描，实施 V2.0.6：密文字段扩容、fingerprint、active generated key、生命周期和 key version；应用侧安全回填历史数据。
5. 实现 CustomerAccessPolicy、独立 Customer Grant、RoleDelegationGuard 敏感权限扩展及敏感访问审计。
6. 实现 Customer Entity/BO/分离 VO/Mapper/Service，先完成脱敏列表与安全详情。

### Phase B2：主档功能

1. 完成新增、修改、状态、删除限制、脱敏/敏感导出；有任意历史关系时删除必须拒绝。
2. 完成企业主档页面，不依赖 CurrentProject。
3. 完成真实 HTTP、MySQL 去重并发、授权撤销及导出验收。

### Phase B3 / TASK-013 前置

1. 提供 `CustomerReferenceService` 和项目候选最小接口。
2. 冻结 Room → Customer → Relation 锁协议。
3. TASK-013 实现关系 CRUD、迁入迁出和历史，不回填 Customer 房屋字段。

## 13. Phase B 开发就绪结论与剩余事项

已定稿：单企业首发、不增加 enterprise_id、有效身份唯一、逻辑删除后允许同身份重建、有历史关系禁止删除、AES 外部密钥、独立 HMAC-SHA256、默认脱敏和独立 PII 权限。上述事项已不再阻碍 Phase B 开发。

**结论：不存在需要继续等待业务决策才能启动 Phase B 的技术问题。** 但以下是 Phase B1 必须首先完成、未完成前不得发布的技术前置项：

1. 当前 `mybatis-encryptor` 默认关闭且默认 BASE64；必须通过外部 Secret 启用 AES，完成 16/24/32 字节密钥启动校验。
2. 当前字段处理器会吞掉字段加解密异常；Customer 敏感写入必须改为 fail-closed 或增加等价的强校验边界，防止明文落库。
3. `id_no VARCHAR(64)` 必须扩容，并用真实 MySQL 验证最大合法输入加密后的长度、唯一键和回填流程。
4. 现有拦截器只有单当前密钥，不支持自动轮换；首期允许，但必须保存 `pii_key_version`、备份密钥并禁止直接替换密钥。密钥丢失将导致证件密文永久不可恢复。
5. HMAC 实现、规范化规则和独立密钥注入需要新增，不能复用 AES 密钥或密文。

仍可在 Phase B 并行确认但不阻断骨架开发的业务细节：企业客户统一社会信用代码的字段模型；姓名是否在部分项目场景也脱敏；敏感导出是否增加审批/水印；注销及历史数据法定保留期限。这些事项应在对应功能进入验收前冻结。
