package com.mrxu.stucomplarear2.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mrxu.stucomplarear2.entity.Follow;
import com.mrxu.stucomplarear2.entity.PrivacySetting;
import com.mrxu.stucomplarear2.entity.Admin;
import com.mrxu.stucomplarear2.mapper.FollowMapper;
import com.mrxu.stucomplarear2.mapper.PrivacySettingMapper;
import com.mrxu.stucomplarear2.mapper.AdminMapper;
import com.mrxu.stucomplarear2.service.PrivacySettingService;
import com.mrxu.stucomplarear2.utils.response.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class PrivacySettingServiceImpl implements PrivacySettingService {

    @Autowired
    private PrivacySettingMapper privacySettingMapper;

    @Autowired
    private FollowMapper followMapper;

    @Autowired
    private AdminMapper adminMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public PrivacySetting getMyPrivacy(String userId) {
        PrivacySetting setting = privacySettingMapper.selectById(userId);
        if (setting == null) {
            setting = new PrivacySetting();
            setting.setUserId(userId);
            setting.setFollowing("all");
            setting.setFollowers("all");
            setting.setLikes("all");
            setting.setCollect("all");
            setting.setPosts("all");
            setting.setGoods("all");
        }
        return setting;
    }

    @Override
    public Result updatePrivacy(PrivacySetting setting) {
        PrivacySetting existing = privacySettingMapper.selectById(setting.getUserId());
        if (existing == null) {
            privacySettingMapper.insert(setting);
        } else {
            privacySettingMapper.updateById(setting);
        }
        return Result.succ("更新成功");
    }

    @Override
    public boolean checkVisibility(String targetUserId, String viewerUserId, String field) {
        return checkVisibility(targetUserId, viewerUserId, field, null);
    }

    @Override
    public boolean checkVisibility(String targetUserId, String viewerUserId, String field, String viewerRole) {
        // 超级管理员(roleId=1)始终绕过隐私检查
        if ("super".equals(viewerRole)) {
            return true;
        }
        // 普通管理员(roleId=2)检查是否有view_privacy权限
        if ("admin".equals(viewerRole)) {
            Admin admin = adminMapper.selectOne(new QueryWrapper<Admin>().eq("username", viewerUserId).last("LIMIT 1"));
            if (admin != null && admin.getPermissions() != null && admin.getPermissions().contains("view_privacy")) {
                return true;
            }
        }
        // 作者自己始终可见
        if (targetUserId.equals(viewerUserId)) {
            return true;
        }

        PrivacySetting setting = getMyPrivacy(targetUserId);
        String visibility;
        switch (field) {
            case "following":
                visibility = setting.getFollowing();
                break;
            case "followers":
                visibility = setting.getFollowers();
                break;
            case "likes":
                visibility = setting.getLikes();
                break;
            case "collect":
                visibility = setting.getCollect();
                break;
            case "posts":
                visibility = setting.getPosts();
                break;
            case "goods":
                visibility = setting.getGoods();
                break;
            default:
                return true;
        }

        if (visibility == null || "all".equals(visibility)) {
            // 检查是否在屏蔽列表中
            return !isBlocked(setting, field, viewerUserId);
        }
        if ("self".equals(visibility)) {
            return false; // 已经排除了作者自己
        }
        if ("following".equals(visibility)) {
            QueryWrapper<Follow> wrapper = new QueryWrapper<>();
            wrapper.eq("follower_id", viewerUserId).eq("following_id", targetUserId);
            return followMapper.selectCount(wrapper) > 0 && !isBlocked(setting, field, viewerUserId);
        }
        if ("mutual".equals(visibility)) {
            // 检查互相关注
            QueryWrapper<Follow> w1 = new QueryWrapper<>();
            w1.eq("follower_id", viewerUserId).eq("following_id", targetUserId);
            QueryWrapper<Follow> w2 = new QueryWrapper<>();
            w2.eq("follower_id", targetUserId).eq("following_id", viewerUserId);
            return followMapper.selectCount(w1) > 0 && followMapper.selectCount(w2) > 0 && !isBlocked(setting, field, viewerUserId);
        }
        if ("custom".equals(visibility)) {
            // 所有人可见，但屏蔽列表中的人不可见
            return !isBlocked(setting, field, viewerUserId);
        }
        return true;
    }

    private boolean isBlocked(PrivacySetting setting, String field, String viewerUserId) {
        String blockedUsersJson = setting.getBlockedUsers();
        if (blockedUsersJson == null || blockedUsersJson.isEmpty()) {
            return false;
        }
        try {
            Map<String, List<String>> blockedMap = objectMapper.readValue(blockedUsersJson, new TypeReference<Map<String, List<String>>>() {});
            List<String> blockedList = blockedMap.getOrDefault(field, new ArrayList<>());
            return blockedList.contains(viewerUserId);
        } catch (Exception e) {
            return false;
        }
    }
}
