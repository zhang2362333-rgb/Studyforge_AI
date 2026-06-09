# StudyForge AI 开发计划与项目架构设计

基于已提供的《StudyForge_AI_完整项目计划文档.docx》整理，并将原本偏 `JSP + Spring MVC 页面渲染` 的方案，调整为更适合团队协作和持续维护的前后端分离方案：

```text
Vue 3
    ↓ Axios / Fetch / Ajax
Spring MVC Controller（返回 JSON）
    ↓
Service
    ↓
MyBatis Mapper
    ↓
MySQL
```

---

## 1. 结论先行

### 1.1 推荐落地方式

本项目不建议一开始拆成微服务，推荐采用：

- 前端：`Vue 3 + Vite + TypeScript + Pinia + Vue Router + Axios`
- 后端：`Spring MVC + Spring + MyBatis + Maven + MySQL 8`
- 架构形态：`前后端分离 + 模块化单体`
- 部署方式：前端静态部署，后端提供 `/api/**` JSON 接口

### 1.2 为什么推荐“模块化单体”

对于课程项目和 2-4 人团队，模块化单体比微服务更合适：

- 业务体量还没有大到需要拆分独立服务
- 单库事务和联表查询更简单
- 部署、调试、联调成本更低
- 可以在代码层保持模块边界，后续如果真的需要再拆服务
- 对答辩和演示更稳定

### 1.3 Vue 方案约束

你的文档原来是 JSP 思路，现在切到前后端分离后，本项目固定使用 `Vue 3`，不再保留第二套前端框架并行方案。

- 前端统一使用 `Vue 3 + Vite + TypeScript`
- 状态管理统一使用 `Pinia`
- 路由统一使用 `Vue Router`
- HTTP 调用统一使用 `Axios`
- 团队维护一套 Vue 组件、路由、状态和接口类型体系

---

## 2. 从原文档调整后的架构方向

原文档整体业务边界是合理的，问题主要在展示层：

- 原方案偏 `JSP 页面渲染`
- Controller 同时承担“页面跳转”和“接口输出”职责
- 不利于前后端并行开发
- 后续接 AI、语音、管理后台时页面耦合会越来越重

调整后的目标是：

1. 后端只做业务和数据接口，不拼页面
2. 前端负责页面、交互、路由、国际化
3. 业务按模块拆分，而不是所有代码都塞进 `controller/service/mapper/entity` 大平铺目录
4. AI 与语音能力单独抽象，统一远程服务与本地兜底出口
5. 管理后台与用户端共用一套后端接口规范

---

## 3. 总体技术架构

## 3.1 系统结构

```text
+---------------------+        +---------------------+
| studyforge-web      |        | studyforge-admin    |
| Vue 3               |        | Vue 3               |
| 用户端前端           |        | 管理端前端           |
+----------+----------+        +----------+----------+
           \                           /
            \                         /
             \------ HTTP JSON ------/
                      /api/**
                         |
+-------------------------------------------------------------+
| studyforge-server                                           |
| Spring MVC + Spring + MyBatis                               |
|                                                             |
|  auth / user / post / interaction / search / ai / voice     |
|  help / moderation / admin / common / framework             |
+---------------------------+---------------------------------+
                            |
                     MySQL 8 Database
```

## 3.2 推荐仓库组织

建议使用单仓库，但前后端目录分离：

```text
StudyForge_AI/
├── docs/
├── studyforge-server/
├── studyforge-web/
├── sql/
└── README.md
```

这样做的好处：

- 同一个仓库便于统一版本管理
- 后端、前端、SQL、文档都能一起提交
- 团队成员不容易因为仓库拆太碎而丢上下文

---

## 4. 后端推荐架构：模块化单体

## 4.1 后端 Maven 多模块结构

建议不是一个大 `webapp` 工程塞所有代码，而是用 Maven 多模块：

