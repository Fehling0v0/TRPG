# PC档案夹

如你所见就是一个整理pc卡的网页，可以录模组ho位之类的东西进去，也可以加自定义字段记录生日之类的
支持直接上传自动卡excel自动解析，但是我只做了 COC七版规则空白卡21.5.25 这个版本的，格式差不太多的可以直接传上去试试，传不进去的新建角色卡可以手动填
ho表页面做得有点丑陋了但是没关系☝️🤓有导出json，可以直接传到别的老师做的summerpupp.github.io这个ho位整理表里用
很多东西都很丑陋因为我急着用所以就直接传了！后续可能会做别的版本的自动卡的适配录卡指令和一些ui优化之类的东西但不一定做
管理员可以生成邀请码/重置用户账号密码/删号，默认管账号/密码是admin/admin123，记得改
好了我说完了

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
