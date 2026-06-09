SET NAMES utf8mb4;

SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE admin_audit_logs;
TRUNCATE TABLE help_answer_likes;
TRUNCATE TABLE help_answers;
TRUNCATE TABLE help_requests;
TRUNCATE TABLE voice_records;
TRUNCATE TABLE ai_logs;
TRUNCATE TABLE reports;
TRUNCATE TABLE post_view_history;
TRUNCATE TABLE favorite_collection_items;
TRUNCATE TABLE favorite_collections;
TRUNCATE TABLE post_favorites;
TRUNCATE TABLE post_likes;
TRUNCATE TABLE comment_likes;
TRUNCATE TABLE comments;
TRUNCATE TABLE uploaded_files;
TRUNCATE TABLE post_i18n;
TRUNCATE TABLE posts;
TRUNCATE TABLE user_tokens;
TRUNCATE TABLE integration_settings;
TRUNCATE TABLE user_experience_logs;
TRUNCATE TABLE friend_messages;
TRUNCATE TABLE friendships;
TRUNCATE TABLE friend_requests;
TRUNCATE TABLE user_follows;
TRUNCATE TABLE users;
TRUNCATE TABLE category_i18n;
TRUNCATE TABLE categories;
SET FOREIGN_KEY_CHECKS = 1;

INSERT INTO categories (category_code, sort_no, status)
VALUES
    ('TECHNOLOGY', 10, 'ACTIVE'),
    ('BUSINESS', 20, 'ACTIVE'),
    ('PRODUCTIVITY', 30, 'ACTIVE'),
    ('CAREER', 40, 'ACTIVE'),
    ('FINANCE', 50, 'ACTIVE')
ON DUPLICATE KEY UPDATE
    sort_no = VALUES(sort_no),
    status = VALUES(status);

INSERT INTO category_i18n (category_id, language_code, name)
SELECT c.category_id, 'zh_CN', t.name
FROM categories c
JOIN (
    SELECT 'TECHNOLOGY' AS category_code, '技术实践' AS name
    UNION ALL SELECT 'BUSINESS', '商业观察'
    UNION ALL SELECT 'PRODUCTIVITY', '效率方法'
    UNION ALL SELECT 'CAREER', '职业成长'
    UNION ALL SELECT 'FINANCE', '财务入门'
) t ON t.category_code = c.category_code
ON DUPLICATE KEY UPDATE name = VALUES(name);

INSERT INTO category_i18n (category_id, language_code, name)
SELECT c.category_id, 'en_US', t.name
FROM categories c
JOIN (
    SELECT 'TECHNOLOGY' AS category_code, 'Technology' AS name
    UNION ALL SELECT 'BUSINESS', 'Business'
    UNION ALL SELECT 'PRODUCTIVITY', 'Productivity'
    UNION ALL SELECT 'CAREER', 'Career'
    UNION ALL SELECT 'FINANCE', 'Finance'
) t ON t.category_code = c.category_code
ON DUPLICATE KEY UPDATE name = VALUES(name);

INSERT INTO users (
    username,
    display_name,
    email,
    password_hash,
    role,
    status,
    bio,
    avatar_url,
    banner_url,
    community_level,
    experience_points,
    last_login_reward_date,
    reputation_score
)
VALUES
    ('chen_jiayi', '陈嘉仪', 'jiayi.chen@studyforge.ai', 'sha256:aa5969061c710df50f3b9724264a64b8ab3cd41c9b3f62f73f19bf8cb444d9a0', 'USER', 'ACTIVE', '偏前端和学习方法，喜欢把长文整理成可以复习的卡片。', '/avatars/chen-jiayi.svg', '/banners/study-lab.svg', 14, 1375, DATE_SUB(CURRENT_DATE, INTERVAL 1 DAY), 860),
    ('li_minghao', '李明昊', 'minghao.li@studyforge.ai', 'sha256:aa5969061c710df50f3b9724264a64b8ab3cd41c9b3f62f73f19bf8cb444d9a0', 'USER', 'ACTIVE', '关注内容运营、知识库治理和团队协作流程。', '/avatars/li-minghao.svg', '/banners/content-ops.svg', 12, 1160, DATE_SUB(CURRENT_DATE, INTERVAL 2 DAY), 740),
    ('zhao_yiran', '赵一然', 'yiran.zhao@studyforge.ai', 'sha256:aa5969061c710df50f3b9724264a64b8ab3cd41c9b3f62f73f19bf8cb444d9a0', 'USER', 'ACTIVE', '长期记录复盘、计划和主动回忆练习。', '/avatars/zhao-yiran.svg', '/banners/review-system.svg', 11, 1045, DATE_SUB(CURRENT_DATE, INTERVAL 1 DAY), 690),
    ('wang_yu', '王屿', 'yu.wang@studyforge.ai', 'sha256:aa5969061c710df50f3b9724264a64b8ab3cd41c9b3f62f73f19bf8cb444d9a0', 'USER', 'ACTIVE', '后端开发，喜欢把事务边界和接口契约写清楚。', '/avatars/wang-yu.svg', '/banners/backend-notes.svg', 10, 960, DATE_SUB(CURRENT_DATE, INTERVAL 3 DAY), 610),
    ('emma_clark', 'Emma Clark', 'emma.clark@studyforge.ai', 'sha256:aa5969061c710df50f3b9724264a64b8ab3cd41c9b3f62f73f19bf8cb444d9a0', 'USER', 'ACTIVE', 'Writes about practical product UX, Markdown tools, and learning workflows.', '/avatars/emma-clark.svg', '/banners/editor-tools.svg', 13, 1260, DATE_SUB(CURRENT_DATE, INTERVAL 1 DAY), 780),
    ('noah_kim', 'Noah Kim', 'noah.kim@studyforge.ai', 'sha256:aa5969061c710df50f3b9724264a64b8ab3cd41c9b3f62f73f19bf8cb444d9a0', 'USER', 'ACTIVE', 'Keeps weekly review logs for interviews, systems thinking, and writing practice.', '/avatars/noah-kim.svg', '/banners/weekly-review.svg', 9, 820, DATE_SUB(CURRENT_DATE, INTERVAL 4 DAY), 530),
    ('ops_admin', 'StudyForge 运营', 'ops.admin@studyforge.ai', 'sha256:d120c09f9b058fd4177b5a79917dc5a67769b9b0d09ccaaf414a30e06d63898b', 'ADMIN', 'ACTIVE', '维护内容质量、AI 配置和社区运行状态。', '/avatars/ops-admin.svg', '/banners/system-console.svg', 18, 1710, DATE_SUB(CURRENT_DATE, INTERVAL 1 DAY), 1200)
ON DUPLICATE KEY UPDATE
    display_name = VALUES(display_name),
    email = VALUES(email),
    password_hash = VALUES(password_hash),
    role = VALUES(role),
    status = VALUES(status),
    bio = VALUES(bio),
    avatar_url = VALUES(avatar_url),
    banner_url = VALUES(banner_url),
    community_level = VALUES(community_level),
    experience_points = VALUES(experience_points),
    last_login_reward_date = VALUES(last_login_reward_date),
    reputation_score = VALUES(reputation_score);

