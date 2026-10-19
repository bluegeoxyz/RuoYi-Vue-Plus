# Flyway 使用与版本管理规范

应用通过 `spring-boot-starter-flyway` 和 `flyway-mysql` 接入 Flyway 12.4.0。扫描位置固定为 `classpath:db/migration`。

- 默认 `ZBM_FLYWAY_ENABLED=false`，因此现有非空 `zbm-data` 启动不会创建历史表或执行 V1。
- 新空库显式设置 `ZBM_FLYWAY_ENABLED=true`。
- `validate-on-migrate=true`、`baseline-on-migrate=false`、`clean-disabled=true`、`fail-on-missing-locations=true`。
- 不允许修改已发布迁移、不允许伪造历史、不允许 baseline 与归档 V2 脚本混跑。
- 现有非空库只有在阶段 D 完成备份、差异复核和人工授权后，才能制定受控接管方式；禁止用 `baselineOnMigrate=true` 跳过核对。
- 迁移失败必须修复新迁移或环境，禁止删历史行、改校验值或关闭 validate 掩盖问题。

