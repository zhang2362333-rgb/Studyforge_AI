# StudyForge AI 技术架构优先设计版

本方案以你明确指定的技术栈为前提，不再以“最快落地”为优先，而是以以下目标为优先级：

1. 架构边界清晰
2. 便于多人并行开发
3. 便于长期维护和扩展
4. 支持 AI、语音、双语等功能持续叠加
5. 保持技术栈稳定在：

```text
Vue 前端
    ↓ Ajax / Fetch / Axios
Spring MVC Controller 返回 JSON
    ↓
Service
    ↓
MyBatis Mapper
    ↓
MySQL
```

---

## 1. 核心架构结论

### 1.1 架构形态

推荐采用：

- 前后端分离
- Java 模块化单体
- Maven 多模块工程
- 单数据库，多业务模块
- Controller 统一返回 JSON

这里的“模块化 Java”建议理解为：

- `Maven 多模块 + 业务模块边界 + 分层依赖规则`

而不是优先使用 `Java 9 JPMS module-info.java`。

### 1.2 为什么不优先用 JPMS

如果你是做 Spring MVC + MyBatis 项目，JPMS 的收益不高，复杂度反而明显上升：

- Spring 反射、代理、扫描与 JPMS 的开放规则容易互相牵扯
- MyBatis 动态代理与模块开放配置不够直观
- 团队成员学习和排障成本会上升
- 对课程项目和常规 Web 项目的维护收益有限

所以更稳妥、也更工程化的做法是：

- 用 Maven 管理模块
- 用包结构和依赖规则管理边界
- 用接口和 DTO/VO 管理模块间契约

这才是当前技术栈下最合适的“模块化 Java 方案”。

---

## 2. 技术选型基线

## 2.1 后端基线

- JDK：`17`
- 构建工具：`Maven`
- Web：`Spring MVC`
- IOC / AOP：`Spring`
- 持久层：`MyBatis`
- 数据库：`MySQL 8`
- JSON：`Jackson`
- 日志：`SLF4J + Logback`
- 连接池：`HikariCP` 或 `Druid`

## 2.2 前端基线

前端固定使用 `Vue 3`，项目内不再保留第二套前端框架并行方案。

- `Vue 3`
- `TypeScript`
- `Vite`
- `Pinia`
- `Vue Router`
- `Axios`
- `Element Plus`
- `vue-i18n`

这里的重点是整个团队只维护一套 Vue 技术体系：组件、路由、状态管理、接口类型和构建脚本保持一致。

---

## 3. 总体系统拓扑

```text
+---------------------------------------------------+
| Frontend                                          |
|                                                   |
|  Portal Web (Vue 3)                                |
|  Admin Web  (Vue 3)                                |
|                                                   |
|  - Router                                          |
|  - State Store                                     |
|  - I18n                                            |
|  - Axios / Fetch                                   |
+-----------------------------+---------------------+
                              |
                              | HTTP / JSON
                              v
+---------------------------------------------------+
| studyforge-webapi                                  |
| Spring MVC Controller                              |
| - AuthController                                   |
| - UserController                                   |
| - PostController                                   |
| - InteractionController                            |
| - AiController                                     |
| - VoiceController                                  |
| - HelpController                                   |
| - AdminController                                  |
+-----------------------------+---------------------+
                              |
                              | Service Interface
                              v
+---------------------------------------------------+
| Business Modules                                   |
| - system                                            |
| - content                                           |
| - interaction                                       |
| - ai                                                |
| - voice                                             |
| - help                                              |
| - admin                                             |
+-----------------------------+---------------------+
                              |
                              | MyBatis Mapper
                              v
+---------------------------------------------------+
| MySQL 8                                            |
+---------------------------------------------------+
```

---

## 4. 后端架构：模块化单体

## 4.1 顶层模块划分

建议后端按 Maven 多模块组织：

