# StudyForge AI 当前实现状态与后续跟进记录

记录时间：2026-05-24

## 1. 当前技术栈约束

项目后续继续严格按以下链路开发：

```text
Vue 前端
    ↓ Axios / Fetch / Ajax
Spring MVC Controller 返回 JSON
    ↓
Service
    ↓
MyBatis Mapper
    ↓
本机 MySQL / MariaDB
```

当前要求：

- 前端固定使用 `Vue 3`，不再引入第二套前端框架。
- 后端固定使用 `Maven` 多模块工程。
- 数据库使用本机数据库。
- Word 文档只作为功能设计参考，不作为部署方式约束。
- 后端 Controller 只返回 JSON，不做页面渲染。

## 2. 当前本地运行状态

当前可用本地服务：

```text
用户侧知识平台 Vue: http://localhost:5174
控制台 Vue:         http://localhost:5173
后端 API:          http://localhost:8080/api/v1/health
本机数据库:         test_studyforge_ai_v2
数据库账号:         lynn，无密码
```

常用启动命令：

```bash
./scripts/start_api_maven.sh
./scripts/start_knowledge_web.sh
./scripts/start_frontend.sh
```

常用停止命令：

```bash
./scripts/stop_api_maven.sh
./scripts/stop_knowledge_web.sh
./scripts/stop_frontend.sh
```

数据库重新导入：

```bash
./scripts/import_local_db.sh
```

后端构建：

```bash
./scripts/build_with_proxy.sh
```

前端验证：

```bash
cd studyforge-frontend
npm run typecheck
npm run build:knowledge
```

## 3. 已经实现并验证的内容

### 3.1 架构闭环

已经跑通完整请求链路：

```text
knowledge-web Vue
    ↓ Axios / Vite proxy
studyforge-webapi Spring MVC Controller
    ↓
studyforge-content Service
    ↓
MyBatis Mapper XML
    ↓
test_studyforge_ai_v2
```

已验证接口：

```text
GET  /api/v1/health
POST /api/v1/auth/login
POST /api/v1/auth/register
POST /api/v1/auth/logout
GET  /api/v1/posts/trending
GET  /api/v1/posts/{postId}
```

已验证用户侧代理接口：

```bash
curl 'http://localhost:5174/api/v1/posts/trending?languageCode=zh_CN&limit=12'
curl 'http://localhost:5174/api/v1/posts/1?languageCode=zh_CN'
```

### 3.2 用户侧知识平台

目录：

```text
studyforge-frontend/apps/knowledge-web
```

已实现页面：

- `/`：双语产品首页，展示主要功能、架构链路和跳转入口
- `/knowledge`：知识流首页
- `/posts/:postId`：内容详情页
- `/library`：我的学习页
- `/login`：登录页

已实现前端能力：

- Vue 3 + Vite + TypeScript
- Vue Router 路由
- Pinia 状态管理
- Axios 请求封装
- 登录 session 本地持久化
- 顶部导航
- 用户侧布局
- 知识分类栏
- 当前知识流本地搜索
- 语言切换状态
- 知识卡片展示
- 内容详情展示
- 加载态、空状态、错误态

### 3.3 后端内容查询

内容查询已接入真实数据库查询。

涉及模块：

```text
studyforge-webapi
studyforge-content
```

已实现能力：

- 热门内容列表查询
- 内容详情查询
- 按 `languageCode` 查询 `post_i18n`
- 指定语言不存在时按原始语言和已有内容兜底
- 查询内容所属分类编码
- `ApiResponse` JSON 统一返回结构

### 3.4 本机数据库

当前数据库：

```text
test_studyforge_ai_v2
```

当前主要表：

```text
users
user_tokens
categories
category_i18n
posts
post_i18n
uploaded_files
comments
post_likes
post_favorites
post_view_history
reports
ai_logs
voice_records
help_requests
help_answers
admin_audit_logs
```

当前种子数据：

```text
users:        7
categories:   5
category_i18n: 10
posts:        8
post_i18n:    8
```

当前可用种子账号：

```text
用户账号: chen_jiayi / StudyForge@2026
管理账号: ops_admin  / AdminForge@2026
```

当前登录已接入数据库账号、密码哈希和 `user_tokens`。

### 3.5 已通过验证

最近一次验证通过：

```text
Maven 多模块构建:      通过
前端 typecheck:       通过
knowledge-web build:  通过
健康检查接口:          通过
知识流接口:            通过
详情接口:              通过
登录接口:              数据库账号登录可用
```

### 3.6 前端 UI 与动效优化

2026-05-24 已对当前前端做一轮视觉优化。

2026-05-24 追加首页与视觉优化：

- 用户侧 `/` 已改为双语产品首页，支持中文 / English 文案自动切换。
- 原知识流入口调整为 `/knowledge`，顶部导航同步新增“首页 / Home”和“知识流 / Knowledge”。
- 首页新增主要功能展示区，功能卡片可跳转到 `/knowledge`、`/library` 和本地控制台 `http://localhost:5173`。
- 首页视觉参考技术社区产品习惯：高信息密度、标签优先、克制中性色、有限强调色、清晰架构信号。
- 首页接入已有 SVG 视觉资产，用于知识流、我的学习、AI 学习卡片、求助讨论的功能展示。
- 首页新增架构展示区，继续明确 `Vue / Axios / Spring MVC JSON / MyBatis / MySQL` 链路。
- 页面级动效已调整为不隐藏首帧内容，避免出现短暂白屏感。
- 已验证 `/`、`/knowledge`、`/library` 在 `http://localhost:5174` 返回 `200 OK`。
- 已接入用户上传的 `logo.png`，裁切为前端品牌图标 `studyforge-logo-mark.png`，用于用户侧顶部、控制台侧栏和控制台登录页。

