# 黛西健康智慧养老后台管理系统

仓库路径：`D:\agent_project\elder_AI`

当前开发分支：`main`

本项目包含一个 Vue 3 管理后台和一个 Spring Boot 后端 API，默认接入 MySQL 数据库。项目按 `黛西健康_概要设计报告.md` 和 `D:\agent_project\elder_AI_opencode\原型对比分析报告.md` 逐步实现，目前已覆盖后台首页、用户、服务、商品、运营、交易、数据、系统等主要管理模块。

## 最近修改
- 补齐移动端可见的退出登录入口：管理端在抽屉菜单底部提供常驻按钮，老人用户端和服务人员端在头部操作区提供醒目的整行按钮；点击后调用后端退出接口、清理本地登录状态并返回登录页。

- 修复移动端退出登录链路：管理端、老人用户端和服务人员端现在都会先调用 `POST /api/v1/auth/logout`，再清除本地 JWT、用户与权限缓存并返回登录页；即使本地后端暂时不可达，也会完成本地退出，避免残留登录状态。
- 交易订单与履约工单改为严格“一单一工单”：只能从履约工单入口新建并同步生成交易订单，任一侧删除时成对删除，履约状态会同步交易状态。启动时会保留并修复历史数据：重复关联的工单各自复制交易订单，缺少工单的交易订单补建未分派工单，随后通过 `work_order.order_id` 唯一索引防止再次产生一对多。
- 用户端新增“服务评价”：仅本人已完成的服务可评价，每个订单只能提交一次；提交过程会锁定对应订单并检查历史评价，避免重复写入。评价直接写入管理端“交易 -> 评价管理”使用的同一张评价表，提交后管理端刷新即可看到。
- 管理端将原“订单管理 / 工单管理”明确命名为“交易订单 / 履约工单”：两者严格一一对应但职责不同，交易订单负责商品、买家、金额和交易状态，履约工单负责服务人员、服务时间和执行进度，因此保留两个管理视角；新增统一从履约工单入口进行。
- 管理端、用户端和服务人员端的数据列表统一启用分页，每页最多显示 20 条；数据超过 20 条后可使用上一页、页码和下一页切换，筛选或刷新后自动回到有效页。
- 用户可见的登录、鉴权、权限、工单状态校验及系统异常提示已统一改为中文，登录页英文版权说明也已中文化。
- 管理端“全部用户”新增按标签搜索，选择标签后通过 `GET /api/v1/users?tag={标签名}` 只显示包含该标签的用户，清空选择可恢复全部用户。
- 管理端、用户端、服务人员端均可校验当前密码后修改本人密码；新密码至少 6 位，修改后当前 JWT 登录状态保持不变。
- 管理端新增“系统 -> 密码设置”，管理员只能把用户或服务人员密码重置为固定值 `753951`，不能替他们设置任意密码。
- 初始化账号密码继续统一使用 `753951`；种子数据重复执行时不再覆盖已经修改过的密码，确保改密结果在重启后仍然有效。
- 完成 P0 收口：老人端创建 `service_order + work_order` 已加入事务，任一写入失败会整体回滚；管理端不再在接口失败时静默展示前端假数据。
- 预约看板启用日期切换，默认一次加载并保留今天起连续 7 天的数据库工单；初始化数据覆盖 7 天，新增预约后会刷新同一时间窗口。
- 通用搜索入口只保留在工单管理、商品服务管理和订单管理；“全部用户”另提供标签筛选。商品服务状态固定为“上架/下架”，订单状态固定为“待服务/已完成/售后中”。
- 移除未实现的全局搜索和通知入口，避免把装饰控件误认为可用功能。
- 完成移动端适配：管理后台在小屏设备上使用完整抽屉菜单，登录页、工作台、列表、宽表、筛选器、详情、表单、弹窗、预约看板、用户端和服务人员端均可在手机上操作；手机与电脑通过同一台本地后端和 MySQL 数据库共享数据，不依赖云服务器。
- 新增后台安全登录体系：后台登录改为查询 `account + admin_profile`，密码使用 BCrypt 校验，登录成功后返回 JWT；后端 API 统一通过 `Authorization: Bearer <token>` 鉴权。
- 新增 RBAC 权限体系：`role.permissions` 使用 JSON 定义模块权限，后端按 token 中的账号身份和请求路径校验模块权限，前端菜单和路由按权限过滤。
- 新增统一账号表设计：新增 `account`、`admin_profile`、`elderly_profile`、`service_profile`，启动数据会从既有 `staff`、`user`、`service_personnel` 表镜像生成账号资料。
- 新增密码迁移能力：启动时会把 `staff`、`account` 中仍为明文的 `password_hash` 自动转换为 BCrypt 哈希。
- 修复前端本地登录缓存容错：`localStorage` 中如果存在旧的 `"undefined"` 用户或权限缓存，前端会自动清理，不再导致 Vue Router 启动失败。
- 改为统一前端登录入口：管理员、用户、服务人员都从 `http://127.0.0.1:5173` 登录，登录后按 `roleType` 分流到后台管理端、用户工作台或服务人员工作台；已删除独立的 `daisy-health-user-frontend` 和 `daisy-health-service-frontend`。
- 用户模块的等级管理仅保留普通、银卡、金卡三种等级，后端保存时增加等级名称白名单校验。
- 积分规则仅保留签到、完成订单、发布评价三种行为，清理初始化数据中的多余等级和多余规则。
- 等级管理和积分规则页面关闭新增入口，并在编辑时禁用已被其他配置占用的选项，避免唯一键冲突导致保存失败。
- 增加本地上传能力：头像、报告文件、轮播图可上传并保存为 `/uploads/**` 地址；用户头像支持预置头像和自定义上传。
- 修复用户头像上传后旧用户表与统一账号表不同步的问题，自定义头像会立即同步到用户端资料。
- JWT 默认有效期由 2 小时延长为 8 小时。
- 商品与服务项目统一为“商品服务管理”，旧 `service_item` 数据启动时会幂等迁入 `product` 统一目录。
- 预约、管理员工单、用户详情工单和订单统一从商品服务目录选择，金额由后端按目录价格确定，不能手工覆盖。
- 用户端新增商品服务浏览、创建工单和“我的工单”；工单记录创建账号，用户只能读取本人创建的工单，管理员仍可读取全部。
- 新建预约和工单会自动记录创建时间与派单时间，预约看板状态统一显示中文。
- 管理端和老人用户端创建工单时均需明确选择服务人员；只能选择状态启用且审核通过的人员，不再自动分配第一名服务人员。
- 管理端工单列表支持按服务人员和用户查询，并同时展示负责人员与服务客户；用户详情中的服务记录也展示负责人员。
- 超级管理员个人资料支持使用预设头像或上传自定义头像，处理和保存逻辑与用户头像一致。
- 用户端社区活动分为“我的活动”和“可选择的活动”，支持报名及取消报名。
- 老人端待服务工单支持取消和改期；服务中、已完成或已取消工单不能由老人修改。
- 老人端新增“给管理员留言”入口；管理端新增按用户分组的“留言处理”板块，留言可流转为待处理、处理中和已解决。