```text
studyforge-server/
├── pom.xml
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

## 4.2 模块职责定义

### `studyforge-common`

放所有业务模块都要复用的纯公共内容：

- 通用返回体 `ApiResponse`
- 分页对象 `PageResult`
- 统一错误码 `ErrorCode`
- 业务异常 `BizException`
- 通用枚举
- 常量定义
- 工具类

这个模块不应该依赖任何业务模块。

### `studyforge-framework`

放框架配置与基础设施能力：

- Spring 容器配置
- Spring MVC 配置
- MyBatis 配置
- 数据源配置
- Jackson 配置
- 跨域配置
- 统一异常处理
- 登录拦截器
- 管理员权限拦截器
- 请求日志与 requestId

### `studyforge-system`

负责用户与认证体系：

- 用户注册
- 用户登录
- 令牌解析
- 用户资料
- 角色与状态
- 信誉分基础能力

### `studyforge-content`

负责内容主域：

- 文章发布
- 文章编辑
- 文章删除
- 文章详情
- 文章列表
- 分类、标签、主题
- 双语或多语言内容读取

### `studyforge-interaction`

负责互动和统计：

- 点赞
- 收藏
- 评论
- 浏览历史
- 热度统计
- 热门排行

### `studyforge-ai`

负责所有 AI 文本能力：

- 摘要
- 标签
- 分类推荐
- 翻译
- 问答
- 复习题
- 审核建议
- AI 调用日志

必须支持：

- `LocalFallbackAiService`
- `RemoteAiService`

### `studyforge-voice`

负责语音相关能力：

- TTS
- STT
- 音频记录
- 语音搜索支持

同样必须支持：

- `LocalFallbackVoiceService`
- `RemoteVoiceService`

### `studyforge-help`

负责学习求助：

- 发布求助
- 求助列表
- 求助详情
- 回答求助
- 采纳答案
- 状态流转

### `studyforge-admin`

负责后台管理能力：

- 举报审核
- 用户管理
- 文章管理
- AI 日志查看
- 简单数据看板

### `studyforge-webapi`

负责 Web 接口层：

- Spring MVC Controller
- 参数绑定
- 参数校验
- 统一返回 JSON

`webapi` 不承载业务规则。

---

## 5. 模块依赖方向

模块化工程最重要的不是“拆出来多少模块”，而是“模块之间怎么依赖”。

推荐依赖方向如下：

```text
studyforge-common
studyforge-framework -> studyforge-common

studyforge-system -> studyforge-common, studyforge-framework
studyforge-content -> studyforge-common, studyforge-framework, studyforge-system
studyforge-interaction -> studyforge-common, studyforge-framework, studyforge-system, studyforge-content
studyforge-ai -> studyforge-common, studyforge-framework, studyforge-content
studyforge-voice -> studyforge-common, studyforge-framework, studyforge-content
studyforge-help -> studyforge-common, studyforge-framework, studyforge-system
studyforge-admin -> studyforge-common, studyforge-framework, studyforge-system, studyforge-content, studyforge-interaction, studyforge-ai

studyforge-webapi -> all business modules
```

硬性规则：

1. `webapi` 可以依赖业务模块
2. 业务模块不能反向依赖 `webapi`
3. 一个业务模块不能直接访问另一个模块的 Mapper
4. 跨模块访问必须通过对方公开的 Service 接口
5. 通用类型进 `common`，不能反过来把业务模型塞进 `common`

---

## 6. 包结构设计

## 6.1 不推荐的方式

不推荐全项目只有一套大平铺结构：

```text
com.studyforge
├── controller
├── service
├── mapper
├── entity
└── util
```

这种结构在初期看起来简单，后期会越来越难维护：

- 模块边界不清晰
- 类名冲突增多
- 协作开发时容易互相污染
- 业务扩展后很难控制依赖

## 6.2 推荐的模块内部分层

每个业务模块内部都遵循类似结构：

```text
com.studyforge.content
├── dto
├── vo
├── entity
├── mapper
├── service
├── service.impl
├── support
├── enums
└── convert
```

说明如下：

- `dto`：请求对象
- `vo`：返回对象
- `entity`：数据库实体
- `mapper`：MyBatis 接口
- `service`：业务接口
- `service.impl`：业务实现
- `support`：算法、组装器、策略类
- `enums`：模块枚举
- `convert`：DTO/Entity/VO 转换

## 6.3 Web API 层包结构

`studyforge-webapi` 内建议这样组织：

```text
com.studyforge.webapi
├── auth
├── user
├── post
├── interaction
├── ai
├── voice
├── help
├── admin
└── config
```

每个子包里只放：

- Controller
- Request 校验类
- Response 适配类

---

## 7. Controller、Service、Mapper 的职责边界

你指定的核心链路是：

```text
Vue
    ↓