用户侧 `knowledge-web` 已优化：

- 全局字体栈，补充中文字体优先级。
- 全局背景、边框、阴影、按钮、输入框和焦点态。
- 首页 hero 区域层次、指标展示和轻量动效。
- 知识卡片入场动画、hover 动效、分类色条和图片缩放反馈。
- 加载态 shimmer 效果。
- 空状态和错误状态视觉层次。
- 移动端布局重排。
- `prefers-reduced-motion` 动效降级。
- 新增多张 SVG 视觉资产：
  - `topic-ai.svg`
  - `topic-help.svg`
  - `topic-writing.svg`
  - 优化 `topic-learning.svg`
  - 优化 `topic-systems.svg`

控制台 `portal-web` 已优化：

- 字体栈和背景风格与用户侧统一。
- 侧栏、顶部栏、按钮、卡片、列表项、加载态增加一致视觉反馈。
- 登录页标题不再使用 viewport 宽度缩放字体。
- 页面入场动画、卡片入场动画和 reduced-motion 降级。

本次 UI 优化不改业务接口、不改后端数据结构。

## 4. 当前剩余增强项

当前主要用户侧链路已经接入真实数据库和真实接口。后续继续实现时，建议优先处理这些增强项：

1. 内容编辑、删除、草稿箱和分页查询。
2. 图片管理页，让用户查看、复用和清理自己上传过的图片。
3. 管理端操作审计列表、审核规则配置和批量处理。
4. AI 摘要缓存与重复生成提示，降低同一文章重复调用模型的次数。
5. 语音识别 STT 与语音搜索。
6. 分类从后端接口读取，替代前端固定分类数组。
7. 密码哈希升级为 BCrypt 或 Argon2，并补迁移策略。
8. 知识流后端分页或游标加载，支持更长内容列表。

## 5. 当前真实数据来源

本地数据由 `sql/002_seed_data.sql` 导入，导入时会重置旧的本地演示数据，并写入一套完整学习社区数据：

- 7 个数据库账号，其中 6 个普通用户和 1 个管理员。
- 8 篇完整 Markdown 原文文章，覆盖中文和英文。
- 文章作者、分类、原始语言、评论、收藏、阅读记录和求助讨论互相关联。
- AI 与语音配置写入 `integration_settings`，由管理员账号维护。

当前种子账号：

```text
用户账号: chen_jiayi / StudyForge@2026
管理账号: ops_admin  / AdminForge@2026
```

## 6. 下一次继续时优先处理项

推荐下一次优先处理内容编辑和数据库草稿箱，原因：

- 当前发布页已有 Markdown 编辑器、本地草稿和图片上传。
- 后端已经有 `posts`、`post_i18n`、`uploaded_files`。
- 补编辑、删除和草稿箱后，知识平台会从“能发布”进一步变成“能长期写作和维护”。

预期改动范围：

```text
studyforge-content
studyforge-webapi
studyforge-frontend/apps/knowledge-web
sql/001_schema.sql
sql/002_seed_data.sql
```

## 7. 2026-05-24 第二轮功能开发完成记录

本轮继续严格保持以下链路：

```text
Vue 前端
    ↓ Axios
Spring MVC Controller 返回 JSON
    ↓
Service
    ↓
MyBatis Mapper
    ↓
本机 MySQL / MariaDB
```

### 7.1 后端已补齐

认证与权限：

- `AuthServiceImpl` 已接入数据库登录。
- 支持 `users` 用户查询、注册写入、`user_tokens` token 写入与退出失效。
- 用户账号 `chen_jiayi / StudyForge@2026` 与管理员账号 `ops_admin / AdminForge@2026` 可登录。
- 管理员接口由 Controller 调用 `AuthService.requireAdmin` 做真实角色校验。

内容发布：

- 新增 `POST /api/v1/posts`。
- 发布文章会写入 `posts` 与 `post_i18n`。
- 用户发帖只保存用户选择的原始语言，不自动生成另一种语言版本。
- 查询详情时如果请求语言没有内容，会回退到文章原始语言，并返回真实 `languageCode`。

互动与学习记录：

- 新增点赞、收藏、评论、浏览记录接口。
- `post_likes`、`post_favorites`、`comments`、`post_view_history` 已接入 MyBatis Mapper。
- 点赞、收藏、评论、阅读会同步更新 `posts` 计数字段和热度。
- `/api/v1/posts/me/favorites` 与 `/api/v1/posts/me/history` 已返回真实数据。

AI：

- 新增真实 `AiService` 实现，按 `api配置.md` 接入 SiliconFlow OpenAI-compatible `/chat/completions`。
- 当前文本模型默认：`deepseek-ai/DeepSeek-V4-Flash`。
- 支持文章摘要、复习卡片、文章问答。
- 调用结果写入 `ai_logs`。
- `/api/v1/ai/me/review-cards` 可读取用户生成过的复习卡片。

语音：

- 新增真实 `VoiceService` 实现，按 `api配置.md` 调用 `/audio/speech`。
- 当前语音模型默认：`FunAudioLLM/CosyVoice2-0.5B`。
- `POST /api/v1/voice/tts` 返回可直接播放的 `data:audio/mpeg;base64,...`。
- 调用记录写入 `voice_records`。

