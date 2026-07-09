# 黛西健康管理后台前端

Vue 3 SPA 管理后台，按概要设计报告生成。当前版本已实现登录、工作台、预约看板、用户列表、用户详情、通用业务列表和数据分析页面。新增用户和通用业务新增表单已接入后端 POST 接口，后端使用 `mysql` profile 时会实时写入 MySQL。

## 技术栈

```text
Vue 3
Vite
Element Plus
Pinia
Vue Router
Axios
ECharts
```

## 运行

首次运行先安装依赖：

```bat
cd D:\agent_project\elder_AI\daisy-health-admin-frontend
cmd /c npm install
```

启动开发服务器：

```bat
cd D:\agent_project\elder_AI\daisy-health-admin-frontend
cmd /c npm run dev
```

访问：

```text
http://127.0.0.1:5173
```

登录：

```text
手机号：13402832834
密码：753951
```

当前前端是后台管理端，不是老人用户端或服务人员端。菜单和路由会根据后台角色权限过滤。

## 构建

```bat
cmd /c npm run build
```

构建产物：

```text
dist
```

## 项目结构

```text
src
├─ api
│  ├─ http.js        Axios 请求封装
│  └─ fallback.js    后端不可用时的本地展示数据
├─ layout
│  └─ AdminLayout.vue
├─ router
│  └─ index.js
├─ stores
│  └─ auth.js
├─ views
│  ├─ LoginView.vue
│  ├─ DashboardView.vue
│  ├─ ScheduleView.vue
│  ├─ UsersView.vue
│  ├─ UserDetailView.vue
│  ├─ GenericListView.vue
│  └─ AnalyticsView.vue
├─ App.vue
├─ main.js
└─ styles.css
```

## API 代理

开发环境中，`vite.config.js` 将：

```text
/api
```

代理到：

```text
http://localhost:8080
```

因此前端请求 `/api/v1/dashboard` 会转发到后端 `http://localhost:8080/api/v1/dashboard`。

## 当前完成页面

```text
登录页
首页工作台
预约看板
用户列表
用户详情
服务人员/审核/工单列表
商品列表
运营内容列表
订单/售后/评价列表
数据分析
员工/角色/日志列表
```