Ajax / Fetch / Axios
    ↓
Spring MVC Controller 返回 JSON
    ↓
Service
    ↓
MyBatis Mapper
    ↓
MySQL
```

这条链路内部必须定义清楚职责。

## 7.1 Controller 职责

Controller 只负责：

1. 接收请求参数
2. 调用基础校验
3. 调用 Service
4. 返回统一 JSON

Controller 不应该写：

- SQL
- 热度计算
- 大段业务判断
- AI prompt 细节
- 权限核心逻辑
- 文件落盘细节

## 7.2 Service 职责

Service 是业务核心层，负责：

- 业务规则
- 事务控制
- 跨表更新
- 权限判断
- 模块协调
- AI / 语音服务调用

建议将复杂业务再拆成：

- `CommandService`：创建、修改、删除
- `QueryService`：详情、列表、分页、查询

例如：

- `PostCommandService`
- `PostQueryService`
- `InteractionCommandService`
- `TrendingQueryService`

## 7.3 Mapper 职责

Mapper 只负责数据访问：

- 插入
- 更新
- 删除
- 查询

Mapper 不负责：

- 业务流程编排
- 跨模块规则判断
- 返回给前端的最终结构组装

---

## 8. 数据库设计：从“双语字段”升级到“可扩展内容国际化”

如果你的目标是架构优先，那么原文档里这种设计：

- `title_zh`
- `title_en`
- `summary_zh`
- `summary_en`
- `content_zh`
- `content_en`

虽然能用，但扩展性一般。

更推荐的数据建模方式是把“主记录”和“多语言内容”拆开。

## 8.1 推荐核心模型

### 用户主表 `users`

保存用户本身：

- `user_id`
- `username`
- `email`
- `password_hash`
- `role`
- `status`
- `reputation_score`
- `created_time`

### 登录令牌表 `user_tokens`

如果采用 token 方案，建议落库管理：

- `token_id`
- `user_id`
- `access_token`
- `expire_time`
- `status`

### 分类主表 `categories`

- `category_id`
- `category_code`
- `sort_no`
- `status`

### 分类国际化表 `category_i18n`

- `id`
- `category_id`
- `language_code`
- `name`

### 文章主表 `posts`

只保存文章主属性和统计属性：

- `post_id`
- `author_id`
- `category_id`
- `original_language`
- `status`
- `featured`
- `like_count`
- `favorite_count`
- `comment_count`
- `view_count`
- `hot_score`
- `created_time`
- `updated_time`

### 文章国际化内容表 `post_i18n`

保存不同语言版本内容：

- `id`
- `post_id`
- `language_code`
- `title`
- `summary`
- `content`
- `ai_tags`
- `source_type`

`source_type` 可表示：

- `ORIGINAL`
- `AI_TRANSLATED`
- `MANUAL_EDITED`

### 评论表 `comments`

- `comment_id`
- `post_id`
- `user_id`
- `language_code`
- `content`
- `status`
- `created_time`

### 点赞表 `post_likes`

- `like_id`
- `post_id`
- `user_id`
- `created_time`

加唯一索引：

- `(post_id, user_id)`

### 收藏表 `post_favorites`

- `favorite_id`
- `post_id`
- `user_id`
- `created_time`

加唯一索引：

- `(post_id, user_id)`

### 浏览历史表 `post_view_history`

- `id`
- `post_id`
- `user_id`
- `view_time`

### 举报表 `reports`

- `report_id`
- `post_id`
- `reporter_id`
- `reason`
- `status`
- `ai_risk_level`
- `ai_suggestion`
- `created_time`
- `processed_by`
- `processed_time`

### AI 日志表 `ai_logs`

- `log_id`
- `user_id`
- `post_id`
- `ai_type`
- `request_text`
- `response_text`
- `success`
- `created_time`

### 语音记录表 `voice_records`

- `record_id`
- `user_id`
- `post_id`
- `voice_type`
- `audio_url`
- `recognized_text`
- `created_time`

### 求助表 `help_requests`

- `help_id`
- `user_id`
- `title`
- `description`
- `category_id`
- `status`
- `reward_points`
- `created_time`

### 求助回答表 `help_answers`

- `answer_id`
- `help_id`
- `user_id`
- `content`
- `is_accepted`
- `created_time`

### 管理员操作日志 `admin_audit_logs`

- `log_id`
- `admin_id`
- `target_type`
- `target_id`
- `action_type`
- `remark`
- `created_time`

## 8.2 为什么推荐 `post_i18n`

这个设计比直接把 `zh/en` 字段塞到 `posts` 表更稳：

- 后续新增语言不改主表结构
- 主表更聚焦于业务主体和统计属性
- 内容编辑、翻译、审核流程更清晰
- 前端切换语言时后端读取逻辑更统一

如果你后面要做：

- AI 翻译
- 人工修正翻译
- 多版本内容

这个模型会明显更好维护。

---

## 9. API 契约设计

架构优先的项目，接口契约必须先稳定。

## 9.1 统一返回结构

```json
{
  "code": 0,
  "message": "success",
  "data": {},
  "requestId": "req-20260523-0001"
}
```

约定：

- `0`：成功
- 非 `0`：业务失败
- `message`：错误提示或成功提示
- `requestId`：排查链路问题

## 9.2 分页结构

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "list": [],
    "pageNum": 1,
    "pageSize": 10,
    "total": 0
  }
}
```