求助讨论：

- 新增求助发布、列表、详情、回答、采纳接口。
- 接入 `help_requests` 与 `help_answers`。

管理员配置：

- 新增 `integration_settings` 表。
- 管理端可维护 AI / 语音 Base URL、API Key、模型和音色。
- 种子数据已写入当前 SiliconFlow 配置；管理员接口返回时会遮罩密钥展示。

### 7.2 用户侧 Vue 已补齐

目录：

```text
studyforge-frontend/apps/knowledge-web
```

新增或增强页面：

- `/publish`：发布学习内容。
- `/help`：求助讨论、发布问题、查看回答、提交回答。
- `/posts/:postId`：点赞、收藏、评论、AI 摘要、复习卡片、语音朗读。
- `/library`：真实收藏、最近阅读、复习卡片。
- `/knowledge`：分类按数据库分类编码匹配，站点语言切换时保留文章实际语言。

重要语言规则：

- 用户发布的帖子不做自动双语转换。
- 前端展示以后端返回的 `languageCode` 为准。
- 如果英文界面打开只有中文原文的用户帖子，页面会显示中文原文，而不是伪装成英文内容。

### 7.3 控制台 Vue 已补齐

目录：

```text
studyforge-frontend/apps/portal-web
```

新增页面：

- `/settings`：AI 与语音设置。

当前可维护项：

- `ai.base_url`
- `ai.api_key`
- `ai.chat_model`
- `voice.base_url`
- `voice.api_key`
- `voice.model`
- `voice.name`

### 7.4 本轮已验证

数据库：

```text
./scripts/import_local_db.sh
返回表数量：18
```

后端：

```text
./scripts/build_with_proxy.sh
通过
```

前端：

```text
cd studyforge-frontend
npm run typecheck
npm run build
均通过
```

接口验证：

- `GET /api/v1/health` 成功。
- `POST /api/v1/auth/login` 成功。
- `GET /api/v1/posts/trending` 成功。
- `POST /api/v1/posts` 发布接口验证通过。
- `GET /api/v1/posts/4?languageCode=en_US` 返回中文原文，确认没有自动双语转换。
- `POST /api/v1/posts/4/likes` 成功。
- `POST /api/v1/posts/4/comments` 成功。
- `GET /api/v1/admin/settings/integrations` 成功。
- `POST /api/v1/ai/posts/4/review-cards` 成功调用真实 AI 并写入日志。
- `POST /api/v1/voice/tts` 成功返回可播放音频 data URL。
- 上述接口验证数据、互动记录、AI 日志和语音记录已从本机数据库清理。

本地服务已启动：

```text
用户侧知识平台: http://localhost:5174
控制台:         http://localhost:5173
后端 API:      http://localhost:8080/api/v1/health
```

## 8. 下一轮建议继续项

本轮已经把主要用户侧链路打通。下一轮建议继续做这些增强：

1. 内容编辑、删除、草稿箱和分页。
2. 管理端操作审计列表、审核规则配置和批量处理。
3. 求助详情独立页面和采纳答案前端交互。
4. AI 生成摘要缓存与重复生成提示，避免同一文章重复调用模型。
5. 语音识别 STT 与语音搜索。
6. 分类从接口读取，替代前端固定分类数组。
7. 更完整的密码哈希方案，例如 BCrypt。

## 9. 2026-05-24 Markdown 知识流与图片上传增强记录

本轮围绕“知识流帖子化、Markdown 写作、图片上传和可维护存取链路”继续实现，仍保持：

```text
Vue 前端
    ↓ Axios
Spring MVC Controller 返回 JSON
    ↓
Service
    ↓
MyBatis Mapper
    ↓
本机 MySQL / MariaDB
```

### 9.1 数据库与后端

新增或增强：

- `posts.cover_image_url`：文章封面图地址。
- `post_i18n.content_format`：正文格式，当前发布统一写入 `MARKDOWN`。
- `uploaded_files`：记录用户上传图片的元数据，包括上传用户、原始文件名、存储文件名、URL、类型、大小和状态。

新增上传链路：

- `POST /api/v1/uploads/images`
  - 需要登录。
  - 支持 `jpg/jpeg/png/webp/gif`。
  - 单图限制 8MB。
  - 文件保存到本机 `uploads/images`。
  - 元数据写入 `uploaded_files`。
- `GET /api/v1/files/images/{filename}`
  - 用于前端展示封面和正文图片。
  - 读取前会校验 `uploaded_files` 中存在有效记录。

相关模块：

```text
studyforge-webapi
studyforge-system
studyforge-content
```

关键文件：

```text
studyforge-webapi/src/main/java/com/studyforge/webapi/upload/UploadController.java
studyforge-system/src/main/java/com/studyforge/system/service/UploadedFileService.java
studyforge-system/src/main/java/com/studyforge/system/service/impl/UploadedFileServiceImpl.java
studyforge-system/src/main/java/com/studyforge/system/mapper/UploadedFileMapper.java
studyforge-system/src/main/resources/mybatis/system/UploadedFileMapper.xml
studyforge-content/src/main/java/com/studyforge/content/service/impl/PostCommandServiceImpl.java
studyforge-content/src/main/java/com/studyforge/content/service/impl/PostQueryServiceImpl.java
```

### 9.2 用户侧前端

`/publish` 已从普通发布表单升级为 Markdown 写作工具：

