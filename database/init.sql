-- 创建数据库
CREATE DATABASE IF NOT EXISTS hotline DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE hotline;

-- 分类表
CREATE TABLE IF NOT EXISTS sys_category (
    category_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '分类ID',
    parent_id INT DEFAULT 0 COMMENT '父分类ID',
    category_name VARCHAR(50) NOT NULL COMMENT '分类名称',
    category_code VARCHAR(20) NOT NULL COMMENT '分类编码',
    sort INT DEFAULT 0 COMMENT '排序',
    status INT DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
    remark VARCHAR(200) COMMENT '备注说明',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工单分类表';

-- 关键词规则表
CREATE TABLE IF NOT EXISTS rule_keyword (
    rule_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '规则ID',
    category_id INT NOT NULL COMMENT '分类ID',
    keyword VARCHAR(100) NOT NULL COMMENT '关键词',
    synonym VARCHAR(500) COMMENT '同义词（逗号分隔）',
    match_type INT DEFAULT 1 COMMENT '匹配类型：1-包含，2-前缀，3-后缀，4-正则',
    weight FLOAT DEFAULT 1.0 COMMENT '权重',
    hit_count INT DEFAULT 0 COMMENT '命中次数',
    status INT DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
    effective_time DATETIME COMMENT '生效时间',
    expire_time DATETIME COMMENT '失效时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_category_id (category_id),
    INDEX idx_keyword (keyword)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='关键词规则表';

-- 工单表
CREATE TABLE IF NOT EXISTS biz_work_order (
    order_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '工单ID',
    order_code VARCHAR(50) COMMENT '工单编号',
    category_id INT COMMENT '分类ID',
    accept_time DATETIME COMMENT '受理时间',
    accept_user VARCHAR(50) COMMENT '受理人',
    user_phone VARCHAR(20) COMMENT '用户电话',
    user_region VARCHAR(100) COMMENT '用户地区',
    user_address VARCHAR(200) COMMENT '用户地址',
    order_content TEXT COMMENT '工单内容',
    ai_candidate_category VARCHAR(50) COMMENT 'AI候选分类',
    ai_match_evidence VARCHAR(500) COMMENT 'AI匹配证据',
    status INT DEFAULT 0 COMMENT '状态：0-待处理，1-已处理，2-已跳过',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_category_id (category_id),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工单表';

-- 用户表（如果需要）
CREATE TABLE IF NOT EXISTS user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    name VARCHAR(50) COMMENT '用户名',
    age INT COMMENT '年龄',
    email VARCHAR(100) COMMENT '邮箱',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 插入默认分类数据
INSERT INTO sys_category (category_id, parent_id, category_name, category_code, sort, status, remark) VALUES
(1, 0, '城市管理', 'CSGL', 1, 1, '城市管理相关问题'),
(2, 0, '社会环境', 'SHHJ', 2, 1, '社会环境相关问题'),
(3, 0, '社会保障', 'SBJ', 3, 1, '社会保障相关问题'),
(4, 0, '机构经济', 'JGJJ', 4, 1, '机构经济相关问题'),
(5, 0, '教育', 'JYJ', 5, 1, '教育相关问题'),
(6, 0, '公司管理', 'GSGL', 6, 1, '公司管理相关问题'),
(7, 0, '交通管理', 'JTGL', 7, 1, '交通管理相关问题'),
(8, 0, '医疗卫生', 'YWJY', 8, 1, '医疗卫生相关问题'),
(9, 0, '林业', 'LYJ', 9, 1, '林业相关问题'),
(10, 0, '房价管理', 'FJGL', 10, 1, '房价管理相关问题');

-- 插入示例关键词规则
INSERT INTO rule_keyword (category_id, keyword, synonym, match_type, weight, status) VALUES
(1, '道路', '马路,街道', 1, 2.0, 1),
(1, '垃圾', '垃圾堆,垃圾清理', 1, 2.0, 1),
(1, '路灯', '照明,灯光', 1, 2.0, 1),
(2, '噪音', '噪声,吵闹', 1, 2.0, 1),
(2, '污染', '空气污染,环境污染', 1, 2.0, 1),
(3, '社保', '养老保险,医疗保险', 1, 2.0, 1),
(3, '公积金', '住房公积金', 1, 2.0, 1),
(4, '税务', '税收,纳税', 1, 2.0, 1),
(5, '学校', '教育,入学', 1, 2.0, 1),
(7, '交通', '堵车,违章', 1, 2.0, 1);

-- 插入示例工单数据
INSERT INTO biz_work_order (order_code, order_content, status) VALUES
('WO202401001', '市民反映XX道路路灯不亮，影响夜间出行安全', 0),
('WO202401002', 'XX小区门口垃圾长期未清理，散发异味', 0),
('WO202401003', '咨询社保转移接续办理流程', 0),
('WO202401004', '反映XX学校招生政策问题', 0),
('WO202401005', 'XX路口交通信号灯故障', 0);