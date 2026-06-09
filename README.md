# StudyForge AI / 智能学习与知识社区平台

StudyForge AI is a full-stack learning platform that combines a knowledge community, AI-assisted reading and writing tools, voice features, and an admin console.

StudyForge AI 是一个全栈学习平台，融合了知识社区、AI 辅助阅读与写作、语音能力和后台管理控制台。

## Overview / 项目简介

StudyForge AI helps learners publish notes, browse knowledge feeds, ask questions, collect useful content, manage learning relationships, and use AI to summarize, format, quiz, answer, and generate cover images for study materials.

StudyForge AI 面向学习者的知识沉淀与协作场景，支持发布笔记、浏览知识流、提问互助、收藏内容、维护学习社交关系，并通过 AI 完成摘要、排版、复习卡片、问答和文章封面生成。

## Features / 功能亮点

- User knowledge platform: posts, categories, comments, likes, favorites, profiles, friends, notifications, and study activities.
- 用户端知识平台：文章、分类、评论、点赞、收藏、个人主页、好友、通知和学习动态。
- AI study tools: summary generation, quiz/review cards, article Q&A, AI formatting, and cover-image generation.
- AI 学习工具：摘要生成、复习卡片、文章问答、AI 排版和封面生图。
- Voice capability: text-to-speech and voice record support.
- 语音能力：文本转语音与语音记录支持。
- Skill and roadmap modules: skill library, roadmap details, and user progress tracking.
- 技能与路线模块：技能库、学习路线详情和用户进度跟踪。
- Admin console: community moderation, post management, reports, user state, integration settings, and health checks.
- 管理后台：社区审核、帖子管理、举报处理、账号状态、集成配置和健康检查。
- Staging deployment: GitHub Actions builds Docker images, pushes to GHCR, and deploys with Docker Compose.
- 预发部署：GitHub Actions 构建 Docker 镜像、推送到 GHCR，并通过 Docker Compose 部署。

## Architecture / 架构

```text
Vue 3 + Vite frontend
    -> Axios API client
Spring MVC JSON API
    -> Service layer
    -> MyBatis mapper
    -> MySQL 8 / MariaDB
```

```text
Vue 3 + Vite 前端
    -> Axios API 客户端
Spring MVC JSON 接口
    -> Service 业务层
    -> MyBatis Mapper
    -> MySQL 8 / MariaDB
```

## Tech Stack / 技术栈

- Frontend / 前端：Vue 3, Vite, TypeScript, Pinia, Vue Router, Axios, markdown-it, DOMPurify, Lucide Vue
- Backend / 后端：Java 17, Spring MVC, MyBatis, HikariCP, Maven, Tomcat
- Database / 数据库：MySQL 8 compatible SQL, MariaDB-compatible import scripts
- Deployment / 部署：Docker, Docker Compose, Nginx, GitHub Actions, GHCR

## Repository Layout / 目录结构

```text
StudyForge_AI/
├── .github/workflows/        # CI/CD workflows / CI/CD 工作流
├── deploy/                   # Docker, Nginx, and systemd examples / 部署配置示例
├── docs/                     # Project and deployment documents / 项目与部署文档
├── scripts/                  # Local, database, and deployment scripts / 本地、数据库与部署脚本
├── sql/                      # Schema, seed data, and migrations / 表结构、种子数据与迁移
├── studyforge-frontend/      # Vue workspace / Vue 前端工作区
├── studyforge-server/        # Maven multi-module backend / Maven 多模块后端
└── README.md
```

## Backend Modules / 后端模块

```text
studyforge-server/
├── studyforge-common
├── studyforge-framework
├── studyforge-system
├── studyforge-content
├── studyforge-interaction
├── studyforge-ai
├── studyforge-voice
├── studyforge-help
├── studyforge-admin
└── studyforge-webapi
```

## Requirements / 环境要求

- JDK 17+
- Maven 3.9+
- Node.js 20.19+
- npm 10+
- MySQL 8 or MariaDB with utf8mb4 support
- Docker and Docker Compose for container deployment

- JDK 17+
- Maven 3.9+
- Node.js 20.19+
- npm 10+
- 支持 utf8mb4 的 MySQL 8 或 MariaDB
- 如需容器部署，需要 Docker 与 Docker Compose

## Configuration / 配置

Do not commit real credentials. Use example files as templates and keep local secrets outside Git.

不要提交真实密钥或本机配置。请以 example 文件为模板，并将本地密钥保留在 Git 之外。

Backend JDBC example:

后端 JDBC 配置模板：

```text
studyforge-server/studyforge-webapi/src/main/resources/jdbc.properties.example
```

