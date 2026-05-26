CREATE DATABASE IF NOT EXISTS database1 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE database1;

DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `user_id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL COMMENT '账号',
  `password` varchar(100) NOT NULL COMMENT '密码',
  `nickname` varchar(50) DEFAULT NULL COMMENT '昵称',
  `sex` varchar(10) DEFAULT '男' COMMENT '性别',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `email` varchar(50) DEFAULT NULL COMMENT '邮箱',
  `signature` varchar(255) DEFAULT NULL COMMENT '个性签名',
  `locked` tinyint(1) DEFAULT 0 COMMENT '是否锁定 0-否 1-是',
  `status` int DEFAULT 0 COMMENT '状态 0-正常 1-锁定',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

DROP TABLE IF EXISTS `category`;
CREATE TABLE `category` (
  `category_id` int NOT NULL AUTO_INCREMENT,
  `category_name` varchar(50) NOT NULL COMMENT '分类名称',
  PRIMARY KEY (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='帖子分类表';

DROP TABLE IF EXISTS `post`;
CREATE TABLE `post` (
  `post_id` int NOT NULL AUTO_INCREMENT,
  `title` varchar(100) NOT NULL COMMENT '标题',
  `detail` text COMMENT '内容',
  `images` varchar(1000) DEFAULT NULL COMMENT '图片',
  `user_id` int NOT NULL COMMENT '发布人ID',
  `category_id` int DEFAULT NULL COMMENT '分类ID',
  `comment_num` int DEFAULT 0 COMMENT '评论数',
  `view_num` int DEFAULT 0 COMMENT '点击数',
  `best_post` tinyint(1) DEFAULT 0 COMMENT '是否精帖',
  `collect_num` int DEFAULT 0 COMMENT '收藏数',
  `like_num` int DEFAULT 0 COMMENT '点赞数',
  `share_num` int DEFAULT 0 COMMENT '分享数',
  `post_status` int DEFAULT 0 COMMENT '状态 0-正常 1-锁定',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`post_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_category_id` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='帖子表';

DROP TABLE IF EXISTS `comment`;
CREATE TABLE `comment` (
  `comment_id` int NOT NULL AUTO_INCREMENT,
  `text` text COMMENT '评论内容',
  `images` varchar(1000) DEFAULT NULL COMMENT '图片',
  `post_id` int NOT NULL COMMENT '帖子ID',
  `parent_id` int DEFAULT 0 COMMENT '父评论ID 0-顶级评论',
  `user_id` int NOT NULL COMMENT '评论人ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`comment_id`),
  KEY `idx_post_id` (`post_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论表';

