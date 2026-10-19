# Flyway 数据库迁移

本目录不再保存手工开发迁移。Spring Boot 仅扫描
`ruoyi-admin/src/main/resources/db/migration/`，当前唯一初始迁移为
`V1__initial_schema.sql`。

原 37 个 V2.0.1～V2.0.36（含 V2.0.33.1）脚本已经原样归档到
`../archive/legacy-development-migrations/`，只用于历史审计，不参与 Flyway 扫描。

后续正式变更从 `V2__description.sql` 开始，只允许新增，不得修改已经执行的版本。

安全默认值：

- `ZBM_FLYWAY_ENABLED` 默认 `false`，保护现有非空 `zbm-data`。
- 新空库必须显式设置为 `true`。
- `baselineOnMigrate=false`、`cleanDisabled=true`、`validateOnMigrate=true`。
- 禁止把 V1 对现有非空库执行，也禁止伪造 `flyway_schema_history`。