- 支持直接写 Markdown 源码。
- 支持“编辑 / 分屏 / 预览”三种模式。
- 工具栏支持插入标题、加粗、引用、链接、代码块、任务列表、有序列表、表格、图片链接。
- 支持上传图片并自动插入 `![alt](url)`。
- 支持封面上传、拖放上传、移除封面。
- 支持在正文编辑区粘贴或拖放图片，上传成功后自动插入 Markdown 图片语法。
- 支持本地草稿保存。
- 支持文章预设：方法笔记、技术文章、阅读卡片。
- 预览区以 Markdown 渲染结果为准，发布后详情页同样使用 Markdown 渲染。

`/knowledge` 知识流展示增强：

- 卡片改为更接近小红书/GitHub 帖子流的瀑布流排布。
- 优先展示用户上传封面，没有封面时使用主题视觉图。
- 卡片展示热度、分类、语言、收藏数、评论数和阅读数。

`/posts/:postId` 详情增强：

- 支持文章封面展示。
- 正文使用安全 Markdown 渲染。
- Markdown 链接默认新窗口打开。
- 任务列表、表格、代码块、引用、图片等样式已补齐。

### 9.3 语言规则

本轮继续保持：

- 用户发帖语言以发布时选择的 `originalLanguage` 为准。
- 不自动把用户帖子翻译成另一种语言。
- 列表和详情页展示后端返回的真实 `languageCode`。
- 如果当前界面语言没有对应内容，后端回退到原文。

### 9.4 本轮验证

已执行并通过：

```text
./scripts/build_with_proxy.sh
./scripts/import_local_db.sh
npm run typecheck --workspace @studyforge/knowledge-web
npm run build --workspace @studyforge/knowledge-web
```

本机服务确认：

```text
后端 API:          http://localhost:8080/api/v1/health
用户侧知识平台:     http://localhost:5174
控制台:            http://localhost:5173
```

接口链路验证：

- `POST /api/v1/auth/login` 成功。
- `POST /api/v1/uploads/images` 成功上传 `logo.png`。
- `POST http://localhost:5174/api/v1/uploads/images` 经 Vite 代理上传成功。
- `GET /api/v1/files/images/{filename}` 成功读取上传图片。
- `POST /api/v1/posts` Markdown 发布接口验证通过。
- `GET /api/v1/posts/{postId}?languageCode=zh_CN` 返回：
  - `coverImageUrl`
  - `contentFormat: MARKDOWN`
  - Markdown 原文内容

验证产生的发布记录、上传记录和图片文件已清理。

### 9.5 后续建议

下一轮可以继续补：

1. 帖子编辑、删除、草稿箱从本地草稿升级为数据库草稿。
2. 图片管理页，让用户查看和复用自己上传过的图片。
3. 后端分页和游标加载，让知识流支持更长列表。
4. 管理端操作审计列表，支持追踪置顶、下架、恢复和账号处理记录。
5. 分类从后端接口读取，发布页和知识流不再维护固定分类数组。

## 10. 2026-05-24 知识流语言规则与 AI 排版增强记录

### 10.1 知识流语言规则调整

本轮根据新要求调整 `/knowledge` 知识流：

- 知识流不再按站点语言选择内容版本。
- 无论当前站点是中文还是 English，知识流都会展示所有已发布帖子。
- 知识流卡片展示每篇帖子的原始语言内容，即 `posts.original_language` 对应的 `post_i18n`。
- 卡片进入详情页时会带上该卡片实际 `languageCode`，详情页优先展示和卡片一致的原文版本。
- 用户发布的中文、英文或其他语言帖子在知识流中平等展示，不做自动翻译，也不因为站点语言被隐藏。

涉及文件：

```text
studyforge-content/src/main/java/com/studyforge/content/service/impl/PostQueryServiceImpl.java
studyforge-frontend/apps/knowledge-web/src/components/KnowledgeCard.vue
studyforge-frontend/apps/knowledge-web/src/views/PostDetailView.vue
```

### 10.2 AI 排版

发布页新增“AI 排版”能力：

- 入口位于 `/publish` Markdown 工具栏，按钮名为 `AI 排版`。
- 用户先输入纯文字或半结构化内容，再点击 `AI 排版`。
- 前端通过 Axios 调用 `POST /api/v1/ai/markdown/format`。
- 后端调用已有 `AiService`，继续使用管理端配置的 AI Base URL、API Key 和文本模型。
- AI 文本服务会读取 `https_proxy`、`HTTPS_PROXY`、`http_proxy`、`HTTP_PROXY` 或 JVM proxy 属性，方便本机 7897 代理环境下访问模型接口。
- API Key 不会进入前端。
- AI 输出只作为 Markdown 正文回填到编辑器，用户仍可继续编辑、预览和发布。
- 处理上限：一次最多 12000 个字符。
- 调用日志写入 `ai_logs`，类型为 `MARKDOWN_FORMAT`。

内置提示词要求 AI：

- 只输出 Markdown 正文。
- 保留用户原意，不新增事实。
- 使用用户原文语言。
- 根据内容自然加入标题、列表、引用、表格或代码块。
- 保留原文中的链接、代码、步骤和清单。
- 不插入不存在的图片。

涉及文件：