## 9.3 API 版本

建议直接使用版本前缀：

```text
/api/v1/**
```

这样未来升级接口时，前端和后端更容易共存演进。

## 9.4 资源型接口规划

```text
/api/v1/auth/login
/api/v1/auth/register
/api/v1/auth/logout

/api/v1/users/me
/api/v1/users/{id}
/api/v1/users/{id}/reputation

/api/v1/posts
/api/v1/posts/{id}
/api/v1/posts/search
/api/v1/posts/trending
/api/v1/posts/{id}/comments
/api/v1/posts/{id}/like
/api/v1/posts/{id}/favorite
/api/v1/posts/{id}/translate

/api/v1/ai/summary
/api/v1/ai/tags
/api/v1/ai/category
/api/v1/ai/translate
/api/v1/ai/moderate
/api/v1/ai/ask
/api/v1/ai/quiz

/api/v1/voice/tts
/api/v1/voice/stt
/api/v1/voice/search

/api/v1/help
/api/v1/help/{id}
/api/v1/help/{id}/answers
/api/v1/help/{id}/accept/{answerId}

/api/v1/admin/dashboard
/api/v1/admin/posts
/api/v1/admin/reports
/api/v1/admin/users
/api/v1/admin/ai-logs
```

---

## 10. 认证与权限架构

## 10.1 推荐方案

前后端分离下推荐：

- 登录成功返回 token
- 前端统一通过 Axios 拦截器附带 token
- 后端 `LoginInterceptor` 校验
- 后端 `AdminInterceptor` 校验管理权限

## 10.2 角色模型

建议至少定义：