INSERT INTO user_follows (follower_id, following_id, status, created_time)
SELECT f.user_id, t.user_id, 'ACTIVE', NOW() - INTERVAL 18 DAY
FROM users f JOIN users t ON t.username = 'chen_jiayi'
WHERE f.username IN ('li_minghao', 'zhao_yiran', 'emma_clark')
UNION ALL SELECT f.user_id, t.user_id, 'ACTIVE', NOW() - INTERVAL 16 DAY FROM users f JOIN users t ON t.username = 'emma_clark' WHERE f.username IN ('chen_jiayi', 'noah_kim')
UNION ALL SELECT f.user_id, t.user_id, 'ACTIVE', NOW() - INTERVAL 14 DAY FROM users f JOIN users t ON t.username = 'zhao_yiran' WHERE f.username IN ('chen_jiayi', 'wang_yu')
UNION ALL SELECT f.user_id, t.user_id, 'ACTIVE', NOW() - INTERVAL 12 DAY FROM users f JOIN users t ON t.username = 'li_minghao' WHERE f.username IN ('chen_jiayi', 'ops_admin')
UNION ALL SELECT f.user_id, t.user_id, 'ACTIVE', NOW() - INTERVAL 10 DAY FROM users f JOIN users t ON t.username = 'wang_yu' WHERE f.username IN ('li_minghao', 'zhao_yiran')
UNION ALL SELECT f.user_id, t.user_id, 'ACTIVE', NOW() - INTERVAL 8 DAY FROM users f JOIN users t ON t.username = 'noah_kim' WHERE f.username IN ('emma_clark', 'chen_jiayi')
UNION ALL SELECT f.user_id, t.user_id, 'ACTIVE', NOW() - INTERVAL 7 DAY FROM users f JOIN users t ON t.username = 'ops_admin' WHERE f.username IN ('chen_jiayi', 'li_minghao');

INSERT INTO friend_requests (requester_id, addressee_id, message, status, processed_time, created_time)
SELECT requester.user_id, addressee.user_id, '最近都在整理 Markdown 编辑器和复习卡片，想加好友后继续交流。', 'ACCEPTED', NOW() - INTERVAL 15 DAY, NOW() - INTERVAL 16 DAY
FROM users requester JOIN users addressee ON addressee.username = 'chen_jiayi'
WHERE requester.username = 'emma_clark'
UNION ALL
SELECT requester.user_id, addressee.user_id, '我在看你写的 Service 层文章，后面想请教事务边界。', 'ACCEPTED', NOW() - INTERVAL 11 DAY, NOW() - INTERVAL 12 DAY
FROM users requester JOIN users addressee ON addressee.username = 'wang_yu'
WHERE requester.username = 'chen_jiayi'
UNION ALL
SELECT requester.user_id, addressee.user_id, '想一起完善学习复盘模板，可以加个好友吗？', 'ACCEPTED', NOW() - INTERVAL 8 DAY, NOW() - INTERVAL 9 DAY
FROM users requester JOIN users addressee ON addressee.username = 'zhao_yiran'
WHERE requester.username = 'chen_jiayi'
UNION ALL
SELECT requester.user_id, addressee.user_id, '看到你也在整理技术面试学习记录，想交流一下每周复盘方式。', 'PENDING', NULL, NOW() - INTERVAL 4 HOUR
FROM users requester JOIN users addressee ON addressee.username = 'chen_jiayi'
WHERE requester.username = 'noah_kim'
UNION ALL
SELECT requester.user_id, addressee.user_id, '运营后台这篇写得很实用，想保持联系。', 'PENDING', NULL, NOW() - INTERVAL 2 HOUR
FROM users requester JOIN users addressee ON addressee.username = 'li_minghao'
WHERE requester.username = 'chen_jiayi';

INSERT INTO friendships (user_low_id, user_high_id, status, created_time)
SELECT LEAST(a.user_id, b.user_id), GREATEST(a.user_id, b.user_id), 'ACTIVE', NOW() - INTERVAL 15 DAY
FROM users a JOIN users b ON b.username = 'chen_jiayi'
WHERE a.username = 'emma_clark'
UNION ALL
SELECT LEAST(a.user_id, b.user_id), GREATEST(a.user_id, b.user_id), 'ACTIVE', NOW() - INTERVAL 11 DAY
FROM users a JOIN users b ON b.username = 'wang_yu'
WHERE a.username = 'chen_jiayi'
UNION ALL
SELECT LEAST(a.user_id, b.user_id), GREATEST(a.user_id, b.user_id), 'ACTIVE', NOW() - INTERVAL 8 DAY
FROM users a JOIN users b ON b.username = 'zhao_yiran'
WHERE a.username = 'chen_jiayi';

INSERT INTO friend_messages (sender_id, receiver_id, content, read_flag, created_time)
SELECT sender.user_id, receiver.user_id, '我把 Markdown 编辑器的图片上传流程又整理了一版，你有空可以看看。', 1, NOW() - INTERVAL 3 DAY
FROM users sender JOIN users receiver ON receiver.username = 'chen_jiayi'
WHERE sender.username = 'emma_clark'
UNION ALL
SELECT sender.user_id, receiver.user_id, '看到了，保留 Markdown 源文这个决定很稳，我准备按这个思路继续做预览。', 1, NOW() - INTERVAL 3 DAY + INTERVAL 30 MINUTE
FROM users sender JOIN users receiver ON receiver.username = 'emma_clark'
WHERE sender.username = 'chen_jiayi'
UNION ALL
SELECT sender.user_id, receiver.user_id, '你那篇复习卡片文章里，问题设计的部分很适合放进我的周复盘。', 0, NOW() - INTERVAL 1 DAY
FROM users sender JOIN users receiver ON receiver.username = 'zhao_yiran'
WHERE sender.username = 'chen_jiayi';

INSERT INTO user_experience_logs (user_id, action_type, experience_delta, source_id, created_date, created_time)
SELECT user_id, 'DAILY_LOGIN', 15, NULL, DATE_SUB(CURRENT_DATE, INTERVAL 1 DAY), NOW() - INTERVAL 1 DAY
FROM users
WHERE username IN ('chen_jiayi', 'zhao_yiran', 'emma_clark', 'ops_admin')
UNION ALL
SELECT user_id, 'PUBLISH_POST', 45, NULL, DATE_SUB(CURRENT_DATE, INTERVAL 3 DAY), NOW() - INTERVAL 3 DAY
FROM users
WHERE username IN ('chen_jiayi', 'emma_clark', 'zhao_yiran', 'li_minghao', 'wang_yu', 'noah_kim')
ON DUPLICATE KEY UPDATE experience_delta = VALUES(experience_delta);

INSERT INTO integration_settings (setting_key, setting_value, secret_flag, updated_by)
SELECT 'ai.base_url', 'https://api.siliconflow.cn/v1', 0, u.user_id FROM users u WHERE u.username = 'ops_admin'
ON DUPLICATE KEY UPDATE setting_value = VALUES(setting_value), secret_flag = VALUES(secret_flag), updated_by = VALUES(updated_by);

INSERT INTO integration_settings (setting_key, setting_value, secret_flag, updated_by)
SELECT 'ai.api_key', '', 1, u.user_id FROM users u WHERE u.username = 'ops_admin'
ON DUPLICATE KEY UPDATE setting_value = VALUES(setting_value), secret_flag = VALUES(secret_flag), updated_by = VALUES(updated_by);

