# TASK-AUTH-016-WH-FIX-5A 仓库新增与编辑权限自洽专项审计

## 1. 审计结论

本次仅进行源码、权限契约和无副作用测试审计，没有修改前后端代码、数据库、角色矩阵、Migration、BusinessContext 或库存状态机。

核心结论：

1. 十个模块的 POST/PUT 提交接口均按各自 add/edit 权限独立保护，add/edit 不会自动获得 remove、confirm、complete 等能力。
2. 十个现有列表页面都会在挂载时请求本模块 list；因此“只授予 add”或“只授予 edit”的用户进入现有页面时，首先会遇到 list 403。页面级操作链均未达到单权限自洽。
3. 所有编辑流程都会先调用 `GET /{id}`，该接口要求 query；因此 edit-only 即使已合法获得目标 ID，也无法初始化编辑表单。
4. 四类单据的 MAINTAIN Context 已正确采用 `add OR edit`，Options 本身不依赖仓库、库区、库位、物料或库存管理权限，属于维护上下文自洽；问题位于页面 list 初始化和编辑详情 query。
5. 基础资料表单仍存在明确的管理 Options 耦合：库区依赖仓库 list；库位依赖仓库和库区 list；物料分类依赖物料 list 及分类 list；物料依赖物料、单位、分类 list；仓库自身公司/项目 Options 也绑定 warehouse list。
6. 服务端资源范围总体完整：公司仓走部门/公司数据范围，项目仓走项目成员范围，库区/库位/物料按真实归属校验，调拨源仓和目标仓分别校验，单据编辑仅允许 DRAFT。

因此当前状态应评为：四类单据“维护 Options 自洽、完整页面部分自洽”；六个基础资料模块“提交安全，但完整页面不自洽”。

## 2. 完整 API 依赖矩阵

表中“页面公共前置”均包含当前页面挂载时自动调用的本模块 list。

