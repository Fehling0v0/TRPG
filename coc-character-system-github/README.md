# PC档案夹（COC 跑团角色卡整理展示系统）

一个用于整理、管理和展示 **COC（克苏鲁的呼唤）TRPG 跑团角色卡** 的 Web 系统。
角色卡核心数据全部以 MySQL JSON 字段存储，字段可自由扩展，支持 Excel 角色卡批量导入。

## 功能特性

- **账号体系**：邀请码注册、JWT 登录鉴权；用户只能管理自己的角色卡；管理员可生成邀请码、管理用户、重置密码
- **角色卡管理**：新建 / 编辑 / 删除角色卡；属性、技能、武器、物品、法术、背景、模组经历等数据以 JSON 灵活存储，支持自定义字段
- **Excel 导入**：支持上传 `.xlsx` 角色卡文件自动解析（基于 Apache POI），可批量解析多个文件
- **角色卡列表**：瀑布流卡片布局、自定义显示字段（刷新后保持）、卡片手动拖拽排序
- **经历模组 / HO 表**：按 HO 位整理模组经历，支持状态、时间、位置标记，并可导出 HO 整理表 JSON
- **响应式 UI**：Vue 3 + Element Plus，桌面与移动端均可用

## 技术栈

| 端 | 技术 |
| --- | --- |
| 后端 | Java 17、Spring Boot 3.3、Spring Security、JWT (jjwt)、Spring Data JPA / Hibernate 6、MySQL 8（JSON 列）、Apache POI |
| 前端 | Vue 3、Vite 5、Element Plus、Pinia、Vue Router、Axios、SortableJS |
| 部署 | Nginx（静态站点 + API 反向代理）、systemd |

## 目录结构

```
.
├── backend/                # Spring Boot 后端
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/coc/card/   # 控制器 / 服务 / 实体 / 安全等
│       └── resources/
│           ├── application.yml       # 开发环境配置
│           └── application-prod.yml  # 生产环境配置模板（占位符）
├── frontend/               # Vue 3 + Vite 前端
│   ├── package.json
│   ├── vite.config.js
│   ├── index.html
│   └── src/                # 页面 / 路由 / 状态 / API 封装
├── sql/
│   └── schema.sql          # 数据库建表脚本（MySQL 8.0+）
└── deploy/                 # 部署参考配置
    ├── application-prod.yml    # 生产外部配置（放在 jar 同目录覆盖内置配置）
    ├── nginx-coc-card.conf     # Nginx 站点配置模板
    └── coc-card.service        # systemd 服务模板
```

## 快速开始

### 环境要求

- JDK 17+、Maven 3.6+
- Node.js 18+、npm
- MySQL 8.0+

### 1. 初始化数据库

```bash
mysql -u root -p < sql/schema.sql
```

脚本会自动创建 `coc_card` 数据库及 `user`、`invite_code`、`pc` 三张表（utf8mb4）。

### 2. 启动后端

按需修改 `backend/src/main/resources/application.yml` 中的数据库账号密码：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/coc_card?...
    username: root
    password: 你的数据库密码
jwt:
  secret: 请替换为不少于 32 字节的随机字符串
```

启动：

```bash
cd backend
mvn spring-boot:run
```

后端运行在 `http://localhost:8081`。首次启动会自动初始化默认管理员账号：

- 用户名：`admin`
- 密码：`admin123`

**登录后请立即通过头像菜单中的「修改密码」功能更换密码。**

### 3. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端运行在 `http://localhost:5173`，开发服务器已配置将 `/api` 请求代理到 `http://localhost:8081`。

## 生产部署（参考）

1. 打包前端：`cd frontend && npm run build`，将 `dist/` 部署为 Nginx 静态站点。
2. 打包后端：`cd backend && mvn clean package`，得到 `target/coc-card-backend-*.jar`。
3. 将 `deploy/application-prod.yml` 放到 jar 同目录，修改其中的 `CHANGE_ME` 占位项（数据库密码、JWT 密钥），以 `--spring.profiles.active=prod` 启动。
4. Nginx 站点配置参考 `deploy/nginx-coc-card.conf`（将 `YOUR_SERVER_IP` 替换为实际域名或 IP）；systemd 服务参考 `deploy/coc-card.service`。

## 安全提示

- 仓库中的 `application.yml` 仅为本地开发配置；生产环境请使用 `application-prod.yml` 并通过外部配置或环境变量注入数据库密码与 JWT 密钥，切勿提交真实密钥。
- 部署后请第一时间修改默认管理员密码。