```text
studyforge-ai/src/main/java/com/studyforge/ai/service/AiService.java
studyforge-ai/src/main/java/com/studyforge/ai/service/impl/SiliconFlowAiServiceImpl.java
studyforge-ai/src/main/java/com/studyforge/ai/service/impl/LocalFallbackAiServiceImpl.java
studyforge-webapi/src/main/java/com/studyforge/webapi/ai/AiController.java
studyforge-frontend/apps/knowledge-web/src/api/ai.ts
studyforge-frontend/apps/knowledge-web/src/views/PublishView.vue
studyforge-frontend/apps/knowledge-web/src/assets/base.css
```

### 10.3 本轮验证

已执行并通过：

```text
./scripts/build_with_proxy.sh
npm run typecheck --workspace @studyforge/knowledge-web
npm run build --workspace @studyforge/knowledge-web
```

接口验证：

- 使用英文原文和中文原文分别验证发布流程。
- `GET /api/v1/posts/trending?languageCode=zh_CN&limit=20` 同时返回英文原文帖和中文原文帖。
- `GET /api/v1/posts/trending?languageCode=en_US&limit=20` 同样同时返回英文原文帖和中文原文帖。
- 两次请求中帖子 `languageCode` 均保持原始语言，不随站点语言变化。
- `POST /api/v1/ai/markdown/format` 成功返回 `MARKDOWN_FORMAT` 结果。

验证产生的发布记录和 AI 排版日志已清理。

## 11. 2026-05-24 真实种子数据与旧演示数据清理记录

### 11.1 本轮调整

本轮清理了运行代码和种子数据中的旧演示内容，并改为可直接用于本地开发联调的真实学习社区数据。

已完成：

- `sql/002_seed_data.sql` 改为导入前清空本机业务表，再写入新的账号、文章、互动、学习记录、求助和审计记录。
- 种子账号更新为 6 个普通用户和 1 个管理员。
- 知识流写入 8 篇完整 Markdown 文章，包含中文原文和英文原文。
- 评论、点赞、收藏、浏览记录、求助问题和回答全部关联到新账号。
- 登录页默认账号改为当前真实种子账号。
- `AuthServiceImpl` 移除旧开发密码兜底，只接受数据库中的密码哈希。
- `UserServiceImpl` 改为从数据库读取用户。
- AI 与语音旧演示实现已替换为本地兜底实现，不再返回演示域名或伪造音频地址。
- 首页热门阅读区域改为读取真实知识流接口。
- 知识流侧栏固定进度条改为根据接口返回的文章分类实时计算内容分布。

### 11.2 当前本机账号

```text
用户账号: chen_jiayi / StudyForge@2026
管理账号: ops_admin  / AdminForge@2026
```

旧账号验证：

- 旧演示账号已无法登录。
- 旧管理员演示账号不再作为种子账号存在。

### 11.3 当前种子数据规模

当前本机数据库 `test_studyforge_ai_v2` 导入后数据：

```text
users            7
posts            8
post_i18n        8
comments         13
post_likes       40
post_favorites   19
post_view_history 8
help_requests    3
help_answers     3
admin_audit_logs 1
```

知识流文章：

- Vue 知识流页面的状态设计：从请求到缓存
- Designing a Markdown Composer That Users Can Trust
- 把一篇长文变成可复习卡片的四步法
- 学习社区为什么需要可追溯的内容运营
- Spring MVC + MyBatis 项目里，Service 层应该承担什么
- 给刚开始工作的人的现金流笔记
- How I Prepare a Technical Interview Study Log
- A Weekly Review System for Learning Projects

### 11.4 本轮验证

已执行并通过：

```text
./scripts/import_local_db.sh
./scripts/build_with_proxy.sh
npm run typecheck
npm run build
```

接口验证：

- `POST /api/v1/auth/login` 使用 `chen_jiayi / StudyForge@2026` 登录成功。
- `POST /api/v1/auth/login` 使用 `ops_admin / AdminForge@2026` 登录成功。
- `POST /api/v1/auth/login` 使用旧演示账号返回账号或密码错误。
- `GET /api/v1/posts/trending?languageCode=zh_CN&limit=20` 返回 8 篇文章，语言包含 `zh_CN` 和 `en_US`。
- `GET /api/v1/help?status=ALL&limit=20` 返回 3 条求助问题。

当前服务地址：

```text
用户侧知识平台: http://localhost:5174
控制台:         http://localhost:5173
后端 API:      http://localhost:8080/api/v1/health
```

## 14. 2026-05-25 用户社区、好友、学习看板与文章编辑记录

本轮继续保持既定链路：

```text
Vue 前端
    ↓ Axios
Spring MVC Controller 返回 JSON
    ↓
Service
    ↓
MyBatis Mapper
    ↓
本机 MySQL / MariaDB
```

### 14.1 好友与私信

新增真实数据库表：

- `friend_requests`：好友申请。
- `friendships`：已通过的好友关系。
- `friend_messages`：好友私信。

新增用户侧接口：

- `GET /api/v1/users/{userId}/friends`
- `GET /api/v1/users/me/friends`
- `POST /api/v1/users/{userId}/friend-requests`
- `GET /api/v1/users/me/friend-requests/incoming`
- `GET /api/v1/users/me/friend-requests/outgoing`
- `POST /api/v1/users/me/friend-requests/{requestId}/review`
- `GET /api/v1/users/me/friends/{friendId}/messages`
- `POST /api/v1/users/me/friends/{friendId}/messages`

当前规则：

- 关注、粉丝、好友是三套独立关系。
- 个人主页的好友列表只读取 `friendships`，不再把关注和粉丝拼接成好友。
- 别人访问用户主页时，可根据关系状态发送好友申请、通过申请或进入好友消息页。
- `/friends` 支持查看好友、处理申请、查看已发申请和发送私信。