## 手机与电脑同一 Wi-Fi 测试

### 工作方式

手机只访问电脑上的前端端口 `5173`。前端把 `/api/**` 和 `/uploads/**` 转发到电脑本机的 Spring Boot `8080`，电脑和手机最终读写同一个 MySQL 数据库与同一个 `uploads/` 目录，因此不需要云服务器。

已经打开的页面不会通过 WebSocket 强制推送数据。跨端修改完成后，在另一端刷新页面或点击页面内的“刷新/搜索”即可看到最新数据。不要同时在手机和电脑上编辑同一条记录，后保存的一端会覆盖先保存的内容。

### 1. 准备网络和数据库

1. 电脑和手机连接同一个 Wi-Fi；电脑网络类型建议设置为“专用网络”。
2. 确认 MySQL 已启动，并存在 `daisy_health` 数据库。默认配置为 `root/root`，配置位置是 `daisy-health-admin-backend/src/main/resources/application.yml`。
3. 在电脑 PowerShell 中执行 `ipconfig`，找到 `无线局域网适配器 WLAN` 下的 `IPv4 地址`。本机当前示例是 `10.200.244.66`，实际测试时必须以 `ipconfig` 当时显示的地址为准。
4. 如果 Wi-Fi 开启了“客户端隔离/AP 隔离”，手机将无法访问电脑；请关闭隔离，或改用手机热点让电脑和手机处于同一局域网。

### 2. 启动后端

在第一个 PowerShell 窗口运行：

```powershell
cd D:\agent_project\elder_AI\daisy-health-admin-backend
mvn spring-boot:run
```

看到 `Tomcat started on port(s): 8080` 后再继续。后端只需供电脑本机和 Vite 代理访问，手机不需要直接访问 `8080`。

### 3. 启动前端

首次运行先安装依赖，之后只需要执行启动命令：

```powershell
cd D:\agent_project\elder_AI\daisy-health-admin-frontend
cmd /c npm install
cmd /c npm run dev
```

Vite 会显示类似下面的地址：

```text
Local:   http://localhost:5173/
Network: http://10.200.244.66:5173/
```

电脑使用 `http://127.0.0.1:5173`，手机浏览器使用 Vite 输出的 `Network` 地址。不要在手机上访问 `localhost` 或 `127.0.0.1`，它们指向的是手机自身。

### 4. 放行 Windows 防火墙

第一次启动 Vite 时，如果 Windows 弹出防火墙提示，勾选当前 Wi-Fi 对应的网络并允许访问。如果没有弹窗且手机打不开页面，先用下面的命令查看 WLAN 当前是 `Public` 还是 `Private`：

```powershell
Get-NetConnectionProfile -InterfaceAlias "WLAN" | Select-Object InterfaceAlias, NetworkCategory
```

确认当前连接的是可信任的家庭/测试 Wi-Fi 后，以管理员身份打开 PowerShell，执行以下命令。规则同时适配 Public/Private，但只允许本地子网访问 `5173`：

```powershell
New-NetFirewallRule -DisplayName "Daisy Health Vite 5173" -Direction Inbound -Action Allow -Protocol TCP -LocalPort 5173 -Profile Any -RemoteAddress LocalSubnet
```

测试完成后如需移除该规则：

```powershell
Remove-NetFirewallRule -DisplayName "Daisy Health Vite 5173"
```

### 5. 登录三类移动端页面

三个角色共用同一个登录地址。初始化数据可使用：

| 角色 | 手机号 | 密码 | 登录后页面 |
| --- | --- | --- | --- |
| 管理员 | `13402832834` | `753951` | 管理后台及全部授权功能 |
| 老人用户 | `13800010001` | `753951` | 用户工作台 |
| 服务人员 | `13900020001` | `753951` | 服务人员工单台 |

如果数据库中已经修改过账号或密码，以数据库当前数据为准。切换角色前请先退出登录，避免浏览器保留上一个角色的 JWT。

### 6. 手机功能回归

管理员端：

1. 点击左上角菜单按钮，依次展开首页、用户、服务、商品服务、运营、交易、数据、系统，确认所有有权限的子菜单都能进入。
2. 检查工作台卡片和图表为单列布局；预约看板可左右滑动并能新建、改状态和删除预约。
3. 在用户列表切换卡片/列表，测试新增、编辑、标签、详情和删除；进入用户详情，逐个检查资料、用药、健康数据、设备、报告、订单、优惠券、积分、内容和服务记录。
4. 仅在工单管理、商品服务管理和订单管理测试搜索；其余管理列表测试横向滑动表格、详情、新增、编辑、删除、分页。
5. 检查弹窗在手机宽度内完整显示，弹窗内容可上下滚动，底部取消/保存按钮可点击。

老人用户端：

1. 检查个人资料和头像编辑、健康趋势、用药、设备编辑、报告、订单、优惠券和积分。
2. 浏览商品服务并创建工单，确认必须选择服务人员；检查“我的工单”。
3. 检查社区活动详情与报名、健康资讯详情和健康讲堂视频入口。

服务人员端：

1. 检查工单统计、状态筛选、列表左右滑动和刷新。
2. 打开工单详情，依次验证“开始服务”“完成服务”“取消工单”的可用状态和保存结果。