```text
studyforge-server/
├── pom.xml                        # 父工程
├── studyforge-common              # 通用模块
├── studyforge-framework           # Spring/MyBatis/拦截器/统一配置
├── studyforge-system              # 用户、认证、角色、权限
├── studyforge-content             # 文章、分类、标签、知识流、搜索
├── studyforge-interaction         # 点赞、评论、收藏、浏览历史、热榜
├── studyforge-ai                  # AI 摘要/翻译/标签/问答/审核建议
├── studyforge-voice               # TTS/STT、语音记录
├── studyforge-help                # 学习求助模块
├── studyforge-admin               # 后台聚合接口、统计看板、审核管理
└── studyforge-webapi              # Controller 入口层，打包部署
```

## 4.2 每个模块职责

### `studyforge-common`

放所有“跨模块复用但不带业务”的内容：

- `ApiResponse`
- `PageResult`
- `BizException`
- 错误码 `ErrorCode`
- 常量类 `Constants`
- 工具类 `DateUtils`、`JsonUtils`
- 枚举 `RoleType`、`PostStatus`、`LanguageCode`

### `studyforge-framework`

放基础设施与框架配置：

- Spring 配置
- MyBatis 配置
- 数据源配置
- 统一异常处理
- 登录拦截器
- 管理员权限拦截器
- CORS 配置
- Jackson JSON 配置
- 日志配置

### `studyforge-system`

负责账号体系和身份能力：

- 注册、登录、退出
- 用户资料
- 角色权限
- 信誉积分基础能力
- 登录态校验

### `studyforge-content`

负责文章主流程：

- 发布文章
- 编辑文章
- 删除文章
- 文章详情
- 文章列表
- 分类筛选
- 双语标题/摘要/正文
- 标签、分类推荐接入点

### `studyforge-interaction`

负责互动和统计：

- 点赞
- 评论
- 收藏
- 浏览记录
- 热度分计算
- 热门榜单

### `studyforge-ai`

负责 AI 相关能力，必须接口化：

- 摘要
- 标签
- 分类推荐
- 翻译
- 内容审核建议
- 文章问答
- 复习题生成
- `LocalFallbackAiService` / `RemoteAiService`

### `studyforge-voice`

负责语音相关能力：

- 文本转语音 TTS
- 语音转文本 STT
- 语音搜索
- 语音记录表
- `LocalFallbackVoiceService` / `RemoteVoiceService`

### `studyforge-help`

负责学习求助：

- 发布求助
- 求助列表
- 回答求助
- 采纳答案
- 状态流转

### `studyforge-admin`

负责后台聚合能力：

- 举报审核
- 用户管理
- 文章管理
- 简单统计看板
- AI 日志查看

### `studyforge-webapi`

只放 Controller 和接口组装，不写核心业务逻辑：

- 参数接收
- 基础校验
- 调用 Service
- 返回 JSON

---

## 5. 后端包结构建议

不建议所有模块都只有一层 `controller/service/mapper/entity` 平铺到底。更适合维护的方式是“模块内再分层”。

以 `studyforge-content` 为例：

```text
studyforge-content/src/main/java/com/studyforge/content/
├── controller          # 若部分模块需要独立 Controller，可放这里；更推荐集中到 webapi
├── dto                 # 请求参数对象
├── vo                  # 返回对象
├── entity              # 持久化实体
├── mapper              # MyBatis Mapper 接口
├── service             # 业务接口
├── service/impl        # 业务实现
├── convert             # DTO/Entity/VO 转换
├── enums               # 模块内枚举
└── support             # 搜索、热度、标签等支持类
```

更推荐 Controller 统一放在 `studyforge-webapi`：

```text
studyforge-webapi/src/main/java/com/studyforge/webapi/
├── auth/
├── user/
├── post/
├── interaction/
├── ai/
├── voice/
├── help/
└── admin/
```

这样前端找接口更方便，后端业务模块也更纯粹。

---

## 6. 前端推荐架构

## 6.1 Vue 3 推荐方案

```text
studyforge-frontend/
├── apps/
│   ├── knowledge-web/      # 用户侧知识平台：知识流、详情、搜索、学习记录
│   └── portal-web/         # 控制台：系统总览、运营管理入口
├── packages/               # 后续沉淀 shared-api/shared-types/shared-utils
├── package.json
└── package-lock.json
```

推荐依赖：

- `vue`
- `vue-router`
- `pinia`
- `axios`
- `element-plus`
- `vue-i18n`

## 6.2 Vue 模块组织原则