| 模块 | 操作 | 最小功能权限 | 页面公共前置 | 表单初始化及辅助 API | 提交 API | 结论 | 修复建议 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| 仓库 | add | `warehouse:warehouse:add` | `GET /warehouse/warehouse/list` → warehouse:list | company-options、project-options，均要求 warehouse:list | `POST /warehouse/warehouse` | 不自洽；同模块 list 耦合 | 建立 Warehouse MAINTAIN Options，按 add/edit 授权并保留公司/项目范围；add 入口不应强制加载 list |
| 仓库 | edit | `warehouse:warehouse:edit` | warehouse:list | `GET /warehouse/warehouse/{id}` → query；company/project Options → list | `PUT /warehouse/warehouse` | 不自洽；额外依赖 list+query | 提供 edit-only 受控详情/维护上下文，或明确 UI 契约为 list+query+edit |
| 库区 | add | `warehouse:zone:add` | `GET /warehouse/zone/list` → zone:list | `GET /warehouse/warehouse/options` → warehouse:list | `POST /warehouse/zone` | 不自洽；跨仓库管理 list | `WAREHOUSE_ZONE_MAINTAIN`，仅返回授权且启用仓库 |
| 库区 | edit | `warehouse:zone:edit` | zone:list | `GET /warehouse/zone/{id}` → zone:query；warehouse/options → warehouse:list | `PUT /warehouse/zone` | 不自洽 | MAINTAIN 上下文 + edit-only 详情；继续校验原仓与目标仓范围 |
| 库位 | add | `warehouse:location:add` | `GET /warehouse/location/list` → location:list | warehouse/options → warehouse:list；zone/options → zone:list | `POST /warehouse/location` | 不自洽；跨两个模块 list | `WAREHOUSE_LOCATION_MAINTAIN`，仓库与库区级联并验证真实归属 |
| 库位 | edit | `warehouse:location:edit` | location:list | `GET /warehouse/location/{id}` → query；warehouse/list；zone/list | `PUT /warehouse/location` | 不自洽 | MAINTAIN 上下文 + edit-only 详情；禁止跨仓库 zoneId |
| 物料分类 | add | `warehouse:materialCategory:add` | `GET /warehouse/materialCategory/list` → category:list | material/company-options → material:list；category/options → category:list | `POST /warehouse/materialCategory` | 不自洽；依赖物料管理 list | `WAREHOUSE_MATERIAL_CATEGORY_MAINTAIN`，提供可见公司和合法父分类 |
| 物料分类 | edit | `warehouse:materialCategory:edit` | category:list | `GET /warehouse/materialCategory/{id}` → query；material:list；category:list | `PUT /warehouse/materialCategory` | 不自洽 | MAINTAIN 上下文 + edit-only 详情；校验同公司父分类、禁止循环 |
| 计量单位 | add | `warehouse:unit:add` | `GET /warehouse/unit/list` → unit:list | 无跨模块 Options | `POST /warehouse/unit` | 部分自洽；表单本身自洽，页面不自洽 | add 模式跳过无权限 list，或明确菜单页面组合契约 |
| 计量单位 | edit | `warehouse:unit:edit` | unit:list | `GET /warehouse/unit/{id}` → unit:query | `PUT /warehouse/unit` | 不自洽；依赖 list+query | edit-only 详情；目标记录需由合法业务入口提供 |
| 物料 | add | `warehouse:material:add` | `GET /warehouse/material/list` → material:list | company-options → material:list；unit/options → unit:list；category/options → category:list | `POST /warehouse/material` | 不自洽；跨单位、分类管理 list | `WAREHOUSE_MATERIAL_MAINTAIN`，提供精简公司/单位/分类引用，不授予其 CRUD |
| 物料 | edit | `warehouse:material:edit` | material:list | `GET /warehouse/material/{id}` → query；material/unit/category list | `PUT /warehouse/material` | 不自洽 | MAINTAIN 上下文 + edit-only 详情；公司、分类、单位状态与归属校验 |
| 入库 | add | `warehouse:inbound:add` | `GET /warehouse/inbound/list` → inbound:list | `/warehouse/inbound/context/*`，INBOUND_MAINTAIN，add OR edit | `POST /warehouse/inbound` | 部分自洽；Options/提交自洽，页面 list 不自洽 | 页面按权限避免无条件 list；保持现有 MAINTAIN |
| 入库 | edit | `warehouse:inbound:edit` | inbound:list | `GET /warehouse/inbound/{id}` → query；现有 MAINTAIN Options | `PUT /warehouse/inbound` | 部分自洽；额外依赖 list+query | edit-only 草稿详情接口或明确组合契约，不改变 confirm/complete |
| 出库 | add | `warehouse:outbound:add` | outbound:list | `/warehouse/outbound/context/*`，OUTBOUND_MAINTAIN，add OR edit | `POST /warehouse/outbound` | 部分自洽；页面 list 不自洽 | 同上；库存候选仍由 MAINTAIN + 范围服务返回 |
| 出库 | edit | `warehouse:outbound:edit` | outbound:list | `GET /warehouse/outbound/{id}` → query；MAINTAIN Options/库存 | `PUT /warehouse/outbound` | 部分自洽；依赖 list+query | edit-only 草稿详情；保留库存不足和范围校验 |
| 调拨 | add | `warehouse:transfer:add` | transfer:list | `/warehouse/transfer/context/*`，TRANSFER_MAINTAIN，add OR edit | `POST /warehouse/transfer` | 部分自洽；页面 list 不自洽 | 保持源/目标仓分别过滤；页面避免无条件 list |
| 调拨 | edit | `warehouse:transfer:edit` | transfer:list | `GET /warehouse/transfer/{id}` → query；双侧 MAINTAIN Options/源库存 | `PUT /warehouse/transfer` | 部分自洽；依赖 list+query | edit-only 草稿详情；源仓、目标仓分别 requireWarehouse |
| 盘点 | add | `warehouse:stocktake:add` | stocktake:list | `/warehouse/stocktake/context/*`，STOCKTAKE_MAINTAIN，add OR edit | `POST /warehouse/stocktake` | 部分自洽；页面 list 不自洽 | 页面避免无条件 list；保留库存快照/范围规则 |
| 盘点 | edit | `warehouse:stocktake:edit` | stocktake:list | `GET /warehouse/stocktake/{id}` → query；MAINTAIN Options/库存 | `PUT /warehouse/stocktake` | 部分自洽；依赖 list+query | edit-only 草稿详情；保留 DRAFT 和账面数量校验 |

