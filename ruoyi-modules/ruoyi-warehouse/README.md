# ruoyi-warehouse

独立仓库管理业务模块，负责仓库、物料、库存及出入库等领域能力。

- Java 根包：`org.dromara.warehouse`
- 数据库表前缀：`wm_`
- 依赖方向：`ruoyi-warehouse -> ruoyi-system`
- 禁止依赖 `ruoyi-property` 或 `ruoyi-hr`，避免业务模块间循环依赖
- 仓库是独立业务域；一个组织或项目可对应零到多个仓库，不强制存在公司总仓

Phase 1 范围固定为：仓库/库区/库位、物料分类/计量单位/物料、库存余额、库存流水、入库、出库、调拨、盘点、库存查询和数据权限。具体业务包、接口和表结构在对应后续任务中按需创建。

数据库演进继续使用平台级 `script/sql/zbm/baseline` 与 `script/sql/zbm/migration`，不建立 Warehouse 独立 baseline 或 migration 序列。
