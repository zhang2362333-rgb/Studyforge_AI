# Staging Docker 自动审查与部署

本仓库使用 `staging` 作为测试集成分支：

```text
feature/* -> pull request -> staging -> 自动审查 -> 自动部署 staging
staging -> main -> 稳定版本
```

当前 staging 部署方式是 Docker Compose。服务器只需要 Docker、Docker Compose plugin 和 SSH，不需要手动安装 Java、Tomcat、Nginx、Node.js、Maven 或 MySQL。

## 部署结构

GitHub Actions 会构建并推送三个 GHCR 镜像：

```text
ghcr.io/niit-workshop-of-shzu/studyforge-ai-api:staging
ghcr.io/niit-workshop-of-shzu/studyforge-ai-web:staging
ghcr.io/niit-workshop-of-shzu/studyforge-ai-migrate:staging
```

服务器使用 `deploy/docker/docker-compose.staging.yml` 启动：

```text
mysql    GHCR 中的 MySQL 8.0 镜像，使用 Docker volume 持久化
migrate  一次性数据库结构迁移，不导入 seed，不清业务数据
api      Tomcat 10.1 + studyforge-webapi.war
web      Nginx + 用户端 dist + 管理端 dist + /api/v1 反向代理
```

默认宿主机端口：

```text
用户端: http://服务器IP:7897/
管理端: http://服务器IP:7897/portal/
API:    http://服务器IP:7897/api/v1/health
```

服务器 IP 作为 SSH host 时只写 IP，例如：

```text
39.106.101.137
```

不要在 GitHub Secret `STAGING_DEPLOY_HOST` 里写 `https://39.106.101.137`。`https://` 只属于浏览器 URL，不属于 SSH 主机名。

## GitHub Secrets

在 GitHub 仓库中创建 Environment：

```text
Settings -> Environments -> New environment -> staging
```

然后在 `staging` Environment 里配置：

```text
STAGING_DEPLOY_HOST       Staging 服务器 IP 或域名，例如 39.106.101.137
STAGING_DEPLOY_PORT       SSH 端口，例如 22
STAGING_DEPLOY_USER       SSH 用户，例如 deploy
STAGING_DEPLOY_SSH_KEY    SSH 私钥内容
STAGING_KNOWN_HOSTS       ssh-keyscan 得到的 known_hosts 内容
```

GitHub Actions 会用本次 workflow 的临时 `GITHUB_TOKEN` 推送和拉取 GHCR 镜像，不需要额外配置 GHCR token。

如果这些 Secrets 尚未配置，workflow 会完成自动审查和镜像推送，但跳过真实部署。

## 服务器默认路径

Docker 部署脚本默认使用：

```text
/opt/studyforge-staging
/opt/studyforge-staging/docker-compose.yml
/opt/studyforge-staging/.env
```

服务器上的 `.env` 不会被 GitHub Actions 覆盖。第一次部署时如果 `.env` 不存在，部署脚本会自动从 `.env.example` 创建。

示例内容见：

```text
deploy/docker/.env.staging.example
```

关键配置：

```bash
WEB_PORT=7897

MYSQL_ROOT_PASSWORD=123456
MYSQL_DATABASE=studyforge_ai
MYSQL_USER=studyforge
MYSQL_PASSWORD=123456
MYSQL_IMAGE=ghcr.io/niit-workshop-of-shzu/studyforge-ai-mysql:staging
```

当前 staging 模板按你的要求使用 `123456`。这只适合作为临时 staging 密码；MySQL 容器第一次初始化后，修改这两个值不会自动重置已有 Docker volume 里的数据库密码。

## 数据库迁移

`migrate` 容器复用仓库里的：

```text
sql/
scripts/import_local_db.sh
```

每次部署会执行：

```bash
docker compose run --rm migrate
```

这会导入 `001_schema.sql` 并执行 `003_*.sql`、`004_*.sql` 这类非破坏性迁移。它不会执行 `002_seed_data.sql`，不会清空业务数据。

## 分支保护建议

建议给 `staging` 分支开启：

```text
Require a pull request before merging
Require status checks to pass before merging
Require branches to be up to date before merging
```

必需检查选择：

```text
Automated Review
SQL MySQL 8 Import
```

## 重要安全说明

真实 API Key 不应提交到仓库。仓库只保留 `api配置.example.md`，本地真实配置请放在被 `.gitignore` 忽略的 `api配置.md` 中。
