<<<<<<< HEAD
# 智联校园：面向校园场景的 AI 推荐型社区与二手交易平台

智联校园是一个功能完善的校园社区平台，融合 AI 智能推荐、论坛交流、表白墙互动、二手市场交易、社交互动、内容审核等模块。采用前后端分离架构，包含学生端、管理端和后端服务三个子项目，致力于打造面向高校场景的一站式智慧社区与交易平台。

---

## 目录

- [项目架构](#项目架构)
- [技术栈](#技术栈)
- [环境要求](#环境要求)
- [快速开始](#快速开始)
- [项目结构](#项目结构)
- [数据库设计](#数据库设计)
- [API 接口文档](#api-接口文档)
- [功能模块详解](#功能模块详解)
- [认证与权限](#认证与权限)
- [隐私与可见性控制](#隐私与可见性控制)
- [核心组件说明](#核心组件说明)
- [部署指南](#部署指南)
- [常见问题](#常见问题)

---

## 项目架构

```
stuSecond-public-master/
├── stucompla-front-feature-v0.0.1/          # 学生前端 (Vue 2 + Element UI)
├── admin/stucompla-front-admin-feature-v0.0.1/  # 管理员前端 (Vue 2 + Element UI)
├── stucompla-rear2-feature-v0.0.1/          # 后端服务 (Spring Boot + MyBatis-Plus)
├── images/                                   # 图片存储目录
├── sql/                                      # 数据库脚本
│   ├── init.sql                              # 初始化脚本
│   ├── ai_config.sql                         # AI配置表
│   └── daily_stats.sql                       # 每日统计表
├── full_rebuild.sql                          # 完整重建脚本
├── start.bat                                 # Windows启动脚本
├── stop.bat                                  # Windows停止脚本
└── README.md
```

---

## 技术栈

### 后端
| 技术 | 版本 | 说明 |
|---|---|---|
| Spring Boot | 2.5.7 | 核心框架 |
| MyBatis-Plus | 3.4.3.4 | ORM 框架 |
| Apache Shiro | 1.3.2 | 认证授权框架 |
| java-jwt | 3.4.1 | JWT Token 生成与验证 |
| Redis | - | Token 缓存、会话管理 |
| MySQL | 8.0 | 关系型数据库 |
| FastJSON | 1.2.4 | JSON 处理 |
| Swagger | 3.0 | API 文档 |
| Lombok | - | 代码简化 |

### 学生前端
| 技术 | 版本 | 说明 |
|---|---|---|
| Vue.js | 2.6.11 | 前端框架 |
| Vue Router | 3.2.0 | 路由管理 |
| Vuex | 3.4.0 | 状态管理 |
| Element UI | 2.15.6 | UI 组件库 |
| Axios | 0.21.4 | HTTP 请求 |
| Vue CLI | 4.5 | 构建工具 |

### 管理员前端
| 技术 | 版本 | 说明 |
|---|---|---|
| Vue.js | 2.6.10 | 前端框架 |
| Element UI | 2.13.2 | UI 组件库 |
| ECharts | 4.2.1 | 数据可视化 |
| xlsx | 0.14.1 | Excel 导出 |

---

## 环境要求

| 软件 | 版本要求 | 说明 |
|---|---|---|
| JDK | 1.8+ | Java 运行环境 |
| Node.js | 12.x+ | 前端构建环境 |
| npm | 6.x+ | 包管理器 |
| MySQL | 8.0+ | 数据库（需支持 utf8mb4） |
| Redis | 5.x+ | 缓存服务 |
| Maven | 3.6+ | Java 构建工具 |

---

## 快速开始

### 1. 初始化数据库

```bash
# 登录 MySQL
mysql -u root -p

# 创建数据库
CREATE DATABASE database1 CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

# 导入数据（注意：必须使用 source 命令以正确处理中文编码）
USE database1;
source d:/Desktop/Javaweb/stuSecond-public-master/full_rebuild.sql;
```

> **重要提示**：不要使用 PowerShell 的 `Get-Content | mysql` 管道方式导入，会导致中文乱码。必须使用 `mysql -e "source file.sql"` 或在 MySQL 客户端内使用 `source` 命令。

### 2. 配置后端

编辑 `stucompla-rear2-feature-v0.0.1/src/main/resources/application.properties`：

```properties
# 数据库配置
spring.datasource.url=jdbc:mysql://localhost:3306/database1?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai
spring.datasource.username=root
spring.datasource.password=111111

# Redis 配置
spring.redis.host=127.0.0.1
spring.redis.port=6379

# 图片存储路径
file.upload-path=d:/Desktop/Javaweb/stuSecond-public-master/images/
```

### 3. 启动后端

```bash
cd stucompla-rear2-feature-v0.0.1
mvn spring-boot:run
```

后端服务启动在 `http://localhost:8086`

### 4. 启动学生前端

```bash
cd stucompla-front-feature-v0.0.1
npm install
npm run dev
```

学生前端启动在 `http://localhost:8081`

### 5. 启动管理员前端

```bash
cd admin/stucompla-front-admin-feature-v0.0.1
npm install
npm run dev
```

管理员前端启动在 `http://localhost:9528`

### 6. 默认账号

| 角色 | 用户名 | 密码 | 说明 |
|---|---|---|---|
| 超级管理员 | superadmin | 123456 | 拥有所有权限 |
| 普通管理员 | admin | 123456 | 部分权限 |
| 测试用户 | zhangsan | 123456 | 普通用户 |
| 测试用户 | lisi | 123456 | 普通用户 |
| 测试用户 | wangwu | 123456 | 普通用户 |

---

## 项目结构

### 学生前端目录

```
stucompla-front-feature-v0.0.1/src/
├── App.vue                          # 根组件（含@提及全局事件委托）
├── main-dev.js                      # 开发环境入口（含全局过滤器）
├── main-prod.js                     # 生产环境入口
├── components/
│   ├── AiChat.vue                   # AI智能助手组件
│   ├── MentionInput.vue             # @提及输入组件
│   └── SearchPanel.vue             # 搜索面板组件
├── router/index.js                  # 路由配置
├── store/index.js                   # Vuex 状态管理
└── views/
    ├── Home.vue                     # 首页框架（侧边栏+导航栏）
    ├── Login.vue                    # 登录页
    ├── Register.vue                 # 注册页
    ├── Dashboard.vue                # 仪表盘（统计+物流+排行榜）
    ├── StatsPage.vue                # 数据统计页
    ├── announcement/
    │   └── AnnouncementList.vue     # 公告列表
    ├── goods/
    │   ├── GoodsList.vue           # 商品列表
    │   ├── GoodsDetail.vue         # 商品详情
    │   ├── GoodsPublish.vue        # 发布商品
    │   └── Cart.vue                # 购物车
    ├── post/
    │   ├── PostList.vue            # 帖子列表
    │   ├── PostDetail.vue          # 帖子详情（含评论、@提及）
    │   └── PostPublish.vue         # 发布帖子
    ├── search/
    │   └── SearchResults.vue       # 搜索结果
    ├── user/
    │   ├── MyInfo.vue              # 个人信息
    │   ├── MyLetter.vue            # 消息中心
    │   ├── UserProfile.vue         # 用户主页
    │   ├── SecurityCenter.vue      # 安全中心
    │   ├── MyPost.vue              # 我的帖子
    │   ├── MyComment.vue           # 我的评论
    │   ├── MyGoods.vue             # 我的闲置
    │   ├── MyOrder.vue             # 我的订单
    │   ├── MySaleOrder.vue         # 销售订单
    │   ├── MyWall.vue              # 我的表白墙
    │   ├── MyFollowers.vue         # 我的粉丝
    │   ├── MyFollowing.vue         # 我的关注
    │   ├── MyPunishment.vue        # 违规记录（合并删除+锁定）
    │   ├── OrderDetail.vue         # 订单详情
    │   └── RecycleBin.vue          # 回收站
    └── wall/
        ├── WallList.vue            # 表白墙列表（含发布）
        ├── WallDetail.vue          # 表白墙详情（含评论）
        └── WallApply.vue           # 申请上墙
```

### 管理员前端目录

```
admin/stucompla-front-admin-feature-v0.0.1/src/
├── App.vue
├── main.js
├── settings.js
├── api/
│   ├── manage.js                    # API 封装
│   └── violation.js                # 违规管理 API
├── layout/index.vue                 # 管理后台布局（含侧边栏菜单）
├── router/index.js                  # 路由配置
├── store/                           # Vuex 状态管理
├── utils/
│   ├── auth.js                     # Token 管理
│   └── request.js                  # Axios 封装
├── components/
│   ├── UserDetailDialog.vue        # 用户详情弹窗
│   ├── PostDetailDialog.vue        # 帖子详情弹窗
│   ├── GoodsDetailDialog.vue       # 商品详情弹窗
│   └── WallDetailDialog.vue        # 表白墙详情弹窗
└── views/
    ├── login/index.vue             # 管理员登录
    ├── dashboard/index.vue         # 仪表盘
    ├── user/index.vue              # 用户管理
    ├── admin/index.vue             # 管理员管理
    ├── post/index.vue              # 帖子管理
    ├── comment/index.vue           # 评论管理
    ├── wall/index.vue              # 表白墙审核
    ├── goods/index.vue             # 商品管理
    ├── goods-category/index.vue    # 商品分类
    ├── post-category/index.vue     # 帖子分类
    ├── order/index.vue             # 订单管理
    ├── after-sale/index.vue        # 售后管理
    ├── review/index.vue            # 评价管理
    ├── logistics/index.vue         # 物流管理
    ├── announcement/index.vue      # 公告管理
    ├── report/index.vue            # 举报管理
    ├── punishment/index.vue        # 处罚管理
    ├── violation/index.vue         # 违规管理（删除+锁定+申诉）
    └── ai-config/index.vue         # AI模型配置
```

### 后端目录

```
stucompla-rear2-feature-v0.0.1/src/main/java/com/mrxu/stucomplarear2/
├── StucomplaRear2Application.java   # 启动类
├── config/
│   ├── GlobalExceptionHandler.java  # 全局异常处理
│   ├── MybatisPlusConfig.java       # MyBatis-Plus 配置
│   ├── RedisConfig.java            # Redis 配置
│   ├── ShiroConfig.java            # Shiro 安全配置
│   └── WebConfig.java             # Web 配置（CORS、静态资源映射）
├── controller/                      # 控制器层（21个）
│   ├── UserController.java
│   ├── admin/AdminController.java
│   ├── PostController.java
│   ├── CommentController.java
│   ├── WallController.java
│   ├── GoodsController.java
│   ├── GoodsCommentController.java
│   ├── MarketOrderController.java
│   ├── CategoryController.java
│   ├── GoodsCategoryController.java
│   ├── CollectController.java
│   ├── FollowController.java
│   ├── LetterController.java
│   ├── AnnouncementController.java
│   ├── ImageController.java
│   ├── LogisticsController.java
│   ├── OrderReviewController.java
│   ├── ReportController.java
│   ├── PunishmentController.java
│   ├── PrivacySettingController.java
│   └── StatsController.java
├── dto/                             # 数据传输对象
│   ├── PostVo.java                 # 帖子视图对象
│   ├── CommentVo.java              # 评论视图对象
│   ├── GoodsVo.java                # 商品视图对象
│   ├── CommentDto.java             # 评论提交DTO
│   ├── PostFindDto.java            # 帖子查询DTO
│   ├── WallFindDto.java            # 表白墙查询DTO
│   ├── WallApplyDto.java           # 上墙申请DTO
│   ├── WallEditDto.java            # 上墙编辑DTO
│   ├── WallAuditDto.java           # 上墙审核DTO
│   ├── AdminFindDto.java           # 管理员查询DTO
│   └── ... (其他DTO)
├── entity/                          # 实体类（22个）
├── mapper/                          # MyBatis Mapper 接口（22个）
├── service/                         # 服务层接口
│   └── impl/                       # 服务层实现
└── utils/                           # 工具类
    ├── IdGenerator.java            # ID 生成器
    ├── jwt/JWTUtil.java            # JWT 工具
    └── redis/RedisUtil.java        # Redis 工具
```

---

## 数据库设计

数据库名：`database1`，字符集：`utf8mb4`，共 22 张表。

### 核心表结构

#### user - 用户表
| 字段 | 类型 | 说明 |
|---|---|---|
| user_id | VARCHAR(19) | 主键，雪花算法生成 |
| username | VARCHAR(50) | 用户名，唯一 |
| password | VARCHAR(100) | 密码（MD5加密） |
| nickname | VARCHAR(50) | 昵称 |
| sex | VARCHAR(10) | 性别 |
| avatar | VARCHAR(255) | 头像路径 |
| phone | VARCHAR(20) | 手机号 |
| email | VARCHAR(50) | 邮箱 |
| signature | VARCHAR(200) | 个性签名 |
| locked | INT | 是否锁定（0否/1是） |
| status | INT | 状态（0正常/1锁定/2注销） |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |

#### admin - 管理员表
| 字段 | 类型 | 说明 |
|---|---|---|
| admin_id | VARCHAR(19) | 主键 |
| username | VARCHAR(50) | 用户名 |
| password | VARCHAR(100) | 密码 |
| role_id | INT | 角色ID（1=超级管理员/2=普通管理员） |
| permissions | TEXT | 权限列表（逗号分隔） |
| create_time | DATETIME | 创建时间 |

#### post - 帖子表
| 字段 | 类型 | 说明 |
|---|---|---|
| post_id | VARCHAR(19) | 主键 |
| title | VARCHAR(100) | 标题 |
| detail | TEXT | 内容 |
| images | TEXT | 图片（逗号分隔） |
| user_id | VARCHAR(19) | 作者ID |
| category_id | INT | 分类ID |
| comment_num | INT | 评论数 |
| view_num | INT | 浏览数 |
| best_post | TINYINT(1) | 是否精帖 |
| collect_num | INT | 收藏数 |
| like_num | INT | 点赞数 |
| share_num | INT | 分享数 |
| post_status | INT | 状态（0正常/1锁定） |
| visibility | VARCHAR(20) | 可见范围（all/following/mutual/self/custom） |
| blocked_users | TEXT | 屏蔽用户列表（JSON数组） |
| mention_users | TEXT | @提及用户列表（JSON数组） |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |

#### comment - 评论表
| 字段 | 类型 | 说明 |
|---|---|---|
| comment_id | VARCHAR(19) | 主键 |
| text | TEXT | 评论内容 |
| images | TEXT | 图片 |
| post_id | VARCHAR(19) | 关联帖子/表白墙ID |
| parent_id | VARCHAR(19) | 父评论ID（空=顶级评论） |
| user_id | VARCHAR(19) | 评论者ID |
| user_type | VARCHAR(10) | 评论者类型（user/admin） |
| target_type | VARCHAR(10) | 目标类型（post/wall） |
| like_num | INT | 点赞数 |
| mention_users | TEXT | @提及用户列表（JSON数组） |
| create_time | DATETIME | 创建时间 |

#### wall - 表白墙表
| 字段 | 类型 | 说明 |
|---|---|---|
| wall_id | VARCHAR(19) | 主键 |
| wall_content | TEXT | 内容 |
| wall_images | TEXT | 图片 |
| user_id | VARCHAR(19) | 作者ID |
| admin_id | VARCHAR(19) | 审核管理员ID |
| audit_state | INT | 审核状态（0待审/1通过/2未通过） |
| cause | VARCHAR(200) | 审核原因 |
| is_anonymous | TINYINT(1) | 是否匿名 |
| visibility | VARCHAR(20) | 可见范围 |
| blocked_users | TEXT | 屏蔽用户列表（JSON数组） |
| mention_users | TEXT | @提及用户列表（JSON数组） |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |

#### goods - 二手商品表
| 字段 | 类型 | 说明 |
|---|---|---|
| goods_id | VARCHAR(19) | 主键 |
| goods_name | VARCHAR(100) | 商品名称 |
| goods_detail | TEXT | 商品详情 |
| goods_images | TEXT | 商品图片 |
| goods_price | DOUBLE | 定价 |
| goods_category_id | INT | 分类ID |
| goods_count | INT | 数量 |
| goods_status | TINYINT(1) | 状态（0下架/1上架） |
| user_id | VARCHAR(19) | 卖家ID |
| view_num | INT | 浏览数 |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |

#### market_order - 订单表
| 字段 | 类型 | 说明 |
|---|---|---|
| order_id | VARCHAR(19) | 主键 |
| seller_id | VARCHAR(19) | 卖家ID |
| buyer_id | VARCHAR(19) | 买家ID |
| goods_id | VARCHAR(19) | 商品ID |
| buy_count | INT | 购买数量 |
| total_price | DOUBLE | 总价 |
| order_status | INT | 状态（0待付款/1已付款/2已发货/3已签收/4已退货/5已完成） |
| pay_time | DATETIME | 付款时间 |
| send_time | DATETIME | 发货时间 |
| receipt_time | DATETIME | 签收时间 |

#### letter - 私信/通知表
| 字段 | 类型 | 说明 |
|---|---|---|
| letter_id | VARCHAR(19) | 主键 |
| receiver_id | VARCHAR(19) | 接收者ID |
| sender_id | VARCHAR(19) | 发送者ID（0=系统） |
| letter_detail | TEXT | 消息内容 |
| letter_status | INT | 状态（0未读/1已读） |
| session_id | VARCHAR(100) | 会话ID |
| message_type | VARCHAR(20) | 类型（letter/comment/like/follow/collect/mention/system） |
| create_time | DATETIME | 创建时间 |

#### report - 举报表
| 字段 | 类型 | 说明 |
|---|---|---|
| report_id | VARCHAR(50) | 主键 |
| reporter_id | VARCHAR(19) | 举报者ID |
| target_type | VARCHAR(20) | 目标类型（post/goods/wall/comment/goods_comment） |
| target_id | VARCHAR(19) | 目标ID |
| reason | VARCHAR(200) | 举报原因 |
| status | INT | 状态（0待处理/1已处理/2已驳回） |
| handler_id | VARCHAR(19) | 处理者ID |
| handle_result | TEXT | 处理结果 |

#### punishment - 处罚表
| 字段 | 类型 | 说明 |
|---|---|---|
| punishment_id | VARCHAR(50) | 主键 |
| user_id | VARCHAR(19) | 被处罚用户ID |
| type | VARCHAR(20) | 类型（mute禁言/ban封号/warning警告） |
| reason | VARCHAR(200) | 原因 |
| start_time | DATETIME | 开始时间 |
| end_time | DATETIME | 结束时间 |
| status | INT | 状态（0生效/1已解除/2已过期） |
| appeal_reason | TEXT | 申诉理由 |
| appeal_state | INT | 申诉状态（0待审核/1通过/2驳回） |

#### 其他表
- **category** - 帖子分类表
- **goods_category** - 商品分类表
- **post_like** - 帖子点赞表
- **collect** - 收藏表
- **follow** - 关注表
- **goods_comment** - 商品评论表
- **order_review** - 订单评价表
- **logistics** - 物流表
- **announcement** - 公告表
- **image** - 图片表
- **privacy_setting** - 隐私设置表
- **role** - 角色表

---

## API 接口文档

后端服务运行在 `http://localhost:8086`，所有接口返回统一格式：

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {}
}
```

### 认证相关

| 方法 | 路径 | 说明 | 权限 |
|---|---|---|---|
| POST | `/user/register` | 用户注册 | 公开 |
| POST | `/user/login` | 用户登录 | 公开 |
| DELETE | `/user/logout` | 用户登出 | 认证 |
| POST | `/admin/info/login` | 管理员登录 | 公开 |
| DELETE | `/admin/info/logout` | 管理员登出 | 认证 |

### 用户模块 (`/user`)

| 方法 | 路径 | 说明 | 权限 |
|---|---|---|---|
| GET | `/user/info` | 获取当前用户信息 | 认证 |
| POST | `/user/editUserInfo` | 编辑个人信息 | 认证 |
| POST | `/user/changePassword` | 修改密码 | 认证 |
| POST | `/user/uploadAvatar` | 上传头像 | 认证 |
| POST | `/user/deactivate` | 注销账号 | user |
| GET | `/user/publicInfo/{userId}` | 获取用户公开信息 | 公开 |
| GET | `/user/batchInfo?ids=id1,id2` | 批量获取用户信息 | 公开 |
| GET | `/user/search?keyword=xxx` | 搜索用户（支持昵称/用户名/ID） | 公开 |
| GET | `/user/list` | 用户列表（管理端） | admin/super |
| POST | `/user/lockedUser` | 锁定用户 | admin/super |
| POST | `/user/unLockUser` | 解锁用户 | admin/super |

### 帖子模块 (`/post`)

| 方法 | 路径 | 说明 | 权限 |
|---|---|---|---|
| POST | `/post/publish` | 发布帖子 | 认证 |
| POST | `/post/edit` | 编辑帖子 | 认证 |
| DELETE | `/post/{postId}` | 删除帖子 | 认证 |
| GET | `/post/{postId}` | 帖子详情 | 公开 |
| GET | `/post/list` | 帖子列表 | 公开 |
| POST | `/post/like/{postId}` | 点赞 | 认证 |
| POST | `/post/unlike/{postId}` | 取消点赞 | 认证 |
| POST | `/post/share/{postId}` | 分享 | 认证 |
| GET | `/post/myLikes` | 我的点赞列表 | 认证 |

### 评论模块 (`/comment`)

| 方法 | 路径 | 说明 | 权限 |
|---|---|---|---|
| POST | `/comment/create` | 发表评论（支持@提及） | 认证 |
| DELETE | `/comment/{commentId}` | 删除评论 | 认证 |
| GET | `/comment/list/{postId}/{page}/{size}` | 帖子评论列表 | 公开 |
| GET | `/comment/wallList/{wallId}/{page}/{size}` | 表白墙评论列表 | 公开 |
| POST | `/comment/like/{commentId}` | 点赞评论 | 认证 |
| POST | `/comment/unlike/{commentId}` | 取消点赞 | 认证 |

### 表白墙模块 (`/wall`)

| 方法 | 路径 | 说明 | 权限 |
|---|---|---|---|
| POST | `/wall/apply` | 申请上墙 | 认证 |
| POST | `/wall/audit` | 审核上墙 | admin/super |
| POST | `/wall/edit` | 编辑表白墙 | 认证 |
| GET | `/wall/wallList` | 表白墙列表 | 公开 |
| GET | `/wall/myWallList` | 我的表白墙 | 认证 |

### 商品模块 (`/goods`)

| 方法 | 路径 | 说明 | 权限 |
|---|---|---|---|
| POST | `/goods/add` | 发布商品 | 认证 |
| POST | `/goods/edit` | 编辑商品 | 认证 |
| GET | `/goods/getList` | 商品列表 | 公开 |
| GET | `/goods/{goodsId}` | 商品详情 | 公开 |
| POST | `/goods/putMyGoods/{goodsId}` | 重新上架 | 认证 |
| POST | `/goods/unShelveMyGoods/{goodsId}` | 下架商品 | 认证 |

### 订单模块 (`/market-order`)

| 方法 | 路径 | 说明 | 权限 |
|---|---|---|---|
| POST | `/market-order/addOrder` | 创建订单 | 认证 |
| POST | `/market-order/payOrder/{orderId}` | 支付订单 | 认证 |
| GET | `/market-order/sendGoods/{orderId}` | 发货 | 认证 |
| POST | `/market-order/receipt/{orderId}` | 签收 | 认证 |
| POST | `/market-order/applyReturn/{orderId}` | 申请退货 | 认证 |
| GET | `/market-order/myOrder` | 我的订单 | 认证 |
| GET | `/market-order/detail/{orderId}` | 订单详情 | 认证 |

### 社交模块

| 方法 | 路径 | 说明 | 权限 |
|---|---|---|---|
| POST | `/follow/add/{userId}` | 关注用户 | 认证 |
| DELETE | `/follow/cancel/{userId}` | 取消关注 | 认证 |
| GET | `/follow/check/{userId}` | 检查是否关注 | 认证 |
| GET | `/follow/followers/{userId}` | 粉丝列表 | 公开 |
| GET | `/follow/following/{userId}` | 关注列表 | 公开 |
| GET | `/collect/check/{postId}` | 检查是否收藏 | 认证 |
| POST | `/collect/add/{postId}` | 收藏帖子 | 认证 |
| DELETE | `/collect/delete/{postId}` | 取消收藏 | 认证 |

### 消息模块 (`/letter`)

| 方法 | 路径 | 说明 | 权限 |
|---|---|---|---|
| POST | `/letter/send` | 发送私信 | 认证 |
| POST | `/letter/mySessionList` | 会话列表 | 认证 |
| GET | `/letter/letterList/{sessionId}` | 会话消息 | 认证 |
| POST | `/letter/myNoticeList` | 通知列表 | 认证 |
| POST | `/letter/myUnReadTotal` | 未读总数 | 认证 |
| POST | `/letter/markAllRead` | 全部标记已读 | 认证 |

### 管理模块

| 方法 | 路径 | 说明 | 权限 |
|---|---|---|---|
| GET | `/admin/info/list` | 管理员列表 | super |
| POST | `/admin/info/add` | 添加管理员 | super |
| POST | `/admin/info/changeRole` | 修改角色 | super |
| POST | `/admin/info/changePermissions` | 修改权限 | super |
| POST | `/report/submit` | 提交举报 | 认证 |
| GET | `/report/list` | 举报列表 | admin/super |
| POST | `/report/handle` | 处理举报 | admin/super |
| POST | `/punishment/create` | 创建处罚 | admin/super |
| POST | `/punishment/lift/{id}` | 解除处罚 | admin/super |
| POST | `/punishment/appeal` | 提交申诉 | user |
| POST | `/punishment/handleAppeal` | 处理申诉 | admin/super |
| POST | `/announcement/publish` | 发布公告 | admin/super |
| GET | `/announcement/publicList` | 公告列表 | 公开 |

### 统计模块 (`/stats`)

| 方法 | 路径 | 说明 | 权限 |
|---|---|---|---|
| GET | `/stats/myStats` | 个人统计 | 认证 |
| GET | `/stats/likeCount` | 点赞数 | 认证 |
| GET | `/stats/overview` | 全局概览 | admin/super |
| GET | `/stats/weeklyTrend` | 周趋势 | admin/super |

### AI 智能助手模块 (`/ai`)

| 方法 | 路径 | 说明 | 权限 |
|---|---|---|---|
| POST | `/ai/chat` | AI 对话 | 认证 |
| GET | `/ai/config` | 获取 AI 配置 | admin/super |
| POST | `/ai/config/update` | 更新 AI 配置 | admin/super |

---

## 功能模块详解

### 1. AI 智能助手

- **智能对话**：集成大语言模型（支持 DeepSeek / OpenAI / 自定义模型），提供自然语言交互
- **内容推荐**：AI 根据用户兴趣和浏览历史，智能推荐帖子、商品、表白墙内容
- **数据分析**：AI 可分析销售数据、用户行为趋势，给出经营建议
- **文案辅助**：帮助用户生成帖子、商品描述等文案内容
- **校园问答**：回答校园生活相关问题，引导用户操作
- **可配置管理**：管理员可在后台配置 AI 模型参数、系统提示词、API Key 等

### 2. 论坛系统

- **帖子发布**：支持标题、内容、分类、图片上传（最多9张）、可见范围设置、屏蔽用户、@提及
- **帖子详情**：展示帖子内容、作者信息、评论列表、点赞/收藏/分享按钮
- **帖子编辑**：作者可编辑自己的帖子，管理员可锁定/解锁帖子
- **评论系统**：支持多级回复（嵌套评论），评论点赞，@提及
- **@提及功能**：输入 `@` 自动弹出候选用户列表，选择后自动插入 `@昵称`，被提及者收到通知

### 2. 表白墙

- **申请上墙**：支持匿名发布、可见范围控制、@提及
- **审核机制**：管理员审核通过后才公开展示
- **评论功能**：与帖子评论共用评论系统，通过 `targetType` 区分

### 4. 二手市场

- **商品发布**：名称、详情、价格、分类、数量、图片
- **商品状态**：上架/下架切换
- **商品评论**：独立的评论系统，支持评分（1-5星）
- **交易流程**：下单→付款→发货→签收→退货（可选）→完成

### 4. 社交功能

- **关注系统**：关注/取消关注，互相关注标识
- **私信系统**：会话式私信，支持实时消息
- **系统通知**：评论通知、点赞通知、收藏通知、关注通知、@提及通知
- **消息中心**：分类展示互动消息（全部/新关注/点赞与收藏/提及/收到的评论/发出的评论）

### 5. 内容审核

- **举报系统**：支持举报帖子、商品、表白墙、评论，选择举报原因
- **处罚系统**：禁言（禁止发帖/评论）、封号（禁止登录）、警告
- **申诉机制**：被处罚用户可提交申诉，管理员审核

### 6. 隐私控制

- **可见范围**：帖子/表白墙支持5种可见范围
  - `all` - 所有人可见
  - `following` - 仅关注者可见
  - `mutual` - 仅互相关注可见
  - `self` - 仅自己可见
  - `custom` - 不给指定用户看
- **隐私设置**：控制个人主页各模块的可见性
- **管理员绕过**：超级管理员和有 `view_privacy` 权限的管理员可查看所有内容

### 7. 数据统计

- **个人统计**：帖子数、评论数、商品数、收藏数、粉丝数、订单数、收入、周趋势
- **全局统计**：用户总数、帖子总数、商品总数、订单总数、销售趋势

---

## 认证与权限

### JWT Token 认证

1. 用户登录后，后端生成 JWT Token 并存入 Redis
2. 前端将 Token 存储在 `sessionStorage`（学生端 key: `token`，管理端 key: `admin_token`）
3. 每次请求在 Header 中携带 `Authorization: <token>`（无 Bearer 前缀）
4. Shiro Filter 验证 Token 有效性

### 角色体系

| 角色 | role_id | 说明 |
|---|---|---|
| user | - | 普通用户 |
| admin | 2 | 普通管理员 |
| super | 1 | 超级管理员 |

### 权限列表

| 权限标识 | 说明 |
|---|---|
| post_manage | 帖子管理 |
| goods_manage | 商品管理 |
| wall_manage | 表白墙管理 |
| comment_manage | 评论管理 |
| order_manage | 订单管理 |
| announcement_manage | 公告管理 |
| user_manage | 用户管理 |
| report_manage | 举报管理 |
| punishment_manage | 处罚管理 |
| appeal_manage | 申诉管理 |
| stats_view | 数据统计查看 |
| view_privacy | 查看隐私内容 |

---

## 隐私与可见性控制

### 可见性检查流程

```
1. 管理员（super 或有 view_privacy 权限）→ 直接通过
2. visibility = all 或 null → 所有人可见
3. visibility = self → 仅作者可见
4. visibility = following → 检查是否关注了作者
5. visibility = mutual → 检查是否互相关注
6. visibility = custom → 检查是否在屏蔽列表中
```

### 隐私设置模块

用户可控制以下模块的可见性：
- 关注列表 (following)
- 粉丝列表 (followers)
- 点赞列表 (likes)
- 收藏列表 (collect)
- 帖子列表 (posts)
- 商品列表 (goods)

---

## 核心组件说明

### MentionInput 组件

@提及输入组件，用于帖子发布、表白墙发布、评论输入等场景。

**功能**：
- 输入 `@` 符号后自动弹出候选用户列表
- 每输入一个字符实时更新候选列表
- 点击候选用户后自动插入 `@昵称 ` 到文本中
- 通过 `mention-change` 事件通知父组件被提及的用户ID列表

**使用方式**：
```vue
<MentionInput
  v-model="form.detail"
  :rows="8"
  placeholder="请输入内容"
  @mention-change="onMentionChange"
/>
```

**事件**：
- `input` - 文本内容变化时触发（v-model 绑定）
- `mention-change` - @提及用户变化时触发，参数为用户ID数组

### @提及链接渲染

被@提及的用户在内容中以蓝色可点击链接形式展示，点击后跳转到该用户的个人主页。

**实现原理**：
1. 后端返回 `mentionUsers` 字段（JSON数组，如 `["userId1","userId2"]`）
2. 前端调用 `/user/batchInfo` 获取被提及用户的昵称
3. `renderMentionText` 方法将文本中的 `@昵称` 替换为 `<a class="mention-link" data-userid="xxx">` 链接
4. App.vue 中通过全局事件委托处理 `.mention-link` 的点击事件

### 全局过滤器

| 过滤器 | 用法 | 说明 |
|---|---|---|
| formatId | `{{ id \| formatId('user') }}` | 格式化ID显示（添加前缀，如 USR-xxx） |
| formatTime | `{{ time \| formatTime }}` | 格式化时间为 yyyy-MM-dd HH:mm:ss |

### ID 生成规则

使用 MyBatis-Plus 的 `ASSIGN_ID` 策略（雪花算法），生成19位数字字符串 ID。

---

## 部署指南

### 生产环境构建

#### 学生前端
```bash
cd stucompla-front-feature-v0.0.1
npm run build
# 产物在 dist/ 目录
```

#### 管理员前端
```bash
cd admin/stucompla-front-admin-feature-v0.0.1
npm run build:prod
# 产物在 dist/ 目录
```

#### 后端
```bash
cd stucompla-rear2-feature-v0.0.1
mvn clean package -DskipTests
# 产物在 target/ 目录
java -jar target/stucompla-rear2-0.0.1-SNAPSHOT.jar
```

### Nginx 配置示例

```nginx
# 学生前端
server {
    listen 80;
    server_name student.example.com;
    root /path/to/stucompla-front/dist;
    index index.html;

    location /dev_api/ {
        proxy_pass http://localhost:8086/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    location /images/ {
        alias /path/to/images/;
    }
}

# 管理员前端
server {
    listen 80;
    server_name admin.example.com;
    root /path/to/admin-front/dist;
    index index.html;

    location /dev_api/ {
        proxy_pass http://localhost:8086/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

---

## 常见问题

### 1. 数据库中文显示为问号

**原因**：PowerShell 的 `Get-Content | mysql` 管道方式会丢失 UTF-8 编码。

**解决**：使用 MySQL 客户端内的 `source` 命令导入：
```sql
USE database1;
source /path/to/full_rebuild.sql;
```

### 2. 图片上传失败

- 检查 `application.properties` 中的 `file.upload-path` 是否正确
- 确保图片存储目录存在且有写入权限
- 检查 Nginx 是否正确映射了 `/images/` 路径

### 3. 登录后 Token 丢失

- 学生端 Token 存储在 `sessionStorage` 的 `token` 键
- 管理端 Token 存储在 `sessionStorage` 的 `admin_token` 键
- `sessionStorage` 在浏览器标签页关闭后清除

### 4. @提及候选列表不弹出

- 确保后端 `/user/search` 接口正常（无需认证）
- 检查 ShiroConfig 中 `/user/search` 是否配置为 `anon`
- 输入 `@` 时必须前面是空格或位于文本开头

### 5. 被@用户链接不可点击

- 确保后端返回了 `mentionUsers` 字段
- 检查 PostVo/CommentVo 是否包含 `mentionUsers` 字段
- 确认 `renderMentionText` 方法正确调用了 `loadMentionUsers`

### 6. 端口冲突

- 后端默认端口：8086
- 学生前端默认端口：8081
- 管理员前端默认端口：9528
- Redis 默认端口：6379
- MySQL 默认端口：3306

---

## 开发注意事项

1. **Vue 2 语法**：不支持可选链 `?.`，使用 `||` 和条件判断代替
2. **Element UI 版本**：学生端使用 2.15.6，不支持 `el-descriptions` 组件
3. **路由跳转**：所有 `router.push()` 调用需添加 `.catch(() => {})` 避免导航重复错误
4. **axios 响应**：响应拦截器已解包 `res.data`，所以 `this.$axios.get().then(res => ...)` 直接获取 `{code, msg, data}`
5. **Authorization Header**：Token 直接作为值，不加 `Bearer ` 前缀
6. **ID 格式**：前端显示时使用 `formatId` 过滤器添加前缀（如 USR-xxx），但传给后端时使用原始 ID
7. **BeanUtils.copyProperties**：只会复制目标类中存在的字段，确保 Vo 类包含所有需要的字段
8. **el-input type="textarea"**：在 Vue 2 中使用 `@input` 事件处理输入，避免与 `v-model` 冲突
9. **v-html 中的事件**：通过 `v-html` 渲染的 HTML 无法绑定 Vue 事件，需使用事件委托
10. **数据库编码**：必须使用 `utf8mb4` 以支持 emoji 和特殊字符

---

## 许可证

本项目仅供学习和研究使用。
=======
# Smart-Campus-An-AI-Powered-Community-and-Secondhand-Marketplace-for-Campus
# 智联校园：面向校园场景的 AI 推荐型社区与二手交易平台  智联校园是一个功能完善的校园社区平台，融合 AI 智能推荐、论坛交流、表白墙互动、二手市场交易、社交互动、内容审核等模块。采用前后端分离架构，包含学生端、管理端和后端服务三个子项目，致力于打造面向高校场景的一站式智慧社区与交易平台。
>>>>>>> 09202f51ce8d014ba7b793c53a23d21d946126f1