## 3. MAINTAIN Context 核查

| Context | 权限组合 | ReferenceType | 前端实际调用 | 结果 |
| --- | --- | --- | --- | --- |
| INBOUND_MAINTAIN | inbound:add OR inbound:edit | WAREHOUSE, ZONE, LOCATION, MEASUREMENT_UNIT, MATERIAL | 仓库、库区、库位、物料 | 匹配 |
| OUTBOUND_MAINTAIN | outbound:add OR outbound:edit | WAREHOUSE, ZONE, LOCATION, MEASUREMENT_UNIT, MATERIAL, INVENTORY | 仓库、库位、物料、可用库存 | 匹配 |
| TRANSFER_MAINTAIN | transfer:add OR transfer:edit | WAREHOUSE, ZONE, LOCATION, MEASUREMENT_UNIT, MATERIAL, INVENTORY | 源/目标仓、双侧库区库位、物料、源库存 | 匹配 |
| STOCKTAKE_MAINTAIN | stocktake:add OR stocktake:edit | WAREHOUSE, ZONE, LOCATION, MEASUREMENT_UNIT, MATERIAL, INVENTORY | 仓库、范围库区/库位、物料、账面库存 | 匹配 |

Controller 和 Policy 均使用 add/edit ANY 语义。拥有 add 的用户可获取制单候选但不能调用 PUT；拥有 edit 的用户可获取编辑候选但不能调用 POST。上下文没有包含 confirm、complete、cancel、remove，也不授予这些操作。

`WarehouseDocumentContextOptionServiceImpl` 对每一种引用类型再次调用 `requireAccess`，随后：

- 仓库来自 `accessibleWarehouseIds()`。
- 库区、库位先 `requireWarehouse(warehouseId)`，并验证库区/库位父子归属。
- 物料按授权仓库真实 companyId 查询，并校验物料公司范围与启用状态。
- 库存查询同时验证仓库、库位、物料及三者真实归属。

因此 MAINTAIN Context 不存在通过前端伪造 warehouseId/locationId 扩大范围的问题。

## 4. add/edit 独立性判断

### 4.1 后端写接口

后端提交接口是独立的：POST 只认 add，PUT 只认 edit。单独 add 不能修改，单独 edit 不能新增；单据 add/edit 也不能确认、完成、取消或删除。

### 4.2 当前前端完整页面

当前前端不支持真正的 add-only/edit-only 页面体验：

- 页面挂载立即请求 list，单独 add/edit 会产生 list 403。
- 编辑按钮位于列表行中；没有 list 时通常无法发现目标记录。
- 即使通过合法外部入口取得目标 ID，编辑初始化仍调用 query 接口。
- 动态菜单是否会在只选择 F 按钮权限时保留父 C 路由，还受现有角色菜单祖先补齐行为影响，需要真实登录态确认；但即使路由出现，上述 API 403 仍确定存在。

不能简单把 edit 等同 query。合理选择是：

1. 产品若要求 edit-only：提供受 edit 保护、仅返回编辑所需字段的 maintain-detail 接口，并设计合法目标 ID 入口；页面无 list 时不得自动加载列表。
2. 产品若认为编辑必须先浏览列表：将页面能力契约明确为 list + query + edit，而不是宣称 edit 单独可用。此方案是契约澄清，不应通过角色名称硬编码。

## 5. 资源范围核查