INSERT INTO integration_settings (setting_key, setting_value, secret_flag, updated_by)
SELECT 'ai.chat_model', 'deepseek-ai/DeepSeek-V4-Flash', 0, u.user_id FROM users u WHERE u.username = 'ops_admin'
ON DUPLICATE KEY UPDATE setting_value = VALUES(setting_value), secret_flag = VALUES(secret_flag), updated_by = VALUES(updated_by);

INSERT INTO integration_settings (setting_key, setting_value, secret_flag, updated_by)
SELECT 'image.base_url', 'https://api.hiyo.top/v1', 0, u.user_id FROM users u WHERE u.username = 'ops_admin'
ON DUPLICATE KEY UPDATE setting_value = VALUES(setting_value), secret_flag = VALUES(secret_flag), updated_by = VALUES(updated_by);

INSERT INTO integration_settings (setting_key, setting_value, secret_flag, updated_by)
SELECT 'image.api_key', '', 1, u.user_id FROM users u WHERE u.username = 'ops_admin'
ON DUPLICATE KEY UPDATE setting_value = VALUES(setting_value), secret_flag = VALUES(secret_flag), updated_by = VALUES(updated_by);

INSERT INTO integration_settings (setting_key, setting_value, secret_flag, updated_by)
SELECT 'image.model', 'gpt-image-2', 0, u.user_id FROM users u WHERE u.username = 'ops_admin'
ON DUPLICATE KEY UPDATE setting_value = VALUES(setting_value), secret_flag = VALUES(secret_flag), updated_by = VALUES(updated_by);

INSERT INTO integration_settings (setting_key, setting_value, secret_flag, updated_by)
SELECT 'image.size', '1536x1024', 0, u.user_id FROM users u WHERE u.username = 'ops_admin'
ON DUPLICATE KEY UPDATE setting_value = VALUES(setting_value), secret_flag = VALUES(secret_flag), updated_by = VALUES(updated_by);

INSERT INTO integration_settings (setting_key, setting_value, secret_flag, updated_by)
SELECT 'voice.base_url', 'https://api.siliconflow.cn/v1', 0, u.user_id FROM users u WHERE u.username = 'ops_admin'
ON DUPLICATE KEY UPDATE setting_value = VALUES(setting_value), secret_flag = VALUES(secret_flag), updated_by = VALUES(updated_by);

INSERT INTO integration_settings (setting_key, setting_value, secret_flag, updated_by)
SELECT 'voice.api_key', '', 1, u.user_id FROM users u WHERE u.username = 'ops_admin'
ON DUPLICATE KEY UPDATE setting_value = VALUES(setting_value), secret_flag = VALUES(secret_flag), updated_by = VALUES(updated_by);

INSERT INTO integration_settings (setting_key, setting_value, secret_flag, updated_by)
SELECT 'voice.model', 'FunAudioLLM/CosyVoice2-0.5B', 0, u.user_id FROM users u WHERE u.username = 'ops_admin'
ON DUPLICATE KEY UPDATE setting_value = VALUES(setting_value), secret_flag = VALUES(secret_flag), updated_by = VALUES(updated_by);

INSERT INTO integration_settings (setting_key, setting_value, secret_flag, updated_by)
SELECT 'voice.name', 'FunAudioLLM/CosyVoice2-0.5B:alex', 0, u.user_id FROM users u WHERE u.username = 'ops_admin'
ON DUPLICATE KEY UPDATE setting_value = VALUES(setting_value), secret_flag = VALUES(secret_flag), updated_by = VALUES(updated_by);

INSERT INTO posts (author_id, category_id, original_language, status, featured, like_count, favorite_count, comment_count, view_count, hot_score)
SELECT u.user_id, c.category_id, 'zh_CN', 'PUBLISHED', 1, 18, 10, 2, 426, 98.40
FROM users u JOIN categories c ON c.category_code = 'TECHNOLOGY'
WHERE u.username = 'chen_jiayi';
SET @post_vue_state = LAST_INSERT_ID();

INSERT INTO post_i18n (post_id, language_code, title, summary, content, content_format, ai_tags, source_type)
VALUES (
    @post_vue_state,
    'zh_CN',
    'Vue 知识流页面的状态设计：从请求到缓存',
    '把加载、错误、筛选、语言和列表状态拆清楚，知识流页面会更稳，也更容易维护。',
    '## 为什么先整理状态

知识流页面看起来只是一个列表，但真正运行时会同时处理很多状态：用户当前选择的主题、搜索关键词、站点语言、接口加载状态、错误消息、分页位置、登录状态，以及每张卡片的统计数据。如果这些状态都散落在组件里，页面很快会变得难以判断问题来自哪里。

我的做法是先把状态分成三类：

- **请求状态**：`loading`、`errorMessage`、接口返回的 `posts`。
- **视图状态**：当前分类、搜索词、排序方式、当前语言。
- **用户状态**：登录信息、收藏关系、阅读历史。

这样写的好处是，接口失败时只影响请求状态，用户筛选时只影响视图状态，登录变化时也不会把整页逻辑打散。

## 列表数据不要在组件里二次造假

知识流最重要的是可信。卡片上的标题、摘要、热度、语言、评论数、收藏数都应该来自后端返回，而不是前端自己补一个看起来好看的数字。前端可以做格式化，例如把热度保留一位小数、把语言码显示为 `zh_CN` 或 `en_US`，但不要编造业务事实。

我倾向于让接口返回这样的结构：

```ts
interface PostSummary {
  postId: number
  title: string
  summary: string
  languageCode: string
  categoryCode: string
  likeCount: number
  favoriteCount: number
  commentCount: number
  viewCount: number
  hotScore: number
}
```

前端拿到数据后只做两件事：按当前分类过滤、按搜索词过滤。排序和热度计算放在后端，避免不同页面出现不一致的内容顺序。

## 缓存要小心过期

知识流可以做轻量缓存，但缓存对象要跟筛选条件绑定。比如 `categoryCode=TECHNOLOGY` 和 `categoryCode=CAREER` 不应该共用一个列表缓存；站点语言切换后，如果系统规则是不按站点语言隔离知识流，也要确保列表仍然展示帖子原始语言，而不是拿站点语言去覆盖卡片内容。

一个实用策略是：

1. 首次进入页面请求热门内容。
2. 切换主题时优先使用已有列表做本地过滤。
3. 用户点击刷新时重新请求接口。
4. 发布新文章后回到知识流，主动刷新一次。

## 组件边界

我会把页面拆成三个层次：

- `HomeView`：负责请求数据、筛选条件和页面组合。
- `TopicRail`：只负责展示分类和抛出选择事件。
- `KnowledgeCard`：只负责展示一篇文章，不自己请求详情。

这样新增瀑布流、封面图或统计数据时，只需要改卡片和样式，不会影响请求逻辑。后续要加分页，也可以直接在页面层处理。

## 复盘

知识流页面的难点不在视觉，而在状态边界。只要数据来自后端、筛选条件集中管理、卡片组件保持纯展示，页面就会比堆叠临时变量稳定得多。',
    'MARKDOWN',
    'Vue,状态管理,知识流,Axios',
    'ORIGINAL'
);

INSERT INTO posts (author_id, category_id, original_language, status, featured, like_count, favorite_count, comment_count, view_count, hot_score)
SELECT u.user_id, c.category_id, 'en_US', 'PUBLISHED', 1, 15, 9, 2, 388, 94.20
FROM users u JOIN categories c ON c.category_code = 'TECHNOLOGY'
WHERE u.username = 'emma_clark';
SET @post_markdown_composer = LAST_INSERT_ID();