### 7. 验证手机与电脑双向同步

建议用一条可删除的测试数据，避免污染正式数据：

1. 电脑和手机分别登录管理员账号，进入“运营 → 话题管理”。
2. 在手机新增话题 `移动同步测试-当前时间` 并保存。
3. 在电脑端刷新话题管理，确认该记录、创建时间和状态与手机一致。
4. 在电脑端把话题名称改为 `电脑同步回写-当前时间` 并保存。
5. 在手机端点击搜索或刷新浏览器，确认名称已变更。
6. 在手机端删除该测试话题，电脑端刷新后确认记录消失。
7. 再用老人用户账号在手机创建一张工单；管理员电脑端刷新“服务 → 工单管理”，确认能看到同一工单；服务人员手机端刷新后确认被分配的工单可见。

如果电脑能访问但手机不能访问，按顺序检查：手机 URL 是否使用电脑 WLAN IPv4、两端是否同一 Wi-Fi、防火墙 `5173` 是否放行、路由器是否开启 AP 隔离、Vite 是否仍显示 `Network` 地址。数据页面能打开但保存失败时，再检查后端 `8080` 和 MySQL 是否正常运行。

## 技术栈

前端：

```text
Vue 3
Vite
Vue Router
Pinia
Element Plus
ECharts
Axios
```

后端：

```text
Java 8
Spring Boot 2.7.18
MyBatis starter
MySQL Connector/J
Knife4j
Spring Data Redis 依赖已保留，但当前业务没有强依赖 Redis
```

数据库：

```text
MySQL 8.x 或兼容版本
默认库名：daisy_health
默认账号：root
默认密码：root
```

## 运行方式

先确认本机 Java、Maven 已配置：

```bat
java -version
javac -version
mvn -v
```

后端启动：

```bat
cd D:\agent_project\elder_AI\daisy-health-admin-backend
mvn spring-boot:run
```

前端启动：

```bat
cd D:\agent_project\elder_AI\daisy-health-admin-frontend
cmd /c npm run dev
```

浏览器访问：

```text
http://127.0.0.1:5173
```

默认登录账号：

```text
手机号：13402832834
密码：753951
```

当前登录入口是后台管理端：`http://127.0.0.1:5173`。

无数据库临时演示模式：

```bat
cd D:\agent_project\elder_AI\daisy-health-admin-backend
mvn spring-boot:run -Dspring-boot.run.profiles=mock
```

## 上传素材要求

上传文件默认保存到后端运行目录下的 `uploads/`，该目录不会提交到 Git。

```text
头像：jpg/jpeg/png/webp，建议 512x512 正方形，原图最大 10MB
轮播图：jpg/jpeg/png/webp，建议 1440x480 或 1200x400 横图，最大 5MB
报告：jpg/jpeg/png/webp/pdf，图片建议清晰扫描图，PDF 建议常规 A4 报告，最大 20MB
```

系统预置头像位于：

```text
daisy-health-admin-frontend/public/default-avatars/
```

如需替换预置头像，保持文件名 `avatar-01.svg` 到 `avatar-06.svg`，或同步修改前端默认头像列表。

上传文件命名规则：

```text
分类_yyyyMMdd_HHmmss_SSS_原文件名.扩展名
```

例如：

```text
avatar_20260708_143522_018_liukun.png
banner_20260708_143601_220_home_banner.webp
report_20260708_143655_481_health_report.pdf
```

## 代码架构与项目结构

```text
D:\agent_project\elder_AI
├─ daisy-health-admin-backend
│  ├─ pom.xml
│  └─ src/main
│     ├─ java/com/daisy/health
│     │  ├─ DaisyHealthApplication.java
│     │  ├─ common
│     │  │  ├─ ApiResponse.java
│     │  │  ├─ GlobalExceptionHandler.java
│     │  │  ├─ PageResult.java
│     │  │  └─ WebConfig.java
│     │  ├─ controller
│     │  │  ├─ AuthController.java
│     │  │  ├─ DashboardController.java
│     │  │  ├─ UserController.java
│     │  │  ├─ ServiceController.java
│     │  │  ├─ ProductController.java
│     │  │  ├─ TradeController.java
│     │  │  ├─ OperationController.java
│     │  │  ├─ PhaseResourceController.java
│     │  │  ├─ AnalyticsController.java
│     │  │  └─ SystemController.java
│     │  ├─ service
│     │  │  ├─ AdminDataService.java
│     │  │  ├─ JdbcAdminDataService.java
│     │  │  └─ MockDataService.java
│     │  ├─ mapper
│     │  └─ model
│     └─ resources
│        ├─ application.yml
│        ├─ schema.sql
│        └─ data.sql
├─ daisy-health-admin-frontend
│  ├─ assets
│  │  └─ login_picture.png
│  ├─ package.json
│  └─ src
│     ├─ api
│     │  ├─ http.js
│     ├─ layout
│     │  └─ AdminLayout.vue
│     ├─ router
│     │  └─ index.js
│     ├─ stores
│     │  └─ auth.js
│     ├─ views
│     │  ├─ LoginView.vue
│     │  ├─ DashboardView.vue
│     │  ├─ ScheduleView.vue
│     │  ├─ UsersView.vue
│     │  ├─ UserDetailView.vue
│     │  ├─ GenericListView.vue
│     │  └─ AnalyticsView.vue
│     ├─ App.vue
│     ├─ main.js
│     └─ styles.css
├─ README.md
└─ 黛西健康_概要设计报告.md
```

## 当前完成情况

已完成基础能力：