前端固定使用 Vue 后，目录按业务模块组织，而不是所有页面都堆进 `views`。

- `api/`：Axios 请求封装和接口函数
- `components/`：可复用展示组件
- `layouts/`：页面框架
- `router/`：Vue Router 路由表
- `stores/`：Pinia 状态
- `views/`：路由级页面
- `types/`：前后端接口类型

## 6.3 是否拆成用户端和管理端两个前端

当前按架构优先方案落地为一个前端工作区、两个独立应用：

- `apps/knowledge-web`：用户侧知识平台，路由包含 `/`、`/posts/:postId`、`/library`、`/login`
- `apps/portal-web`：控制台，路由包含 `/feed`、`/posts/:postId`、`/admin`、`/login`

原因：

- 用户侧和控制台的页面密度、权限、导航完全不同
- 两端都通过 Axios 访问同一套 Spring MVC JSON API
- 后续可以把 `shared-api`、`shared-types`、`shared-utils` 抽到 `packages/` 复用

---

## 7. 前后端接口设计规范

## 7.1 统一响应结构

所有接口统一返回 JSON：

```json
{
  "code": 0,
  "message": "success",
  "data": {},
  "requestId": "20260523-abc123"
}
```

建议约定：

- `code = 0` 表示成功
- 非 `0` 为业务异常
- `message` 给前端直接展示或日志记录
- `data` 放实际返回内容
- `requestId` 方便排查问题

## 7.2 分页结构

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "list": [],
    "pageNum": 1,
    "pageSize": 10,
    "total": 123
  }
}
```

## 7.3 API 路径建议

```text
/api/auth/login
/api/auth/register
/api/auth/logout

/api/users/me
/api/users/{id}
/api/users/{id}/reputation

/api/posts
/api/posts/{id}
/api/posts/{id}/publish
/api/posts/search
/api/posts/trending
/api/posts/category/{categoryCode}

/api/comments
/api/posts/{id}/comments
/api/posts/{id}/like
/api/posts/{id}/favorite
/api/users/me/favorites
/api/users/me/history

/api/ai/summary
/api/ai/tags
/api/ai/translate
/api/ai/category
/api/ai/ask
/api/ai/quiz
/api/ai/moderate

/api/voice/tts
/api/voice/stt
/api/voice/search

/api/help
/api/help/{id}
/api/help/{id}/answers
/api/help/{id}/accept/{answerId}

/api/admin/dashboard
/api/admin/posts
/api/admin/reports
/api/admin/users
/api/admin/ai-logs
```

## 7.4 Controller 的职责边界

Controller 只做四件事：

1. 接收参数
2. 调用参数校验
3. 调用 Service
4. 返回 JSON

不要在 Controller 里写：

- SQL 逻辑
- 热度算法
- 大段 if/else 业务判断
- AI Prompt 拼接细节
- 文件上传处理细节

这些全部下沉到 Service 或 Support 类。

---

## 8. 推荐认证与权限方案

## 8.1 课程项目推荐实现

如果你们主要目标是稳定交付，推荐：

- 登录成功后生成 token
- 前端存储在 `localStorage` 或 `cookie`
- 请求头统一带 `Authorization: Bearer xxx`
- 后端 `LoginInterceptor` 解析 token
- `AdminInterceptor` 校验管理员角色

这样比传统 JSP Session 更适合前后端分离。

## 8.2 如果你们更熟 Spring Session

也可以保留 Session 方案，但要满足：

- 前端开发环境通过 Vite 代理 `/api`
- 同域部署
- 处理好跨域和 Cookie 传递

如果团队对 token 熟悉，建议直接走 token 鉴权。

---

## 9. 数据库设计重构建议

原文档中的表结构已经能覆盖 MVP，但为了更利于维护，建议做以下调整。

## 9.1 核心表保留

- `users`
- `posts`
- `comments`
- `post_likes`
- `post_favorites`
- `reports`
- `help_requests`
- `help_answers`
- `ai_logs`
- `voice_records`

## 9.2 建议新增或调整的表

### 1）分类表 `categories`

不要把分类完全写死在代码里，建议保留字典表：

- `category_id`
- `category_code`
- `name_zh`
- `name_en`
- `sort_no`
- `status`

### 2）标签表 `tags` 与关系表 `post_tags`

如果后续要做搜索、推荐、筛选，标签不要只存在 `posts.ai_tags_zh` 字符串字段中。

建议：

- `tags(tag_id, tag_name_zh, tag_name_en, source_type)`
- `post_tags(id, post_id, tag_id)`

MVP 阶段可以先保留 `posts.ai_tags_zh/en`，后期再规范到标签表。

### 3）浏览历史表 `post_view_history`

如果“最近浏览”要持久化，就不要只放内存 `Stack`：

- `id`
- `user_id`
- `post_id`
- `view_time`

前端展示最近浏览时，先从数据库拿最近 N 条，内存 `Stack` 只做演示说明和热点缓存。

### 4）管理员操作日志 `admin_audit_log`

用于审核追踪：

- `log_id`
- `admin_id`
- `target_type`
- `target_id`
- `action_type`
- `remark`
- `created_time`

## 9.3 对 posts 表的建议

建议保留统计冗余字段，方便列表页直接查询：

- `like_count`
- `favorite_count`
- `comment_count`
- `view_count`
- `hot_score`

注意不要一会儿叫 `likes`，一会儿叫 `like_count`。命名要统一。

建议统一为：

- `like_count`
- `favorite_count`
- `comment_count`
- `view_count`

这样语义更清楚。

---

## 10. 业务模块边界设计

## 10.1 用户与认证模块

职责：

- 注册、登录、退出
- 用户资料
- 角色与状态
- 信誉积分计算入口

接口：

- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/auth/logout`
- `GET /api/users/me`
- `PUT /api/users/me`