### 14.2 账号资料编辑

新增用户侧页面：

- `/account`

能力：

- 修改用户名、邮箱、显示名称、签名、头像、主页背景。
- 支持上传头像和主页背景，继续走现有 `uploaded_files` 与 `/api/v1/files/images/{filename}` 链路。
- 支持修改密码，当前沿用项目现有 `sha256:` 哈希格式。
- 保存后同步更新前端 session 中的用户显示信息。

### 14.3 文章编辑

新增后端接口：

- `PUT /api/v1/posts/{postId}`

能力：

- 只有作者本人可以编辑帖子。
- 支持修改标题、摘要、正文 Markdown、封面、主题和原始语言。
- 编辑时保留原文章的阅读、点赞、收藏、评论和热度数据。

新增用户侧入口：

- `/posts/:postId/edit`
- 文章卡片上作者本人可看到“编辑”按钮。
- 文章详情页作者本人可看到“编辑文章”按钮。
- 发布页复用为编辑页，支持读取原文、继续 Markdown 预览、上传封面和保存修改。

### 14.4 我的学习与文章卡片 UI

用户侧 `/library` 已调整为学习看板布局：

- 顶部展示继续阅读入口。
- 收藏、最近读过、复习卡片使用仪表盘式数据块。
- 新增学习看板区，展示最新收藏、最近阅读和已保存内容的互动情况。
- 复习卡片区域继续使用 Markdown 渲染。

文章卡片 `KnowledgeCard` 已优化为内容社区卡片：

- 封面高度统一为 200px，移动端为 180px。
- 标题限制 2 行，摘要限制 2 行。
- 卡片顶部只保留主题和语言，减少小标签挤压。
- 底部统一为互动数据和阅读按钮区域。
- 作者本人可直接从卡片进入编辑页。

### 14.5 本轮验证

已执行并通过：

```text
./scripts/build_with_proxy.sh
./scripts/import_local_db.sh
npm --workspace @studyforge/knowledge-web run typecheck
npm --workspace @studyforge/knowledge-web run build
```

接口验证：

- `GET /api/v1/health` 成功。
- `POST /api/v1/auth/login` 使用 `chen_jiayi / StudyForge@2026` 成功。
- `GET /api/v1/users/me/profile` 返回真实邮箱、等级、好友数。
- `GET /api/v1/users/me/friends` 返回 3 位真实好友：`zhao_yiran`、`wang_yu`、`emma_clark`。
- `GET /api/v1/users/me/friend-requests/incoming?status=PENDING` 返回来自 `noah_kim` 的待处理申请。
- `GET /api/v1/users/5/profile` 返回 `friendStatus=FRIEND`，且非本人不返回邮箱。
- `PUT /api/v1/posts/1` 使用作者账号更新成功。
- `PUT /api/v1/posts/1` 使用非作者账号返回 `4030`，确认作者权限生效。
- `http://localhost:5174/api/v1/posts/trending?languageCode=zh_CN&limit=12` 经 Vite proxy 返回 8 篇真实文章。

当前服务地址：

```text
用户侧知识平台: http://localhost:5174
后端 API:      http://localhost:8080/api/v1/health
```

## 14. 2026-05-25 AI 提示词语言跟随站点语言

本轮补充 AI 语言规则：

- 前端调用 AI 时将文章原文语言和用户当前站点语言分开传给后端。
- `contentLanguageCode` 用于读取当前文章实际内容，避免中英文帖子被错误切换。
- `promptLanguageCode` 使用用户当前站点语言，即顶部语言切换里的 `zh_CN` 或 `en_US`。
- 后端 AI prompt 模板改为中英两套：
  - 中文站点使用中文提示词。
  - English 站点使用 English prompts。
- 文章摘要、复习卡片、文章问答、AI 排版都已接入该规则。
- AI 排版仍明确要求保留用户原文语言，不因为站点语言改变而自动翻译用户文章。

涉及文件：

```text
studyforge-frontend/apps/knowledge-web/src/api/ai.ts
studyforge-frontend/apps/knowledge-web/src/views/PostDetailView.vue
studyforge-frontend/apps/knowledge-web/src/views/PublishView.vue
studyforge-server/studyforge-webapi/src/main/java/com/studyforge/webapi/ai/AiController.java
studyforge-server/studyforge-ai/src/main/java/com/studyforge/ai/service/impl/SiliconFlowAiServiceImpl.java
studyforge-server/studyforge-ai/src/main/java/com/studyforge/ai/service/impl/LocalFallbackAiServiceImpl.java
```

验证：

```text
./scripts/build_with_proxy.sh
npm --workspace @studyforge/knowledge-web run typecheck
npm --workspace @studyforge/knowledge-web run build
```

当前服务已重启：

```text
用户侧知识平台: http://localhost:5174
后端 API:      http://localhost:8080/api/v1/health
```

### 11.5 详情页旧登录态兼容修复

重新导入数据库后，浏览器本地可能仍保留旧的 `studyforge.knowledge.session`。旧 token 会导致文章详情页加载互动状态时返回 `4010 login has expired`。

已修复：

- 文章详情和评论作为主流程加载。
- 点赞、收藏状态和阅读记录作为登录增强能力加载。
- 互动状态请求失败不会阻止文章正文展示。
- 如果识别到登录态过期，前端会清理本地 session，并提示重新登录后再使用互动功能。