- 公司仓：`WarehouseAccessService.assertAccess` 调用部门数据范围；仓库新增/修改还执行 `validateCompany`。
- 项目仓：调用 `resolveWarehouseScope()`，要求项目位于有效仓库项目范围；仓库写入调用 `validateProject`。
- 无范围：`applyScope` 使用不可能的仓库 ID 返回空范围，不回退全部仓库。
- SuperAdmin：绕过普通范围过滤，但仍校验资源存在、启用状态、真实归属和单据状态。
- 库区/库位：Service 校验 warehouseId；库位同时校验 zone 属于同一仓库。
- 物料/分类：使用 `accessibleMaterialCompanyIds`、`assertMaterialCompanyAccess`、`validateMaterialCompany`，不能跨公司。
- 调拨：创建、编辑和状态操作分别校验源仓及目标仓；不能用已授权源仓推导目标仓权限。
- 单据状态：四类单据 update 仅允许 DRAFT；confirm 仅 DRAFT；complete 仅 CONFIRMED；CAS 防止状态并发变化。

## 6. 确定缺陷及源码位置

| 严重度 | 缺陷 | 证据位置 |
| --- | --- | --- |
| 高 | 所有 edit-only 流程被 query 详情权限阻断 | 前端十个 `index.vue` 的 handleUpdate/edit/open；各 Controller `GET /{id}` 仅 query |
| 高 | 库位维护依赖 warehouse:list + zone:list | `src/views/warehouse/location/index.vue`；WarehouseController/WarehouseZoneController options |
| 高 | 物料维护依赖 material:list + unit:list + materialCategory:list | `src/views/warehouse/material/index.vue`；三个管理 Options Controller |
| 中 | 库区维护依赖 warehouse:list | `src/views/warehouse/zone/index.vue` |
| 中 | 物料分类维护依赖 material:list，并用 category:list 获取父节点 | `src/views/warehouse/materialCategory/index.vue` |
| 中 | 仓库维护的公司/项目 Options 绑定 warehouse:list | `src/views/warehouse/warehouse/index.vue`、WarehouseController |
| 中 | 所有 add/edit 页面挂载即请求 list | 十个页面的 onMounted/load/getList |
| 低 | 计量单位表单无跨模块耦合，但仍受页面 list 和编辑 query 影响 | `src/views/warehouse/unit/index.vue` |

## 7. 最小修复任务建议

建议拆分，避免一次性扩大权限：

1. **FIX-5B 基础资料 MAINTAIN Context**：新增 Warehouse、Zone、Location、MaterialCategory、Material 的固定维护场景与精简 Options；继续复用 WarehouseAccessService；不新增数据库权限码。
2. **FIX-5C 编辑详情解耦**：为十个模块提供 edit-only 的维护详情读取方式，返回编辑所需字段并执行真实资源范围校验；不把 edit 自动等同普通 query。
3. **FIX-5D 页面操作模式**：页面根据实际权限决定是否加载 list；支持 add-only 的空列表/直接新增模式。edit-only 必须先明确合法目标 ID 的产品入口，否则保持 list+query+edit 的显式组合契约。
4. 为 add-only、edit-only、add+edit、list-only、query-only建立自动化权限组合测试；单据状态写测试继续使用事务回滚或隔离数据。

## 8. SQL / Migration 判断

上述缺陷是前后端权限契约和 Options API 设计问题，不需要数据库 Schema 或业务 Migration，也不应修改七角色默认权限矩阵。

如果未来必须新增菜单入口而现有父菜单无法承载 add-only 页面，才可能需要独立菜单增量 SQL；当前阶段没有证据支持立即新增菜单，需先完成真实登录态与产品入口确认。

## 9. 自动化与真实浏览器验证

| 验证项 | 结果 |
| --- | --- |
| PermissionContractAuditTest | 通过，6/6 |
| 仓库模块测试 | 通过，127/127，0 failure，0 error，0 skipped |
| 真实库存写操作 | 未执行 |
| 数据库写入 | 未执行 |
| 真实 add-only/edit-only 浏览器登录 | 未执行，不能宣称通过 |

自动化测试证明现有 Controller 权限、Context 和资源范围基线未回归，但没有覆盖浏览器动态菜单祖先、页面挂载 403 的最终用户体验。后者应在后续修复前后用临时动态授权角色验证。

