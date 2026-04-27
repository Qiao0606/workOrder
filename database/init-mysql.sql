-- ============================================
-- 热线工单智能分类系统 - MySQL 数据库初始化脚本
-- 数据库名: hotline
-- 创建时间: 2026-04-27
-- 数据库版本: MySQL 8.x
-- 字符集: utf8mb4
-- ============================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `hotline`
DEFAULT CHARACTER SET utf8mb4
DEFAULT COLLATE utf8mb4_general_ci;

USE `hotline`;

-- ============================================
-- 1. 分类表 (sys_category)
-- ============================================
DROP TABLE IF EXISTS `sys_category`;
CREATE TABLE `sys_category` (
  `category_id` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `parent_id` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '父分类ID',
  `category_name` VARCHAR(50) NOT NULL COMMENT '分类名称',
  `category_code` VARCHAR(20) NOT NULL COMMENT '分类编码',
  `sort` TINYINT NOT NULL DEFAULT 10 COMMENT '排序',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
  `remark` VARCHAR(200) DEFAULT '' COMMENT '备注说明',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`category_id`),
  UNIQUE KEY `uk_category_code` (`category_code`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工单分类表';

-- ============================================
-- 2. 关键词规则表 (rule_keyword)
-- ============================================
DROP TABLE IF EXISTS `rule_keyword`;
CREATE TABLE `rule_keyword` (
  `rule_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '规则ID',
  `category_id` INT UNSIGNED NOT NULL COMMENT '分类ID',
  `keyword` VARCHAR(64) NOT NULL COMMENT '关键词',
  `synonym` VARCHAR(500) DEFAULT '' COMMENT '同义词（逗号分隔）',
  `match_type` TINYINT NOT NULL DEFAULT 1 COMMENT '匹配类型：1-包含，2-前缀，3-后缀，4-正则',
  `weight` FLOAT NOT NULL DEFAULT 1 COMMENT '权重',
  `hit_count` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '命中次数',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
  `effective_time` DATETIME DEFAULT NULL COMMENT '生效时间',
  `expire_time` DATETIME DEFAULT NULL COMMENT '失效时间',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`rule_id`),
  KEY `idx_category_status` (`category_id`, `status`),
  KEY `idx_effective_time` (`effective_time`),
  KEY `idx_keyword` (`keyword`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='关键词规则表';

-- ============================================
-- 3. 工单表 (biz_work_order)
-- ============================================
DROP TABLE IF EXISTS `biz_work_order`;
CREATE TABLE `biz_work_order` (
  `order_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '工单ID',
  `order_code` VARCHAR(32) NOT NULL COMMENT '工单编号',
  `category_id` INT UNSIGNED NOT NULL COMMENT '分类ID',
  `accept_time` DATETIME DEFAULT NULL COMMENT '受理时间',
  `accept_user` VARCHAR(32) DEFAULT '' COMMENT '受理人',
  `user_phone` VARCHAR(20) DEFAULT '' COMMENT '用户电话',
  `user_region` VARCHAR(64) DEFAULT '' COMMENT '用户地区',
  `user_address` VARCHAR(200) DEFAULT '' COMMENT '用户地址',
  `order_content` TEXT COMMENT '工单内容',
  `ai_candidate_category` VARCHAR(100) DEFAULT '' COMMENT 'AI候选分类',
  `ai_match_evidence` TEXT COMMENT 'AI匹配证据',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-待处理，1-已处理，2-已跳过',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`order_id`),
  UNIQUE KEY `uk_order_code` (`order_code`),
  KEY `idx_accept_time` (`accept_time`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_status` (`status`),
  KEY `idx_user_region` (`user_region`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工单表';

-- ============================================
-- 4. 历史工单表 (ht_work_order)
-- ============================================
DROP TABLE IF EXISTS `ht_work_order`;
CREATE TABLE `ht_work_order` (
  `order_id` BIGINT UNSIGNED NOT NULL COMMENT '工单ID',
  `order_code` VARCHAR(32) NOT NULL COMMENT '工单编号',
  `category_id` INT UNSIGNED NOT NULL COMMENT '分类ID',
  `accept_time` DATETIME DEFAULT NULL COMMENT '受理时间',
  `accept_user` VARCHAR(32) DEFAULT '' COMMENT '受理人',
  `user_phone` VARCHAR(20) DEFAULT '' COMMENT '用户电话',
  `user_address` VARCHAR(64) DEFAULT '' COMMENT '用户地址',
  `order_content` TEXT COMMENT '工单内容',
  `ai_candidate_category` VARCHAR(100) DEFAULT '' COMMENT 'AI候选分类',
  `ai_match_evidence` TEXT COMMENT 'AI匹配证据',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-待处理，1-已处理，2-已跳过',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`order_id`),
  UNIQUE KEY `uk_order_code` (`order_code`),
  KEY `idx_accept_time` (`accept_time`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='历史工单表';

-- ============================================
-- 5. 部门表 (base_department)
-- ============================================
DROP TABLE IF EXISTS `base_department`;
CREATE TABLE `base_department` (
  `dept_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '部门ID',
  `dept_name` VARCHAR(64) NOT NULL COMMENT '部门名称',
  `dept_code` VARCHAR(20) NOT NULL COMMENT '部门编码',
  `parent_dept_id` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '父部门ID',
  `responsibility` VARCHAR(500) DEFAULT '' COMMENT '职责描述',
  `contact_user` VARCHAR(32) DEFAULT '' COMMENT '联系人',
  `contact_phone` VARCHAR(20) DEFAULT '' COMMENT '联系电话',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`dept_id`),
  UNIQUE KEY `uk_dept_code` (`dept_code`),
  KEY `idx_dept_name` (`dept_name`),
  KEY `idx_parent_dept_id` (`parent_dept_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部门表';

-- ============================================
-- 插入默认分类数据
-- ============================================
INSERT INTO `sys_category` (`category_id`, `parent_id`, `category_name`, `category_code`, `sort`, `status`, `remark`) VALUES
(1, 0, '城市管理', 'CSGL', 1, 1, '城市管理相关问题：道路、路灯、垃圾等'),
(2, 0, '社会环境', 'SHHJ', 2, 1, '社会环境相关问题：噪音、污染等'),
(3, 0, '社会保障', 'SBJ', 3, 1, '社会保障相关问题：社保、公积金等'),
(4, 0, '机构经济', 'JGJJ', 4, 1, '机构经济相关问题：税务、工商等'),
(5, 0, '教育', 'JYJ', 5, 1, '教育相关问题：学校、招生等'),
(6, 0, '公司管理', 'GSGL', 6, 1, '公司管理相关问题'),
(7, 0, '交通管理', 'JTGL', 7, 1, '交通管理相关问题：交通信号、违章等'),
(8, 0, '医疗卫生', 'YWJY', 8, 1, '医疗卫生相关问题'),
(9, 0, '林业', 'LYJ', 9, 1, '林业相关问题'),
(10, 0, '房价管理', 'FJGL', 10, 1, '房价管理相关问题');

-- ============================================
-- 插入示例关键词规则
-- ============================================
INSERT INTO `rule_keyword` (`category_id`, `keyword`, `synonym`, `match_type`, `weight`, `status`) VALUES
-- 城市管理类关键词
(1, '道路', '马路,街道,路面', 1, 2.0, 1),
(1, '路灯', '照明,灯光,路灯不亮', 1, 2.0, 1),
(1, '垃圾', '垃圾堆,垃圾清理,垃圾桶', 1, 2.5, 1),
(1, '下水道', '排水,污水,下水道堵塞', 1, 2.0, 1),
(1, '井盖', '窨井盖,下水道盖', 1, 2.0, 1),

-- 社会环境类关键词
(2, '噪音', '噪声,吵闹,扰民', 1, 2.5, 1),
(2, '污染', '空气污染,环境污染,大气污染', 1, 2.0, 1),
(2, '扬尘', '灰尘,粉尘', 1, 2.0, 1),
(2, '油烟', '餐饮油烟,油烟扰民', 1, 2.0, 1),

-- 社会保障类关键词
(3, '社保', '养老保险,医疗保险,社会保险', 1, 2.5, 1),
(3, '公积金', '住房公积金,公积金提取', 1, 2.5, 1),
(3, '医保', '医疗保险,医保报销', 1, 2.0, 1),

-- 机构经济类关键词
(4, '税务', '税收,纳税,发票', 1, 2.0, 1),
(4, '工商', '营业执照,注册登记', 1, 2.0, 1),

-- 教育类关键词
(5, '学校', '教育,入学,招生', 1, 2.5, 1),
(5, '学区', '学区房,划片招生', 1, 2.0, 1),
(5, '补课', '补习班,培训', 1, 2.0, 1),

-- 交通管理类关键词
(7, '交通', '堵车,拥堵', 1, 2.0, 1),
(7, '信号灯', '红绿灯,交通信号', 1, 2.5, 1),
(7, '违章', '交通违章,违停', 1, 2.0, 1),
(7, '停车', '停车位,停车场', 1, 2.0, 1);

-- ============================================
-- 插入示例工单数据
-- ============================================
INSERT INTO `biz_work_order` (`order_code`, `category_id`, `accept_time`, `accept_user`, `user_phone`, `user_region`, `user_address`, `order_content`, `status`) VALUES
('WO202401001', 0, NOW(), '张三', '13800138001', '朝阳区', 'XX街道XX社区', '市民反映XX道路路灯不亮，影响夜间出行安全，希望尽快维修', 0),
('WO202401002', 0, NOW(), '李四', '13800138002', '海淀区', 'YY街道YY小区', 'XX小区门口垃圾长期未清理，散发异味，影响居民生活', 0),
('WO202401003', 0, NOW(), '王五', '13800138003', '西城区', 'ZZ街道ZZ路', '咨询社保转移接续办理流程，想了解需要哪些材料', 0),
('WO202401004', 0, NOW(), '赵六', '13800138004', '东城区', 'AA街道BB路', '反映XX学校招生政策问题，咨询入学条件', 0),
('WO202401005', 0, NOW(), '钱七', '13800138005', '丰台区', 'CC街道DD路', 'XX路口交通信号灯故障，已持续3天，造成交通拥堵', 0),
('WO202401006', 0, NOW(), '孙八', '13800138006', '石景山区', 'EE街道FF路', '夜间施工噪音扰民，每晚持续到凌晨2点', 0),
('WO202401007', 0, NOW(), '周九', '13800138007', '通州区', 'GG街道HH路', '下水道堵塞，污水外溢，气味难闻', 0),
('WO202401008', 0, NOW(), '吴十', '13800138008', '大兴区', 'II街道JJ路', '道路破损严重，有大坑，车辆通行困难', 0),
('WO202401009', 0, NOW(), '郑十一', '13800138009', '昌平区', 'KK街道LL路', '咨询公积金提取条件和流程', 0),
('WO202401010', 0, NOW(), '王十二', '13800138010', '顺义区', 'MM街道NN路', '小区停车位被占用，物业不处理', 0);

-- ============================================
-- 插入示例部门数据
-- ============================================
INSERT INTO `base_department` (`dept_name`, `dept_code`, `parent_dept_id`, `responsibility`, `contact_user`, `contact_phone`, `status`) VALUES
('城市管理执法局', 'CSGLJ', 0, '负责城市管理综合执法', '张局长', '010-12345678', 1),
('环境卫生管理处', 'HJWLC', 0, '负责环境卫生管理', '李处长', '010-12345679', 1),
('交通运输局', 'JTYBJ', 0, '负责交通运输管理', '王局长', '010-12345680', 1),
('教育局', 'JYJ', 0, '负责教育事务管理', '赵局长', '010-12345681', 1),
('人力资源和社会保障局', 'RLSBYBJ', 0, '负责社会保障事务', '钱局长', '010-12345682', 1);

-- ============================================
-- 验证数据插入
-- ============================================
SELECT '===== 数据库初始化完成 =====' AS message;
SELECT CONCAT('分类表: ', COUNT(*), ' 条记录') AS sys_category FROM sys_category;
SELECT CONCAT('关键词规则表: ', COUNT(*), ' 条记录') AS rule_keyword FROM rule_keyword;
SELECT CONCAT('工单表: ', COUNT(*), ' 条记录') AS biz_work_order FROM biz_work_order;
SELECT CONCAT('部门表: ', COUNT(*), ' 条记录') AS base_department FROM base_department;