- 登录页按参考图重做，左侧使用 `daisy-health-admin-frontend/assets/login_picture.png`。
- 登录后主布局改为 8 个主功能入口，左侧深色主导航，右侧显示当前主功能的细分菜单。
- 首页合并“工作台”和“预约看板”入口。
- 工作台使用真实数据库统计：用户标签分布来自 `user_tag_rel`，服务占比来自 `service_order`，趋势来自 `user` 和 `service_order`。
- 用户列表使用卡片式展示，支持标签选择、标签绑定、标签管理。
- 用户详情已将个人信息和健康信息合并为同一个“个人信息”页签，并支持编辑保存。
- 用户详情扩展 Tab 已覆盖用药信息、健康数据、设备信息、报告信息、订单信息、资产信息、内容信息、服务记录。
- 预约看板支持在今天起连续 7 天内切换日期、新建、更新状态和删除；7 天数据均来自 `work_order`。
- 通用列表支持新增、编辑、详情、删除；详情为只读查看，编辑保存写回原业务表。
- 搜索只在工单管理、商品服务管理和订单管理显示；工单可叠加人员/用户条件，其余页面不提供搜索栏。
- 工作台、用户列表、通用列表和预约看板在接口失败时展示明确错误，不再静默切换到前端假数据。
- 关联用户的新增/编辑支持按姓名、昵称、手机号或用户 ID 解析。
- 管理端和用户端创建工单均支持选择服务人员；管理端工单页可按服务人员、用户查询。
- 超级管理员可在右上角个人资料中选择预设头像或上传自定义头像。
- Git 已初始化，默认主分支为 `main`，当前功能开发位于 `codex/fix-p0-query-filters`。

已完成模块：

```text
首页：首页工作台、预约看板
用户：全部用户、设备信息、报告信息、健康设置、优惠券、用户积分、等级管理、积分规则
服务：服务人员、审核管理、工单管理
商品服务：商品服务管理
运营：动态、话题、轮播图、活动、活动报名、食谱、健康资讯、疾病宝典、养老机构、健康讲堂、食物管理、测评管理
交易：订单管理、售后管理、评价管理
数据：数据分析
系统：员工管理、角色管理、操作日志
```

最近已修复的编辑保存问题：

- 优惠券管理：门槛 `minAmount` 可保存。
- 设备信息：状态改为“绑定 / 解绑”。
- 健康设置：心率上限、心率下限可保存。
- 用户积分：累计获得、累计消耗可保存，等级改为“普通 / 银卡 / 金卡”选择。
- 等级管理：权益可保存，等级名称改为“普通 / 银卡 / 金卡”选择。
- 积分规则：行为类型中文展示，去掉每日上限编辑项。
- 服务人员、审核管理：编辑可保存；审核管理补充 `PUT /api/v1/audits/{id}`。
- 工单管理：状态改为中文选择；新增工单需选择客户和服务人员，列表支持按两者查询。
- 服务项目：商品改为下拉选择，描述可保存。
- 活动报名：活动改为下拉选择，状态改为中文。
- 健康讲堂：列表去掉播放数据列。
- 测评管理：类型改为中文，说明和状态可保存，去掉题目 JSON、规则 JSON 编辑项。
- 订单管理：状态改为中文，服务类型可保存。
- 评价管理：评价内容可保存。
- 员工管理：手机号、备注可保存。

## 后端设计

## 安全认证与角色体系

本次新增的是后台管理端的安全登录和 RBAC 权限控制，重点解决原来固定 token、明文密码、接口无权限拦截的问题。

已实现内容：

- `PasswordConfig`：提供 BCrypt `PasswordEncoder`。
- `JwtService`：生成和解析 JWT，token 中包含 `accountId`、`roleType`、`phone`。
- `JwtAuthFilter`：拦截 `/api/v1/**` 请求，放行 `/api/v1/auth/login`，其余接口必须携带 `Authorization: Bearer <token>`。
- `PermissionService`：按请求路径映射权限模块，读取 `role.permissions` 判断是否允许 `view`、`edit`、`delete`。
- `PasswordMigrationRunner`：启动后自动把 `staff`、`account` 中仍为明文的密码迁移成 BCrypt 哈希。
- `JdbcAdminDataService.login()`：改为查询 `account + admin_profile`，校验 BCrypt 密码后签发 JWT。
- `auth.js`、`AdminLayout.vue`、`router/index.js`：前端保存权限，并按权限过滤菜单和路由。

默认后台超级管理员：

```text
手机号：13402832834
密码：753951
角色：超级管理员
权限：{"*":["*"]}
```

当前角色权限模块：

```text
dashboard   工作台、预约看板
users       用户、标签、用户资产、用户健康扩展
service     服务人员、审核、工单
products    商品、分类、服务项目
trade       订单、售后、评价
operations  运营内容、活动、轮播、食谱、文章等
analytics   数据分析
system      员工、角色、日志、协议
```

当前账号体系：

```text
account          统一登录账号表，保存手机号、密码哈希、角色类型、通用头像/昵称
admin_profile    后台管理员资料，关联 account.id 和 role.id
elderly_profile  老人/用户资料镜像，当前用于统一账号设计预留
service_profile  服务人员资料镜像，当前用于统一账号设计预留
```

重要说明：

- 当前只有一个前端入口：`daisy-health-admin-frontend`，所有账号都在同一个登录页登录。
- `staff` 后台员工账号登录后进入 `/dashboard`，按 `role.permissions` 做 RBAC，可管理授权范围内的数据。
- `elderly` 用户账号登录后进入 `/portal/user`，只能查看本人健康数据、用药、设备、报告、订单、优惠券和积分。
- `service` 服务人员账号登录后进入 `/portal/service`，只能查看和更新分配给自己的工单。
- `user` 和 `service_personnel` 已被镜像到 `account`、`elderly_profile`、`service_profile`，用户和服务人员初始密码统一为 `753951`。
- 用户端和服务人员端的数据归属权限由后端按 token 中的 `accountId` 反查资料表完成，前端不传入 owner id。

当前角色入口：

```text
后台管理端：/dashboard
用户工作台：/portal/user
服务人员工作台：/portal/service
后端接口：/api/v1/elderly/**、/api/v1/service-app/**
权限规则：按 roleType 分支校验数据归属，而不是复用后台管理端模块权限
```

后端入口：

```text
daisy-health-admin-backend/src/main/java/com/daisy/health/DaisyHealthApplication.java
```

通用响应：

```text
ApiResponse<T>      统一返回 code/message/data
PageResult<T>       列表返回 total/list
GlobalExceptionHandler 统一异常转 API 响应
WebConfig           CORS 和 Web 配置
PasswordConfig      BCrypt 密码编码器配置
JwtService          JWT 签发和解析
JwtAuthFilter       API token 鉴权和 RBAC 拦截
PermissionService   后台角色权限解析与校验
PasswordMigrationRunner 明文密码启动迁移
```

