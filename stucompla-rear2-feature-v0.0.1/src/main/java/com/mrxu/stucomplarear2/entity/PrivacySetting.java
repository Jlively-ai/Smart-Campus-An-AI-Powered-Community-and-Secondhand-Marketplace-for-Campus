package com.mrxu.stucomplarear2.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/*
CREATE TABLE privacy_setting (
    user_id VARCHAR(50) PRIMARY KEY,
    following VARCHAR(20) DEFAULT 'all',
    followers VARCHAR(20) DEFAULT 'all',
    likes VARCHAR(20) DEFAULT 'all',
    collect VARCHAR(20) DEFAULT 'all',
    posts VARCHAR(20) DEFAULT 'all',
    goods VARCHAR(20) DEFAULT 'all',
    blocked_users TEXT DEFAULT NULL
);
-- If table already exists:
-- ALTER TABLE privacy_setting ADD COLUMN blocked_users TEXT DEFAULT NULL COMMENT '屏蔽用户列表JSON';
*/
@Data
@TableName("privacy_setting")
public class PrivacySetting {

    @TableId
    private String userId;

    private String following;   // all, following, mutual, self, custom

    private String followers;   // all, following, mutual, self, custom

    private String likes;       // all, following, mutual, self, custom

    private String collect;     // all, following, mutual, self, custom

    private String posts;       // all, following, mutual, self, custom

    private String goods;       // all, following, mutual, self, custom

    private String blockedUsers; // JSON: {"following":["uid1"],"likes":[],...}
}