验证：

- `GET /api/v1/posts/1?languageCode=zh_CN` 返回完整正文。
- `GET /api/v1/posts/2?languageCode=en_US` 返回完整正文。
- `GET /api/v1/posts/1/interaction` 使用无效 token 返回 `4010`，但不再影响文章详情打开。

### 11.6 AI 与语音超时调整

本轮将 AI 和语音相关链路的超时统一提升到 200 秒，避免长文本排版、摘要、复习卡片或语音生成时前端、Vite 代理或后端任意一层提前断开。

已调整：

- 后端 AI HTTP Client 连接超时：200 秒。
- 后端 AI chat/completions 请求超时：200 秒。
- 后端语音 HTTP Client 连接超时：200 秒。
- 后端语音 audio/speech 请求超时：200 秒。
- 用户侧 Axios 请求超时：200 秒。
- 管理端 Axios 请求超时：200 秒。
- 用户侧 Vite `/api` 代理 `timeout` 与 `proxyTimeout`：200 秒。
- 管理端 Vite `/api` 代理 `timeout` 与 `proxyTimeout`：200 秒。

验证：

- `./scripts/build_with_proxy.sh` 通过。
- `npm run typecheck` 通过。
- `npm run build` 通过。
- 已重启后端 API、用户侧知识平台和控制台。

### 11.7 2026-05-25 AI 输出与互动内容 Markdown 渲染

本轮补齐用户侧知识平台中容易出现 Markdown 的文本展示场景。

已调整：

- 文章详情页的 AI 摘要使用安全 Markdown 渲染。
- 文章详情页的复习卡片生成结果使用安全 Markdown 渲染。
- 我的学习页中已保存的复习卡片使用安全 Markdown 渲染。
- 文章评论支持 Markdown 渲染。
- 求助详情与回答支持 Markdown 渲染。
- 这些区域继续使用 `DOMPurify + markdown-it` 处理输出，链接默认新窗口打开。
- 卡片、评论和求助区域使用更紧凑的 Markdown 样式，避免正文级标题和代码块撑开布局。

验证：

- `npm --workspace @studyforge/knowledge-web run typecheck` 通过。
- `npm --workspace @studyforge/knowledge-web run build` 通过。

## 12. 2026-05-25 个人主页、关注与收藏夹增强记录

本轮继续保持：

```text
Vue 前端
    ↓ Axios
Spring MVC Controller 返回 JSON
    ↓
Service
    ↓
MyBatis Mapper
    ↓
本机 MySQL / MariaDB
```

### 12.1 数据库与种子数据

新增或增强：

- `users` 增加展示名、签名、头像、主页背景、社区等级、经验值和每日登录奖励日期。
- 新增 `user_follows`，支持关注、粉丝和互相关注好友统计。
- 新增 `user_experience_logs`，记录每日登录经验。
- 新增 `favorite_collections` 和 `favorite_collection_items`，支持收藏夹与收藏夹内文章。
- 种子数据已补齐用户头像、本地主页背景、用户签名、关注关系、收藏夹和收藏夹条目。
- 当前本机导入后表数量为 22。

### 12.2 后端接口

新增接口：

- `GET /api/v1/users/me/profile`
- `PUT /api/v1/users/me/profile`
- `GET /api/v1/users/{userId}/profile`
- `GET /api/v1/users/{userId}/posts`
- `POST /api/v1/users/{userId}/follow`
- `DELETE /api/v1/users/{userId}/follow`
- `GET /api/v1/users/{userId}/followers`
- `GET /api/v1/users/{userId}/following`
- `GET /api/v1/collections/me`
- `POST /api/v1/collections`
- `GET /api/v1/collections/{collectionId}/posts`
- `POST /api/v1/collections/{collectionId}/posts/{postId}`
- `DELETE /api/v1/collections/{collectionId}/posts/{postId}`

行为说明：

- 登录成功后，如果当天还没有领取登录经验，会增加 15 经验并更新等级。
- 个人主页会返回发帖数、收藏数、浏览历史数、粉丝数、关注数、互关好友数、评论数和收到点赞数。
- 收藏按钮会把文章写入“默认收藏”收藏夹；取消收藏会同步移除该文章在个人收藏夹中的条目。
- 文章摘要数据现在带作者 ID、作者展示名和头像 URL，知识流卡片可以跳到作者主页。

### 12.3 用户侧 Vue

新增页面：

- `/me`：当前登录用户个人主页。
- `/users/:userId`：公开用户主页。
- `/favorites`：收藏夹管理页。

新增前端能力：

- 顶部导航新增“我的主页”。
- 登录用户区域可进入个人主页。
- 个人主页包含头像、背景、签名、等级、经验进度、好友 / 关注 / 粉丝 / 收藏 / 历史浏览统计。
- 个人主页包含“动态 / 投稿 / 好友”分栏。
- 支持关注和取消关注其他用户。
- 收藏夹页支持查看收藏夹、新建收藏夹、查看收藏夹内文章、从收藏夹移出文章。
- 知识流文章卡片显示作者信息并支持跳转作者主页。
- 文章详情页的作者信息支持跳转个人主页。

### 12.4 本轮验证

已执行并通过：

```text
./scripts/build_with_proxy.sh
./scripts/import_local_db.sh
npm --workspace @studyforge/knowledge-web run typecheck
npm --workspace @studyforge/knowledge-web run build
```

接口验证：