## 10.2 内容模块

职责：

- 文章发布、编辑、删除
- 双语内容保存
- 列表、详情
- 分类和标签绑定

接口：

- `GET /api/posts`
- `GET /api/posts/{id}`
- `POST /api/posts`
- `PUT /api/posts/{id}`
- `DELETE /api/posts/{id}`

## 10.3 互动模块

职责：

- 点赞
- 评论
- 收藏
- 最近浏览
- 热榜计算

接口：

- `POST /api/posts/{id}/like`
- `DELETE /api/posts/{id}/like`
- `POST /api/posts/{id}/favorite`
- `DELETE /api/posts/{id}/favorite`
- `GET /api/posts/{id}/comments`
- `POST /api/comments`

## 10.4 搜索与推荐模块

职责：

- 关键词搜索
- 分类筛选
- 作者搜索
- 双语搜索扩展
- 推荐列表

建议先做规则推荐，不要做机器学习推荐。

## 10.5 AI 模块

职责：

- 摘要
- 标签
- 分类推荐
- 翻译
- 审核建议
- 文章问答
- 复习题生成

约束：

- 必须统一从 `AiService` 出口调用
- 必须支持远程服务调用与本地兜底处理
- 所有 AI 调用记录落库到 `ai_logs`

## 10.6 语音模块

职责：

- TTS
- STT
- 语音搜索
- 语音记录

建议先完成：

- TTS 统一接口
- STT 统一接口

真实第三方接入放到 P2。

## 10.7 举报与审核模块

职责：

- 用户举报
- 管理员处理
- AI 风险建议
- 审核日志

这个模块和后台强相关，建议由一个人从后端到前端整块负责。

---

## 11. 模块之间的调用规则

为了避免代码后期越来越乱，必须定几个硬规则：

1. `webapi` 只能调用 `service`，不能直接调 `mapper`
2. `service` 可以调本模块 `mapper`，必要时调其他模块公开的 `service`
3. 不允许跨模块直接引用对方的 `mapper`
4. DTO、VO 放在各自模块，通用对象才放 `common`
5. AI、语音第三方接入必须经过适配层，不允许业务代码里到处散落 HTTP 调用

推荐依赖方向：

```text
webapi -> system/content/interaction/ai/voice/help/admin
admin  -> system/content/interaction/ai
content -> ai（可选）
interaction -> content/system
help -> system
all business modules -> common/framework
```

---

## 12. 代码规范与维护约定

## 12.1 命名规范

