# 黛西健康智慧养老后台管理系统

本仓库根据 [黛西健康_概要设计报告.md](D:/agent_project/elder_AI/黛西健康_概要设计报告.md) 生成，包含一个前端管理后台项目和一个后端 REST API 项目。当前版本已从早期 mock 数据模式切换为默认连接 MySQL，并在启动时自动建表、写入演示数据。

## 项目结构

```text
D:\agent_project\elder_AI
├─ daisy-health-admin-frontend    前端 Vue 3 管理后台
├─ daisy-health-admin-backend     后端 Spring Boot API 服务
├─ 黛西健康_概要设计报告.md          概要设计来源文档
├─ README.md                      当前说明文档
└─ .gitignore
```

## 技术栈

前端：

- Vue 3
- Vite
- Element Plus
- Pinia
- Vue Router
- Axios
- ECharts

后端：

- JDK 8
- Spring Boot 2.7.18
- Spring MVC
- MyBatis 风格 Mapper
- Knife4j / Springfox
- MySQL 8 DDL 脚本
- Redis 依赖预留

本机已验证环境：

```text
JDK: 1.8.0_491
Maven: 3.6.3
Node.js: 24.14.0
npm: 11.9.0
```

## 当前完成情况

已完成：

- 前后端两个独立项目骨架
- 后端统一响应结构：`ApiResponse`
- 后端分页结构：`PageResult`
- 后端全局异常处理
- 后端 CORS 跨域配置
- 后端 `mysql` profile，默认连接 MySQL
- 后端 `mock` profile 保留，可作为无数据库演示模式
- 后端核心 REST Controller
- 后端 MySQL 核心表建表脚本
- 后端 MySQL 初始化演示数据脚本
- 前端登录页
- 前端后台主布局：左侧菜单、顶部导航、主内容区
- 前端工作台：指标卡、快捷入口、ECharts 图表
- 前端预约看板
- 前端用户列表：卡片/列表切换
- 前端用户详情：10 个 Tab 结构
- 前端通用列表页：服务、商品、运营、交易、系统设置
- 前端数据分析页
- 前端 Axios 请求封装和本地 fallback 数据
- 新增用户可通过前端表单写入 MySQL
- 通用列表页支持新增服务人员、商品、订单、工单、售后、评价、运营内容、员工、角色、协议等数据，并写入 MySQL

已验证：

- `mvn -DskipTests compile` 后端编译通过
- 后端 Java 编译通过
- 后端数据库服务实现已接入 JDBC/MySQL
- `POST /api/v1/users` 与 `POST /api/v1/products` 已实际验证可写入 MySQL
- `GET /api/v1/dashboard` 可返回 JSON
- 前端 `npm run build` 构建通过
- 前端页面可登录并进入工作台

注意：当前机器检查不到 MySQL/MariaDB 服务，`localhost:3306` 没有监听。运行数据库模式前，需要先安装并启动 MySQL。

已修复的问题：

- MySQL 驱动坐标改为 `com.mysql:mysql-connector-j`
- Knife4j/Springfox 与 Spring Boot 2.7 路径匹配兼容问题，已配置：

```yaml
spring:
  mvc:
    pathmatch:
      matching-strategy: ant_path_matcher
```

## 运行方式

建议开两个终端：一个运行后端，一个运行前端。

### 1. 启动后端

```bat
cd D:\agent_project\elder_AI\daisy-health-admin-backend
mvn spring-boot:run
```

启动成功后控制台应看到：

```text
Tomcat started on port(s): 8080
Started DaisyHealthApplication
```

后端接口测试：

```text
http://127.0.0.1:8080/api/v1/dashboard
```

当前默认使用 `mysql` profile，需要本机 MySQL 正在监听：

```text
localhost:3306
```

默认数据库配置：

```text
数据库：daisy_health
账号：root
密码：root
```

首次启动时后端会自动执行：

```text
daisy-health-admin-backend/src/main/resources/schema.sql
daisy-health-admin-backend/src/main/resources/data.sql
```

如果没有 MySQL，只想临时看页面，也可以使用 mock 模式：

```bat
cd D:\agent_project\elder_AI\daisy-health-admin-backend
mvn spring-boot:run -Dspring-boot.run.profiles=mock
```

### 2. 启动前端

如果没有安装过依赖，先执行：

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

登录信息：

```text
手机号：13800000000
密码：admin123
```

说明：如果 PowerShell 直接执行 `npm run dev` 报执行策略错误，使用 `cmd /c npm run dev`。

## 用 IntelliJ IDEA 运行后端

1. 打开 `D:\IntelliJ IDEA 2023.1.2\bin\idea64.exe`
2. 选择 `Open`
3. 打开目录：

```text
D:\agent_project\elder_AI\daisy-health-admin-backend
```

4. 等待 Maven 依赖加载完成
5. 配置 Project SDK 为：

```text
D:\Java\jdk1.8.0_491
```

6. 配置 Maven Home 为：

```text
D:\apache-maven-3.6.3
```

7. 打开并运行主类：

```text
src/main/java/com/daisy/health/DaisyHealthApplication.java
```

如果 8080 被占用，先查端口：

```bat
netstat -ano | findstr :8080
```