INSERT INTO post_i18n (post_id, language_code, title, summary, content, content_format, ai_tags, source_type)
VALUES (
    @post_markdown_composer,
    'en_US',
    'Designing a Markdown Composer That Users Can Trust',
    'A reliable composer needs plain Markdown, visual shortcuts, preview, image handling, and clear save behavior.',
    '## Start with the writing job

A Markdown composer is not just a large textarea. People use it when they want to turn messy notes into something another person can read. The editor should therefore support two habits at the same time: fast plain-text writing and structured formatting.

The first rule is to keep Markdown as the source of truth. Toolbar actions can insert headings, links, images, code blocks, task lists, or tables, but the user should always be able to see and edit the actual Markdown. This makes the content portable and avoids locking the article into a private editor format.

## The three modes that matter

I usually design the composer with three modes:

- **Write**: a focused Markdown textarea for drafting.
- **Split**: source on one side and rendered preview on the other.
- **Preview**: the final reading layout without editor noise.

Split mode is the default because it teaches the relationship between Markdown and the rendered article. Write mode is useful for longer drafts, and preview mode helps catch layout issues before publishing.

## Toolbar behavior

Toolbar buttons should be small and predictable. A button should not hide a complex workflow unless it clearly says what it does. For example:

```md
## Section title

> A quote that frames the point.

| Decision | Reason |
| --- | --- |
| Keep Markdown visible | Users can recover and edit content easily |
```

For links, prompting for text and URL is better than inserting an unfinished `[](https://)` fragment. For images, upload should return a stable URL and insert `![description](url)` at the cursor.

## Image and cover handling

Images need a storage path, not only a browser preview. A robust flow is:

1. User selects, pastes, or drops an image.
2. Frontend validates type and size.
3. Backend saves the file and writes metadata to the database.
4. API returns the file URL.
5. Composer inserts Markdown or stores the cover URL.

This keeps published articles readable after refresh, across devices, and after the editor state is gone.

## Save behavior

Local draft saving is still useful because it protects the user from accidental navigation. It should not pretend to be a cloud draft. A later database draft feature can add cross-device recovery, version history, and scheduled publishing.

## Final check

The preview should render exactly the same Markdown that will be sent to the server. If preview and published detail use different renderers, users will lose trust quickly. One renderer, sanitized output, and consistent article styles are enough for a dependable first version.',
    'MARKDOWN',
    'Markdown,Editor,Image Upload,UX',
    'ORIGINAL'
);

INSERT INTO posts (author_id, category_id, original_language, status, featured, like_count, favorite_count, comment_count, view_count, hot_score)
SELECT u.user_id, c.category_id, 'zh_CN', 'PUBLISHED', 1, 13, 8, 2, 342, 91.80
FROM users u JOIN categories c ON c.category_code = 'PRODUCTIVITY'
WHERE u.username = 'zhao_yiran';
SET @post_review_cards = LAST_INSERT_ID();

INSERT INTO post_i18n (post_id, language_code, title, summary, content, content_format, ai_tags, source_type)
VALUES (
    @post_review_cards,
    'zh_CN',
    '把一篇长文变成可复习卡片的四步法',
    '读完文章之后立刻提炼问题、答案和关键词，比隔几天重新翻全文更有效。',
    '## 读完不等于学会

很多人收藏了很多文章，但真正复习时还是从头开始找重点。问题不在于文章不够好，而在于阅读结束时没有把内容转成可以回看的形态。复习卡片的目标不是复制全文，而是把文章变成几个可以主动回忆的问题。

## 第一步：写出文章要解决的问题

先用一句话回答：这篇文章到底在解决什么问题？

如果一篇文章讲的是 Vue 状态管理，不要只写“Vue 笔记”。更好的问题是：

> 在一个知识流页面里，哪些状态应该放在页面层，哪些状态应该留给组件自己处理？

问题越具体，之后复习越容易。

## 第二步：提炼三个关键判断

我会从文章中找三个判断，而不是三个段落标题。判断通常长这样：

- 列表排序应该由后端给出，前端只做展示和轻量筛选。
- Markdown 编辑器应该以源码为准，预览只是渲染结果。
- 图片上传必须有服务端存储和数据库记录，不能只依赖本地预览。

这些判断可以直接转成卡片答案。

## 第三步：给每张卡片加关键词

关键词不是标签墙，而是帮助自己下次找到记忆路径。比如一张卡片可以写：

```text
问题：为什么知识流列表不应该按站点语言隐藏帖子？
答案：因为用户发布的内容以原始语言为准，站点语言只影响界面文案，不应该改变社区内容可见性。
关键词：original_language、languageCode、知识流、原文展示
```

## 第四步：安排下一次回看

复习卡片最好在 24 小时内看一次。第一次回看只需要判断自己能不能回答问题；答不上来再打开原文。这样复习会从“重新阅读”变成“检查记忆”。

## 一个简单模板

```md
### 卡片标题

- 问题：
- 简短答案：
- 关键词：
- 需要回到原文的位置：
```

## 小结

长文的价值不在收藏夹里，而在你能不能用自己的话复述。把文章变成卡片，就是把被动阅读改成主动记忆。',
    'MARKDOWN',
    '复习卡片,学习方法,主动回忆',
    'ORIGINAL'
);

INSERT INTO posts (author_id, category_id, original_language, status, featured, like_count, favorite_count, comment_count, view_count, hot_score)
SELECT u.user_id, c.category_id, 'zh_CN', 'PUBLISHED', 0, 11, 5, 2, 286, 87.10
FROM users u JOIN categories c ON c.category_code = 'BUSINESS'
WHERE u.username = 'li_minghao';
SET @post_content_ops = LAST_INSERT_ID();

INSERT INTO post_i18n (post_id, language_code, title, summary, content, content_format, ai_tags, source_type)
VALUES (
    @post_content_ops,
    'zh_CN',
    '学习社区为什么需要可追溯的内容运营',
    '内容平台不只是发帖列表，审核、设置、日志和数据来源都要能被团队追踪。',
    '## 内容运营的核心不是删帖

学习社区的运营目标不是制造热闹，而是让有价值的内容被看到、被保存、被继续讨论。为了做到这一点，平台需要知道内容从哪里来、谁发布、谁修改过、被哪些用户收藏、哪些讨论带来了新的解释。

如果运营后台只能看到一个静态列表，团队很难判断一篇文章为什么变热，也无法快速处理低质量内容。

## 最少需要追踪的几类信息

我认为早期系统至少要保留这些记录：

- 文章作者、分类、原始语言、发布时间。
- 点赞、收藏、评论、阅读等互动计数。
- 举报原因、处理状态、处理人和处理时间。
- AI 摘要、复习卡片、语音朗读等外部服务调用日志。
- 管理端设置变更记录，尤其是 API Key、模型和 Base URL。

这些信息不是为了堆功能，而是为了让团队能回答“为什么会这样”。

## 管理端应该克制

后台不需要一开始就做成复杂 BI 系统。更实用的第一版是：

1. 能登录并识别管理员。
2. 能查看服务健康状态。
3. 能查看内容列表和详情。
4. 能维护 AI 与语音配置。
5. 能记录关键操作。

只要这些链路真实接入数据库，后续增加审核和用户管理就不会推倒重来。

## 数据要真实

运营看板最忌讳为了好看写死数字。假数据会让团队误判系统状态，也会掩盖接口问题。首页可以有产品介绍，但控制台里的指标应该来自真实接口，哪怕一开始只有服务状态和内容列表。

## 小结

可追溯不是大公司才需要的流程，而是学习产品保持信任的基础。用户相信平台，是因为内容、互动和管理动作都能被解释，而不是因为页面上有漂亮数字。',
    'MARKDOWN',
    '内容运营,审计日志,管理端',
    'ORIGINAL'
);