- `GUEST`
- `USER`
- `AUTHOR`
- `ADMIN`

其中：

- `AUTHOR` 可以是 `USER` 的业务标签，不一定要独立权限体系
- 真正权限控制重点是 `USER` 和 `ADMIN`

## 10.3 权限校验原则

权限不能只在前端控制，后端必须重复校验：

- 文章编辑者是否本人
- 管理员接口是否管理员
- 举报处理是否已登录
- 收藏和点赞是否重复

---

## 11. AI 与语音模块的架构约束

AI 和语音不是“附加工具类”，而是独立业务能力。

## 11.1 AI 模块接口

```java
public interface AiService {
    String generateSummary(String content, String language);
    String generateTags(String content, String language);
    String recommendCategory(String content, String language);
    String translateText(String text, String sourceLang, String targetLang);
    String moderateContent(String content, String language);
    String answerQuestion(String postContent, String question, String answerLanguage);
    String generateQuiz(String postContent, String language);
}
```

## 11.2 语音模块接口

```java
public interface VoiceService {
    String textToSpeech(String text, String language);
    String speechToText(String audioFilePath, String language);
    String detectSpeechLanguage(String audioFilePath);
}
```

## 11.3 为什么必须区分远程服务和本地兜底

架构优先并不意味着所有外部能力都要先接真接口。

你真正要先确定的是：

- 调用入口统一
- 参数模型统一
- 日志模型统一
- 异常处理统一

所以必须从一开始就设计：

- `LocalFallbackAiServiceImpl`
- `RemoteAiServiceImpl`
- `LocalFallbackVoiceServiceImpl`
- `RemoteVoiceServiceImpl`

这样以后替换底层实现，不会波及上层业务模块。

---

## 12. 前端工程架构

如果你强调架构优先，我不建议把用户端和管理端全塞进一个前端应用里。

更推荐下面这套结构：

```text
studyforge-frontend/
├── apps/
│   ├── knowledge-web
│   └── portal-web
├── packages/
│   ├── shared-api
│   ├── shared-types
│   └── shared-utils
└── package.json
```

### 这样拆的原因

- 用户端和管理端路由、权限、菜单天然不同
- 后续管理端 UI 变动不会污染用户端
- 公共 API 封装和类型可复用
- 更符合真实团队协作方式

如果你们团队规模不大，也可以退一步做成单前端工程，但仍然建议保留业务模块目录：

```text
src/
├── api/
├── core/
├── modules/
│   ├── auth/
│   ├── post/
│   ├── interaction/
│   ├── ai/
│   ├── voice/
│   ├── help/
│   └── admin/
├── pages/
├── router/
├── store/
└── i18n/
```

## 12.1 前端层次职责

### `api/`

- Axios 实例
- 请求拦截器
- 响应拦截器
- 模块接口封装

### `core/`

- 登录态管理
- 路由守卫
- 错误处理
- 全局配置

### `modules/`

每个业务模块自己管理：

- 页面
- 组件
- hooks/composables
- 类型定义
- 业务 API 封装

### `i18n/`

只管理 UI 文案国际化，不负责文章内容国际化。

---

## 13. 模块与数据库的对应关系

为了方便团队分工，建议从一开始就把模块和表对应起来：

### `studyforge-system`

- `users`
- `user_tokens`

### `studyforge-content`

- `posts`
- `post_i18n`
- `categories`
- `category_i18n`

### `studyforge-interaction`

- `comments`
- `post_likes`
- `post_favorites`
- `post_view_history`

### `studyforge-ai`

- `ai_logs`

### `studyforge-voice`

- `voice_records`

### `studyforge-help`

- `help_requests`
- `help_answers`

### `studyforge-admin`

- `reports`
- `admin_audit_logs`

这样一来：

- 谁负责哪个模块
- 谁改哪些表
- 哪些接口归哪个模块

都比较清晰。

---

## 14. 开发顺序：按架构层推进，而不是按页面堆功能