- 实体：`Post`, `User`, `Comment`
- 请求对象：`CreatePostRequest`, `LoginRequest`
- 返回对象：`PostDetailVO`, `UserProfileVO`
- Service 接口：`PostService`
- Service 实现：`PostServiceImpl`
- Mapper：`PostMapper`

## 12.2 方法设计原则

一个 Service 方法只做一件完整的业务事：

- `createPost`
- `updatePost`
- `toggleLike`
- `generateSummary`

不要出现难维护的方法名：

- `handlePostAllThings`
- `doProcess`
- `commonSave`

## 12.3 异常处理

统一使用业务异常，不要到处 `return null`：

- 参数错误
- 未登录
- 无权限
- 文章不存在
- AI 服务不可用

全部通过统一异常处理器映射为 JSON。

## 12.4 SQL 与 MyBatis 规范

- 简单 SQL 用注解可接受
- 列表搜索、动态筛选、联表统计建议用 XML
- 每个 Mapper XML 只写本模块 SQL
- 复杂 SQL 要加注释说明用途

---

## 13. 推荐前端页面拆分

## 13.1 用户端页面

- 登录页
- 注册页
- 首页知识流
- 文章详情页
- 发布/编辑文章页
- 搜索结果页
- 分类页
- 热门页
- 我的收藏
- 我的主页
- 作者主页
- 学习求助页

## 13.2 管理端页面

- 管理首页
- 文章管理
- 举报审核
- 用户管理
- AI 日志

## 13.3 页面与接口的责任边界

前端负责：

- 表单校验
- 路由切换
- 国际化文案
- 交互反馈
- 组件复用

后端负责：

- 权限判断
- 数据校验
- 持久化
- 业务规则
- 风险控制

---

## 14. 国际化方案调整

原文档是 Spring MVC 服务端国际化思路。改成前后端分离后，建议这样处理：

### 前端国际化

由前端负责：

- 菜单
- 按钮
- 提示语
- 页面标题

即：

- Vue：`vue-i18n`

### 内容国际化

由数据库负责：

- `title_zh`
- `title_en`
- `summary_zh`
- `summary_en`
- `content_zh`
- `content_en`

### 搜索国际化

后端处理：

- 搜中文词匹配中文字段
- 搜英文词匹配英文字段
- 有余力时做中英关键词扩展

这比把 UI 文案也交给 Spring MVC 管理更清晰。

---

## 15. 开发优先级重排

## 15.1 P0：必须完成

- 前后端基础骨架
- 登录注册
- 用户信息
- 文章 CRUD
- 首页知识流
- 搜索和分类
- 点赞、评论、收藏
- 热门排行
- 举报与管理员审核
- 中英文界面切换
- AI 摘要接口
- AI 翻译接口

## 15.2 P1：有余力完成

- AI 标签
- AI 分类推荐
- 作者主页
- 信誉积分
- 推荐列表
- TTS 接口

## 15.3 P2：答辩加分项

- AI 问答
- AI 复习题生成
- STT 搜索
- 语音笔记
- 学习求助模块
- 数据看板

---

## 16. 四周开发计划

## 第 1 周：骨架与基础能力

目标：

- 建好前端工程
- 建好后端多模块工程
- 建库建表
- 跑通登录注册
- 跑通统一返回结构
- 跑通前后端联调

交付物：

- `studyforge-web` 初始化完成
- `studyforge-server` 多模块初始化完成
- 基础 SQL 脚本
- 登录注册接口
- 登录/注册页面

## 第 2 周：文章主流程

目标：

- 文章发布、编辑、删除、详情、列表
- 搜索、分类
- 点赞、评论、收藏
- 热门排行

交付物：

- 知识流闭环可演示
- 文章详情页完成
- 热榜页完成

## 第 3 周：AI 与双语增强

目标：

- 双语字段接入
- AI 摘要、翻译、标签
- 推荐与信誉积分
- 作者主页
- TTS 接口与本地兜底

交付物：

- AI 能力闭环可演示
- 中英切换可演示
- 作者页与信誉分可演示

## 第 4 周：后台与收尾

目标：

- 举报审核
- 管理员后台
- 学习求助或 AI 问答二选一
- 全链路测试
- 文档与答辩材料

交付物：

- 完整演示版本
- 测试清单
- 项目说明文档
- PPT 与答辩流程稿