INSERT INTO posts (author_id, category_id, original_language, status, featured, like_count, favorite_count, comment_count, view_count, hot_score)
SELECT u.user_id, c.category_id, 'zh_CN', 'PUBLISHED', 0, 9, 6, 1, 244, 83.60
FROM users u JOIN categories c ON c.category_code = 'FINANCE'
WHERE u.username = 'wang_yu';
SET @post_cashflow = LAST_INSERT_ID();

INSERT INTO post_i18n (post_id, language_code, title, summary, content, content_format, ai_tags, source_type)
VALUES (
    @post_cashflow,
    'zh_CN',
    '给刚开始工作的人的现金流笔记',
    '先把收入、固定支出、弹性支出和应急金分清，再谈投资会更稳。',
    '## 先看现金流，不急着谈收益率

刚开始工作时，很多理财建议会直接谈基金、股票和收益率。但真正影响生活稳定性的，往往是每个月的钱从哪里来、流向哪里、什么时候会紧张。现金流清楚之后，理财才有基础。

## 四个账户就够用

我建议先把钱分成四类：

| 类别 | 用途 | 建议 |
| --- | --- | --- |
| 日常账户 | 吃饭、交通、生活用品 | 每月固定额度 |
| 固定支出 | 房租、保险、订阅、还款 | 发工资后先留出 |
| 应急金 | 生病、换工作、突发支出 | 逐步攒到 3 到 6 个月生活费 |
| 长期账户 | 学习、投资、长期目标 | 不用短期生活钱承担风险 |

分类的目的不是限制生活，而是避免所有支出混在一起，最后不知道钱花到了哪里。

## 每月只记录三件事

记账不需要复杂。每个月固定看三件事：

1. 固定支出占收入多少。
2. 弹性支出有没有明显超出预期。
3. 应急金是否比上个月增加。

只要这三项稳定，现金流就不会太差。

## 投资前的检查

在考虑投资之前，先问自己：

- 是否还有高利率负债？
- 应急金是否够三个月？
- 投资的钱是否三年内不用？
- 是否理解最坏情况下会亏多少？

如果这些问题答不上来，先学习和整理现金流，比急着买产品更重要。

## 小结

刚工作的人最需要的不是复杂模型，而是稳定感。把现金流看清楚，知道自己每个月能承担什么，再去学习投资，会少很多焦虑。',
    'MARKDOWN',
    '现金流,应急金,个人财务',
    'ORIGINAL'
);

INSERT INTO posts (author_id, category_id, original_language, status, featured, like_count, favorite_count, comment_count, view_count, hot_score)
SELECT u.user_id, c.category_id, 'en_US', 'PUBLISHED', 0, 10, 6, 1, 218, 81.30
FROM users u JOIN categories c ON c.category_code = 'CAREER'
WHERE u.username = 'noah_kim';
SET @post_interview_log = LAST_INSERT_ID();

INSERT INTO post_i18n (post_id, language_code, title, summary, content, content_format, ai_tags, source_type)
VALUES (
    @post_interview_log,
    'en_US',
    'How I Prepare a Technical Interview Study Log',
    'A useful interview log records decisions, mistakes, source links, and the next question to practice.',
    '## Why a study log matters

Technical interview preparation can become a pile of links very quickly. I used to save algorithm notes, backend articles, and system design diagrams without recording what I had actually learned from them. A study log fixed that problem because every session ended with a clear artifact.

## The structure I use

Each entry has five sections:

```md
## Topic

## What I understood

## Mistakes I made

## Source links

## Next practice question
```

The structure is simple on purpose. If the template is too long, I will avoid writing it after a hard practice session.

## Record mistakes directly

The most valuable part is the mistake section. A mistake might be:

- I explained caching before clarifying the read/write ratio.
- I used a data structure because it was familiar, not because it fit the operation.
- I forgot to mention failure modes in an API design.

These notes are more useful than copying the correct answer because they show what I need to watch next time.

## Connect notes to review cards

After writing the log, I turn the hardest point into one review card. For example:

> Question: When should a feed service use cursor pagination instead of page numbers?

The answer does not need to be long. It only needs to trigger the reasoning path: stable ordering, new items arriving, duplicate avoidance, and database index usage.

## Weekly review

Every Friday I scan the logs and mark repeated patterns. If the same mistake appears three times, it becomes next week focus. This makes preparation feel less random.

## Final thought

A study log is not a diary. It is a feedback system. The goal is to make the next practice session sharper than the last one.',
    'MARKDOWN',
    'Career,Interview,Study Log',
    'ORIGINAL'
);

INSERT INTO posts (author_id, category_id, original_language, status, featured, like_count, favorite_count, comment_count, view_count, hot_score)
SELECT u.user_id, c.category_id, 'en_US', 'PUBLISHED', 0, 8, 5, 1, 196, 78.90
FROM users u JOIN categories c ON c.category_code = 'PRODUCTIVITY'
WHERE u.username = 'emma_clark';
SET @post_weekly_review = LAST_INSERT_ID();

INSERT INTO post_i18n (post_id, language_code, title, summary, content, content_format, ai_tags, source_type)
VALUES (
    @post_weekly_review,
    'en_US',
    'A Weekly Review System for Learning Projects',
    'A weekly review helps turn scattered reading, code experiments, and questions into a clear next plan.',
    '## The problem with scattered learning

Learning projects often spread across browser tabs, code branches, notebooks, and chat messages. By the end of the week, it can be hard to tell what actually changed. A weekly review gives the work a visible shape.

## Three questions

I use three questions every Sunday:

1. What did I understand better this week?
2. What still feels confusing?
3. What will I do first next week?

The first question prevents progress from disappearing. The second question keeps unresolved topics visible. The third question lowers the cost of restarting.

## Evidence, not impressions

For each answer I link to evidence: an article I finished, a pull request, a Markdown note, a discussion thread, or a review card. This keeps the review honest. If I cannot point to evidence, I rewrite the statement as an intention instead of pretending it is progress.

## A short review template

```md
## This week

- Finished:
- Built:
- Asked:

## Still unclear

- 

## Next Monday

- First task:
- Reading:
- Practice:
```

## Keep it small

The weekly review should take less than twenty minutes. If it becomes a reporting ritual, it will not last. The value comes from noticing patterns and choosing the next move.

## Result

After a month, the reviews become a map of learning decisions. They show which topics repeat, which notes became useful, and which questions deserve deeper work.',
    'MARKDOWN',
    'Weekly Review,Productivity,Learning System',
    'ORIGINAL'
);

INSERT INTO posts (author_id, category_id, original_language, status, featured, like_count, favorite_count, comment_count, view_count, hot_score)
SELECT u.user_id, c.category_id, 'zh_CN', 'PUBLISHED', 0, 12, 7, 2, 304, 86.40
FROM users u JOIN categories c ON c.category_code = 'TECHNOLOGY'
WHERE u.username = 'li_minghao';
SET @post_service_layer = LAST_INSERT_ID();

