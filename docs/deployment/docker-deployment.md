# Docker 部署与发布说明

## 1. 部署边界

本部署只管理两个容器：

- `backend`：Java 21 JRE 容器，挂载本地构建的 `ruoyi-admin.jar`。
- `frontend`：Nginx 容器，挂载本地构建的 `dist`。

MySQL 8.4 和 Redis 由现有 Compose 独立维护。本部署不创建、停止、重建或删除它们，只通过外部 Docker 网络访问。

## 2. 已核对的工程参数

| 项目 | 实际值 |
|---|---|
| JDK | 21 |
| 后端容器端口 | 8080 |
| Spring Profile | `prod` |
| 前端 API 前缀 | `/prod-api` |
| 消息路径 | `/resource/message`，默认 SSE，支持 WebSocket 升级 |
| 上传限制 | 前端代理 20MB，与 Spring `max-request-size` 一致 |
| Flyway 目录 | `classpath:db/migration` |

## 3. 服务器目录

```text
/opt/zbm/
├── .env
├── docker-compose.yml
├── backend/Dockerfile
├── nginx/conf/nginx.conf
├── bin/*.sh
├── backend/
│   ├── incoming/<version>/
│   ├── releases/<version>/ruoyi-admin.jar
│   └── current -> releases/<version>
└── frontend/
    ├── incoming/<version>/
    ├── releases/<version>/index.html
    └── current -> releases/<version>
```

将仓库 `script/docker/` 的 Compose、Nginx 配置、`.env.example` 和 `bin/` 上传到 `/opt/zbm/`。复制 `.env.example` 为 `.env`，仅在服务器填写真实凭据；`.env` 不得提交。

### 后端环境变量

| 环境变量 | Spring 属性/用途 | prod 要求 |
|---|---|---|
| `ZBM_DB_URL` | `spring.datasource.dynamic.datasource.master.url` | 必填，填写容器 DNS 与目标库；Flyway 复用该数据源 |
| `ZBM_DB_USERNAME` / `ZBM_DB_PASSWORD` | master 数据源账号、密码 | 必填 |
| `ZBM_REDIS_HOST` / `ZBM_REDIS_PORT` | `spring.data.redis.host/port` | 必填，由 Compose 从现有容器参数组装传入 |
| `ZBM_REDIS_DATABASE` / `ZBM_REDIS_PASSWORD` | Redis DB 与密码 | 必填 |
| `ZBM_FLYWAY_ENABLED` | `spring.flyway.enabled` | 必须显式选择；仅空库或已纳管库使用 `true` |
| `ZBM_JWT_SECRET_KEY` | `sa-token.jwt-secret-key` | 必填，使用长随机值 |
| `ZBM_MULTIPART_LOCATION` | `spring.servlet.multipart.location` | 默认 `/app/temp`，容器非 root 用户可写 |
| `ZBM_CUSTOMER_SECURITY_ENABLED` | 客户敏感数据安全总开关 | 显式配置 |
| `ZBM_FIELD_ENCRYPT_ENABLED` | MyBatis 字段加密开关 | 显式配置 |
| `ZBM_CUSTOMER_FIELD_ENCRYPT_KEY` | 既有客户字段加密密钥 | 启用加密时必须提供，禁止擅自轮换 |
| `ZBM_CUSTOMER_IDENTITY_HMAC_KEY` | 客户身份指纹 HMAC 密钥 | 启用客户安全时必须提供 |
| `ZBM_CUSTOMER_PII_KEY_VERSION` | 客户敏感数据密钥版本 | 启用客户安全时必须提供 |
| `ZBM_PUBLIC_URL` | 第三方登录回调的外部前端地址 | 使用第三方登录时配置 |

prod 不再提供 `localhost:3306/ry-vue`、`root/root` 或 Redis `localhost` 回退值。必要变量缺失时 JAR 会启动失败；这属于预期的 fail-fast 行为。

## 4. 外部网络与服务确认

先从现有 MySQL/Redis Compose 获取真实网络和容器 DNS 名称：

```bash
docker network ls
docker network inspect <network>
docker inspect -f '{{range $name, $conf := .NetworkSettings.Networks}}{{$name}} {{end}}' <mysql-container>
docker inspect -f '{{range $name, $conf := .NetworkSettings.Networks}}{{$name}} {{end}}' <redis-container>
```