既然你目标是架构优先，开发顺序就不应该是“先做几个页面看看效果”，而应该按技术骨架推进。

## 阶段 0：架构定稿

完成内容：

- 模块划分
- 包结构规则
- 表结构定稿
- API 契约定稿
- 错误码约定
- Git 分支策略

交付物：

- 架构设计文档
- SQL 建模文档
- API 文档

## 阶段 1：后端基础骨架

完成内容：

- Maven 多模块工程
- Spring MVC / Spring / MyBatis 基础配置
- 通用返回结构
- 统一异常
- 登录拦截器
- 基础数据库连接

交付物：

- 可启动的 `studyforge-webapi`
- 可访问的 `/api/v1/health`
- 基础 parent pom 和子模块

## 阶段 2：系统与内容主域

完成内容：

- 注册、登录
- 用户信息
- 文章 CRUD
- 分类管理
- `post_i18n` 内容模型

交付物：

- 用户与内容主链路打通

## 阶段 3：互动与后台

完成内容：

- 评论
- 点赞
- 收藏
- 热榜
- 举报
- 后台审核

交付物：

- 用户互动闭环
- 管理审核闭环

## 阶段 4：AI 与语音扩展

完成内容：

- AI 摘要
- AI 翻译
- AI 标签
- TTS
- STT

交付物：

- AI/语音可插拔能力

## 阶段 5：前端分层实现

完成内容：

- 用户端前端
- 管理端前端
- 国际化
- Axios 统一封装
- 权限路由控制

交付物：

- 前后端完整联调

---

## 15. 团队协作建议

## 15.1 分工方式

以模块为单位分工，而不是以“谁写 controller，谁写 mapper”分工。

推荐方式：

- A：`system + framework`
- B：`content`
- C：`interaction + admin`
- D：`frontend + i18n`
- E：`ai + voice`（如果有第五人）

这样每个人拥有相对完整的模块上下文，更利于维护。

## 15.2 Git 规则

建议：

- `main`：稳定版本
- `develop`：集成分支
- `feature/module-name`：功能分支

例如：

- `feature/system-auth`
- `feature/content-post`
- `feature/interaction-like`
- `feature/admin-report-review`

## 15.3 变更约束

任何人修改以下内容时，必须同步文档：

- 数据库表结构
- API 入参出参
- 错误码
- 公共枚举

---

## 16. 最终推荐目录结构

```text
StudyForge_AI/
├── docs/
│   ├── StudyForge_AI_开发计划与项目架构设计_前后端分离版.md
│   └── StudyForge_AI_技术架构优先设计版.md
├── sql/
│   ├── 001_schema.sql
│   ├── 002_seed_data.sql
│   └── 003_indexes.sql
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
└── studyforge-frontend/
    ├── apps/
    │   ├── knowledge-web/
    │   ├── portal-web/
    └── packages/
        ├── shared-api/
        ├── shared-types/
        └── shared-utils/
```

---

## 17. 最终建议

如果你现在把“技术架构优先”作为首要目标，那么我建议最终采用下面这套定稿：

### 后端

- `Spring MVC + Spring + MyBatis + MySQL`
- `Maven 多模块`
- `模块化单体`
- `Controller -> Service -> Mapper -> MySQL`

### 前端

- `Vue 3`
- 前后端分离
- 用户端与管理端分开组织
- 统一 `Axios` 封装

### 数据模型

- 主实体和国际化内容拆表
- 统计字段与行为记录分离
- 所有扩展能力通过独立模块接入

### 工程策略

- 先定模块边界
- 先定数据库模型
- 先定 API 契约
- 再开始写业务代码

这套方案的重点不是“最快出效果”，而是后面你们团队继续加功能时，不会因为最初结构太散而反复返工。

下一步最合适的是直接进入两项工程化产出：

1. Maven 多模块后端骨架设计
2. 数据库 Schema 定稿

这两项定下来，整个项目就真正进入可控状态了。
