-- ============================================================
-- COC（克苏鲁的呼唤）跑团角色卡整理展示系统 - 数据库建表脚本
-- 适用：MySQL 8.0+
-- 字符集：utf8mb4（支持中文及 emoji）
-- 说明：角色卡核心数据全部使用 JSON 字段存储，保持最大灵活性
-- ============================================================

CREATE DATABASE IF NOT EXISTS `coc_card`
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE `coc_card`;

-- ------------------------------------------------------------
-- 1. 用户表 user
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `pc`;
DROP TABLE IF EXISTS `invite_code`;
DROP TABLE IF EXISTS `user`;

CREATE TABLE `user` (
    `id`             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '自增主键',
    `username`       VARCHAR(50)  NOT NULL COMMENT '登录账号（唯一）',
    `password`       VARCHAR(100) NOT NULL COMMENT 'BCrypt 加密后的密码',
    `role`           VARCHAR(20)  NOT NULL DEFAULT 'USER' COMMENT '角色：ADMIN-管理员，USER-普通用户',
    `display_fields` JSON         NULL COMMENT '角色卡列表自定义显示列，如 ["name","gender","age","occupation"]（默认值由应用层维护）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_username` (`username`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT ='用户表';

-- ------------------------------------------------------------
-- 2. 邀请码表 invite_code
-- ------------------------------------------------------------
CREATE TABLE `invite_code` (
    `id`         BIGINT      NOT NULL AUTO_INCREMENT COMMENT '自增主键',
    `code`       VARCHAR(32) NOT NULL COMMENT '邀请码字符串（唯一）',
    `created_by` BIGINT      NOT NULL COMMENT '生成该邀请码的管理员 ID（user.id）',
    `used`       BOOLEAN     NOT NULL DEFAULT FALSE COMMENT '是否已被使用',
    `used_by`    BIGINT      NULL COMMENT '使用该邀请码注册的用户 ID（user.id），未使用时为 NULL',
    `created_at` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '生成时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_invite_code_code` (`code`),
    KEY `idx_invite_code_created_by` (`created_by`),
    KEY `idx_invite_code_used_by` (`used_by`),
    CONSTRAINT `fk_invite_code_created_by` FOREIGN KEY (`created_by`)
        REFERENCES `user` (`id`),
    CONSTRAINT `fk_invite_code_used_by` FOREIGN KEY (`used_by`)
        REFERENCES `user` (`id`) ON DELETE SET NULL
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT ='邀请码表';

-- ------------------------------------------------------------
-- 3. 角色卡主表 pc
-- ------------------------------------------------------------
CREATE TABLE `pc` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '自增主键',
    `user_id`       BIGINT       NOT NULL COMMENT '所属用户 ID（user.id）',
    `pc_number`     INT          NOT NULL COMMENT '该用户下的角色卡编号，从 1 开始自动递增',
    `sort_order`    INT          NOT NULL DEFAULT 0 COMMENT '手动排序序号（越小越靠前，新卡追加到末尾）',
    `name`          VARCHAR(50)  NOT NULL COMMENT '角色姓名',
    `gender`        VARCHAR(10)  NULL COMMENT '性别',
    `age`           INT          NULL COMMENT '年龄',
    `era`           VARCHAR(50)  NULL COMMENT '时代，如“现代”、“1920s”',
    `occupation`    VARCHAR(100) NULL COMMENT '职业',
    `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
                                   ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `attributes`    JSON         NULL COMMENT '属性：{"str":90,"con":65,"siz":65,"dex":65,"app":40,"int":90,"pow":85,"edu":50,"luck":75}',
    `skills`        JSON         NULL COMMENT '技能表，键为技能名（含自定义）：{"会计":{"initial":5,"growth":0,"occupation":20,"interest":10,"total":35}}',
    `weapons`       JSON         NULL COMMENT '武器列表：[{"name":"袖剑","type":"格斗","damage":"1D8+1D4+DB","range":"接触","note":""}]',
    `items`         JSON         NULL COMMENT '物品列表：[{"name":"储物戒","location":"左手","status":"显露","note":""}]',
    `background`    JSON         NULL COMMENT '背景：结构化对象 {"personal_description":"...","ideology":"...",...} 或纯文本 {"text":"完整背景故事"}',
    `experiences`   JSON         NULL COMMENT '模组经历：[{"module_name":"毒汤","ho":"HO1","change_description":"SAN-6,HP-2,侦查+2"}]',
    `spells`        JSON         NULL COMMENT '法术列表：[{"name":"灰色束缚","cost":"8mp 1d6san 1h","effect":"可以控制死去的人，直到腐烂为止"}]',
    `custom_fields` JSON         NULL COMMENT '用户完全自定义的键值对：{"birthday":"1995-03-21","height":"175cm","weight":"70kg","hobby":"收集古董"}',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_pc_user_number` (`user_id`, `pc_number`),
    KEY `idx_pc_user_id` (`user_id`),
    CONSTRAINT `fk_pc_user_id` FOREIGN KEY (`user_id`)
        REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT ='角色卡主表';