Controller 分工：

```text
AuthController       登录、个人资料读取和保存
DashboardController  工作台、预约看板、统计入口
UserController       用户、标签、用户详情扩展接口
ServiceController    服务人员、审核、工单
ProductController    商品
TradeController      订单、售后、评价
OperationController  动态、活动、资讯、食谱、疾病、机构、视频、食物、测评
PhaseResourceController  Phase 1/2 补充资源：设备、报告、健康设置、优惠券、积分、等级、规则、分类、服务项目、轮播、活动报名、话题等
AnalyticsController  数据分析
SystemController     员工、角色、协议、日志
```

Service 分工：

```text
AdminDataService      业务能力接口
JdbcAdminDataService  MySQL 实现，当前主要业务逻辑都在这里
MockDataService       mock profile 下使用的内存/模拟数据
```

当前后端采用“控制器按模块拆分，数据访问集中在 `JdbcAdminDataService`”的实现方式。后续如果继续扩大功能，建议逐步把 `JdbcAdminDataService` 拆成用户、服务、交易、运营、系统等多个 Service，避免单文件继续膨胀。

配置文件：

```text
application.yml
```

默认 profile：

```text
spring.profiles.active=mysql
```

MySQL 启动时自动执行：

```text
schema.sql  建表和兼容字段
data.sql    演示数据，主要使用 insert ignore，重复启动不会重复插入同 ID 数据
```

## 前端设计

前端入口：

```text
daisy-health-admin-frontend/src/main.js
```

路由：

```text
daisy-health-admin-frontend/src/router/index.js
```

路由设计：

```text
/login                         登录页
/dashboard                     工作台
/schedule                      预约看板
/users                         用户列表
/users/messages                按用户分组的留言处理
/users/:id                     用户详情
/user-health/:resource         用户健康扩展通用页
/user-assets/:resource         用户资产扩展通用页
/service/personnel             服务人员
/service/audits                审核管理
/service/work-orders           工单管理
/products/:category?           商品服务管理
/product-ext/:resource         旧商品扩展地址（重定向到商品服务管理）
/operations/:resource          运营通用页
/trade/orders                  订单管理
/trade/after-sales             售后管理
/trade/reviews                 评价管理
/analytics                     数据分析
/system/staffs                 员工管理
/system/roles                  角色管理
/system/logs                   操作日志
```

布局：

```text
AdminLayout.vue
```

`AdminLayout.vue` 负责：

- 左侧 8 个主功能入口。
- 右侧二级菜单。
- 页面导航和个人资料入口。
- 个人资料弹窗保存到 `staff` 表，并同步统一账号资料；头像支持预设图片和自定义上传。

API 封装：

```text
src/api/http.js
```

`http.js` 负责：

- Axios 实例。
- token 请求头。
- 统一拆包 `ApiResponse.data`。
- REST 接口方法。
- 前端资源名到后端路径的转换，例如 `healthSettings -> health-settings`。

通用列表：

```text
src/views/GenericListView.vue
```

`GenericListView.vue` 是当前大部分模块的核心页面，包含：

- `columnMap`：各资源列表列定义。
- `createFieldMap`：各资源新增/编辑表单字段。
- `titleMap` 和 `descriptors`：页面标题和说明。
- 新增、编辑、详情、删除逻辑。
- 用户、商品、活动下拉选项加载。

以后要新增一个简单 CRUD 模块，通常要改这些地方：

1. 后端加表：`schema.sql`。
2. 后端加演示数据：`data.sql`。
3. 后端把资源接入 `JdbcAdminDataService.phaseTableName`、`phaseRows`、`phaseCreateValues`、必要时 `phaseUpdateValues`。
4. 后端 Controller 增加路径，或放进 `PhaseResourceController` 的映射数组。
5. 前端 `src/api/http.js` 的 `resourcePaths` 增加路径映射。
6. 前端 `src/router/index.js` 增加路由。
7. 前端 `AdminLayout.vue` 增加菜单入口。
8. 前端 `GenericListView.vue` 增加 `columnMap` 和 `createFieldMap`。

用户详情：

```text
src/views/UserDetailView.vue
```

负责用户详情页个人信息、用药、健康数据、设备、报告、订单、资产、内容、服务记录等扩展信息的展示和维护。

工作台：

```text
src/views/DashboardView.vue
```

负责顶部指标卡、快捷入口、用户标签分布、服务占比、用户趋势图。

## 数据库设计

数据库初始化文件：

```text
daisy-health-admin-backend/src/main/resources/schema.sql
daisy-health-admin-backend/src/main/resources/data.sql
```

统一账号与角色扩展表：

```text
account              统一登录账号表，区分 role_type=staff/elderly/service
admin_profile        后台管理员扩展资料，account_id 同时关联后台员工身份和 role.id
elderly_profile      老人/用户扩展资料，从既有 user 表镜像生成
service_profile      服务人员扩展资料，从既有 service_personnel 表镜像生成
role                 后台 RBAC 角色，permissions 保存 JSON 权限
```

核心用户和健康表：

```text
user                  用户基础信息、个人信息、健康基础字段
health_data           健康数据记录，按 user_id + data_type + record_date 查询
medication_record     用药记录
health_settings       健康设置，心率阈值、步数目标、睡眠目标、用药提醒
device                用户设备
report                用户报告，含 summary 摘要
```

标签表：

```text
user_tag              标签定义，含颜色、状态、用户数
user_tag_rel          用户和标签多对多关系
```

服务和交易表：

```text
service_personnel     服务人员和审核数据，audit_status 表示审核状态
work_order            工单和预约看板数据
product               商品
service_order         订单
after_sale            售后
review                评价
```

运营表：

```text
operation_content     早期运营内容通用表，仍用于 posts/comments 等内容
banner                轮播图
activity              活动
activity_enroll       活动报名
elderly_message       老人留言及处理状态
topic                 话题
recipe                食谱
article               健康资讯
disease               疾病宝典
institution           养老机构
video                 健康讲堂
food                  食物管理
assessment            测评管理
assessment_result     测评结果
```