结束占用进程：

```bat
taskkill /PID 进程ID /F
```

## 后端架构

后端入口：

```text
daisy-health-admin-backend/src/main/java/com/daisy/health/DaisyHealthApplication.java
```

核心目录：

```text
common       通用响应、分页、异常处理、Web 配置
controller   REST API 控制器
service      当前 mock 数据服务
model        数据模型示例
mapper       MyBatis Mapper 示例
resources    配置文件和数据库脚本
```

Controller 划分：

```text
AuthController        登录、登出、当前用户、密码修改
DashboardController   工作台、预约看板、数据概览
UserController        用户、标签、报告、消息
ServiceController     服务人员、审核、工单
ProductController     商品
OperationController   运营内容
TradeController       订单、售后、评价
AnalyticsController   数据分析
SystemController      员工、角色、日志、协议
```

统一响应格式：

```json
{
  "code": 0,
  "message": "success",
  "data": {}
}
```

分页响应格式：

```json
{
  "total": 100,
  "list": []
}
```

## 前端架构

前端入口：

```text
daisy-health-admin-frontend/src/main.js
```

核心目录：

```text
api          Axios 封装和 fallback 数据
layout       后台主布局
router       路由配置和登录守卫
stores       Pinia 状态管理
views        页面组件
styles.css   全局样式
```

页面划分：

```text
LoginView          登录页
DashboardView      工作台
ScheduleView       预约看板
UsersView          用户列表
UserDetailView     用户详情
GenericListView    服务/商品/运营/交易/系统设置通用列表
AnalyticsView      数据分析
```

前端请求默认走：

```text
/api/v1
```

Vite 开发代理将 `/api` 转发到：

```text
http://localhost:8080
```

## 主要接口

认证：

```text
POST /api/v1/auth/login
POST /api/v1/auth/logout
GET  /api/v1/auth/profile
PUT  /api/v1/auth/password
```

首页：

```text
GET /api/v1/dashboard
GET /api/v1/appointments
```

用户：

```text
GET  /api/v1/users
POST /api/v1/users
GET  /api/v1/users/{id}
PUT  /api/v1/users/{id}
GET  /api/v1/users/{id}/health-data
GET  /api/v1/users/{id}/medications
```

服务：

```text
GET /api/v1/personnel
GET /api/v1/audits
GET /api/v1/work-orders
```

交易：

```text
GET /api/v1/orders
GET /api/v1/after-sales
GET /api/v1/reviews
```

数据分析：

```text
GET /api/v1/analytics/overview
GET /api/v1/analytics/users/overview
GET /api/v1/analytics/trade/overview
GET /api/v1/analytics/service/work-orders
```

系统：

```text
GET /api/v1/staffs
GET /api/v1/roles
GET /api/v1/logs
GET /api/v1/agreements
```

## 数据库

建表脚本：

```text
daisy-health-admin-backend/src/main/resources/schema.sql
```

当前脚本包含概要设计中的核心表和演示所需扩展表：

```text
user
health_data
medication_record
user_tag
user_tag_rel
service_personnel
work_order
service_order
staff
role
operation_log
product
after_sale
review
operation_content
agreement
```

当前默认已切换真实 MySQL：

```bat
cd D:\agent_project\elder_AI\daisy-health-admin-backend
mvn spring-boot:run
```

如果数据库账号密码不是 `root/root`，修改：

```text
daisy-health-admin-backend/src/main/resources/application.yml
```

`mysql` profile 中的：

```yaml
spring:
  datasource:
    username: root
    password: root
```

## 常见问题

### Maven 提示 MySQL 驱动版本缺失

已修复，当前使用：

```xml
<groupId>com.mysql</groupId>
<artifactId>mysql-connector-j</artifactId>
```

### 启动时报 documentationPluginsBootstrapper 空指针

已修复，原因是 Knife4j/Springfox 与 Spring Boot 2.7 路径匹配策略兼容问题。

### 端口 8080 被占用

```bat
netstat -ano | findstr :8080
taskkill /PID 进程ID /F
```

### 端口 5173 被占用

```bat
netstat -ano | findstr :5173
taskkill /PID 进程ID /F
```

### npm 在 PowerShell 中被执行策略拦截

使用：

```bat
cmd /c npm run dev
```

### 启动时报 MySQL Communications link failure

如果看到：

```text
Communications link failure
Connection refused: connect
```

说明 MySQL 没有在 `localhost:3306` 启动。先启动 MySQL 服务，再重新运行后端。也可以临时用 mock 模式：

```bat
mvn spring-boot:run -Dspring-boot.run.profiles=mock
```

## 后续开发建议

优先级建议：

1. 将更多写操作从“接收并记录日志”完善为真实增删改
2. 接入 JWT 登录鉴权
3. 增加 RBAC 权限模型
4. 完善用户、工单、订单的状态流转校验
5. 增加文件上传能力，用于头像、报告、视频、轮播图
6. 补充单元测试和接口测试
7. 前端按 131 页原型继续拆分更多独立页面
8. 生产构建时对前端图表库和 Element Plus 做按需优化

## 调试约定

如果临时启动后端或前端进行验证，调试完成后应关闭对应进程，避免占用端口。