- `POST /api/v1/auth/login` 使用 `chen_jiayi / StudyForge@2026` 登录成功，并触发每日登录经验。
- `GET /api/v1/users/me/profile` 返回个人主页统计、等级和经验进度。
- `GET /api/v1/users/1/profile` 可公开读取用户主页。
- `GET /api/v1/users/1/posts` 返回该用户发布的文章，并包含作者信息。
- `GET /api/v1/collections/me` 返回当前用户收藏夹。
- `GET /api/v1/collections/{collectionId}/posts` 返回收藏夹内文章。
- `POST /api/v1/users/4/follow` 和 `DELETE /api/v1/users/4/follow` 验证通过。

当前服务地址：

```text
用户侧知识平台: http://localhost:5174
后端 API:      http://localhost:8080/api/v1/health
```

## 13. 2026-05-25 社区管理与举报审核增强记录

本轮继续保持：

```text
Vue 前端
    ↓ Axios
Spring MVC Controller 返回 JSON
    ↓
Service
    ↓
MyBatis Mapper
    ↓
本机 MySQL / MariaDB
```

### 13.1 后端接口

新增社区管理服务：

- `CommunityAdminService`
- `AdminCommunityMapper`
- `AdminCommunityMapper.xml`

新增管理端接口：

- `GET /api/v1/admin/community/overview`
- `GET /api/v1/admin/community/posts`
- `GET /api/v1/admin/community/posts/{postId}`
- `POST /api/v1/admin/community/posts/{postId}/featured`
- `POST /api/v1/admin/community/posts/{postId}/status`
- `GET /api/v1/admin/community/reports`
- `POST /api/v1/admin/community/reports/{reportId}/review`
- `GET /api/v1/admin/community/users`
- `POST /api/v1/admin/community/users/{userId}/status`

新增用户侧举报接口：

- `POST /api/v1/posts/{postId}/reports`

接口能力：

- 用户可提交帖子举报，写入 `reports`。
- 管理员可查看举报队列，并选择下架、驳回或恢复帖子。
- 管理员可置顶或取消置顶帖子。
- 管理员可发布、下架、恢复帖子。
- 管理员可查看所有账号信息，包括邮箱、角色、状态、等级、经验、声望、发帖数、评论数、收藏数和粉丝数。
- 管理员可将普通用户恢复为正常、锁定或停用。
- 管理端操作会写入 `admin_audit_logs`。

### 13.2 数据库与种子数据

本轮增强：

- `posts.featured` 已作为置顶标记使用。
- `reports` 种子数据增加 3 条真实审核场景，其中 2 条待处理、1 条已驳回。
- `sql/001_schema.sql` 增加兼容已有本机表的 `featured` 与 `reputation_score` 补列语句。

### 13.3 管理端 Vue

目录：

```text
studyforge-frontend/apps/portal-web
```

新增：

- `/community`：社区管理页面。
- `src/api/community.ts`：管理端社区接口封装。
- `src/components/MarkdownRenderer.vue` 与 `src/utils/markdown.ts`：管理端安全 Markdown 渲染。

页面能力：

- 运营看板读取真实社区概览数据。
- 侧边栏新增“社区管理”入口。
- 社区管理页分为“举报审核 / 帖子管理 / 账号信息”三块。
- 举报审核可以处理待审举报，并触发下架或驳回。
- 帖子管理可以搜索、筛选、预览 Markdown、置顶、下架和恢复发布。
- 账号信息可以查看所有账号的真实社区数据，并处理普通账号状态。
- 管理端文章详情页改为调用 admin 接口，可渲染 Markdown，并提供置顶、下架和恢复发布操作。

### 13.4 用户侧 Vue

用户侧文章详情页新增：

- “举报文章”表单。
- 提交后调用 `POST /api/v1/posts/{postId}/reports`。
- 举报进入管理端审核队列。

### 13.5 本轮验证

已执行并通过：

```text
./scripts/build_with_proxy.sh
./scripts/import_local_db.sh
npm --workspace @studyforge/portal-web run typecheck
npm --workspace @studyforge/knowledge-web run typecheck
npm --workspace @studyforge/portal-web run build
npm --workspace @studyforge/knowledge-web run build
```

接口验证：

- `POST /api/v1/auth/login` 使用 `ops_admin / AdminForge@2026` 登录成功。
- `GET /api/v1/admin/community/overview` 成功返回社区概览。
- `GET /api/v1/admin/community/posts?status=ALL&limit=5` 成功返回管理端帖子列表。
- `POST /api/v1/admin/community/posts/{postId}/featured` 成功置顶文章。
- `POST /api/v1/admin/community/posts/{postId}/status` 成功更新文章状态。
- `GET /api/v1/admin/community/reports?status=ALL&limit=10` 成功返回举报列表。
- `POST /api/v1/posts/{postId}/reports` 使用普通用户登录后提交举报成功。
- `POST /api/v1/admin/community/reports/{reportId}/review` 选择 `TAKE_DOWN` 后成功把文章置为 `ARCHIVED`。
- 验证完成后通过文章状态接口恢复为 `PUBLISHED`。
- `GET /api/v1/admin/community/users?status=ALL&limit=10` 成功返回账号列表。
- `POST /api/v1/admin/community/users/{userId}/status` 成功验证账号状态接口。

当前服务地址：

```text
用户侧知识平台: http://localhost:5174
控制台:         http://localhost:5173
后端 API:      http://localhost:8080/api/v1/health
```