INSERT INTO post_i18n (post_id, language_code, title, summary, content, content_format, ai_tags, source_type)
VALUES (
    @post_service_layer,
    'zh_CN',
    'Spring MVC + MyBatis 项目里，Service 层应该承担什么',
    'Controller 保持轻量，Mapper 专注 SQL，Service 层负责业务规则、事务和跨模块协调。',
    '## Service 层不是转发器

在 Spring MVC + MyBatis 项目里，最常见的问题是 Service 只做一行调用：Controller 调 Service，Service 原样调 Mapper。这样虽然能跑，但业务规则会慢慢散落到 Controller、Mapper XML 和前端里。

Service 层真正应该承担三件事：业务判断、事务边界、跨模块协调。

## Controller 只处理 Web 语义

Controller 适合做：

- 接收路径参数、查询参数和请求体。
- 读取登录用户。
- 调用 Service。
- 返回统一 JSON。

它不适合写复杂业务规则。例如“发帖时要校验分类是否存在、封面地址是否合法、正文格式是什么”，这些应该在内容 Service 中完成。

## Mapper 只处理数据访问

MyBatis Mapper 应该让 SQL 清晰可控，但不要把业务判断塞进 XML。XML 可以负责查询条件、排序、分页和字段映射；是否允许操作、操作后要写哪些关联记录，应该由 Service 决定。

## 事务边界放在 Service

发布文章时至少会写两张表：

1. `posts`：文章聚合信息。
2. `post_i18n`：标题、摘要、正文和语言。

这两个写入必须在一个事务里。否则主表写成功、正文写失败，列表就会出现打不开的文章。`@Transactional` 放在 Service 方法上，比放在 Controller 或 Mapper 上更符合业务边界。

## 跨模块协调

当用户收藏文章时，互动模块要写收藏表，内容模块要更新收藏数。这里不能让 Controller 同时调两个 Mapper。更好的做法是互动 Service 注入内容模块公开的 Mapper 或 Service，在一个业务方法中完成。

## 小结

Service 层写得好，项目会更像一个产品；Service 层只是转发器，项目很快会变成接口和 SQL 的堆叠。把业务规则集中在 Service，是模块化单体能长期维护的关键。',
    'MARKDOWN',
    'Spring MVC,Service,MyBatis,事务',
    'ORIGINAL'
);

INSERT INTO comments (post_id, user_id, language_code, content)
SELECT @post_vue_state, u.user_id, 'zh_CN', '状态分层这一段很实用，尤其是把请求状态和视图状态分开之后，页面问题确实更好定位。'
FROM users u WHERE u.username = 'li_minghao';
INSERT INTO comments (post_id, user_id, language_code, content)
SELECT @post_vue_state, u.user_id, 'zh_CN', '我之前把搜索词和接口结果混在一个 store 里，后面分页很难处理，这篇给了我一个调整方向。'
FROM users u WHERE u.username = 'zhao_yiran';
INSERT INTO comments (post_id, user_id, language_code, content)
SELECT @post_markdown_composer, u.user_id, 'en_US', 'Keeping Markdown as the source of truth is the part that makes the editor feel safe for long-form notes.'
FROM users u WHERE u.username = 'noah_kim';
INSERT INTO comments (post_id, user_id, language_code, content)
SELECT @post_markdown_composer, u.user_id, 'en_US', 'The image upload flow is clear. I would also add a small image reuse library later.'
FROM users u WHERE u.username = 'emma_clark';
INSERT INTO comments (post_id, user_id, language_code, content)
SELECT @post_review_cards, u.user_id, 'zh_CN', '把卡片问题写具体这一点很关键，问题太泛的时候复习就变成重新阅读。'
FROM users u WHERE u.username = 'chen_jiayi';
INSERT INTO comments (post_id, user_id, language_code, content)
SELECT @post_review_cards, u.user_id, 'zh_CN', '这个模板可以直接放到我的学习页里，后面生成卡片时也能参考。'
FROM users u WHERE u.username = 'wang_yu';
INSERT INTO comments (post_id, user_id, language_code, content)
SELECT @post_content_ops, u.user_id, 'zh_CN', '运营后台不写假指标这一点赞同，宁可少展示，也不要展示不能解释的数据。'
FROM users u WHERE u.username = 'chen_jiayi';
INSERT INTO comments (post_id, user_id, language_code, content)
SELECT @post_content_ops, u.user_id, 'zh_CN', '后续如果加审核队列，可以把举报和 AI 风险建议放在同一个处理页。'
FROM users u WHERE u.username = 'ops_admin';
INSERT INTO comments (post_id, user_id, language_code, content)
SELECT @post_cashflow, u.user_id, 'zh_CN', '四个账户的拆法很容易执行，比一开始就分类几十个消费项轻很多。'
FROM users u WHERE u.username = 'zhao_yiran';
INSERT INTO comments (post_id, user_id, language_code, content)
SELECT @post_interview_log, u.user_id, 'en_US', 'The mistake section is exactly what I usually skip. I will try making it mandatory in my next practice round.'
FROM users u WHERE u.username = 'emma_clark';
INSERT INTO comments (post_id, user_id, language_code, content)
SELECT @post_weekly_review, u.user_id, 'en_US', 'Evidence-based review is a useful framing. It keeps the weekly note from becoming a vague status update.'
FROM users u WHERE u.username = 'noah_kim';
INSERT INTO comments (post_id, user_id, language_code, content)
SELECT @post_service_layer, u.user_id, 'zh_CN', '事务边界这段解释得很清楚，发布文章写两张表就是很好的例子。'
FROM users u WHERE u.username = 'chen_jiayi';
INSERT INTO comments (post_id, user_id, language_code, content)
SELECT @post_service_layer, u.user_id, 'zh_CN', 'Service 不是转发器，这句话可以贴在代码评审清单里。'
FROM users u WHERE u.username = 'wang_yu';

UPDATE comments c
JOIN (
    SELECT
        comment_id,
        ROW_NUMBER() OVER (PARTITION BY post_id ORDER BY created_time ASC, comment_id ASC) AS floor_no
    FROM comments
) ranked ON ranked.comment_id = c.comment_id
SET c.floor_no = ranked.floor_no
WHERE c.floor_no = 0;

INSERT INTO post_likes (post_id, user_id)
SELECT p.post_id, u.user_id
FROM (
    SELECT @post_vue_state AS post_id UNION ALL SELECT @post_markdown_composer UNION ALL SELECT @post_review_cards UNION ALL
    SELECT @post_content_ops UNION ALL SELECT @post_cashflow UNION ALL SELECT @post_interview_log UNION ALL
    SELECT @post_weekly_review UNION ALL SELECT @post_service_layer
) p
JOIN users u ON u.username IN ('chen_jiayi', 'li_minghao', 'zhao_yiran', 'wang_yu', 'emma_clark', 'noah_kim')
WHERE NOT (p.post_id = @post_vue_state AND u.username = 'chen_jiayi')
  AND NOT (p.post_id = @post_markdown_composer AND u.username = 'emma_clark')
  AND NOT (p.post_id = @post_review_cards AND u.username = 'zhao_yiran')
  AND NOT (p.post_id = @post_content_ops AND u.username = 'li_minghao')
  AND NOT (p.post_id = @post_cashflow AND u.username = 'wang_yu')
  AND NOT (p.post_id = @post_interview_log AND u.username = 'noah_kim')
  AND NOT (p.post_id = @post_weekly_review AND u.username = 'emma_clark')
  AND NOT (p.post_id = @post_service_layer AND u.username = 'li_minghao')