For local development, copy it to `jdbc.properties` and update the values locally. `jdbc.properties` is ignored by Git.

本地开发时可复制为 `jdbc.properties` 并在本机修改配置。`jdbc.properties` 已被 Git 忽略。

Frontend production environment examples:

前端生产环境变量模板：

```text
studyforge-frontend/apps/knowledge-web/.env.production.example
studyforge-frontend/apps/portal-web/.env.production.example
```

AI, voice, and image provider keys are managed through integration settings. Seed data keeps these secret values empty by default.

AI、语音和生图服务密钥通过集成设置维护。种子数据默认不会写入真实密钥。

## Database / 数据库

Create the database and import the schema:

创建数据库并导入表结构：

```bash
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS studyforge_ai CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
mysql -u root -p studyforge_ai < sql/001_schema.sql
```

Import demo seed data only when you intentionally want to reset local business data:

只有在明确希望重置本地业务数据时，才导入演示数据：

```bash
mysql -u root -p studyforge_ai < sql/002_seed_data.sql
```

The helper script applies schema changes and migrations while preserving user-created content by default:

辅助脚本默认会导入表结构并应用迁移，同时保留用户创建的数据：

```bash
DB_CLIENT=mysql DB_NAME=studyforge_ai DB_USER=studyforge DB_PASSWORD=your-password ./scripts/import_local_db.sh
```

Reset to the demo dataset:

重置为演示数据集：

```bash
RESET_SEED=1 DB_CLIENT=mysql DB_NAME=studyforge_ai DB_USER=studyforge DB_PASSWORD=your-password ./scripts/import_local_db.sh
```

Seeded demo accounts:

演示账号：

```text
User / 用户：  chen_jiayi / StudyForge@2026
Admin / 管理： ops_admin  / AdminForge@2026
```

## Build / 构建

Backend:

后端：

```bash
cd studyforge-server
mvn -DskipTests package
```

The backend WAR is generated at:

后端 WAR 产物位置：

```text
studyforge-server/studyforge-webapi/target/studyforge-webapi-1.0.0-SNAPSHOT.war
```

Frontend:

前端：

```bash
cd studyforge-frontend
npm ci
npm run typecheck
npm run build
```

Build individual apps:

单独构建应用：

```bash
npm run build:knowledge
npm run build:portal
```

## Run Locally / 本地运行

Start the backend API:

启动后端 API：

```bash
./scripts/start_api_maven.sh
```

Stop the backend API:

停止后端 API：

```bash
./scripts/stop_api_maven.sh
```

Health check:

健康检查：

```bash
curl http://localhost:8080/api/v1/health
```

Start the user knowledge platform:

启动用户端知识平台：

```bash
./scripts/start_knowledge_web.sh
```

```text
http://localhost:5174
```

Start the admin console:

启动管理后台：

```bash
./scripts/start_frontend.sh
```

```text
http://localhost:5173
```

## Deployment / 部署

Docker-based deployment docs:

Docker 部署文档：

```text
docs/server-deployment.md
docs/staging-deployment.md
```

Staging builds and deploys through GitHub Actions:

预发环境通过 GitHub Actions 构建与部署：

```text
.github/workflows/staging.yml
```

Default staging web port:

默认预发 Web 端口：

```text
7897
```

Main deployment files:

主要部署文件：

```text
deploy/docker/api.Dockerfile
deploy/docker/web.Dockerfile
deploy/docker/migrate.Dockerfile
deploy/docker/mysql.Dockerfile
deploy/docker/docker-compose.staging.yml
deploy/docker/nginx.conf
scripts/bootstrap_staging_docker_alinux.sh
scripts/deploy_staging_docker.sh
```

## Security Notes / 安全说明

- Never commit real API keys, database passwords, `.env` files, or local `jdbc.properties`.
- 不要提交真实 API Key、数据库密码、`.env` 文件或本机 `jdbc.properties`。
- Use `api配置.example.md`, `.env*.example`, and `jdbc.properties.example` as templates only.
- `api配置.example.md`、`.env*.example` 和 `jdbc.properties.example` 仅作为模板使用。
- Rotate any key that has ever appeared in public Git history.
- 任何曾经出现在公开 Git 历史里的密钥都应轮换。

## Useful Commands / 常用命令

```bash
# Frontend typecheck / 前端类型检查
cd studyforge-frontend && npm run typecheck

# Frontend build / 前端构建
cd studyforge-frontend && npm run build

# Backend build / 后端构建
cd studyforge-server && mvn -DskipTests package

# Import database without resetting user data / 导入数据库且不重置用户数据
./scripts/import_local_db.sh

# Reset demo data / 重置演示数据
RESET_SEED=1 ./scripts/import_local_db.sh
```
