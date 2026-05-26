CREATE TABLE IF NOT EXISTS daily_stats (
    id VARCHAR(32) PRIMARY KEY,
    item_type VARCHAR(20) NOT NULL COMMENT 'post/wall/goods',
    item_id VARCHAR(20) NOT NULL,
    user_id VARCHAR(20) NOT NULL,
    view_num INT DEFAULT 0,
    like_num INT DEFAULT 0,
    collect_num INT DEFAULT 0,
    share_num INT DEFAULT 0,
    stat_date DATE NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_item_date (item_type, item_id, stat_date),
    KEY idx_user_type (user_id, item_type, stat_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='每日统计';