LIMIT 42;

INSERT INTO post_favorites (post_id, user_id)
SELECT @post_vue_state, u.user_id FROM users u WHERE u.username IN ('li_minghao', 'zhao_yiran', 'emma_clark')
UNION ALL SELECT @post_markdown_composer, u.user_id FROM users u WHERE u.username IN ('chen_jiayi', 'noah_kim', 'ops_admin')
UNION ALL SELECT @post_review_cards, u.user_id FROM users u WHERE u.username IN ('chen_jiayi', 'wang_yu')
UNION ALL SELECT @post_content_ops, u.user_id FROM users u WHERE u.username IN ('ops_admin', 'chen_jiayi')
UNION ALL SELECT @post_cashflow, u.user_id FROM users u WHERE u.username IN ('zhao_yiran', 'li_minghao')
UNION ALL SELECT @post_interview_log, u.user_id FROM users u WHERE u.username IN ('emma_clark', 'chen_jiayi')
UNION ALL SELECT @post_weekly_review, u.user_id FROM users u WHERE u.username IN ('noah_kim', 'zhao_yiran')
UNION ALL SELECT @post_service_layer, u.user_id FROM users u WHERE u.username IN ('chen_jiayi', 'wang_yu', 'ops_admin');

INSERT INTO favorite_collections (user_id, name, description, visibility, sort_no)
SELECT user_id, '默认收藏', '临时保存，稍后再整理。', 'PRIVATE', 0
FROM users
UNION ALL
SELECT user_id, '前后端项目', 'Vue、Spring MVC、MyBatis 和工程实践文章。', 'PUBLIC', 10
FROM users
WHERE username IN ('chen_jiayi', 'wang_yu', 'emma_clark', 'ops_admin')
UNION ALL
SELECT user_id, '学习复盘', '适合转成复习卡片和周计划的内容。', 'PUBLIC', 20
FROM users
WHERE username IN ('chen_jiayi', 'zhao_yiran', 'noah_kim')
UNION ALL
SELECT user_id, '社区运营', '内容治理、审核和知识社区运营资料。', 'PUBLIC', 30
FROM users
WHERE username IN ('li_minghao', 'ops_admin')
UNION ALL
SELECT user_id, '职业成长', '面试、工作记录和长期能力建设。', 'PUBLIC', 40
FROM users
WHERE username IN ('emma_clark', 'noah_kim');

INSERT INTO favorite_collection_items (collection_id, post_id, user_id, created_time)
SELECT fc.collection_id, @post_markdown_composer, fc.user_id, NOW() - INTERVAL 9 DAY
FROM favorite_collections fc JOIN users u ON u.user_id = fc.user_id
WHERE u.username = 'chen_jiayi' AND fc.name = '前后端项目'
UNION ALL SELECT fc.collection_id, @post_service_layer, fc.user_id, NOW() - INTERVAL 8 DAY FROM favorite_collections fc JOIN users u ON u.user_id = fc.user_id WHERE u.username = 'chen_jiayi' AND fc.name = '前后端项目'
UNION ALL SELECT fc.collection_id, @post_review_cards, fc.user_id, NOW() - INTERVAL 7 DAY FROM favorite_collections fc JOIN users u ON u.user_id = fc.user_id WHERE u.username = 'chen_jiayi' AND fc.name = '学习复盘'
UNION ALL SELECT fc.collection_id, @post_interview_log, fc.user_id, NOW() - INTERVAL 6 DAY FROM favorite_collections fc JOIN users u ON u.user_id = fc.user_id WHERE u.username = 'chen_jiayi' AND fc.name = '学习复盘'
UNION ALL SELECT fc.collection_id, @post_content_ops, fc.user_id, NOW() - INTERVAL 5 DAY FROM favorite_collections fc JOIN users u ON u.user_id = fc.user_id WHERE u.username = 'li_minghao' AND fc.name = '社区运营'
UNION ALL SELECT fc.collection_id, @post_vue_state, fc.user_id, NOW() - INTERVAL 4 DAY FROM favorite_collections fc JOIN users u ON u.user_id = fc.user_id WHERE u.username = 'li_minghao' AND fc.name = '默认收藏'
UNION ALL SELECT fc.collection_id, @post_cashflow, fc.user_id, NOW() - INTERVAL 3 DAY FROM favorite_collections fc JOIN users u ON u.user_id = fc.user_id WHERE u.username = 'zhao_yiran' AND fc.name = '学习复盘'
UNION ALL SELECT fc.collection_id, @post_weekly_review, fc.user_id, NOW() - INTERVAL 2 DAY FROM favorite_collections fc JOIN users u ON u.user_id = fc.user_id WHERE u.username = 'zhao_yiran' AND fc.name = '学习复盘'
UNION ALL SELECT fc.collection_id, @post_review_cards, fc.user_id, NOW() - INTERVAL 2 DAY FROM favorite_collections fc JOIN users u ON u.user_id = fc.user_id WHERE u.username = 'wang_yu' AND fc.name = '默认收藏'
UNION ALL SELECT fc.collection_id, @post_service_layer, fc.user_id, NOW() - INTERVAL 1 DAY FROM favorite_collections fc JOIN users u ON u.user_id = fc.user_id WHERE u.username = 'wang_yu' AND fc.name = '前后端项目'
UNION ALL SELECT fc.collection_id, @post_vue_state, fc.user_id, NOW() - INTERVAL 12 HOUR FROM favorite_collections fc JOIN users u ON u.user_id = fc.user_id WHERE u.username = 'emma_clark' AND fc.name = '前后端项目'
UNION ALL SELECT fc.collection_id, @post_interview_log, fc.user_id, NOW() - INTERVAL 10 HOUR FROM favorite_collections fc JOIN users u ON u.user_id = fc.user_id WHERE u.username = 'emma_clark' AND fc.name = '职业成长'
UNION ALL SELECT fc.collection_id, @post_markdown_composer, fc.user_id, NOW() - INTERVAL 8 HOUR FROM favorite_collections fc JOIN users u ON u.user_id = fc.user_id WHERE u.username = 'noah_kim' AND fc.name = '默认收藏'
UNION ALL SELECT fc.collection_id, @post_weekly_review, fc.user_id, NOW() - INTERVAL 6 HOUR FROM favorite_collections fc JOIN users u ON u.user_id = fc.user_id WHERE u.username = 'noah_kim' AND fc.name = '学习复盘'
UNION ALL SELECT fc.collection_id, @post_service_layer, fc.user_id, NOW() - INTERVAL 5 HOUR FROM favorite_collections fc JOIN users u ON u.user_id = fc.user_id WHERE u.username = 'ops_admin' AND fc.name = '前后端项目'
UNION ALL SELECT fc.collection_id, @post_content_ops, fc.user_id, NOW() - INTERVAL 4 HOUR FROM favorite_collections fc JOIN users u ON u.user_id = fc.user_id WHERE u.username = 'ops_admin' AND fc.name = '社区运营';

