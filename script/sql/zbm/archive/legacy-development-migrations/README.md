# 历史开发迁移归档

本目录保存 TASK-V1-005-C 前通过手工方式执行的 37 个开发迁移 SQL，以及
`V2.0.6__customer_security_backfill.md` 配套说明。

- 文件内容和名称保持原样。
- 本目录不在 Spring Boot Flyway locations 中。
- 这些脚本不能与新的 `V1__initial_schema.sql` 混合执行。
- `script/audit/Get-ZbmMigrationManifest.ps1` 用于重复生成 SHA-256 清单。
- 原开发库没有 `flyway_schema_history`，不得补写虚假执行记录。
