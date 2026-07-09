# 黛西健康后台服务

Spring Boot 2.7.18 后端 API 项目，按概要设计报告生成。当前版本默认运行在 `mysql` profile 下，连接真实 MySQL 数据库，并在启动时自动建表、写入演示数据。

## 环境要求

```text
JDK 8
Maven 3.6.3+
```

已验证环境：

```text
java 1.8.0_491
mvn 3.6.3
```

## 运行

```bat
cd D:\agent_project\elder_AI\daisy-health-admin-backend
mvn spring-boot:run
```

默认端口：

```text
8080
```

测试接口：

```text
http://127.0.0.1:8080/api/v1/dashboard
```

默认数据库配置：

```text
地址：localhost:3306
数据库：daisy_health
账号：root
密码：root
```

启动前需要确认 MySQL 已启动并监听 `3306`。如果账号密码不同，修改 `src/main/resources/application.yml`。

## 项目结构

```text
src/main/java/com/daisy/health
├─ DaisyHealthApplication.java
├─ common       通用响应、分页、异常处理、跨域配置
├─ controller   REST Controller
├─ service      MockDataService
├─ model        Entity 示例
└─ mapper       MyBatis Mapper 示例

src/main/resources
├─ application.yml
└─ schema.sql
```

## Profile

默认：

```text
mysql
```

依赖 MySQL，会自动执行：

```text
src/main/resources/schema.sql
src/main/resources/data.sql
```

无数据库临时演示模式：

```bat
mvn spring-boot:run -Dspring-boot.run.profiles=mock
```

`mock` 模式不依赖 MySQL/Redis。

## 安全认证与账号

后台管理端登录已接入 BCrypt + JWT + RBAC：

```text
默认后台超级管理员账号：13402832834
默认后台超级管理员密码：753951
登录接口：POST /api/v1/auth/login
鉴权方式：Authorization: Bearer <JWT>
```

账号与角色相关表：

```text
account          统一账号表，区分 role_type=staff/elderly/service
admin_profile    后台管理员资料，关联 role.id
elderly_profile  老人/用户资料镜像，当前预留给用户端
service_profile  服务人员资料镜像，当前预留给服务人员端
role             后台 RBAC 角色，permissions 保存 JSON 权限
```

当前只有后台管理端员工账号可以登录现有 Vue 管理后台。老人用户端和服务人员端的前端入口、专属 API、数据归属权限尚未实现。

## 核心接口

```text
POST /api/v1/auth/login
POST /api/v1/users
POST /api/v1/personnel
POST /api/v1/work-orders
POST /api/v1/products
POST /api/v1/orders
POST /api/v1/after-sales
POST /api/v1/reviews
POST /api/v1/staffs
POST /api/v1/roles
GET  /api/v1/dashboard
GET  /api/v1/appointments
GET  /api/v1/users
GET  /api/v1/users/{id}
GET  /api/v1/personnel
GET  /api/v1/audits
GET  /api/v1/work-orders
GET  /api/v1/products
GET  /api/v1/orders
GET  /api/v1/after-sales
GET  /api/v1/reviews
GET  /api/v1/analytics/overview
GET  /api/v1/staffs
GET  /api/v1/roles
GET  /api/v1/logs
```

## 已知说明

- 当前 Controller 通过 `AdminDataService` 接口访问数据
- `mysql` profile 使用 `JdbcAdminDataService` 查询数据库
- `mock` profile 使用 `MockDataService` 返回本地演示数据
- `schema.sql` 已包含核心表结构
- `data.sql` 已包含初始化演示数据
- 已配置 Knife4j/Springfox 与 Spring Boot 2.7 的兼容项：

```yaml
spring:
  mvc:
    pathmatch:
      matching-strategy: ant_path_matcher
```