把网络填入 `DOCKER_EXTERNAL_NETWORK`，把容器 DNS 名填入 `ZBM_DB_HOST`、`ZBM_REDIS_HOST`。端口使用容器内部端口，通常为 MySQL `3306`、Redis `6379`，不能填写 `localhost`。

## 5. 本地构建发布物

后端：

```bash
./mvnw -Pprod -pl ruoyi-admin -am clean package
sha256sum ruoyi-admin/target/ruoyi-admin.jar > ruoyi-admin.jar.sha256
```

上传 `ruoyi-admin.jar` 与 `ruoyi-admin.jar.sha256` 到 `/opt/zbm/backend/incoming/<version>/`。

前端在 `zbm-platform-ui` 构建：

```bash
pnpm install --frozen-lockfile
pnpm typecheck
pnpm lint
pnpm test
pnpm build:prod
tar -C dist -czf frontend-dist.tar.gz .
sha256sum frontend-dist.tar.gz > frontend-dist.tar.gz.sha256
```

上传两个前端文件到 `/opt/zbm/frontend/incoming/<version>/`。

## 6. 首次启动

```bash
cd /opt/zbm
cp .env.example .env
chmod 600 .env
chmod +x bin/*.sh
docker compose --env-file .env config
docker compose build backend
```

先发布后端和前端产物，再启动或重建对应容器：

```bash
./bin/release-backend.sh <version>
./bin/release-frontend.sh <version>
docker compose ps
```

脚本会先校验 SHA-256，把文件复制/解压到临时版本目录，校验结构后再原子切换 `current` 软链接。Backend 挂载单个 JAR 文件，切换后使用 `--force-recreate` 重新解析 bind mount；Frontend 挂载稳定父目录 `/opt/zbm/frontend`，Nginx 每次请求解析容器内 `current`，因此切换版本不重建容器。首次发布 Frontend 时脚本会启动尚不存在的容器。不会覆盖正在使用的发布目录。

## 7. 独立发布与回滚

```bash
cd /opt/zbm && ./bin/release-backend.sh 20261016-01
cd /opt/zbm && ./bin/release-frontend.sh 20261016-01
cd /opt/zbm && ./bin/rollback-backend.sh 20261015-02
cd /opt/zbm && ./bin/rollback-frontend.sh 20261015-02
```

Backend 回滚会切换软链接并强制重建 Backend 容器；Frontend 回滚仅切换软链接并执行 `nginx -t`，运行中的 Nginx 无需重建。两者都不删除版本目录或数据库数据。

后端宿主机端口只绑定 `127.0.0.1:${BACKEND_PORT}`，不能从服务器外部直接访问；Nginx 仍通过外部 Docker 网络中的 `backend:8080` 代理请求。

## 8. Flyway 安全策略

- `ZBM_FLYWAY_ENABLED=true` 仅用于全新空库或已由 Flyway 纳管的数据库。
- 全新空库首次启动执行不可变的 V1；后续启动先校验再执行新增的 V2、V3……迁移。
- `validate-on-migrate=true`、`baseline-on-migrate=false`、`clean-disabled=true`。
- 旧的非空未纳管库（例如 `zbm-data`）禁止直接开启 Flyway，不能用自动 baseline 绕过核对。
- Flyway 失败会使应用启动失败，不应通过关闭校验掩盖问题。
- 不单独设置 `spring.flyway.url/user/password`；Spring Boot Flyway 复用业务 master 数据源，避免业务与迁移指向不同数据库。

## 9. 验证

```bash
curl -f http://127.0.0.1:8080/actuator/health
curl -f http://127.0.0.1:8088/healthz
curl -I http://127.0.0.1:8088/
curl -I http://127.0.0.1:8088/prod-api/captchaImage
docker compose logs --tail=100 backend frontend
docker compose restart backend frontend
docker compose ps
```

Nginx 对 SPA 路由回退到 `index.html`；`/assets/` 使用 immutable 缓存；HTML 禁止长期缓存；`/prod-api/` 支持普通 API、文件上传、SSE 和 WebSocket；`/prod-api/actuator` 被公网代理明确拒绝。

## 10. 停止边界

`docker compose down` 只停止本部署的前后端容器。禁止附带 `-v`，也不要对现有 MySQL/Redis Compose 执行任何停止、删除或重建命令。