资产和积分表：

```text
coupon                优惠券，min_amount 为使用门槛
user_points           用户积分，total_earned/total_spent 为累计获得和消耗
points_record         积分流水
member_level          会员等级，benefits 为权益说明
points_rule           积分规则，action_type 内部存 signin/order/review，前端中文展示
```

商品扩展表：

```text
product_category      商品分类
service_item          商品下属服务项目，product_id 关联 product
```

系统表：

```text
staff                 后台员工 legacy 业务表，新增/更新员工时会同步到 account/admin_profile
account               统一账号表，后台登录实际读取这里的 phone/password_hash/role_type
admin_profile         后台员工账号资料，关联 account.id 和 role.id
role                  后台角色，permissions JSON 控制 RBAC 权限
operation_log         操作日志
agreement             协议
```

重要字段映射：

```text
个人资料姓名       -> staff.name
个人资料员工编号   -> staff.staff_no
个人资料手机号     -> staff.phone
个人资料头像       -> staff.avatar_url
个人资料角色       -> staff.role_id -> role.name
个人资料备注       -> staff.remark

用户标签           -> user_tag / user_tag_rel
用户积分等级       -> user_points.level
等级权益           -> member_level.benefits
优惠券门槛         -> coupon.min_amount
健康设置心率上限   -> health_settings.heart_rate_upper
健康设置心率下限   -> health_settings.heart_rate_lower
服务项目商品       -> service_item.product_id -> product.id
活动报名活动       -> activity_enroll.activity_id -> activity.id
评价内容           -> review.content
测评说明           -> assessment.description
```

状态值说明：

```text
device.status: 1=绑定, 0=解绑
work_order.status: pending=待服务, service_in=服务中, completed=已完成, cancelled=已取消
service_order.status: pending_accept=待接单, pending_service=待服务, completed=已完成, closed=已关闭, after_sale=售后中
activity_enroll.status: enrolled=已报名, cancelled=已取消, attended=已到场
assessment.type: sleep=睡眠测评, fall=跌倒风险, custom=综合测评
points_rule.action_type: signin=签到, order=完成订单, review=发布评价
```

## 主要 API

认证和个人资料：

```text
POST /api/v1/auth/login
GET  /api/v1/auth/profile
PUT  /api/v1/auth/profile
PUT  /api/v1/auth/password
```

首页和预约：

```text
GET    /api/v1/dashboard
GET    /api/v1/appointments?startDate=2026-07-13&endDate=2026-07-19
POST   /api/v1/appointments
DELETE /api/v1/appointments/{id}
GET    /api/v1/analytics/overview?startDate=2026-07-01&endDate=2026-07-31
```

预约看板接口最多查询连续 7 天；前端默认使用“今天至第 7 天”的窗口，并按 `serviceDate` 在本地切换当天看板。

用户和标签：