INSERT INTO post_view_history (post_id, user_id, view_time)
SELECT @post_vue_state, u.user_id, NOW() - INTERVAL 7 HOUR FROM users u WHERE u.username = 'li_minghao'
UNION ALL SELECT @post_markdown_composer, u.user_id, NOW() - INTERVAL 6 HOUR FROM users u WHERE u.username = 'chen_jiayi'
UNION ALL SELECT @post_review_cards, u.user_id, NOW() - INTERVAL 5 HOUR FROM users u WHERE u.username = 'wang_yu'
UNION ALL SELECT @post_content_ops, u.user_id, NOW() - INTERVAL 4 HOUR FROM users u WHERE u.username = 'ops_admin'
UNION ALL SELECT @post_cashflow, u.user_id, NOW() - INTERVAL 3 HOUR FROM users u WHERE u.username = 'zhao_yiran'
UNION ALL SELECT @post_interview_log, u.user_id, NOW() - INTERVAL 2 HOUR FROM users u WHERE u.username = 'emma_clark'
UNION ALL SELECT @post_weekly_review, u.user_id, NOW() - INTERVAL 1 HOUR FROM users u WHERE u.username = 'noah_kim'
UNION ALL SELECT @post_service_layer, u.user_id, NOW() - INTERVAL 30 MINUTE FROM users u WHERE u.username = 'chen_jiayi';

UPDATE posts p
LEFT JOIN (SELECT post_id, COUNT(*) AS cnt FROM comments WHERE status = 'VISIBLE' GROUP BY post_id) c ON c.post_id = p.post_id
LEFT JOIN (SELECT post_id, COUNT(*) AS cnt FROM post_likes GROUP BY post_id) l ON l.post_id = p.post_id
LEFT JOIN (SELECT post_id, COUNT(*) AS cnt FROM post_favorites GROUP BY post_id) f ON f.post_id = p.post_id
SET p.comment_count = COALESCE(c.cnt, 0),
    p.like_count = COALESCE(l.cnt, 0),
    p.favorite_count = COALESCE(f.cnt, 0);

INSERT INTO reports (
    post_id,
    reporter_id,
    reason,
    status,
    ai_risk_level,
    ai_suggestion,
    processed_by,
    processed_time,
    created_time
)
SELECT @post_content_ops,
       reporter.user_id,
       '文章里提到的运营指标比较绝对，担心新用户会把它当成固定承诺，希望管理员确认表达是否需要调整。',
       'PENDING',
       'MEDIUM',
       '建议核对正文中的指标描述。若属于经验分享，可以保留；若像平台承诺，建议要求作者改写。',
       NULL,
       NULL,
       NOW() - INTERVAL 5 HOUR
FROM users reporter
WHERE reporter.username = 'wang_yu'
UNION ALL
SELECT @post_cashflow,
       reporter.user_id,
       '这篇现金流文章有不少个人理财建议，希望补充风险提示，避免读者直接照搬。',
       'PENDING',
       'MEDIUM',
       '建议审核是否包含明确收益承诺。若没有违规，可以保留并提示作者补充免责声明。',
       NULL,
       NULL,
       NOW() - INTERVAL 3 HOUR
FROM users reporter
WHERE reporter.username = 'emma_clark'
UNION ALL
SELECT @post_markdown_composer,
       reporter.user_id,
       '我以为示例里的编辑器链接会跳到外部广告，复核后发现只是产品工具说明。',
       'REJECTED',
       'LOW',
       '举报理由风险较低，正文未发现广告导流。建议驳回并保留文章。',
       admin.user_id,
       NOW() - INTERVAL 1 DAY,
       NOW() - INTERVAL 2 DAY
FROM users reporter
JOIN users admin ON admin.username = 'ops_admin'
WHERE reporter.username = 'li_minghao';

INSERT INTO help_requests (user_id, title, description, category_id, status, reward_points)
SELECT u.user_id,
       'Markdown 编辑器粘贴图片后，怎样避免上传重复文件？',
       '我在实现粘贴图片自动上传时发现，同一张图片连续粘贴会生成多个文件记录。现在想知道前端应该做哈希去重，还是后端根据文件内容去重更合适？希望能兼顾实现成本和数据一致性。',
       c.category_id,
       'OPEN',
       30
FROM users u JOIN categories c ON c.category_code = 'TECHNOLOGY'
WHERE u.username = 'chen_jiayi';
SET @help_image_dedupe = LAST_INSERT_ID();

INSERT INTO help_requests (user_id, title, description, category_id, status, reward_points)
SELECT u.user_id,
       '复习卡片应该按文章保存，还是按用户保存？',
       '如果同一篇文章被很多用户生成复习卡片，卡片内容可能和用户的学习目标有关。想讨论一下数据库设计：复习卡片是挂在文章下面共享，还是挂在用户下面作为个人学习记录？',
       c.category_id,
       'OPEN',
       20
FROM users u JOIN categories c ON c.category_code = 'PRODUCTIVITY'
WHERE u.username = 'zhao_yiran';
SET @help_review_card_scope = LAST_INSERT_ID();

INSERT INTO help_requests (user_id, title, description, category_id, status, reward_points)
SELECT u.user_id,
       'How should the feed handle mixed-language search?',
       'The feed now shows posts in their original language. I am trying to decide whether keyword search should match only the visible original text or also use AI-generated tags across languages. I would like a practical first version.',
       c.category_id,
       'OPEN',
       25
FROM users u JOIN categories c ON c.category_code = 'TECHNOLOGY'
WHERE u.username = 'emma_clark';
SET @help_mixed_search = LAST_INSERT_ID();

INSERT INTO help_answers (help_id, user_id, content, is_accepted)
SELECT @help_image_dedupe, u.user_id,
       '第一版建议先在后端做内容哈希。前端粘贴和拖放都可能绕过同一段逻辑，后端根据文件 bytes 计算 hash 后再决定复用或新建记录，会更一致。前端可以只做上传中的临时去重，避免用户连续触发两次。',
       0
FROM users u WHERE u.username = 'li_minghao';

INSERT INTO help_answers (help_id, user_id, content, is_accepted)
SELECT @help_review_card_scope, u.user_id,
       '建议按用户保存。复习卡片是个人理解和记忆路径，应该挂在 user_id 下；文章级别可以缓存 AI 摘要，但卡片最好允许用户编辑和保留自己的版本。',
       1
FROM users u WHERE u.username = 'chen_jiayi';

INSERT INTO help_answers (help_id, user_id, content, is_accepted)
SELECT @help_mixed_search, u.user_id,
       'For the first version, search the original visible text and tags only. Cross-language semantic search can come later after tags and embeddings are trustworthy. This keeps the feed behavior easy to explain.',
       0
FROM users u WHERE u.username = 'noah_kim';

UPDATE help_answers a
JOIN (
    SELECT
        answer_id,
        ROW_NUMBER() OVER (PARTITION BY help_id ORDER BY created_time ASC, answer_id ASC) AS floor_no
    FROM help_answers
) ranked ON ranked.answer_id = a.answer_id
SET a.floor_no = ranked.floor_no
WHERE a.floor_no = 0;

INSERT INTO admin_audit_logs (admin_id, target_type, target_id, action_type, remark)
SELECT u.user_id, 'integration_settings', 0, 'UPDATE_INTEGRATION_SETTINGS', 'AI, voice, and image provider settings initialized for the local StudyForge environment.'
FROM users u WHERE u.username = 'ops_admin';
