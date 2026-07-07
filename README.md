# 黛西健康智慧养老后台管理系统

当前仓库：`D:\agent_project\elder_AI`

当前 Git 分支：`main`

本项目根据 `黛西健康_概要设计报告.md` 和 `D:\agent_project\elder_AI_opencode\原型对比分析报告.md` 生成并持续补齐，包含前端 Vue 管理后台和后端 Spring Boot API。默认使用 MySQL 数据库，启动后自动执行 `schema.sql` 和 `data.sql`。

## 项目结构

```text
D:\agent_project\elder_AI
├─ daisy-health-admin-backend
│  ├─ src/main/java/com/daisy/health
│  │  ├─ common          通用响应、分页、异常、跨域配置
│  │  ├─ controller      REST API 控制器
│  │  ├─ service         AdminDataService、JdbcAdminDataService、MockDataService
│  │  ├─ mapper          Mapper 示例
│  │  └─ model           Model 示例
│  └─ src/main/resources
│     ├─ application.yml MySQL/mock profile 配置
│     ├─ schema.sql      建表和兼容旧库字段扩展
│     └─ data.sql        演示数据
├─ daisy-health-admin-frontend
│  ├─ assets             登录页图片等静态资源
│  └─ src
│     ├─ api             Axios API 封装
│     ├─ layout          后台主布局、个人资料弹窗
│     ├─ router          路由和登录守卫
│     ├─ stores          Pinia 登录状态
│     ├─ views           登录、工作台、用户、通用列表、数据分析
│     └─ styles.css      全局样式
├─ README.md
└─ 黛西健康_概要设计报告.md
```

## 运行方式

后端：

```bat
cd D:\agent_project\elder_AI\daisy-health-admin-backend
mvn spring-boot:run
```

前端：

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
手机号：13800000000
密码：admin123
```

默认数据库：

```text
MySQL: localhost:3306
database: daisy_health
username: root
password: root
```

无数据库临时演示：

```bat
cd D:\agent_project\elder_AI\daisy-health-admin-backend
mvn spring-boot:run -Dspring-boot.run.profiles=mock
```

## 当前完成情况

基础功能：

- 登录页已按参考图重做，左侧使用 `daisy-health-admin-frontend/assets/login_picture.png`
- 工作台、预约看板、用户管理、用户详情、服务、商品、运营、交易、数据、系统设置主结构已完成
- 后端默认接入 MySQL，保留 `mock` profile
- 前端新增、编辑、删除多数业务数据会实时写入数据库
- 已初始化 Git，当前使用 `main` 分支

近期已完成：

- 用户卡片按原型卡片样式重做，并缩小为更适合后台列表的尺寸
- 用户标签支持选择、绑定、标签管理、新增、编辑、删除
- 预约看板支持新建和删除预约
- 个人资料可编辑并保存：姓名、员工编号、手机号码、头像、角色、备注
- 用户详情新增真实数据 Tab：设备信息、报告信息、订单信息、资产信息、内容信息、服务记录
- 健康信息编辑扩展：紧急联系人、紧急联系电话、吸烟/饮酒等后端字段已预留或接入

## 个人资料落库说明

个人资料保存到：

```text
staff
```

字段映射：

```text
姓名       -> staff.name
员工编号   -> staff.staff_no
手机号码   -> staff.phone
头像       -> staff.avatar_url
备注       -> staff.remark
角色       -> staff.role_id
```

角色名称显示来自：

```text
role.name
```

如果编辑资料时输入了不存在的角色名称，后端会创建一条 `role` 记录并把 `staff.role_id` 指向它。

## 数据库表

核心表：

```text
user
health_data
medication_record
user_tag
user_tag_rel
service_personnel
product
service_order
work_order
after_sale
review
operation_content
staff
role
operation_log
agreement
```

Phase 1 已补表：

```text
health_settings
device
report
coupon
user_points
points_record
member_level
points_rule
product_category
service_item
banner
activity
activity_enroll
```

Phase 2 已补表：

```text
topic
recipe
article
disease
institution
video
food
assessment
assessment_result
```

兼容性字段：

```text
user.emergency_contact
user.emergency_phone
user_tag.color
staff.avatar_url
```

## 前端页面覆盖

独立页面：

```text
LoginView.vue          登录页
DashboardView.vue      工作台
ScheduleView.vue       预约看板
UsersView.vue          用户列表和标签管理
UserDetailView.vue     用户详情和扩展 Tab
GenericListView.vue    通用 CRUD 列表页
AnalyticsView.vue      数据分析
```

通用列表已接入的资源：

```text
服务：服务人员、审核、工单
商品：商品、商品分类、服务项目
运营：动态、话题、轮播图、活动、活动报名、食谱、资讯、疾病、机构、视频、食物、测评
交易：订单、售后、评价
用户扩展：设备、报告、健康设置、优惠券、积分、等级、积分规则
系统：员工、角色、日志
```

## 主要 API

认证和个人资料：

```text
POST /api/v1/auth/login
GET  /api/v1/auth/profile
PUT  /api/v1/auth/profile
```

用户：

```text
GET    /api/v1/users
POST   /api/v1/users
GET    /api/v1/users/{id}
PUT    /api/v1/users/{id}
DELETE /api/v1/users/{id}
GET    /api/v1/tags
POST   /api/v1/tags
PUT    /api/v1/tags/{id}
DELETE /api/v1/tags/{id}
PUT    /api/v1/users/{id}/tags
```

预约：

```text
GET    /api/v1/appointments
POST   /api/v1/appointments
DELETE /api/v1/appointments/{id}
```

Phase 1/2 通用资源：

```text
GET/POST/PUT/DELETE /api/v1/devices
GET/POST/PUT/DELETE /api/v1/reports
GET/POST/PUT/DELETE /api/v1/coupons
GET/POST/PUT/DELETE /api/v1/member-levels
GET/POST/PUT/DELETE /api/v1/points-rules
GET/POST/PUT/DELETE /api/v1/product-categories
GET/POST/PUT/DELETE /api/v1/service-items
GET/POST/PUT/DELETE /api/v1/banners
GET/POST/PUT/DELETE /api/v1/topics
GET/POST/PUT/DELETE /api/v1/recipes
GET/POST/PUT/DELETE /api/v1/articles
GET/POST/PUT/DELETE /api/v1/diseases
GET/POST/PUT/DELETE /api/v1/institutions
GET/POST/PUT/DELETE /api/v1/videos
GET/POST/PUT/DELETE /api/v1/foods
GET/POST/PUT/DELETE /api/v1/assessments
```

## 验证记录

最近一次验证：

```text
后端：mvn -DskipTests compile 通过
前端：npm run build 通过
运行验证：Spring Boot 临时启动成功，MySQL schema/data 执行成功
接口验证：个人资料保存、devices/reports/product-categories/banners/topics 查询、topic 新增删除、用户详情扩展数据均通过
进程清理：验证后已关闭 8080/5173 相关服务
```

前端构建仍有 Vite/Rollup 大 chunk 警告，属于体积优化提示，不影响运行。

## 后续建议

下一步可继续做：

- Phase 1 的订单详情页 6 种状态、售后详情页 3 种状态的独立详情页面
- 健康数据 7 种类型的独立趋势和记录页面
- 文件上传：头像、报告、轮播图、视频封面
- JWT 登录鉴权、密码加密、RBAC 权限控制
- Redis 接入和接口测试
