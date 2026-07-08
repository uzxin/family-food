-- ============================================
-- 家庭点菜小程序 数据库初始化脚本
-- ============================================

CREATE DATABASE IF NOT EXISTS `family_food` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `family_food`;

-- 1. 用户表
CREATE TABLE IF NOT EXISTS `user` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `openid` VARCHAR(64) DEFAULT NULL COMMENT '微信openid',
  `union_id` VARCHAR(64) DEFAULT NULL COMMENT '微信unionid',
  `username` VARCHAR(64) DEFAULT NULL COMMENT '用户名（密码登录用）',
  `password` VARCHAR(128) DEFAULT NULL COMMENT '密码（加密存储）',
  `nickname` VARCHAR(64) DEFAULT '' COMMENT '昵称',
  `avatar_url` VARCHAR(512) DEFAULT '' COMMENT '头像URL',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_openid` (`openid`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_union_id` (`union_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 2. 家庭空间表
CREATE TABLE IF NOT EXISTS `family` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `owner_user_id` BIGINT UNSIGNED NOT NULL COMMENT '创建者用户ID',
  `name` VARCHAR(64) NOT NULL COMMENT '家庭名称',
  `invite_code` VARCHAR(8) NOT NULL COMMENT '邀请码',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_invite_code` (`invite_code`),
  KEY `idx_owner` (`owner_user_id`),
  CONSTRAINT `fk_family_owner` FOREIGN KEY (`owner_user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='家庭空间表';

-- 3. 家庭成员表
CREATE TABLE IF NOT EXISTS `family_member` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `family_id` BIGINT UNSIGNED NOT NULL COMMENT '家庭ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  `nickname` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '在家庭中的昵称',
  `role` TINYINT NOT NULL DEFAULT 0 COMMENT '角色：0=成员, 1=管理员',
  `join_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_family_user` (`family_id`, `user_id`),
  KEY `idx_user_id` (`user_id`),
  CONSTRAINT `fk_member_family` FOREIGN KEY (`family_id`) REFERENCES `family` (`id`),
  CONSTRAINT `fk_member_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='家庭成员表';

-- 4. 菜品分类表
CREATE TABLE IF NOT EXISTS `category` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `family_id` BIGINT UNSIGNED NOT NULL COMMENT '家庭ID',
  `code` VARCHAR(32) NOT NULL COMMENT '分类编码',
  `name` VARCHAR(32) NOT NULL COMMENT '分类名称',
  `icon` VARCHAR(16) DEFAULT '🍽️' COMMENT '分类图标emoji',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序序号',
  `is_builtin` TINYINT NOT NULL DEFAULT 0 COMMENT '是否内置分类：0=自定义, 1=内置',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_family_code` (`family_id`, `code`),
  KEY `idx_family_id` (`family_id`),
  CONSTRAINT `fk_category_family` FOREIGN KEY (`family_id`) REFERENCES `family` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜品分类表';

-- 5. 菜品表
CREATE TABLE IF NOT EXISTS `dish` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `family_id` BIGINT UNSIGNED NOT NULL COMMENT '家庭ID',
  `creator_user_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '创建者用户ID',
  `name` VARCHAR(64) NOT NULL COMMENT '菜品名称',
  `category_code` VARCHAR(32) NOT NULL COMMENT '分类编码',
  `difficulty` VARCHAR(16) NOT NULL DEFAULT 'normal' COMMENT '难度：quick/normal/hard',
  `image_url` VARCHAR(512) DEFAULT '' COMMENT '菜品图片URL',
  `remark` VARCHAR(500) DEFAULT '' COMMENT '备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_family_id` (`family_id`),
  KEY `idx_family_category` (`family_id`, `category_code`),
  KEY `idx_name` (`name`),
  CONSTRAINT `fk_dish_family` FOREIGN KEY (`family_id`) REFERENCES `family` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜品表';

-- 6. 菜品食材关联表
CREATE TABLE IF NOT EXISTS `dish_ingredient` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `dish_id` BIGINT UNSIGNED NOT NULL COMMENT '菜品ID',
  `ingredient_name` VARCHAR(64) NOT NULL COMMENT '食材名称',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序',
  PRIMARY KEY (`id`),
  KEY `idx_dish_id` (`dish_id`),
  KEY `idx_ingredient_name` (`ingredient_name`),
  CONSTRAINT `fk_ingredient_dish` FOREIGN KEY (`dish_id`) REFERENCES `dish` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜品食材关联表';

-- 7. 每日菜单表
CREATE TABLE IF NOT EXISTS `daily_menu` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `family_id` BIGINT UNSIGNED NOT NULL COMMENT '家庭ID',
  `menu_date` DATE NOT NULL COMMENT '菜单日期',
  `creator_user_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '创建者用户ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_family_date` (`family_id`, `menu_date`),
  KEY `idx_family_date` (`family_id`, `menu_date`),
  CONSTRAINT `fk_menu_family` FOREIGN KEY (`family_id`) REFERENCES `family` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='每日菜单表';

-- 8. 菜单-菜品关联表
CREATE TABLE IF NOT EXISTS `daily_menu_dish` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `menu_id` BIGINT UNSIGNED NOT NULL COMMENT '每日菜单ID',
  `dish_id` BIGINT UNSIGNED NOT NULL COMMENT '菜品ID',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_menu_dish` (`menu_id`, `dish_id`),
  KEY `idx_dish_id` (`dish_id`),
  CONSTRAINT `fk_md_menu` FOREIGN KEY (`menu_id`) REFERENCES `daily_menu` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_md_dish` FOREIGN KEY (`dish_id`) REFERENCES `dish` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单-菜品关联表';