---

## 17. 团队分工建议

## 17.1 四人团队推荐分工

### A：后端基础负责人

- 多模块工程
- 认证权限
- 通用返回结构
- 数据库与 MyBatis 基础设施

### B：内容与互动负责人

- 文章 CRUD
- 搜索分类
- 点赞评论收藏
- 热门排行

### C：AI 与扩展能力负责人

- AI 模块
- 语音模块
- 推荐与信誉积分
- 远程服务与本地兜底切换

### D：前端与联调负责人

- 前端工程
- 用户端页面
- 管理端页面
- 国际化
- 联调测试

## 17.2 两人团队压缩方案

- A：后端全栈主责，负责接口、数据库、权限、AI 服务接入
- B：前端主责，负责页面、联调、文档、演示

此时建议砍掉：

- STT
- 学习求助
- AI 复习题
- 复杂推荐

---

## 18. 团队协作规则

## 18.1 Git 分支策略

建议：

- `main`：稳定可演示版本
- `develop`：日常集成分支
- `feature/*`：功能分支

例如：

- `feature/auth-login`
- `feature/post-crud`
- `feature/ai-summary`

## 18.2 提交规范

推荐提交信息：

- `feat: add post create api`
- `fix: correct like count update`
- `refactor: split ai service fallback and remote impl`
- `docs: update api contract`

## 18.3 每日协作要求

- 每天至少合并一次 `develop`
- SQL 变更必须同步到 `sql/` 目录
- 接口变更必须同步前端同学
- 新增字段必须同步文档

---

## 19. 测试策略

## 19.1 后端测试重点

- 注册登录
- 文章 CRUD
- 重复点赞/重复收藏拦截
- 评论计数同步
- 热度分计算
- 举报状态流转
- AI 本地兜底返回稳定性

## 19.2 前端测试重点

- 登录态失效处理
- 表单校验
- 中英切换
- 列表分页
- 接口失败提示
- 管理员页面权限访问

## 19.3 答辩演示稳定性要求

所有外部能力必须有兜底：

- AI：`LocalFallbackAiService`
- 语音：`LocalFallbackVoiceService`

不要把演示成功率押在第三方 API 的实时可用性上。

---

## 20. 推荐的最终 MVP

如果目标是“稳交付、能答辩、可继续开发”，最终 MVP 建议定为：

- 登录注册
- 文章发布、编辑、删除、查看
- 搜索和分类
- 点赞、评论、收藏
- 热门排行
- 举报审核
- 中英文界面
- AI 摘要接口
- AI 翻译接口
- 管理后台基础页面

这个版本已经足够完整，而且从架构上没有走偏。后续再往上叠加：

- AI 标签
- TTS
- 推荐
- 学习求助

---

## 21. 最终推荐的项目目录

```text
StudyForge_AI/
├── docs/
│   └── StudyForge_AI_开发计划与项目架构设计_前后端分离版.md
├── sql/
│   ├── 001_init_tables.sql
│   ├── 002_seed_data.sql
│   └── 003_alter_posts_add_bilingual_fields.sql
├── studyforge-server/
│   ├── pom.xml
│   ├── studyforge-common/
│   ├── studyforge-framework/
│   ├── studyforge-system/
│   ├── studyforge-content/
│   ├── studyforge-interaction/
│   ├── studyforge-ai/
│   ├── studyforge-voice/
│   ├── studyforge-help/
│   ├── studyforge-admin/
│   └── studyforge-webapi/
└── studyforge-web/
    ├── package.json
    ├── vite.config.ts
    └── src/
```

---

## 22. 实施建议

如果你接下来准备正式开做，我建议按这个顺序推进：

1. 前端固定使用 `Vue 3`，不要再引入第二套前端框架
2. 先建多模块后端骨架，不要直接写业务页面
3. 先把 `auth + post + interaction` 跑通
4. AI 和语音先接统一接口与本地兜底
5. 第三周之后再补推荐、求助、问答等加分项

如果你希望，我下一步可以直接继续给你两份可落地内容中的任意一份：

- 一份 `Maven 多模块后端目录脚手架设计`
- 一份 `Vue 3 前端目录结构 + 页面模块清单 + API 对接规范`