```text
GET    /api/v1/users?keyword=王秀兰&tag=重点关怀&startDate=2026-07-01&endDate=2026-07-31
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

“全部用户”页面支持 `tag` 参数按标签名称精确筛选。工单管理、商品服务管理和订单管理页面支持以下通用查询参数：

```text
keyword    对当前行可见字段做关键词匹配
status     按中文状态精确匹配
startDate  起始日期，YYYY-MM-DD
endDate    结束日期，YYYY-MM-DD
```

工单查询可额外使用 `personnelId` 和 `customerId`。

密码设置：

```text
PUT /api/v1/auth/password
PUT /api/v1/settings/users/{id}/password/reset
PUT /api/v1/settings/personnel/{id}/password/reset
```

本人改密请求需要 `currentPassword`、`newPassword`、`confirmPassword`。管理端重置接口不接收自定义密码，目标密码固定重置为 `753951`；`settings` 权限只授予超级管理员和管理员角色。

服务：

```text
GET/POST/PUT/DELETE /api/v1/personnel
GET/PUT             /api/v1/audits
GET/POST/PUT/DELETE /api/v1/work-orders
GET                 /api/v1/work-orders?personnelId={服务人员ID}&customerId={用户ID}
```

工单创建请求中的 `personnelId` 为必填项。管理端创建时还需选择客户；服务人员必须处于“启用、已通过审核”状态。

商品：

```text
GET/POST/PUT/DELETE /api/v1/products
GET/POST/PUT/DELETE /api/v1/product-categories
GET/POST/PUT/DELETE /api/v1/service-items
```

运营：

```text
GET/POST/PUT/DELETE /api/v1/posts
GET/POST/PUT/DELETE /api/v1/activities
GET/POST/PUT/DELETE /api/v1/activity-enrolls
GET/POST/PUT/DELETE /api/v1/topics
GET/POST/PUT/DELETE /api/v1/banners
GET/POST/PUT/DELETE /api/v1/recipes
GET/POST/PUT/DELETE /api/v1/articles
GET/POST/PUT/DELETE /api/v1/diseases
GET/POST/PUT/DELETE /api/v1/institutions
GET/POST/PUT/DELETE /api/v1/videos
GET/POST/PUT/DELETE /api/v1/foods
GET/POST/PUT/DELETE /api/v1/assessments
```

交易：

```text
GET/POST/PUT/DELETE /api/v1/orders
GET/POST/PUT/DELETE /api/v1/after-sales
GET/POST/PUT/DELETE /api/v1/reviews
```

用户扩展：

```text
GET/POST/PUT/DELETE /api/v1/devices
GET/POST/PUT/DELETE /api/v1/reports
GET/POST/PUT/DELETE /api/v1/health-settings
GET/POST/PUT/DELETE /api/v1/coupons
GET/POST/PUT/DELETE /api/v1/user-points
GET/POST/PUT/DELETE /api/v1/member-levels
GET/POST/PUT/DELETE /api/v1/points-rules
```

系统：

```text
GET/POST/PUT/DELETE /api/v1/staffs
GET/POST/PUT/DELETE /api/v1/roles
GET                 /api/v1/logs
GET/POST/PUT/DELETE /api/v1/agreements
```

## 后期修改指南

修改菜单：

```text
前端文件：daisy-health-admin-frontend/src/layout/AdminLayout.vue
修改位置：menuGroups
```

新增路由：

```text
前端文件：daisy-health-admin-frontend/src/router/index.js
```

修改通用列表字段：

```text
前端文件：daisy-health-admin-frontend/src/views/GenericListView.vue
列表列：columnMap
新增/编辑表单：createFieldMap
标题：titleMap
页面说明：descriptors
```

修改接口路径映射：

```text
前端文件：daisy-health-admin-frontend/src/api/http.js
修改位置：resourcePaths
```

修改后端资源查询和保存：

```text
后端文件：daisy-health-admin-backend/src/main/java/com/daisy/health/service/JdbcAdminDataService.java
列表查询：resource 或 phaseRows
新增保存：createResource 或 phaseCreateValues
编辑保存：updateResource 或 phaseUpdateValues
表名映射：tableName / phaseTableName
状态转换：orderStatus / workOrderStatus / activityEnrollStatus / pointActionType / assessmentType
```

修改数据库结构：

```text
后端文件：daisy-health-admin-backend/src/main/resources/schema.sql
```

补充演示数据：

```text
后端文件：daisy-health-admin-backend/src/main/resources/data.sql
```

注意：`schema.sql` 会在后端启动时执行。新增表推荐使用 `create table if not exists`。给旧表补字段时推荐使用 `information_schema.columns` 判断后再 `alter table`，避免重复执行失败。

## 验证记录

最近一次验证：

```text
后端：mvn.cmd -q test 通过（67 项）
前端：npm.cmd test 通过（21 项）
前端：npm.cmd run build 通过
安全验证：后端编译测试通过，前端构建通过；JWT/RBAC 相关代码已进入构建链路
```

最近本地提交：

```text
7d62500 feat: support explicit work order assignment
20e7053 fix: preserve independent health measurements
737cb9a Handle invalid auth cache
```

前端构建目前仍有 Vite/Rollup 大 chunk 警告，属于体积优化提示，不影响运行。

## 后续建议

建议后续优先处理：

- 把 `JdbcAdminDataService` 拆分为多个模块 Service。
- 继续完善用户端可操作业务，例如预约/下单、资料编辑、用药打卡。
- 继续完善服务人员端可操作业务，例如服务记录表单、异常上报、位置/时间签到。
- 增加更细的按钮级权限控制和接口自动化测试。
- 接入 Redis 缓存或会话能力，目前 Redis 依赖存在但业务未强依赖。

## 统一登录与角色工作台

当前只保留一个前端入口：

```text
daisy-health-admin-frontend
访问地址：http://127.0.0.1:5173
```

所有角色都从同一个登录页进入。登录成功后，前端根据后端返回的 `roleType` 自动分流：

```text
staff    -> /dashboard       后台管理端，按 RBAC 管理全部授权数据
elderly  -> /portal/user     用户端，只能查看本人健康、用药、设备、报告、订单、优惠券、积分
service  -> /portal/service  服务人员端，只能查看和更新分配给自己的工单
```

测试账号：

```text
超级管理员：13402832834 / 753951
用户账号：用户表中的手机号，例如 13800010001 / 753951
服务人员账号：服务人员表中的手机号，例如 13900020001 / 753951
```

三类账号登录后均可在各自界面打开“修改密码”，输入当前密码和两次新密码完成修改。管理端还可从“系统 -> 密码设置”重置用户或服务人员密码；该操作始终恢复为 `753951`，不会提供任意密码输入框。

数据归属控制由后端完成：

- 用户端根据 token 中的 `accountId` 反查 `elderly_profile.legacy_user_id`，不会相信前端传入的用户 ID。
- 服务人员端根据 token 中的 `accountId` 反查 `service_profile.legacy_personnel_id`，工单查询和状态更新都限定 `work_order.personnel_id = 当前服务人员 ID`。
- 后台管理员仍通过 `role.permissions` 控制模块权限，超级管理员拥有 `{"*":["*"]}`。

## 日志文件说明

`.log` 后缀文件仅用于本地调试和运行验证，不属于业务源码，也不需要提交到 Git。

当前仓库 `.gitignore` 已忽略：

```text
*.log
```

此前用于尝试后台启动独立前端的空日志文件已经删除：

```text
user-frontend-dev.log
user-frontend-dev.err.log
service-frontend-dev.log
service-frontend-dev.err.log
```

如需排查后端启动问题，可以临时保留本地运行日志；问题解决后可直接删除。

## 2026-07-12 用户端与数据同步更新

本次完善了老人用户端的资料、设备、活动和健康内容能力，所有业务数据均与管理端共用数据库表，不维护用户端副本。

### 用户资料与设备

- 用户端“个人资料”现在展示并可修改昵称、真实姓名、手机号、性别、生日、身份证号、地址、简介、身高、体重、民族、文化程度、血型、RH 阴性、慢性病、睡眠、吸烟、饮酒、运动、饮食偏好及紧急联系人信息。
- 用户端保存资料时更新 `user` 主表，并同步 `account`、`elderly_profile` 统一账号镜像；管理端修改用户后仍通过 `syncElderlyAccount` 同步，因此两端读取和修改的是同一份用户信息。
- 用户端可以查看并修改本人的设备名称、类型、编号和绑定状态。后端更新条件同时包含设备 ID 和当前用户 ID，不能修改其他用户的设备。
- 管理端用户详情已补齐上述完整资料字段，管理端和用户端均可修改。

新增用户端接口：

```text
PUT /api/v1/elderly/profile
PUT /api/v1/elderly/devices/{id}
```

### 活动、健康资讯和健康讲堂

- “社区活动”读取管理端 `activity` 表，只展示已发布或已结束活动；老人只能选择已有活动报名，不能创建活动。
- 报名写入 `activity_enroll`，后端根据 token 确定当前老人用户。用户端和管理端显示的报名人数均按每位用户最新一条有效报名记录实时统计：`enrolled`（已报名）和 `attended`（已到场）计入人数，`cancelled`（已取消）不计入；历史重复记录不会重复计数。
- 报名名额校验同样使用上述真实人数；用户报名以及管理端新增报名、将取消记录恢复为有效报名、迁移活动或更换报名用户时，都会在 `READ_COMMITTED` 事务内锁定目标活动并检查名额，保证等待并发事务提交后读取最新人数，避免缓存计数不一致或并发写入导致超额。
- `activity.enrolled` 仅作为兼容字段，不再作为页面展示和名额判断的数据源；用户报名以及管理端新增、修改、删除报名记录后都会按真实人数回写该字段。
- 社区活动页面按当前用户的最新报名记录分成“我的活动”和“可选择的活动”；报名成功后活动会立即移动到“我的活动”。
- 活动详情展示管理端维护的封面、标题、状态、开始/结束时间、地点、名额和活动介绍，并显示当前用户的报名状态与报名时间。管理端修改 `activity` 后，用户端重新读取同一张表即可同步。
- “健康资讯”读取管理端已发布的 `article` 数据。
- “健康讲堂”读取管理端已发布的 `video` 数据。
- 管理端仍通过原有活动、活动报名、健康资讯和健康讲堂页面创建和维护内容；用户端与管理端实时读取同一数据库数据。

新增用户端接口：

```text
GET  /api/v1/elderly/activities
POST /api/v1/elderly/activities/{id}/enroll
GET  /api/v1/elderly/health-articles
GET  /api/v1/elderly/health-videos
```

### 管理端健康数据保存修复

管理端用户详情编辑健康数据时，前端原来把资源名 `healthData` 直接拼成了不存在的 `/api/v1/healthData/{id}`，而后端实际接口是 `/api/v1/health-data/{id}`，因此无论数据库是否启动都会保存失败。

现在 `resourcePaths` 已增加 `healthData -> health-data` 映射，并增加前端回归测试锁定该请求路径。

### 服务人员端当前完成情况

服务人员登录后进入 `/portal/service`，数据范围由后端根据 JWT 中的 `accountId` 反查 `service_profile.legacy_personnel_id`，不接受前端传入服务人员 ID。

已完成：

- 查看本人资料，包括姓名、手机号、头像、服务类型、服务区域、入职时间、审核状态和评分。
- 查看只分配给本人的工单列表和工单详情。
- 更新本人名下工单状态，支持 `pending`、`service_in`、`completed`、`cancelled`；完成工单时自动写入完成时间。
- 工单读取和更新 SQL 均带 `personnel_id = 当前服务人员 ID` 归属条件，不能读取或修改他人工单。

当前尚未完成：

- 服务过程记录表单、图片或附件上传。
- 异常情况上报、取消原因的完整交互。
- 位置签到、开始/结束服务的时间与定位校验。
- 服务人员自行修改完整资料、头像和密码。
- 消息通知、排班日历、收入统计及评价明细。

服务人员端接口：

```text
GET /api/v1/service-app/profile
GET /api/v1/service-app/work-orders
GET /api/v1/service-app/work-orders/{id}
PUT /api/v1/service-app/work-orders/{id}/status
```

## 2026-07-13 工单分配、查询与管理员头像更新

### 工单分配逻辑

修改前，管理端或用户端未传入服务人员时，后端会自动取第一名启用的服务人员完成派单。该方式无法体现用户或管理员的选择，也可能把工单集中分配给同一人。

修改后：

- 管理端“服务 → 工单管理”新增工单时，必须选择商品服务、客户和服务人员。
- 管理端用户详情的“服务记录”新增工单时，客户固定为当前用户，必须选择服务人员。
- 老人用户在商品服务页面创建工单时，必须从可接单人员列表中选择服务人员。
- 可选人员统一限制为 `service_personnel.status = 1` 且 `audit_status = '已通过'`。
- 后端在写入订单和工单前再次校验 `personnelId`，避免绕过前端提交无效人员，也避免校验失败时产生半成品订单。
- 工单创建后，管理端、老人用户端和服务人员端读取同一条 `work_order` 数据，分配结果实时同步。

老人用户端新增接口：

```text
GET  /api/v1/elderly/personnel
POST /api/v1/elderly/work-orders
PUT  /api/v1/elderly/work-orders/{id}/cancel
PUT  /api/v1/elderly/work-orders/{id}/reschedule
PUT  /api/v1/elderly/activities/{id}/cancel-enrollment
GET  /api/v1/elderly/messages
POST /api/v1/elderly/messages
GET  /api/v1/messages
PUT  /api/v1/messages/{id}
```

`POST /api/v1/elderly/work-orders` 请求示例：

```json
{
  "productId": 1,
  "personnelId": 2,
  "serviceTime": "2026-07-14 09:00:00"
}
```

### 管理端按人员查询工单

管理端工单列表现在同时返回 `customerId/customer/customerPhone` 和 `personnelId/personnelName/personnelPhone`，页面提供“查询服务人员”和“查询用户”两个下拉条件。两项可以单独使用，也可以组合查询：

```text
GET /api/v1/work-orders?personnelId=2
GET /api/v1/work-orders?customerId=10001
GET /api/v1/work-orders?personnelId=2&customerId=10001
```

管理端用户详情的服务记录查询也关联 `service_personnel`，因此可以直接看到每条用户工单的负责人员。

### 超级管理员头像

超级管理员可从右上角进入“个人资料”，使用与用户端相同的 `AvatarPicker`：

- 可选择系统内置的 6 个头像。
- 可上传 JPG、JPEG、PNG、WebP 图片，前端会执行头像格式、大小和图像处理校验。
- 保存时调用原有 `PUT /api/v1/auth/profile`，同时更新管理员资料和统一账号头像。
- 顶部导航头像通过统一的资源地址处理函数显示，支持 `/uploads/**` 相对地址。
- 后端启动脚本只在 `staff.avatar_url` 为空时填充默认头像，不再用默认头像覆盖管理员已经保存的头像；随后同步到 `account.avatar_url` 的仍是已保存值，因此重启后保持不变。

### 验证结果

```text
后端：mvn.cmd -q test 通过
前端：npm.cmd test 通过（6 项）
前端：npm.cmd run build 通过
代码审查：需求符合性无缺口，无阻塞性规范问题
本地提交：7d62500 feat: support explicit work order assignment
```