DROP TABLE IF EXISTS `collect`;
CREATE TABLE `collect` (
  `collect_id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL COMMENT '用户ID',
  `post_id` int NOT NULL COMMENT '帖子ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`collect_id`),
  UNIQUE KEY `uk_user_post` (`user_id`, `post_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收藏表';

DROP TABLE IF EXISTS `wall`;
CREATE TABLE `wall` (
  `wall_id` int NOT NULL AUTO_INCREMENT,
  `wall_content` text COMMENT '墙内容',
  `wall_images` varchar(1000) DEFAULT NULL COMMENT '墙图片',
  `user_id` int NOT NULL COMMENT '申请人ID',
  `admin_id` int DEFAULT NULL COMMENT '审核人ID',
  `audit_state` int DEFAULT 0 COMMENT '审核状态 0-待审 1-通过 2-未通过',
  `cause` varchar(255) DEFAULT NULL COMMENT '原因',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`wall_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='表白墙表';

DROP TABLE IF EXISTS `goods_category`;
CREATE TABLE `goods_category` (
  `goods_category_id` int NOT NULL AUTO_INCREMENT,
  `goods_category_name` varchar(50) NOT NULL COMMENT '分类名称',
  PRIMARY KEY (`goods_category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类表';

DROP TABLE IF EXISTS `goods`;
CREATE TABLE `goods` (
  `goods_id` int NOT NULL AUTO_INCREMENT,
  `goods_name` varchar(100) NOT NULL COMMENT '商品名称',
  `goods_detail` text COMMENT '商品详情',
  `goods_images` varchar(1000) DEFAULT NULL COMMENT '商品图片',
  `goods_price` double DEFAULT NULL COMMENT '商品定价',
  `goods_category_id` int DEFAULT NULL COMMENT '商品分类ID',
  `goods_count` int DEFAULT 1 COMMENT '商品数量',
  `goods_status` tinyint(1) DEFAULT 1 COMMENT '上架状态 0-下架 1-上架',
  `user_id` int NOT NULL COMMENT '发布人ID',
  `view_num` int DEFAULT 0 COMMENT '点击数',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`goods_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_goods_category_id` (`goods_category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='二手商品表';

DROP TABLE IF EXISTS `market_order`;
CREATE TABLE `market_order` (
  `order_id` int NOT NULL AUTO_INCREMENT,
  `seller_id` int NOT NULL COMMENT '卖家ID',
  `buyer_id` int NOT NULL COMMENT '买家ID',
  `goods_id` int NOT NULL COMMENT '商品ID',
  `buy_count` int DEFAULT 1 COMMENT '购买数量',
  `total_price` double DEFAULT NULL COMMENT '总价',
  `order_status` int DEFAULT 0 COMMENT '订单状态 0-未付 1-已付 2-已发货 3-已签收 4-已退货 5-订单完成',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `remark` text DEFAULT NULL COMMENT '备注/售后信息',
  PRIMARY KEY (`order_id`),
  KEY `idx_seller_id` (`seller_id`),
  KEY `idx_buyer_id` (`buyer_id`),
  KEY `idx_goods_id` (`goods_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

DROP TABLE IF EXISTS `role`;
CREATE TABLE `role` (
  `role_id` int NOT NULL AUTO_INCREMENT,
  `role_name` varchar(50) NOT NULL COMMENT '角色名称',
  PRIMARY KEY (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

DROP TABLE IF EXISTS `admin`;
CREATE TABLE `admin` (
  `admin_id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL COMMENT '账号',
  `password` varchar(100) NOT NULL COMMENT '密码',
  `role_id` int DEFAULT NULL COMMENT '角色ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`admin_id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员表';

DROP TABLE IF EXISTS `letter`;
CREATE TABLE `letter` (
  `letter_id` int NOT NULL AUTO_INCREMENT,
  `receiver_id` int NOT NULL COMMENT '接收方ID',
  `sender_id` int NOT NULL COMMENT '发送方ID',
  `letter_detail` text COMMENT '私信内容',
  `letter_status` int DEFAULT 0 COMMENT '状态 0-未读 1-已读',
  `session_id` varchar(50) DEFAULT NULL COMMENT '会话标识',
  `message_type` varchar(20) DEFAULT 'letter' COMMENT '消息类型: comment-评论/回复通知, letter-私信, system-系统通知',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`letter_id`),
  KEY `idx_receiver_id` (`receiver_id`),
  KEY `idx_sender_id` (`sender_id`),
  KEY `idx_session_id` (`session_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='私信表';

DROP TABLE IF EXISTS `image`;
CREATE TABLE `image` (
  `image_id` int NOT NULL AUTO_INCREMENT,
  `image_name` varchar(255) DEFAULT NULL COMMENT '图片名称',
  `image_url` varchar(255) DEFAULT NULL COMMENT '图片路径',
  `image_type` varchar(20) DEFAULT NULL COMMENT '图片类型',
  `image_size` bigint DEFAULT NULL COMMENT '图片大小',
  `user_id` int DEFAULT NULL COMMENT '上传用户ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`image_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='图片表';

INSERT INTO `role` (`role_id`, `role_name`) VALUES (1, 'super'), (2, 'admin');
INSERT INTO `admin` (`username`, `password`, `role_id`) VALUES ('xzj4', '123456', 1);
INSERT INTO `user` (`username`, `password`, `nickname`, `sex`) VALUES ('test1', '123456', '测试用户1', '男');
INSERT INTO `user` (`username`, `password`, `nickname`, `sex`) VALUES ('test2', '123456', '测试用户2', '女');
INSERT INTO `user` (`username`, `password`, `nickname`, `sex`) VALUES ('test3', '123456', '测试用户3', '男');
INSERT INTO `category` (`category_name`) VALUES ('学习交流'), ('生活杂谈'), ('求助问答'), ('校园活动'), ('技术分享'), ('情感话题'), ('求职招聘'), ('考研考公'), ('兼职实习'), ('租房合租'), ('美食探店'), ('旅行出行'), ('影视娱乐'), ('游戏动漫'), ('摄影美妆'), ('运动健身'), ('音乐艺术'), ('志愿公益'), ('失物招领'), ('二手闲置');
INSERT INTO `goods_category` (`goods_category_name`) VALUES ('教材书籍'), ('电子产品'), ('生活用品'), ('运动器材'), ('服装配饰'), ('其他'), ('数码配件'), ('文具办公'), ('美妆护肤'), ('食品零食'), ('乐器音响'), ('租房房源'), ('票券卡券'), ('交通工具'), ('家居装饰'), ('母婴用品'), ('宠物用品'), ('图书音像'), ('手工文创'), ('虚拟服务');

DROP TABLE IF EXISTS `follow`;
CREATE TABLE `follow` (
  `follow_id` int NOT NULL AUTO_INCREMENT,
  `follower_id` int NOT NULL COMMENT '关注者ID',
  `following_id` int NOT NULL COMMENT '被关注者ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`follow_id`),
  UNIQUE KEY `uk_follower_following` (`follower_id`, `following_id`),
  KEY `idx_follower_id` (`follower_id`),
  KEY `idx_following_id` (`following_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='关注表';
