# StudyForge AI Frontend

前端采用 Vue 3 + Vite + TypeScript + Axios，按前后端分离方式对接 Spring MVC JSON API。

## 目录结构

```text
studyforge-frontend/
  apps/
    portal-web/
    knowledge-web/
      src/
        api/          # Axios 实例与接口封装
        assets/       # 全局样式与静态资源
        components/   # 可复用 UI 组件
        layouts/      # 页面框架
        router/       # Vue Router
        stores/       # Pinia 状态管理
        types/        # 接口契约类型
        views/        # 业务页面
```

## 本地开发

后端需要先运行在 `http://localhost:8080`。

```bash
cd /home/lynn/Studyforge_AI/studyforge-frontend
npm install
npm run dev:portal
npm run dev:knowledge
```

默认访问地址：

```text
控制台: http://localhost:5173
用户知识平台: http://localhost:5174
```

也可以使用根目录脚本：

```bash
cd /home/lynn/Studyforge_AI
./scripts/start_frontend.sh
./scripts/start_knowledge_web.sh
```

两个 Vite 开发服务器都会把 `/api/*` 代理到 `http://localhost:8080`，前端代码统一访问 `/api/v1`。

## 模块约定

- 页面只调用 `src/api` 中的函数，不直接写 Axios 请求。
- 后端 JSON 包装结构统一映射为 `ApiEnvelope<T>`。
- 登录态由 `stores/auth.ts` 管理，并持久化到浏览器 `localStorage`。
- 多语言偏好由 `stores/preferences.ts` 管理，当前与后端的 `languageCode` 参数保持一致。
