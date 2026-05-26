-- 将所有ID字段从Integer AUTO_INCREMENT改为String(19)
-- 执行前请备份数据库！

-- 1. Post表
ALTER TABLE post MODIFY COLUMN post_id VARCHAR(19) NOT NULL;
ALTER TABLE post MODIFY COLUMN user_id VARCHAR(19);

-- 2. Goods表
ALTER TABLE goods MODIFY COLUMN goods_id VARCHAR(19) NOT NULL;
ALTER TABLE goods MODIFY COLUMN user_id VARCHAR(19);

-- 3. Comment表
ALTER TABLE comment MODIFY COLUMN comment_id VARCHAR(19) NOT NULL;
ALTER TABLE comment MODIFY COLUMN post_id VARCHAR(19);
ALTER TABLE comment MODIFY COLUMN user_id VARCHAR(19);
ALTER TABLE comment MODIFY COLUMN parent_id VARCHAR(19) DEFAULT '';

-- 4. Follow表
ALTER TABLE follow MODIFY COLUMN follow_id VARCHAR(19) NOT NULL;

-- 5. Letter表
ALTER TABLE letter MODIFY COLUMN letter_id VARCHAR(19) NOT NULL;

-- 6. Wall表
ALTER TABLE wall MODIFY COLUMN wall_id VARCHAR(19) NOT NULL;
ALTER TABLE wall MODIFY COLUMN user_id VARCHAR(19);

-- 7. Admin表
ALTER TABLE admin MODIFY COLUMN admin_id VARCHAR(19) NOT NULL;

-- 8. Announcement表
ALTER TABLE announcement MODIFY COLUMN announcement_id VARCHAR(19) NOT NULL;
ALTER TABLE announcement MODIFY COLUMN admin_id VARCHAR(19);

-- 9. Collect表
ALTER TABLE collect MODIFY COLUMN collect_id VARCHAR(19) NOT NULL;
ALTER TABLE collect MODIFY COLUMN user_id VARCHAR(19);
ALTER TABLE collect MODIFY COLUMN post_id VARCHAR(19);

-- 10. GoodsComment表
ALTER TABLE goods_comment MODIFY COLUMN comment_id VARCHAR(19) NOT NULL;
ALTER TABLE goods_comment MODIFY COLUMN goods_id VARCHAR(19);
ALTER TABLE goods_comment MODIFY COLUMN user_id VARCHAR(19);

-- 11. Image表
ALTER TABLE image MODIFY COLUMN image_id VARCHAR(19) NOT NULL;
ALTER TABLE image MODIFY COLUMN goods_id VARCHAR(19);

-- 12. Logistics表
ALTER TABLE logistics MODIFY COLUMN logistics_id VARCHAR(19) NOT NULL;
ALTER TABLE logistics MODIFY COLUMN order_id VARCHAR(19);

-- 13. OrderReview表
ALTER TABLE order_review MODIFY COLUMN review_id VARCHAR(19) NOT NULL;
ALTER TABLE order_review MODIFY COLUMN order_id VARCHAR(19);
ALTER TABLE order_review MODIFY COLUMN user_id VARCHAR(19);

-- 14. MarketOrder表 (goodsId外键)
ALTER TABLE market_order MODIFY COLUMN goods_id VARCHAR(19);

-- 移除AUTO_INCREMENT（如果存在）
-- 注意：MySQL中修改主键类型时会自动移除AUTO_INCREMENT

-- OrderReview表新增reply和reply_time字段
ALTER TABLE order_review ADD COLUMN reply VARCHAR(500) DEFAULT NULL COMMENT '卖家回复';
ALTER TABLE order_review ADD COLUMN reply_time datetime DEFAULT NULL COMMENT '回复时间';
